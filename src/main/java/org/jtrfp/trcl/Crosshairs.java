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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.TextureManager;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject2DVisibleEverywhere;

public class Crosshairs extends WorldObject2DVisibleEverywhere {

    public Crosshairs(TR tr) {
	super(tr);
	setImmuneToOpaqueDepthTest(true);
	// Crosshairs
	Model crossModel = null;
	final TextureManager tm = tr.gpu.get().textureManager.get();
	// Fallback
	final int NUM_FRAMES = 16;
	final double LUM_STEP = 200./NUM_FRAMES;
	Texture[] greenThrobFrames = new Texture[NUM_FRAMES];
	for (int f = 0; f < NUM_FRAMES; f++) {
	    greenThrobFrames[f] = greenThrobFrames[(NUM_FRAMES-1) - f] = (Texture) tm
		    .solidColor(new Color(
			    (int)(f * LUM_STEP * .8), 
			    (int)(f * LUM_STEP), 
			    (int)(f * LUM_STEP*.8), 170));
	}//end for(NUM_FRAMES)
	TextureDescription greenThrob = new AnimatedTexture(new Sequencer(80,
		greenThrobFrames.length, false), greenThrobFrames);/*tr.gpu.get().textureManager.get().getFallbackTexture();*/
	// TODO: Set crosshairs as a player-tracking object
	/*
	 * The official crosshairs. We supply the 'green throb' TARGET.BIN has a
	 * size range of [-8192,8192], a far cry from OpenGL's [-1,1] range.
	 * Also has a Z offset of +204,800. Scaling down by 204800 and
	 * subtracting 1 for a Z of zero we get correct size. In the real game
	 * TARGET.BIN is apparently appended to the player ship model itself
	 * such that the Z protrusion is real. Furthermore, enemies try to
	 * attack the crosshairs instead of the plane, perhaps as a kludge for
	 * motion-compensated aiming.
	 */
	try {
	    crossModel = tr.getResourceManager().getBINModel("TARGET.BIN",
		    greenThrob, 1. / 204800., true, tr.getGlobalPaletteVL(),null);
	} catch (Exception e) {
	    tr.showStopper(e);
	}
	final TriangleList tl = crossModel.getTriangleList();
	Triangle[] tris = tl.getPrimitives()[0];
	for (Triangle t : tris)
	    t.setCentroidNormal(Vector3D.ZERO);
	this.setRenderFlags((byte) 1);
	setModel(crossModel.finalizeModel());
	this.movePositionBy(new Vector3D(0, 0, -1));
    }//end constructor
}// end Crosshairs
