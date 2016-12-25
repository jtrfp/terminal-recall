/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class OverworldSystem extends RenderableSpacePartitioningGrid {
    private SkySystem 	     skySystem;
    private AltitudeMap              altitudeMap, normalizedAltitudeMap;
    private RenderableSpacePartitioningGrid 
    				     terrainMirror = 
    new RenderableSpacePartitioningGrid();
    private boolean 		     chamberMode = false;
    private boolean 		     tunnelMode = false;
    private final TR 		     tr;
    private final LoadingProgressReporter 
    				     terrainReporter, 
    				     cloudReporter, 
    				     objectReporter;
    private		ObjectSystem objectSystem;
    private Future<TerrainSystem>    terrainSystem;
    private TextureMesh              textureMesh;

    public OverworldSystem(TR tr, final LoadingProgressReporter progressReporter) {
	super();
	this.tr = tr;
	final LoadingProgressReporter []reporters = progressReporter.generateSubReporters(256);
	terrainReporter	= reporters[0];
	cloudReporter 	= reporters[1];
	objectReporter 	= reporters[2];
    }
    public void loadLevel(final LVLFile lvl, final TDFFile tdf){
	try {
	    final World w = tr.getWorld();
	    Color[] globalPalette = tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile());
	    Texture[] texturePalette = tr
		    .getResourceManager().getTextures(
			    lvl.getLevelTextureListFile(), new ColorPaletteVectorList(globalPalette),null,false, true);
	    System.out.println("Loading height map...");
	    altitudeMap = new ScalingAltitudeMap(normalizedAltitudeMap = new InterpolatingAltitudeMap(tr.getResourceManager()
		    .getRAWAltitude(lvl.getHeightMapOrTunnelFile())),new Vector3D(TRFactory.mapSquareSize,tr.getWorld().sizeY / 2,TRFactory.mapSquareSize));
	    System.out.println("... Done");
	    textureMesh = tr.getResourceManager().getTerrainTextureMesh(
		    lvl.getTexturePlacementFile(), texturePalette);
	    // Terrain
	    System.out.println("Building terrain...");
	    final boolean flatShadedTerrain = lvl.getHeightMapOrTunnelFile()
		    .toUpperCase().contains("BORG");//TODO: This should be in a config file.
	    terrainSystem = tr.getThreadManager().submitToThreadPool(new Callable<TerrainSystem>(){
		@Override
		public TerrainSystem call() throws Exception {
		    return new TerrainSystem(altitudeMap, textureMesh,TRFactory.mapSquareSize, terrainMirror, tr, tdf,
			    flatShadedTerrain, terrainReporter, lvl.getHeightMapOrTunnelFile());
		}});
	    System.out.println("...Done.");
	    // Clouds
	    System.out.println("Setting up sky...");
	    skySystem = new SkySystem(this, tr, this, lvl,
			TRFactory.mapSquareSize * 8,
			(int) (TRFactory.mapWidth / (TRFactory.mapSquareSize * 8)),
			w.sizeY/2, cloudReporter);
	    System.out.println("...Done.");
	    // Objects
	    System.out.println("Setting up objects...");
	    final ObjectSystem objectSystem = getObjectSystem();
	    //objectSystem.populateFromLVL(lvl);
	    
	    System.out.println("Adding terrain and object system to OverworldSystem...");
	    World.relevanceExecutor.submit(new Runnable(){
		@Override
		public void run() {
		    assert objectSystem != null;
		    OverworldSystem.this.addBranch(objectSystem);
		    try{OverworldSystem.this.addBranch(terrainSystem.get());}catch(Exception e){e.printStackTrace();}
		}}).get();
	    
	    System.out.println("...Done.");
	    // Tunnel activators
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }// end constructor
    
    public List<DEFObject> getDefList() {
	return getObjectSystem().getDefList();
    }

    public SpacePartitioningGrid<PositionedRenderable> getTerrainMirror() {
	return terrainMirror;
    }

    public void setChamberMode(boolean mirrorTerrain) {
	System.out.println("setChamberMode from "+chamberMode+" to "+mirrorTerrain);
	Features.get(tr, Reporter.class).report("org.jtrfp.OverworldSystem.isInChamber?",
		"" + mirrorTerrain);
	if(chamberMode == mirrorTerrain)
	    return;//Nothing to change.
	chamberMode = mirrorTerrain;
	final SkySystem clouds = getCloudSystem();
	if (mirrorTerrain) {
	    OverworldSystem.this.nonBlockingAddBranch(getTerrainMirror());
	    //No skycube updates in chamber
	    tr.mainRenderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	    if (clouds != null)
		OverworldSystem.this.nonBlockingRemoveBranch(clouds);
	} else {
	    OverworldSystem.this.nonBlockingRemoveBranch(getTerrainMirror());
	    //Turn skycube updates back on
	    tr.mainRenderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	    if (clouds != null)
		OverworldSystem.this.nonBlockingAddBranch(clouds);
	}
    }// end chamberMode

    private SkySystem getCloudSystem() {
	return skySystem;
    }

    /**
     * @return the chamberMode
     */
    public boolean isChamberMode() {
	return chamberMode;
    }

    /**
     * @return the tunnelMode
     */
    public boolean isTunnelMode() {
	return tunnelMode;
    }

    /**
     * @param tunnelMode
     *            the tunnelMode to set
     */
    public void setTunnelMode(boolean tunnelMode) {
	this.tunnelMode = tunnelMode;
    }
    /**
     * @return the objectSystem
     */
    public ObjectSystem getObjectSystem() {
	if(objectSystem == null){
	    objectSystem = new ObjectSystem();
	    objectSystem.setProgressReporter(objectReporter);
	    objectSystem.setTr(tr);
	}
        return objectSystem;
    }
    public AltitudeMap getAltitudeMap() {
	return altitudeMap;
    }
    public SkySystem getSkySystem() {
	return skySystem;
    }
    public TerrainSystem getTerrainSystem(){
	try{return terrainSystem.get();}catch(Exception e){throw new RuntimeException(e);}
    }
    /**
     * @return the normalizedAltitudeMap
     */
    public AltitudeMap getNormalizedAltitudeMap() {
        return normalizedAltitudeMap;
    }
    public TextureMesh getTextureMesh() {
	return textureMesh;
    }
}// end OverworldSystem
