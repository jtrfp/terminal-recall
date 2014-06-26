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
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.CloudCeiling;

public class CloudSystem extends RenderableSpacePartitioningGrid {
    Color fogColor;
    double ceilingHeight;
    TextureDescription cloudTexture;
    double cloudTileSideSize;
    int gridSideSizeInTiles;
    private final TR tr;

    public CloudSystem(OverworldSystem os, TR tr,
	    RenderableSpacePartitioningGrid grid, LVLFile lvl,
	    double cloudTileSideSize, int gridSideSizeInTiles,
	    double ceilingHeight) throws IllegalAccessException,
	    FileLoadException, IOException {
	super(grid);
	this.tr = tr;
	final int transpose = 48;
	this.ceilingHeight = ceilingHeight;
	this.cloudTileSideSize = cloudTileSideSize;
	this.gridSideSizeInTiles = gridSideSizeInTiles;
	String cloudTextureFileName = lvl.getCloudTextureFile();
	Color[] palette = tr.getResourceManager().getPalette(
		lvl.getBackgroundGradientPaletteFile());
	Color[] newPalette = new Color[256];
	// Transpose palette by 48
	for (int i = 0; i < 256; i++) {
	    newPalette[TR.bidiMod((i + transpose), 256)] = palette[i];
	}

	cloudTexture = tr.getResourceManager().getRAWAsTexture(
		cloudTextureFileName, newPalette,
		GammaCorrectingColorProcessor.singleton, tr.gpu.get().getGl(),true);
	addToWorld(os);
    }// end constructor

    private void addToWorld(OverworldSystem os) {
	// Set fog
	try {
	    final Color averageColor = cloudTexture.getAverageColor();
	    tr.getWorld().setFogColor(averageColor);
	    os.setFogColor(averageColor);
	    // Create a grid
	    for (int z = 0; z < gridSideSizeInTiles; z++) {
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
		    // rq.setPosition(new Vector3D(xPos,ceilingHeight,zPos));
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
	}
    }// end addToWorld
}// end CloudSystem
