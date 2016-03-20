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

import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.VQTexture;

public class TexturePageAnimator implements Tickable{
    private final TriangleVertexWindow 	vertexWindow;
    private final int 			gpuTVIndex;
    private 	  String 		debugName = "[not set]";
    private final AnimatedTexture       animatedTexture;
    private int                         currentTexturePage;
    
    public TexturePageAnimator(AnimatedTexture at, TriangleVertexWindow vw, int gpuTVIndex) {
	this.vertexWindow	=vw;
	this.gpuTVIndex		=gpuTVIndex;
	animatedTexture         =at;
    }//end constructor

    @Override
    public void tick() {
	try{final int newTexturePage = animatedTexture.getCurrentTexturePage();
	    if(currentTexturePage != newTexturePage){
		vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(newTexturePage & 0xFF));
		vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((newTexturePage >> 8) & 0xFF));
		vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((newTexturePage >> 16) & 0xFF));
		currentTexturePage = newTexturePage;
		}
	    }catch(Exception e){e.printStackTrace();}
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
