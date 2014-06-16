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
import java.util.concurrent.Future;

import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;

public class AnimatedTexture implements TextureDescription {
    private Future<Texture>[] frames;
    // private int timeBetweenFramesInMillis;
    private Controller textureSequencer;

    public AnimatedTexture(Controller textureSequencer, Future<Texture>[] frames) {
	this.frames = frames;
	this.textureSequencer = textureSequencer;
    }

    /**
     * @return the frames
     */
    public Future<Texture>[] getFrames() {
	return frames;
    }

    /**
     * @param frames
     *            the frames to set
     */
    public void setFrames(Future<Texture>[] frames) {
	this.frames = frames;
    }

    public Controller getTextureSequencer() {
	return textureSequencer;
    }

    @Override
    public Color getAverageColor() {
	try {
	    return frames[0].get().getAverageColor();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
}// end AnimatedTexture
