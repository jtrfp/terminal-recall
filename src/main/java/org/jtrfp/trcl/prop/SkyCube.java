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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.NotReadyException;
import org.jtrfp.trcl.core.RenderList;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;

public class SkyCube {
    private final TR tr;
    private volatile GLTexture skyCubeTexture;
    private volatile SkyCubeGen skyCubeGen=null;
    
    public SkyCube(TR tr){
	this.tr=tr;
    }

    public void render(RenderList rl, GL3 gl) throws NotReadyException {
	final Renderer renderer = tr.renderer.getRealtime();
	final GPU gpu = tr.gpu.getRealtime();
	gl.glDepthMask(false);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	gl.glDisable(GL3.GL_CULL_FACE);
	gpu.defaultFrameBuffers();
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	renderer.getSkyCubeProgram().use()
		.getUniform("projectionRotationMatrix")
		.set4x4Matrix(renderer.getCamRotationProjectionMatrix(), true);

	getSkyCubeTexture().bindToTextureUnit(0, gl);
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 36);
	// Cleanup
	gl.glDepthMask(true);
	gl.glEnable(GL3.GL_CULL_FACE);
	gl.glDepthFunc(GL3.GL_LESS);
    }
    
    private void buildSkyCubeTexture(){
	final SkyCubeGen cubeGen = getSkyCubeGen();
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		final int colorMode = GL3.GL_RGB8;
		skyCubeTexture = tr.gpu.get()
			.newTexture()
			.setBindingTarget(GL3.GL_TEXTURE_CUBE_MAP)
			.bind()
			.setImagePositiveX(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getEast())
			.setImageNegativeX(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getWest())
			.setImagePositiveY(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getTop())
			.setImageNegativeY(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getBottom())
			.setImagePositiveZ(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getNorth())
			.setImageNegativeZ(colorMode,32,32,GL3.GL_RGBA,GL3.GL_UNSIGNED_BYTE,cubeGen.getSouth())
			.setMinFilter(GL3.GL_LINEAR)
			.setMagFilter(GL3.GL_LINEAR)
			.setWrapR(GL3.GL_CLAMP_TO_EDGE)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("Sky Cube Texture");
		return null;
	    }}).get();
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
    public void setSkyCubeGen(SkyCubeGen skyCubeGen) {
	if(this.skyCubeGen!=null)
	    if(this.skyCubeGen.hashCode()==skyCubeGen.hashCode())
		return;//Nothing to do.
        this.skyCubeGen = skyCubeGen;
        final GLTexture thisSkyCubeTexture = this.skyCubeTexture;
	if(thisSkyCubeTexture!=null)
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    thisSkyCubeTexture.delete();
		    return null;
		}}).get();
	this.skyCubeTexture=null;
    }//end setSkyCubeGen(...)

    /**
     * @return the skyCubeTexture
     */
    GLTexture getSkyCubeTexture() {
	if(skyCubeTexture==null)
	    buildSkyCubeTexture();
        return skyCubeTexture;
    }

    /**
     * @param skyCubeTexture the skyCubeTexture to set
     */
    void setSkyCubeTexture(GLTexture skyCubeTexture) {
	final GLTexture thisSkyCubeTexture = this.skyCubeTexture;
	if(thisSkyCubeTexture!=null)
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    thisSkyCubeTexture.delete();
		    return null;
		}}).get();
        this.skyCubeTexture = skyCubeTexture;
    }//end setSkyCubeTexture(...)
}//end SkyCube
