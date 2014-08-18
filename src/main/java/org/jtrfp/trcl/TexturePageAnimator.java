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

import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TriangleVertexWindow;

public class TexturePageAnimator implements Tickable{
    private final TriangleVertexWindow 	vertexWindow;
    private final int 			gpuTVIndex;
    private 	  String 		debugName = "[not set]";
    private final Controller		controller;
    private final Texture[]		frames;
    private int				tickCounter;
    
    public TexturePageAnimator(AnimatedTexture at, TriangleVertexWindow vw, int gpuTVIndex) {
	this.vertexWindow	=vw;
	this.gpuTVIndex		=gpuTVIndex;
	this.controller		=at.getTextureSequencer();
	frames 			=at.getFrames();
    }//end constructor

    @Override
    public void tick() {
	try{
	final int texturePage = frames[
	 (int)controller.getCurrentFrame()].
	  getTexturePage();
	tickCounter++;
	/*if(tickCounter%10==0&& debugName.contains("TARGET.BIN")){
	    System.err.println(debugName+" FRAME "+controller.getCurrentFrame());
	}*/
	vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(texturePage & 0xFF));
	vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((texturePage >> 8) & 0xFF));
	vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((texturePage >> 16) & 0xFF));}
	catch(Exception e){e.printStackTrace();}
    }//end tick()

    public TexturePageAnimator setDebugName(String debugName) {
	this.debugName=debugName;
	return this;
    }//end setDebugName(...)

    /**
     * @return the debugName
     */
    public String getDebugName() {
        return debugName;
    }//end getDebugName()

}//end TextureIDAnimator
