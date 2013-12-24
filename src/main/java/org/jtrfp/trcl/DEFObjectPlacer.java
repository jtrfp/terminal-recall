package org.jtrfp.trcl;

import java.util.List;
import java.util.concurrent.Future;

import javax.media.opengl.GL3;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.objects.DEFObject;
import org.jtrfp.trcl.objects.ObjectDirection;
import org.jtrfp.trcl.objects.ObjectPlacer;
import org.jtrfp.trcl.objects.WorldObject;

public class DEFObjectPlacer implements ObjectPlacer
	{
	private DEFFile def;
	private World world;
	private TerrainSystem terrainSystem;
	
	//private static final ExecutorService 
	
	public DEFObjectPlacer(DEFFile def, World world,TerrainSystem terrainSystem)
		{this.def=def;this.world=world;this.terrainSystem=terrainSystem;}
	@Override
	public void placeObjects(RenderableSpacePartitioningGrid target)
		{
		final List<EnemyDefinition> defs = def.getEnemyDefinitions();
		final List<EnemyPlacement> places = def.getEnemyPlacements();
		final Model [] models = new Model[defs.size()];
		final GL3 gl = world.getTr().getGPU().takeGL();
		final TR tr = world.getTr();
		
		tr.getGPU().releaseGL();
		
		Future []futures = new Future[defs.size()];
		//Get BIN models
		for(int i=0; i<defs.size(); i++)
			{
			final int index = i;
			futures[i]=TR.threadPool.submit(new Runnable()
				{
				public void run()
					{
					final EnemyDefinition def = defs.get(index);
					tr.getGPU().takeGL();
					try{models[index]=tr.getResourceManager().getBINModel(def.getComplexModelFile(),tr.getGlobalPalette(),gl);}
					catch(Exception e){e.printStackTrace();}
					if(models[index]==null)System.out.println("Failed to get a model from BIN "+def.getComplexModelFile()+" at index "+index);
					tr.getGPU().releaseGL();
					}
				});
			}
		for(Future f:futures){try{f.get();}catch(Exception e){e.printStackTrace();}}
		
		for(EnemyPlacement pl:places)
			{//behavior objects cannot be shared because they contain state data for each mobile object
			tr.getGPU().releaseGL();
			tr.getGPU().takeGL();
			Model model =models[pl.getDefIndex()];
			if(model!=null)
				{
				final EnemyDefinition def = defs.get(pl.getDefIndex());
				//,new TVBehavior(null,def,terrainSystem,pl.getStrength())
				final WorldObject obj =new DEFObject(tr,model,def);
				//USING  z,x coords
				obj.setPosition(new Vector3D(
						TR.legacy2Modern(pl.getLocationOnMap().getZ()),
						(TR.legacy2Modern(pl.getLocationOnMap().getY())/TR.mapWidth)*16.*world.sizeY,
						TR.legacy2Modern(pl.getLocationOnMap().getX())
						));
				//NOTE: The current scheme might very well be wrong because of the lack of pivot impl. Namely the ZYX config of TRCL and TRI's XYZ stuff
				//Vector3D heading = new Vector3D(Math.cos((double)pl.getPitch())/32767.,0.,Math.sin((double)pl.getPitch()/32767.));
				try{obj.setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));}
				catch(MathArithmeticException e){e.printStackTrace();}
				target.add(obj);
				}
			else{System.out.println("Skipping triangle list at index "+pl.getDefIndex());}
			}//end for(places)
		}//end placeObjects
	}//end DEFObjectPlacer
