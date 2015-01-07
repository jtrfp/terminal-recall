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
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.LoadingProgressReporter;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

public class OverworldSystem extends RenderableSpacePartitioningGrid {
    private CloudSystem 	     cloudSystem;
    private InterpolatingAltitudeMap altitudeMap;
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
    private		SkyCubeGen   skyCubeGen;

    public OverworldSystem(World w, final LoadingProgressReporter progressReporter) {
	super(w);
	this.tr = w.getTr();
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
			    lvl.getLevelTextureListFile(), new ColorPaletteVectorList(globalPalette),false);
	    System.out.println("Loading height map...");
	    altitudeMap = new InterpolatingAltitudeMap(tr.getResourceManager()
		    .getRAWAltitude(lvl.getHeightMapOrTunnelFile()));
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
	    cloudSystem = new CloudSystem(this, tr, this, lvl,
			TR.mapSquareSize * 8,
			(int) (TR.mapWidth / (TR.mapSquareSize * 8)),
			w.sizeY / 3.5, cloudReporter);
	    System.out.println("...Done.");
	    // Objects
	    System.out.println("Setting up objects...");
	    objectSystem = new ObjectSystem(this, w, lvl, defList,
		    null, Vector3D.ZERO, objectReporter);
	    objectSystem.activate();
	    terrainSystem.activate();
	    System.out.println("...Done.");
	    // Tunnel activators
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }// end constructor
    
    @Override
    public void activate(){
	super.activate();
    }
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
	tr.getReporter().report("org.jtrfp.OverworldSystem.isInChamber?",
		"" + mirrorTerrain);
	chamberMode = mirrorTerrain;
	final CloudSystem cs = getCloudSystem();
	if (mirrorTerrain) {
	    this.getTerrainMirror().activate();
	    if (cs != null)
		cs.deactivate();
	} else {
	    this.getTerrainMirror().deactivate();
	    if (cs != null)
		cs.activate();
	}
    }// end chamberMode

    private CloudSystem getCloudSystem() {
	return cloudSystem;
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
    public InterpolatingAltitudeMap getAltitudeMap() {
	return altitudeMap;
    }
    /**
     * @return the skyCubeGen
     */
    public SkyCubeGen getSkyCubeGen() {
	if(skyCubeGen==null){
	    if(cloudSystem!=null)
		skyCubeGen = new HorizGradientCubeGen
		 (fogColor,new Color(fogColor.getRed(),fogColor.getGreen(),255));//TODO: Use the game's gradient info
	    else
		skyCubeGen = new HorizGradientCubeGen(Color.black,new Color(0,0,0,0)).
			setEastTexture("/StarsA.png").
			setWestTexture("/StarsA.png").
			setTopTexture("/StarsA.png").
			setSouthTexture("/StarsB.png").
			setNorthTexture("/StarsB.png").
			setVerticalBias(.65f);
	}//end null
        return skyCubeGen;
    }
}// end OverworldSystem
