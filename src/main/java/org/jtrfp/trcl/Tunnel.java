package org.jtrfp.trcl;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.beh.CubeCollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.phy.RotatingObjectBehavior;
import org.jtrfp.trcl.beh.phy.ShiftingObjectBehavior;
import org.jtrfp.trcl.beh.tun.DestructibleWallBehavior;
import org.jtrfp.trcl.beh.tun.IrisBehavior;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TNLFile;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.file.TNLFile.Segment.Obstacle;
import org.jtrfp.trcl.obj.BarrierCube;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.TunnelExitObject;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.WorldObject;

public class Tunnel extends RenderableSpacePartitioningGrid{
	private LVLFile lvl;
	private final TR tr;
	private final Color [] palette;
	private final GL3 gl;
	final double tunnelDia=200000;
	final double wallThickness=5000;
	private final World world;
	private final TDFFile.Tunnel sourceTunnel;
	
	public static final Vector3D TUNNEL_START_POS = new Vector3D(0,Math.pow(2, 16)*.75,Math.pow(2, 17));
	public static final ObjectDirection TUNNEL_START_DIRECTION = new ObjectDirection(new Vector3D(1,0,0),new Vector3D(0,1,0));

	public Tunnel(World world, TDFFile.Tunnel sourceTunnel){
		super(world);
		this.world=world;
		this.sourceTunnel=sourceTunnel;
		deactivate();//Sleep until activated by tunnel entrance
		tr=world.getTr();
		palette=tr.getGlobalPalette();
		gl=tr.getGPU().takeGL();
		
		try {   lvl=world.getTr().getResourceManager().getLVL(sourceTunnel.getTunnelLVLFile());
			tr.getGPU().releaseGL();
			DirectionVector exitDV=sourceTunnel.getExit();
			//Vector3D entranceVector = new Vector3D((double)entranceDV.getZ()/65535.,-.1,(double)entranceDV.getX()/65535.).normalize();
			final Vector3D entranceVector = TUNNEL_START_DIRECTION.getHeading(); 
			tr.getGPU().takeGL();
			final Vector3D tunnelEnd = buildTunnel(sourceTunnel,entranceVector,false);
			final TunnelExitObject eo = new TunnelExitObject(tr,this);
			System.out.println("Tunnel ends at "+tunnelEnd);
			eo.setPosition(tunnelEnd.subtract(new Vector3D(10000,0,0)));
			add(eo);
			// X is tunnel depth, Z is left-right
			new ObjectSystem(this,world,null,lvl);
			}
		catch(Exception e){e.printStackTrace();}
		}//end constructor

	private Vector3D buildTunnel(TDFFile.Tunnel _tun, Vector3D groundVector, boolean entrance) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
	    	//Entrance uses only a stub. Player is warped to 0,0,0 facing 0,0,1
		ResourceManager rm = tr.getResourceManager();
		LVLFile tlvl = rm.getLVL(_tun.getTunnelLVLFile());
		TextureDescription [] tunnelTexturePalette = rm.getTextures(tlvl.getLevelTextureListFile(), palette, null, gl);
		TNLFile tun = tr.getResourceManager().getTNLData(tlvl.getHeightMapOrTunnelFile());
		
		double segLen=256*TunnelSegment.TUNNEL_DIA_SCALAR;
		final double tunnelScalar=(TunnelSegment.TUNNEL_DIA_SCALAR/TR.crossPlatformScalar);
		final double bendiness=tunnelScalar;
		List<Segment> segs = tun.getSegments();
		Vector3D tunnelEnd = new Vector3D(0,0,0);
		Rotation rotation = entrance?new Rotation(new Vector3D(0,0,1),groundVector):new Rotation(new Vector3D(0,0,1),new Vector3D(1,0,0));
		//CALCULATE ENDPOINT
		for(Segment s:segs){
		    	Vector3D positionDelta=new Vector3D((double)(s.getEndX()-s.getStartX())*bendiness,(double)(s.getEndY()-s.getStartY())*bendiness,segLen);
			tunnelEnd=tunnelEnd.add(rotation.applyTo(positionDelta));
			System.out.println("New endpoint: "+tunnelEnd);
			}
		final Vector3D finalEnd=tunnelEnd;
		Vector3D startPoint= TUNNEL_START_POS;
		
