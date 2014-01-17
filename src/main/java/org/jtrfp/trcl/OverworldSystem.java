package org.jtrfp.trcl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class OverworldSystem extends RenderableSpacePartitioningGrid
	{
	private CloudSystem cloudSystem;
	private TextureMesh textureMesh;
	private InterpolatingAltitudeMap altitudeMap;
	private Color fogColor=Color.black;
	private final ArrayList<DEFObject> defList = new ArrayList<DEFObject>();
	private RenderableSpacePartitioningGrid terrainMirror=new RenderableSpacePartitioningGrid(this){};
	private boolean chamberMode=false;
	
	public OverworldSystem(World w, LVLFile lvl, TDFFile tdf){
		super(w);
		try{
			TR tr = w.getTr();
			//Active by default
			Color [] globalPalette = tr.getGlobalPalette();
			Future<TextureDescription> [] texturePalette=tr.getResourceManager().getTextures(lvl.getLevelTextureListFile(), 
					globalPalette,GammaCorrectingColorProcessor.singleton,tr.getGPU().takeGL());
			System.out.println("Loading height map...");
			altitudeMap=new InterpolatingAltitudeMap(tr.getResourceManager().getRAWAltitude(lvl.getHeightMapOrTunnelFile()));
			tr.setAltitudeMap(altitudeMap);
			System.out.println("... Done");
			textureMesh = tr.getResourceManager().getTerrainTextureMesh
					(lvl.getTexturePlacementFile(),texturePalette);
			//Terrain
			System.out.println("Building terrain...");
			TerrainSystem terrain = new TerrainSystem(altitudeMap, textureMesh, TR.mapSquareSize,this,terrainMirror,tr,tdf);
			System.out.println("...Done.");
			//Clouds
			System.out.println("Setting up sky...");
			if(!lvl.getCloudTextureFile().contentEquals("stars.vox")){
				cloudSystem = new CloudSystem(
						this,
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
			ObjectSystem objectSystem=new ObjectSystem(this,w,lvl,defList);
			objectSystem.activate();
			System.out.println("...Done.");
			//Tunnel activators
			}
		catch(Exception e)
			{e.printStackTrace();}
		terrainMirror.deactivate();
		}//end constructor

	public Color getFogColor() {
	    return fogColor;
	}
	public void setFogColor(Color c){
	    fogColor=c;if(fogColor==null)throw new NullPointerException("Passed color is intolerably null.");}

	public List<DEFObject> getDefList() {
	    return defList;
	}

	public SpacePartitioningGrid<PositionedRenderable> getTerrainMirror() {
	    return terrainMirror;
	}

	public void setChamberMode(boolean mirrorTerrain) {
	    chamberMode=mirrorTerrain;
	    if(mirrorTerrain){
		this.getTerrainMirror().activate();
		this.getCloudSystem().deactivate();
	    }else{
		this.getTerrainMirror().deactivate();
		this.getCloudSystem().activate();
	    }
	}//end chamberMode

	private CloudSystem getCloudSystem() {
	    return cloudSystem;
	}

	/**
	 * @return the chamberMode
	 */
	public boolean isChamberMode() {
	    return chamberMode;
	}
}//end OverworldSystem
