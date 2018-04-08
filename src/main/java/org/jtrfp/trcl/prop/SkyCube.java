/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.prop;

import java.awt.Color;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jogamp.opengl.GL3;

import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;

public class SkyCube {
    private final    GPU        gpu;
    private volatile GLTexture  skyCubeTexture;
    private volatile SkyCubeGen skyCubeGen=null;
    private AtomicBoolean	skyCubeGenStale = new AtomicBoolean(true);
    
    public SkyCube(GPU gpu){
	this.gpu = gpu;
    }
    
    private void buildSkyCubeTextureGL(){
	final SkyCubeGen cubeGen = getSkyCubeGen();
	final int sideWidth = cubeGen.getSideWidth();
	final int colorMode = GL3.GL_RGB8;
	if(skyCubeTexture==null)
	    skyCubeTexture = gpu
	    .newTexture()
	    .setBindingTarget(GL3.GL_TEXTURE_CUBE_MAP);
	skyCubeTexture.bind()
	.setImagePositiveX(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getEast())
	.setImageNegativeX(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getWest())
	.setImagePositiveY(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getTop())
	.setImageNegativeY(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getBottom())
	.setImagePositiveZ(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getNorth())
	.setImageNegativeZ(colorMode,sideWidth,sideWidth,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getSouth())
	.setMinFilter(GL3.GL_NEAREST)
	.setMagFilter(GL3.GL_LINEAR)
	.setWrapR(GL3.GL_CLAMP_TO_EDGE)
	.setWrapS(GL3.GL_CLAMP_TO_EDGE)
	.setWrapT(GL3.GL_CLAMP_TO_EDGE)
	.setDebugName("Sky Cube Texture");
    }//end buildSkyCubeTexture()

    /**
     * @return the skyCubeGen
     */
    public SkyCubeGen getSkyCubeGen() {
	if(skyCubeGen==null)
	    setSkyCubeGen(new HorizGradientCubeGen(Color.black,Color.DARK_GRAY));
        return skyCubeGen;
    }

    /**
     * @param skyCubeGen the skyCubeGen to set
     */
    public synchronized void setSkyCubeGen(SkyCubeGen skyCubeGen) {
	//If this is the same type of gen as before, we don't want to go through all the redundant work.
	if(this.skyCubeGen!=null)
	    if(this.skyCubeGen.hashCode()==skyCubeGen.hashCode())
		{System.out.println("skyCubeGen rejected: "+skyCubeGen);return;}//Nothing to do.
        this.skyCubeGen = skyCubeGen;
        skyCubeGenStale.set(true);
    }//end setSkyCubeGen(...)
    
    @Override
    public void finalize() throws Throwable{
	if(skyCubeTexture!=null)
	    gpu.submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    skyCubeTexture.delete();
		    return null;
		}});
	super.finalize();
    }//end finalize()

    public GLTexture getSkyCubeTexture() {
	if(skyCubeGenStale.getAndSet(false)==true)
	    buildSkyCubeTextureGL();
	return skyCubeTexture;
    }
}//end SkyCube
