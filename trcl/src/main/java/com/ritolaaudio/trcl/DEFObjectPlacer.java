package com.ritolaaudio.trcl;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.ritolaaudio.trcl.file.DEFFile;
import com.ritolaaudio.trcl.file.DEFFile.EnemyDefinition;
import com.ritolaaudio.trcl.file.DEFFile.EnemyPlacement;

public class DEFObjectPlacer implements ObjectPlacer
	{
	private DEFFile def;
	private World world;
	private TerrainSystem terrainSystem;
	
	public DEFObjectPlacer(DEFFile def, World world,TerrainSystem terrainSystem)
		{this.def=def;this.world=world;this.terrainSystem=terrainSystem;}
	@Override
	public void placeObjects(RenderableSpacePartitioningGrid target)
		{
		EnemyDefinition [] defs = def.getEnemyDefinitions();
		EnemyPlacement [] places = def.getEnemyPlacements();
		//com.ritolaaudio.trcl.file.TDFFile.Tunnel [] tuns = tdf.getTunnels();
		Model [] models = new Model[defs.length];
		GL3 gl = world.getTr().getGl();
		TR tr = world.getTr();
		
		//Get BIN models
		for(int i=0; i<defs.length; i++)
			{
			final EnemyDefinition def = defs[i];
			tr.releaseGL();
			tr.takeGL();
			try{models[i]=tr.getResourceManager().getBINModel(def.getComplexModelFile(),tr.getGlobalPalette(),gl);}
			catch(Exception e){e.printStackTrace();}
			if(models[i]==null)System.out.println("Failed to get a model from BIN "+def.getComplexModelFile()+" at index "+i);
			}
		
		for(EnemyPlacement pl:places)
			{//behavior objects cannot be shared because they contain state data for each mobile object
			tr.releaseGL();
			tr.takeGL();
			Model model =models[pl.getDefIndex()];
			if(model!=null)
				{
				final EnemyDefinition def = defs[pl.getDefIndex()];
				final RigidMobileObject obj =new RigidMobileObject(model,new TVBehavior(def,terrainSystem,pl.getStrength()),world);
				//USING  z,x coords
				obj.setPosition(new Vector3D(
						TR.legacy2Modern(pl.getLocationOnMap().getZ()),
						(TR.legacy2Modern(pl.getLocationOnMap().getY())/TR.mapWidth)*16.*world.sizeY,
						TR.legacy2Modern(pl.getLocationOnMap().getX())
						));
				//NOTE: The current scheme might very well be wrong because of the lack of pivot impl. Namely the ZYX config of TRCL and TRI's XYZ stuff
				//Vector3D heading = new Vector3D(Math.cos((double)pl.getPitch())/32767.,0.,Math.sin((double)pl.getPitch()/32767.));
				obj.setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));
				
				target.add(obj);
				}
			else{System.out.println("Skipping triangle list at index "+pl.getDefIndex());}
			}//end for(places)
		}//end placeObjects
	}//end DEFObjectPlacer
