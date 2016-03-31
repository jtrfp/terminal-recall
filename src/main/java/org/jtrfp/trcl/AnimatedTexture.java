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
import java.awt.geom.Point2D;

import org.jtrfp.trcl.gpu.DynamicTexture;
import org.jtrfp.trcl.gpu.VQTexture;

public class AnimatedTexture extends DynamicTexture {
    private VQTexture[]  frames;
    private Controller textureSequencer;

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
    
    public VQTexture getCurrentFrame(){
	return getFrames()[(int)getTextureSequencer().getCurrentFrame()];
    }

    public int getCurrentTexturePage() {
	return getCurrentFrame().getTexturePage();
    }

    @Override
    public Point2D.Double getSize() {
	return getCurrentFrame().getSize();
    }
}// end AnimatedTexture
