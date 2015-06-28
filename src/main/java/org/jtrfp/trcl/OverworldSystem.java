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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.LoadingProgressReporter;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class OverworldSystem extends RenderableSpacePartitioningGrid {
    private SkySystem 	     skySystem;
    private AltitudeMap              altitudeMap;
    private Color 		     fogColor = Color.black;
    private final List<DEFObject>    defList = new ArrayList<DEFObject>();
    private RenderableSpacePartitioningGrid 
    				     terrainMirror = 
    new RenderableSpacePartitioningGrid(this) {};
    private boolean 		     chamberMode = false;
    private boolean 		     tunnelMode = false;
    private final TR 		     tr;
    private final LoadingProgressReporter 
    				     terrainReporter, 
    				     cloudReporter, 
    				     objectReporter;
    private		ObjectSystem objectSystem;
    private            TerrainSystem terrainSystem;

    public OverworldSystem(TR tr, final LoadingProgressReporter progressReporter) {
	super(tr.getDefaultGrid());
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
	    TextureDescription[] texturePalette = tr
		    .getResourceManager().getTextures(
			    lvl.getLevelTextureListFile(), new ColorPaletteVectorList(globalPalette),null,false);
	    System.out.println("Loading height map...");
	    altitudeMap = new ScalingAltitudeMap(new InterpolatingAltitudeMap(tr.getResourceManager()
		    .getRAWAltitude(lvl.getHeightMapOrTunnelFile())),new Vector3D(TR.mapSquareSize,tr.getWorld().sizeY / 2,TR.mapSquareSize));
	    System.out.println("... Done");
	    final TextureMesh textureMesh = tr.getResourceManager().getTerrainTextureMesh(
		    lvl.getTexturePlacementFile(), texturePalette);
	    // Terrain
	    System.out.println("Building terrain...");
	    boolean flatShadedTerrain = lvl.getHeightMapOrTunnelFile()
		    .toUpperCase().contains("BORG");//TODO: This should be in a config file.
	    terrainSystem = new TerrainSystem(altitudeMap, textureMesh,
		    TR.mapSquareSize, this, terrainMirror, tr, tdf,
		    flatShadedTerrain, terrainReporter);
	    System.out.println("...Done.");
	    // Clouds
	    System.out.println("Setting up sky...");
	    skySystem = new SkySystem(this, tr, this, lvl,
			TR.mapSquareSize * 8,
			(int) (TR.mapWidth / (TR.mapSquareSize * 8)),
			w.sizeY/4, cloudReporter);
	    System.out.println("...Done.");
	    // Objects
	    System.out.println("Setting up objects...");
	    objectSystem = new ObjectSystem(this, tr, lvl, defList,
		    null, Vector3D.ZERO, objectReporter);
	    System.out.println("Adding terrain and object system to OverworldSystem...");
	    this.blockingAddBranch(terrainSystem);
	    this.blockingAddBranch(objectSystem);
	    /*World.relevanceExecutor.submit(new Runnable(){
		@Override
		public void run() {
		    OverworldSystem.this.addBranch(objectSystem);
		    OverworldSystem.this.addBranch(terrainSystem);
		}});*/
	    
	    System.out.println("...Done.");
	    // Tunnel activators
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }// end constructor
/*
    public Color getFogColor() {
	return fogColor;
    }

    public void setFogColor(Color c) {
	fogColor = c;
	if (fogColor == null)
	    throw new NullPointerException("Passed color is intolerably null.");
	tr.renderer.get().getSkyCube().setSkyCubeGen(new HorizGradientCubeGen(c,new Color(c.getRed(),c.getGreen(),255)));
    }
*/
    public List<DEFObject> getDefList() {
	return defList;
    }

    public SpacePartitioningGrid<PositionedRenderable> getTerrainMirror() {
	return terrainMirror;
    }

    public void setChamberMode(boolean mirrorTerrain) {
	System.out.println("setChamberMode from "+chamberMode+" to "+mirrorTerrain);
	tr.getReporter().report("org.jtrfp.OverworldSystem.isInChamber?",
		"" + mirrorTerrain);
	if(chamberMode == mirrorTerrain)
	    return;//Nothing to change.
	chamberMode = mirrorTerrain;
	final SkySystem clouds = getCloudSystem();
	if (mirrorTerrain) {
	    OverworldSystem.this.nonBlockingAddBranch(getTerrainMirror());
	    //No skycube updates in chamber
	    tr.mainRenderer.get().getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	    if (clouds != null)
		OverworldSystem.this.nonBlockingRemoveBranch(clouds);
	} else {
	    OverworldSystem.this.nonBlockingRemoveBranch(getTerrainMirror());
	    //Turn skycube updates back on
	    tr.mainRenderer.get().getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
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
        return objectSystem;
    }
    public AltitudeMap getAltitudeMap() {
	return altitudeMap;
    }
    public SkySystem getSkySystem() {
	return skySystem;
    }
    public TerrainSystem getTerrainSystem(){
	return terrainSystem;
    }
}// end OverworldSystem
