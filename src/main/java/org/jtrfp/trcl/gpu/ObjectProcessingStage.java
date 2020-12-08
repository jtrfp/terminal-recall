/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gpu;

import java.io.IOException;

import com.jogamp.opengl.GL3;

import org.jtrfp.trcl.gpu.GLProgram.ValidationHandler;
import org.jtrfp.trcl.gpu.GPU.GPUVendor;

public class ObjectProcessingStage {
    public static final int OBJECT_TEXTURE_SIDE_LEN  = 512;
    
    private final GLFrameBuffer    objectProcessingFrameBuffer;
    private GPU                    gpu;
    private final GLProgram        objectProgram;
    private final GLTexture        camMatrixTexture,noCamMatrixTexture;
    private final GLVertexShader   objectVertexShader;
    private final GLFragmentShader objectFragShader;

    public ObjectProcessingStage(GPU gpu, ValidationHandler vh){
	this.gpu=gpu;

	objectVertexShader   = gpu.newVertexShader();
	objectFragShader     = gpu.newFragmentShader();

	try{
	    objectVertexShader.setSourceFromResource("/shader/objectVertexShader.glsl");
	    objectFragShader  .setSourceFromResource("/shader/objectFragShader.glsl");}
	catch(IOException e){e.printStackTrace();}
	objectProgram	     = gpu.newProgram().setValidationHandler(vh).attachShader(objectFragShader)	  .attachShader(objectVertexShader).link();
	
	camMatrixTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RGBA32F, OBJECT_TEXTURE_SIDE_LEN, OBJECT_TEXTURE_SIDE_LEN, 
			GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("camMatrixTexture");
	noCamMatrixTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RGBA32F, OBJECT_TEXTURE_SIDE_LEN, OBJECT_TEXTURE_SIDE_LEN, 
			GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("noCamMatrixTexture");

	objectProcessingFrameBuffer = gpu.newFrameBuffer()
		.bindToDraw()
		.attachDrawTexture(camMatrixTexture, GL3.GL_COLOR_ATTACHMENT0)
		.attachDrawTexture(noCamMatrixTexture, GL3.GL_COLOR_ATTACHMENT1)
		.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
	
	if(gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
	    throw new RuntimeException("Object frame buffer setup failure. OpenGL code "+gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
	}
	gpu.getGl().glClear(GL3.GL_COLOR_BUFFER_BIT);
	
	objectProgram.use();
	objectProgram.getUniform("rootBuffer").set((int)0);
	objectProgram.getUniform("rowTweak").setui((int)
		(gpu.getGPUVendor()==GPUVendor.AMD?1:0));//AMD needs a tweak. Not yet sure why.
    }//end constructor

    public void process(GL3 gl, float [] cameraMatrixAsFlatArray,int opaqueRenderListLogicalVec4Offset,
	    int numTransparentBlocks, int numOpaqueBlocks, int numOpaqueUnoccludedTBlocks, int numTransUnoccludedTBlocks){
	//final GLProgram objectProgram = rFactory.getObjectProgram();
	objectProgram.use();
	objectProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);

	gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	objectProgram.getUniform("cameraMatrix").set4x4Matrix(cameraMatrixAsFlatArray, true);
	//rFactory.getObjectFrameBuffer().bindToDraw();
	objectProcessingFrameBuffer.bindToDraw();
	//gl.glGetIntegerv(GL3.GL_VIEWPORT, previousViewport);
	gl.glViewport(0, 0, OBJECT_TEXTURE_SIDE_LEN, OBJECT_TEXTURE_SIDE_LEN);
	gpu.memoryManager.get().bindToUniform(0, objectProgram,
		objectProgram.getUniform("rootBuffer"));
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDisable(GL3.GL_LINE_SMOOTH);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glDisable(GL3.GL_MULTISAMPLE);
	gl.glLineWidth(1);
	{//Start variable scope
	    final int blocksPerRow = OBJECT_TEXTURE_SIDE_LEN / 4;
	    int remainingBlocks = numTransparentBlocks+numOpaqueBlocks+numOpaqueUnoccludedTBlocks+numTransUnoccludedTBlocks;
	    int numRows = (int)Math.ceil(remainingBlocks/(double)blocksPerRow);
	    for(int i=0; i<numRows; i++){
		gl.glDrawArrays(GL3.GL_LINE_STRIP, i*(blocksPerRow+1), (remainingBlocks<=blocksPerRow?remainingBlocks:blocksPerRow)+1);
		remainingBlocks -= blocksPerRow;
	    }
	}//end variable scope
	gl.glFinish();//XXX Intel GPU flickers if this is not here. Issue #246
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
    }// end process()

    public void sendRenderListPageTable(int[] hostRenderListPageTable) {
	objectProgram.use();
	objectProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);
    }//end sendRenderListPageTable(...)

    /**
     * @return the camMatrixTexture
     */
    public GLTexture getCamMatrixTexture() {
        return camMatrixTexture;
    }

    /**
     * @return the noCamMatrixTexture
     */
    public GLTexture getNoCamMatrixTexture() {
        return noCamMatrixTexture;
    }

}//end ObjectProcessingStage
