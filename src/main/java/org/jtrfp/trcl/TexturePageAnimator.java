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

import java.awt.geom.Point2D;

import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.DynamicTexture;

public class TexturePageAnimator implements Tickable{
    private final TriangleVertexWindow 	vertexWindow;
    private final int 			gpuTVIndex;
    private 	  String 		debugName = "[not set]";
    private final DynamicTexture        dynamicTexture;
    private int                         currentTexturePage;
    private Double                      u,v;
    
    public TexturePageAnimator(DynamicTexture at, TriangleVertexWindow vw, int gpuTVIndex) {
	this.vertexWindow	=vw;
	this.gpuTVIndex		=gpuTVIndex;
	dynamicTexture         =at;
    }//end constructor

    @Override
    public void tick() {
	try{final int newTexturePage = dynamicTexture.getCurrentTexturePage();
	if(currentTexturePage != newTexturePage){//TODO: Cache vertexWindow var
	    vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(newTexturePage & 0xFF));
	    vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((newTexturePage >> 8) & 0xFF));
	    vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((newTexturePage >> 16) & 0xFF));
	    //Update U,V coords if they are expected to change.
	    final Point2D.Double size = dynamicTexture.getSize();
		if(u!=null)
		    vertexWindow.u.set(gpuTVIndex, (short) Math.rint(size.getX() * u));
		if(v!=null)
		    vertexWindow.v.set(gpuTVIndex, (short) Math.rint(size.getY() * v));
	    currentTexturePage = newTexturePage;
	    vertexWindow.flush();
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

    public double getU() {
        return u;
    }

    public void setU(double u) {
        this.u = u;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }
}//end TextureIDAnimator
