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
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.flow.LoadingProgressReporter;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.CloudCeiling;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

public class SkySystem extends RenderableSpacePartitioningGrid {
    double 		ceilingHeight;
    TextureDescription 	cloudTexture;
    double 		cloudTileSideSize;
    int 		gridSideSizeInTiles;
    private final TR 	tr;
    private LoadingProgressReporter []
	    		cloudTileReporters;
    private Color	suggestedFogColor;
    private Color []	gradientPalette;
    private String	cloudTextureFileName;
    private		SkyCubeGen   belowCloudsSkyCubeGen,aboveCloudsSkyCubeGen;
    public static final int 
    	    GRADIENT_PALETTE_START = 193,
	    GRADIENT_PALETTE_END = 208;
    public static final SkyCubeGen PLANET_STARS = new HorizGradientCubeGen(Color.black,new Color(0,0,0,0)).
		setEastTexture("/StarsA.png").
		setWestTexture("/StarsA.png").
		setTopTexture("/StarsA.png").
		setSouthTexture("/StarsB.png").
		setNorthTexture("/StarsB.png").
		setVerticalBias(.65f);
    public static final SkyCubeGen SPACE_STARS = new HorizGradientCubeGen(new Color(0,0,0,0),new Color(0,0,0,0)).
		setEastTexture("/StarsA.png").
		setWestTexture("/StarsA.png").
		setTopTexture("/StarsA.png").
		setSouthTexture("/StarsB.png").
		setNorthTexture("/StarsB.png");

    public SkySystem(OverworldSystem os, TR tr,
	    RenderableSpacePartitioningGrid grid, LVLFile lvl,
	    double cloudTileSideSize, int gridSideSizeInTiles,
	    double ceilingHeight, final LoadingProgressReporter cloudReporter) throws IllegalAccessException,
	    FileLoadException, IOException {
	super(grid);
	this.tr = tr;
	final int transpose = 48;
	this.ceilingHeight 	= ceilingHeight;
	this.cloudTileSideSize 	= cloudTileSideSize;
	this.gridSideSizeInTiles= gridSideSizeInTiles;
	cloudTextureFileName 	= lvl.getCloudTextureFile();
	gradientPalette 	= tr.getResourceManager().getPalette(
		lvl.getBackgroundGradientPaletteFile());
	if(hasClouds()){
	    Color[] cloudPalette= new Color[256];
	    // Transpose palette by 48
	    for (int i = 0; i < 256; i++) {
		cloudPalette[TR.bidiMod((i + transpose), 256)] = gradientPalette[i];
	    }
	    cloudTexture = tr.getResourceManager().getRAWAsTexture(
		    cloudTextureFileName, new ColorPaletteVectorList(cloudPalette),true);
	    cloudTileReporters = cloudReporter.generateSubReporters(gridSideSizeInTiles);
	    generateClouds(os);
	}
	activate();
    }// end constructor

    private void generateClouds(OverworldSystem os) {
	// Set fog
	try {
	    // Create a grid
	    for (int z = 0; z < gridSideSizeInTiles; z++) {
		cloudTileReporters[z].complete();
		for (int x = 0; x < gridSideSizeInTiles; x++) {
		    double xPos = x * cloudTileSideSize;
		    double zPos = z * cloudTileSideSize;
		    
		    Triangle[] tris = Triangle.quad2Triangles(new double[] { 0,
			    0 + cloudTileSideSize, 0 + cloudTileSideSize, 0 },
			    new double[] { ceilingHeight, ceilingHeight,
				    ceilingHeight, ceilingHeight },
			    new double[] { 0, 0, 0 + cloudTileSideSize,
				    0 + cloudTileSideSize },
			    new double[] { 0, 1, 1, 0 },// u
			    new double[] { 1, 1, 0, 0 }, cloudTexture,
			    RenderMode.STATIC, Vector3D.ZERO,"CloudSystem");
		    final Model m = new Model(false, tr);
		    m.setDebugName("CloudSystem.cloudQuad");
		    m.addTriangle(tris[0]);
		    m.addTriangle(tris[1]);
		    final CloudCeiling rq = new CloudCeiling(tr,
			    m.finalizeModel());
		    
		    final double[] rqPos = rq.getPosition();
		    rqPos[0] = xPos;
		    rqPos[1] = ceilingHeight;
		    rqPos[2] = zPos;
		    rq.notifyPositionChange();
		    add(rq);
		}// end for(x)
	    }// end for(z)
	} catch (Exception e) {
	    e.printStackTrace();
	}//end catch(e)
    }// end addToWorld

    /**
     * @return the suggestedFogColor
     */
    public Color getSuggestedFogColor() {
	if(suggestedFogColor==null){
	    if(hasClouds())
		suggestedFogColor=cloudTexture.getAverageColor();
	    else
		if(areStarsVisible()){
		    return Color.black;
		}else return getHorizonGradientBottom();
		    
	}//end if(suggestedFogColor==null)
        return suggestedFogColor;
    }//end getSuggetedFogColor()
    
    public boolean hasClouds(){
	return !cloudTextureFileName.toUpperCase().contentEquals("STARS.VOX");
    }
    
    public boolean areStarsVisible(){
	Color c = getHorizonGradientTop();
	return c.getRed()+c.getGreen()+c.getBlue()==0 || !hasClouds();
    }
    
    public Color getHorizonGradientBottom(){//Intentionally backwards.
	System.out.println("bottom color = "+getGradientPalette()[GRADIENT_PALETTE_END]);
	return getGradientPalette()[GRADIENT_PALETTE_END];
    }
    
    public Color getHorizonGradientTop(){
	System.out.println("top color = "+getGradientPalette()[GRADIENT_PALETTE_START]);
	return getGradientPalette()[GRADIENT_PALETTE_START];
    }

    /**
     * @return the gradientPalette
     */
    public Color[] getGradientPalette() {
        return gradientPalette;
    }
    
    public SkyCubeGen getBelowCloudsSkyCubeGen() {
	if(belowCloudsSkyCubeGen==null){
	    final Color fogColor = getSuggestedFogColor();
	    if(hasClouds())
		belowCloudsSkyCubeGen = new HorizGradientCubeGen
		(fogColor,fogColor);
	    else
		    belowCloudsSkyCubeGen = new HorizGradientCubeGen(fogColor,new Color(0,0,0,0)).
		    setEastTexture("/StarsA.png").
		    setWestTexture("/StarsA.png").
		    setTopTexture("/StarsA.png").
		    setSouthTexture("/StarsB.png").
		    setNorthTexture("/StarsB.png").
		    setVerticalBias(.7f);
	}//end null
	return belowCloudsSkyCubeGen;
    }//end getBelowCloudsSkyCubeGen
    
    public SkyCubeGen getAboveCloudsSkyCubeGen() {
	if(aboveCloudsSkyCubeGen==null){
	    if(hasClouds())
		aboveCloudsSkyCubeGen = new HorizGradientCubeGen
		 (getHorizonGradientBottom(),getHorizonGradientTop());
	    else
		aboveCloudsSkyCubeGen = getBelowCloudsSkyCubeGen();
	}//end null
        return aboveCloudsSkyCubeGen;
    }//end getAboveCloudsSkyCubeGen
    
}// end CloudSystem
