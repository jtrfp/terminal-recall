/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
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
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.gpu.Texture;

public class AnimatedTexture implements Texture {
    private VQTexture[]  frames;
    private Controller textureSequencer;
    private final TextureBehavior.Support tbs = new TextureBehavior.Support();

    public AnimatedTexture(Controller textureSequencer, VQTexture[] frames2) {
	this.frames 	      = frames2;
	this.textureSequencer = textureSequencer;
    }//end constructor

    /**
     * @return the frames
     */
    public VQTexture[] getFrames() {
	return frames;
    }//end getFrames()

    /**
     * @param frames
     *            the frames to set
     */
    public void setFrames(VQTexture[] frames) {
	this.frames = frames;
    }//end setFrames()

    public Controller getTextureSequencer() {
	return textureSequencer;
    }//end getTextureSequencer()

    @Override
    public Color getAverageColor() {
	try {
	    return frames[0].getAverageColor();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }//end getAverageColor()

    /**
     * @param beh
     * @see org.jtrfp.trcl.TextureBehavior.Support#addBehavior(org.jtrfp.trcl.TextureBehavior)
     */
    public void addBehavior(TextureBehavior beh) {
	tbs.addBehavior(beh);
    }

    /**
     * @param beh
     * @see org.jtrfp.trcl.TextureBehavior.Support#removeBehavior(org.jtrfp.trcl.TextureBehavior)
     */
    public void removeBehavior(TextureBehavior beh) {
	tbs.removeBehavior(beh);
    }

    /**
     * @param triangleList
     * @param gpuTVIndex
     * @param numFrames
     * @param thisTriangle
     * @param pos
     * @param vw
     * @see org.jtrfp.trcl.TextureBehavior.Support#apply(org.jtrfp.trcl.TriangleList, int, int, org.jtrfp.trcl.Triangle, org.apache.commons.math3.geometry.euclidean.threed.Vector3D, org.jtrfp.trcl.core.TriangleVertexWindow)
     */
    public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames,
	    Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw) {
	tbs.apply(triangleList, gpuTVIndex, numFrames, thisTriangle, pos, vw);
    }
}// end AnimatedTexture