		Vector3D segPos=Vector3D.ZERO;
		final Vector3D top=rotation.applyTo(new Vector3D(0,1,0));
		tunnelEnd=entrance?segPos.add(tunnelEnd):segPos;
		if(entrance){
		    	//Entrance is just a stub so we only need a few of the segments
		    	List<Segment>newSegs = new ArrayList<Segment>();
			for(int i=0; i<10; i++){newSegs.add(segs.get(i));}
			segs=newSegs;
			}
		//CONSTRUCT AND INSTALL SEGMENTS
		for(Segment s:segs){
		    	//Figure out the space the segment will take
			Vector3D positionDelta=new Vector3D((double)(s.getEndX()-s.getStartX())*bendiness*-1,(double)(s.getEndY()-s.getStartY())*bendiness,segLen);
			//Create the segment
			Vector3D position=startPoint.add(rotation.applyTo(segPos));
			TunnelSegment ts = new TunnelSegment(tr,s,tunnelTexturePalette,segLen,positionDelta.getX(),positionDelta.getY());
			ts.setPosition(position);
			ts.setHeading(entrance?groundVector:Vector3D.PLUS_I);
			ts.setTop(entrance?top:Vector3D.PLUS_J);
			//Install the segment
			add(ts);
			installObstacles(s,tunnelTexturePalette,entrance?groundVector:Vector3D.PLUS_I,entrance?top:Vector3D.PLUS_J,position,
					TR.legacy2Modern(s.getStartWidth()*TunnelSegment.TUNNEL_DIA_SCALAR),TR.legacy2Modern(s.getStartWidth()*TunnelSegment.TUNNEL_DIA_SCALAR),tr);
			//Move origin to next segment
			segPos=segPos.add(positionDelta);
			}//end for(segments)
		return finalEnd;
		}//end buildTunnel(...)
	
	/**
	 * Tunnel items:
	 * FANBODY.BIN - fan
	 * IRIS.BIN - animated iris
	 * BEAM.BIN / PIPE.BIN
	 * JAW1.BIN (right) JAW2.BIN (left) - jaws
	 * ELECTRI[0-3].RAW - force field
	 * TP1.RAW - good enough for blastable door?
	 * @throws IOException 
	 * @throws FileLoadException 
	 * 
	 * 
	 */
	
	private void installObstacles(Segment s, TextureDescription[] tunnelTexturePalette, Vector3D heading, Vector3D top, Vector3D wPos, double width, double height, TR tr) throws IllegalAccessException, FileLoadException, IOException{
		Color [] palette = tr.getGlobalPalette();
		Obstacle obs = s.getObstacle();
		WorldObject wo;
		GL3 gl = tr.getGPU().takeGL();
		Model m;
		switch(obs){
			case none0:
				break;
			case doorway:{
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),.5,.5,1,1);
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
				}
			case closedDoor:{
			    	BarrierCube bc = new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),.5,.5,0,1,false);
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.addBehavior(new DamageableBehavior().setHealth(4096));
				bc.addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
				bc.addBehavior(new DeathBehavior());
				bc.addBehavior(new DebrisOnDeathBehavior());
				bc.addBehavior(new DestructibleWallBehavior());
				bc.setTop(top);
				add(bc);
			    /*
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),.5,.5,0,1);
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				*/
				break;
				}
			case blownOpenDoor:
				BarrierCube bc = new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),.5,.5,1,1,true);
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				add(bc);
				break;
			case wallUpSTUB:
			    /*
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos.add(top.scalarMultiply(tunnelDia/2)));
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				*/
				break;
			case wallDownSTUB:
			    /*
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos.subtract(top.scalarMultiply(tunnelDia/2)));
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				*/
				break;
			case wallLeftSTUB:
			    /*
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0));
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				*/
				break;
			case wallRightSTUB:
			    /*
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia,tunnelDia/2.,0));
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				*/
				break;
			case movingWallLeft:{
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				Vector3D endPos = wPos.add(heading.crossProduct(top).scalarMultiply(tunnelDia));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
				bc.addBehavior(new ShiftingObjectBehavior(3000,wPos,endPos));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
				}
			case movingWallRight:{
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				Vector3D endPos = wPos.subtract(heading.crossProduct(top).scalarMultiply(tunnelDia));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
				bc.addBehavior(new ShiftingObjectBehavior(3000,wPos,endPos));
				bc.addBehavior(new CubeCollisionBehavior(bc));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
				}
			case movingWallDown:{
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				Vector3D endPos = wPos.subtract(top.scalarMultiply(tunnelDia));
				bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
				//wo = new WorldObject(tr,m);
				bc.addBehavior(new ShiftingObjectBehavior(3000,wPos,endPos));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
				}
			case movingWallUp:{
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				Vector3D endPos = wPos.add(top.scalarMultiply(tunnelDia));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
				bc.addBehavior(new ShiftingObjectBehavior(3000,wPos,endPos));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
				}
			case wallLeft:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0));
				//wo = new WorldObject(world.getTr(),m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0.,tunnelDia/2.,0),false);
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case wallRight:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia,tunnelDia/2.,0));
				//wo = new WorldObject(world.getTr(),m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia,tunnelDia/2.,0),false);
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case wallDown:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
				//wo = new WorldObject(world.getTr(),m);
				bc.setPosition(wPos.subtract(top.scalarMultiply(tunnelDia/2)));
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case wallUp:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0));
				//wo = new WorldObject(world.getTr(),m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),false);
			    	bc.setPosition(wPos.add(top.scalarMultiply(tunnelDia/2)));
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case rotatingHalfWall:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0),false);
				bc.addBehavior(new RotatingObjectBehavior(heading,heading,top,6000,0));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case rotating34Wall:
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,10));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,10),false);
				bc.addBehavior(new RotatingObjectBehavior(heading,heading,top,6000,0));
				bc.setPosition(wPos);
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				
				//m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0));
				//wo = new WorldObject(tr,m);
			    	bc =new BarrierCube(tr,tunnelDia,tunnelDia,wallThickness,tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(0,tunnelDia/2.,0),false);
				bc.addBehavior(new RotatingObjectBehavior(heading,heading,top,6000,Math.PI/2));
				bc.setPosition(wPos.add(new Vector3D(100,0,0)));
				bc.setHeading(heading);
				bc.setTop(top);
				bc.addBehavior(new CubeCollisionBehavior(bc));
				add(bc);
				break;
			case fan:
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("BLADE.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("FANBODY.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
			case jawsVertical:
				//Up jaw
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("JAW2.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.addBehavior(new ShiftingObjectBehavior(3000,wPos,wPos.add(top.scalarMultiply(tunnelDia/2))));
				wo.addBehavior(new CubeCollisionBehavior(wo));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(heading.crossProduct(top).negate());
				add(wo);
				//Down jaw
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("JAW1.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.addBehavior(new ShiftingObjectBehavior(3000,wPos,wPos.subtract(top.scalarMultiply(tunnelDia/2))));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(heading.crossProduct(top).negate());
				add(wo);
				break;
			case jawsHorizontal:
				//Left jaw
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("JAW2.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.addBehavior(new ShiftingObjectBehavior(3000,wPos,wPos.add(heading.crossProduct(top).scalarMultiply(tunnelDia/2))));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				//Right jaw
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("JAW1.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.addBehavior(new ShiftingObjectBehavior(3000,wPos,wPos.subtract(heading.crossProduct(top).scalarMultiply(tunnelDia/2))));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
			case metalBeamUp:
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("BEAM.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos.add(new Vector3D(0,tunnelDia/6,0)));
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
			case metalBeamDown:
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("BEAM.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos.add(new Vector3D(0,-tunnelDia/6,0)));
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
			case metalBeamLeft:
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("BEAM.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos.add(new Vector3D(-tunnelDia/6,0,0)));
				wo.setHeading(heading);
				wo.setTop(top.crossProduct(heading));
				add(wo);
				break;
			case metalBeamRight:
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("BEAM.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8,false,palette,gl));
				wo.setPosition(wPos.add(new Vector3D(tunnelDia/6,0,0)));
				wo.setHeading(heading);
				wo.setTop(top.crossProduct(heading));
				add(wo);
				break;
			case forceField:{//TODO
				m=Model.buildCube(tunnelDia, tunnelDia, wallThickness, tunnelTexturePalette[s.getObstacleTextureIndex()], new Vector3D(tunnelDia/2.,tunnelDia/2.,0),.5,.5,1,1);
				wo = new WorldObject(world.getTr(),m);
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;
				}
			//Invisible walls, as far as I know, are never used.
			//This makes sense: There is nothing fun about trying to get through a tunnel and crashing into invisible walls.
			case invisibleWallUp://TODO
				break;
			case invisibleWallDown://TODO
				break;
			case invisibleWallLeft://TODO
				break;
			case invisibleWallRight://TODO
				break;
			case iris:{
				wo = new WorldObject(tr,tr.getResourceManager().getBINModel("IRIS.BIN",tunnelTexturePalette[s.getObstacleTextureIndex()],8*256,false,palette,gl));
				final Model mod = wo.getModel();
				wo.addBehavior(new IrisBehavior(new Sequencer(mod.getFrameDelayInMillis(), 2, true),width));
				wo.setPosition(wPos);
				wo.setHeading(heading);
				wo.setTop(top);
				add(wo);
				break;}
			}//end switch(obstruction)
		}//end installObstacles()
	
	public WorldObject getFallbackModel() throws IllegalAccessException, FileLoadException, IOException
		{return new WorldObject(tr,tr.getResourceManager().getBINModel("NAVTARG.BIN",null,8,false,palette,gl));}

	/**
	 * @return the sourceTunnel
	 */
	public TDFFile.Tunnel getSourceTunnel() {
	    return sourceTunnel;
	}
}//end Tunnel
