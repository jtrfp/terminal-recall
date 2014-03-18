package org.jtrfp.trcl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.media.opengl.GL3;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.dbg.Reporter;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition.EnemyLogic;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectPlacer;

public class DEFObjectPlacer implements ObjectPlacer{
	private DEFFile def;
	private World world;
	private List<DEFObject> defList;
	private Vector3D headingOverride=null;
	
	public DEFObjectPlacer(DEFFile def, World world)
		{this.def=def;this.world=world;}
	public DEFObjectPlacer(DEFFile defFile, World w,
		ArrayList<DEFObject> defList) {
	    this(defFile,w);
	    this.defList=defList;
	}//end constructor
	@Override
	public void placeObjects(RenderableSpacePartitioningGrid target, Vector3D positionOffset){
		final List<EnemyDefinition> defs = def.getEnemyDefinitions();
		final List<EnemyPlacement> places = def.getEnemyPlacements();
		final Model [] models = new Model[defs.size()];
		final TR tr = world.getTr();
		
		//Future []futures = new Future[defs.size()];
		//Get BIN models
		for(int i=0; i<defs.size(); i++){
			final int index = i;
			//futures[i]=TR.threadPool.submit(new Runnable(){
			//	public void run(){
					final EnemyDefinition def = defs.get(index);
					try{models[index]=tr.getResourceManager().getBINModel(def.getComplexModelFile(),tr.getGlobalPalette(),tr.getGPU().getGl());}
					catch(Exception e){e.printStackTrace();}
					if(models[index]==null)System.out.println("Failed to get a model from BIN "+def.getComplexModelFile()+" at index "+index);
			//		}
			//	});
			final Reporter reporter = tr.getReporter();
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".complexModelFile", defs.get(i).getComplexModelFile());
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".logic", defs.get(i).getLogic());
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".simpleModelFile", defs.get(i).getSimpleModel());
			}//end for(i:defs)
		//for(Future f:futures){try{f.get();}catch(Exception e){e.printStackTrace();}}
		
		for(EnemyPlacement pl:places){
			Model model =models[pl.getDefIndex()];
			if(model!=null){
				final EnemyDefinition def = defs.get(pl.getDefIndex());
				final DEFObject obj =new DEFObject(tr,model,def,pl);
				if(defList!=null)defList.add(obj);
				//USING  z,x coords
				final double [] objPos = obj.getPosition();
				objPos[0]= TR.legacy2Modern	(pl.getLocationOnMap().getZ())+positionOffset.getX();
				objPos[1]=(TR.legacy2Modern	(pl.getLocationOnMap().getY())/TR.mapWidth)*16.*world.sizeY+positionOffset.getY();
				objPos[2]= TR.legacy2Modern	(pl.getLocationOnMap().getX())+positionOffset.getZ();
				obj.notifyPositionChange();
				
				if(def.getLogic()==EnemyLogic.groundStaticRuin){
				    //Spawn a second, powerup-free model using the simplemodel
				    Model simpleModel=null;
				    try{simpleModel = tr.getResourceManager().getBINModel(def.getSimpleModel(),tr.getGlobalPalette(),tr.getGPU().getGl());}
				    catch(Exception e){e.printStackTrace();}
				    EnemyDefinition ed = new EnemyDefinition();
				    ed.setLogic(EnemyLogic.groundDumb);
				    ed.setDescription("auto-generated enemy rubble def");
				    ed.setPowerupProbability(0);
				    EnemyPlacement simplePlacement = new EnemyPlacement();//TODO: EnemyPlacement.clone()
				    simplePlacement.setPitch	(pl.getPitch());
				    simplePlacement.setStrength	(pl.getStrength());
				    simplePlacement.setRoll	(pl.getRoll());
				    simplePlacement.setYaw	(pl.getYaw());
				    simplePlacement.setStrength	(pl.getStrength());
				    final DEFObject ruin = new DEFObject(tr,simpleModel,ed,simplePlacement);
				    ruin.setVisible(false);//TODO: Use setActive later
				    ruin.setIsRuin(true);
				    obj.setRuinObject(ruin);
				    ruin.setPosition(obj.getPosition());
				    try{ruin.setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));}
					catch(MathArithmeticException e){e.printStackTrace();}
				    target.add(ruin);
				}//end if(groundStaticRuin)
				try{obj.setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));}
				catch(MathArithmeticException e){e.printStackTrace();}
				if(headingOverride!=null){
				    final double [] headingArray = obj.getHeadingArray();
				    headingArray[0]=headingOverride.getX();
				    headingArray[1]=headingOverride.getY();
				    headingArray[2]=headingOverride.getZ();
				  }//end if(headingOverride)
				target.add(obj);
				}//end if(model!=null)
			else{System.out.println("Skipping triangle list at index "+pl.getDefIndex());}
			}//end for(places)
		}//end placeObjects
	/**
	 * @return the headingOverride
	 */
	public Vector3D getHeadingOverride() {
	    return headingOverride;
	}
	/**
	 * @param headingOverride the headingOverride to set
	 */
	public DEFObjectPlacer setHeadingOverride(Vector3D headingOverride) {
	    this.headingOverride = headingOverride;
	    return this;
	}
	}//end DEFObjectPlacer
