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
import java.util.List;

import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.DynamicTexture;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;

public class TexturePageAnimator implements Tickable{
    private final TriangleVertexWindow 	vertexWindow;
    private final int 			gpuTVIndex;
    private 	  String 		debugName = "[not set]";
    private final DynamicTexture        dynamicTexture;
    private Integer                     currentTexturePage;
    private Double                      u,v;
    
    public TexturePageAnimator(DynamicTexture at, TriangleVertexWindow vw, int gpuTVIndex) {
	this.vertexWindow	=vw;
	this.gpuTVIndex		=gpuTVIndex;
	dynamicTexture         =at;
    }//end constructor

    @Override
    public void tick() {
	try{final Texture newTextureObject = dynamicTexture.getCurrentTexture();
	if( newTextureObject == null)
	    return;//Unset.
	if(! (newTextureObject instanceof VQTexture) )
	    throw new IllegalStateException("Only can accept VQTextures. Got "+newTextureObject);
	final VQTexture newTexture = (VQTexture)newTextureObject;
	    final int newTexturePage = newTexture.getTexturePage();
	final Integer currentTexturePage = this.currentTexturePage;
	if(currentTexturePage == null || currentTexturePage != newTexturePage){
	    final TriangleVertexWindow vertexWindow = (TriangleVertexWindow)this.vertexWindow.newContextWindow();
	    final List<VQTexture> mipTextures = ((VQTexture)dynamicTexture.getCurrentTexture()).getMipTextures();
	    final int mipIndex = (gpuTVIndex % 3) - 1; // -1 means no mip
	    if(mipTextures != null && mipIndex >= 0){
		VQTexture mipTexture = mipTextures.get(mipIndex);
		final int mipTextureID = mipTexture.getTexturePage();
		vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(mipTextureID         & 0xFF));
		vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((mipTextureID >> 8)  & 0xFF));
		vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((mipTextureID >> 16) & 0xFF));
	    } else {
		vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(newTexturePage         & 0xFF));
		vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((newTexturePage >> 8)  & 0xFF));
		vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((newTexturePage >> 16) & 0xFF));
	    }
	    //vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(newTexturePage & 0xFF));
	    //vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((newTexturePage >> 8) & 0xFF));
	    //vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((newTexturePage >> 16) & 0xFF));
	    //Update U,V coords if they are expected to change.
	    final Point2D.Double size = dynamicTexture.getSize();
		if(u!=null)
		    vertexWindow.u.set(gpuTVIndex, (short) Math.rint(size.getX() * u));
		if(v!=null)
		    vertexWindow.v.set(gpuTVIndex, (short) Math.rint(size.getY() * v));
	    this.currentTexturePage = newTexturePage;
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
