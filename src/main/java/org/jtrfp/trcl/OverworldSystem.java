package org.jtrfp.trcl;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.obj.Explosion;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PowerupObject;

public class OverworldSystem extends RenderableSpacePartitioningGrid
	{
	private CloudSystem cloudSystem;
	private TextureMesh textureMesh;
	private InterpolatingAltitudeMap altitudeMap;
	
	public OverworldSystem(World w, LVLFile lvl){
		super(w);
		try{
			TR tr = w.getTr();
			//Active by default
			Color [] globalPalette = tr.getGlobalPalette();
			TextureDescription [] texturePalette=tr.getResourceManager().getTextures(lvl.getLevelTextureListFile(), 
					globalPalette,GammaCorrectingColorProcessor.singleton,tr.getGPU().takeGL());
			System.out.println("Loading height map...");
			altitudeMap=new InterpolatingAltitudeMap(tr.getResourceManager().getRAWAltitude(lvl.getHeightMapOrTunnelFile()));
			tr.setAltitudeMap(altitudeMap);
			System.out.println("... Done");
			textureMesh = tr.getResourceManager().getTerrainTextureMesh
					(lvl.getTexturePlacementFile(),texturePalette);
			//Terrain
			System.out.println("Building terrain...");
			TerrainSystem terrain = new TerrainSystem(altitudeMap, textureMesh, TR.mapSquareSize,this,tr);
			System.out.println("...Done.");
			//Clouds
			System.out.println("Setting up sky...");
			if(!lvl.getCloudTextureFile().contentEquals("stars.vox")){
				cloudSystem = new CloudSystem(
						tr,
						this,
						lvl,
						TR.mapSquareSize*8,
						(int)(TR.mapWidth/(TR.mapSquareSize*8)),
						w.sizeY/3.5);
				}
			System.out.println("...Done.");
			//Objects
			System.out.println("Setting up objects...");
			ObjectSystem objectSystem=new ObjectSystem(this,w,terrain,lvl);
			objectSystem.activate();
			System.out.println("...Done.");
			//Tunnel activators
			}
		catch(Exception e)
			{e.printStackTrace();}
		}//end constructor
	}//end OverworldSystem
