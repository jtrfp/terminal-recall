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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.gpu.GLProgram.ValidationHandler;

public class VertexProcessingStage {
    public static final int	VERTEX_BUFFER_WIDTH     = 1024;
    public static final int	VERTEX_BUFFER_HEIGHT    = 4096;
    
    private final GPU                   gpu;
    private final ObjectProcessingStage objectProcessingStage;
    private final GLFrameBuffer         vertexFrameBuffer;
    private final GLProgram             vertexProgram;
    
    private final GLTexture vertexXYTexture,vertexUVTexture,vertexWTexture,
                            vertexZTexture,vertexTextureIDTexture,
                            vertexNormXYTexture,vertexNormZTexture;
    
    public VertexProcessingStage(GPU gpu, ObjectProcessingStage ops, ValidationHandler vh) throws IOException{
	this.gpu=gpu;
	this.objectProcessingStage=ops;

	GLVertexShader fullScreenTriangleShader	= gpu.newVertexShader();
	GLFragmentShader vertexFragShader	= gpu.newFragmentShader();
	vertexFragShader .setSourceFromResource("/shader/vertexFragShader.glsl");
	fullScreenTriangleShader  .setSourceFromResource("/shader/fullScreenTriangleVertexShader.glsl");
	vertexProgram	=gpu.newProgram().setValidationHandler(vh).attachShader(fullScreenTriangleShader)  .attachShader(vertexFragShader).link();

	vertexProgram.use();
	vertexProgram.getUniform("rootBuffer").set((int)0);
	vertexProgram.getUniform("camMatrixBuffer").set((int)1);
	vertexProgram.getUniform("noCamMatrixBuffer").set((int)2);

	gpu.defaultProgram();
	gpu.defaultTIU();
	/////// VERTEX
	vertexXYTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RG32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RG, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexXYTexture");
	vertexNormXYTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RG16F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RG, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexNormXYTexture");
	vertexNormZTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RG16F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexNormZTexture");
	vertexUVTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_RG16F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RG, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexUVTexture");
	vertexZTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexZTexture");
	vertexWTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()//// This is actually W-reciprocal.
		.bind()
		.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("vertexWTexture");
	vertexTextureIDTexture = gpu //Does not need to be in reshape() since it is off-screen.
		.newTexture()
		.bind()
		.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
			GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setExpectedMaxValue(.001, .001, .001, .001)
			.setDebugName("vertexTextureIDTexture")
			.unbind();
	vertexFrameBuffer = gpu
		.newFrameBuffer()
		.bindToDraw()
		.attachDrawTexture(vertexXYTexture, GL3.GL_COLOR_ATTACHMENT0)
		.attachDrawTexture(vertexUVTexture, GL3.GL_COLOR_ATTACHMENT1)
		.attachDrawTexture(vertexZTexture, GL3.GL_COLOR_ATTACHMENT2)
		.attachDrawTexture(vertexWTexture, GL3.GL_COLOR_ATTACHMENT3)
		.attachDrawTexture(vertexTextureIDTexture, GL3.GL_COLOR_ATTACHMENT4)
		.attachDrawTexture(vertexNormXYTexture, GL3.GL_COLOR_ATTACHMENT5)
		.attachDrawTexture(vertexNormZTexture, GL3.GL_COLOR_ATTACHMENT6)
		.setDrawBufferList(
			GL3.GL_COLOR_ATTACHMENT0,
			GL3.GL_COLOR_ATTACHMENT1,
			GL3.GL_COLOR_ATTACHMENT2,
			GL3.GL_COLOR_ATTACHMENT3,
			GL3.GL_COLOR_ATTACHMENT4,
			GL3.GL_COLOR_ATTACHMENT5,
			GL3.GL_COLOR_ATTACHMENT6);
	if(gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
	    throw new RuntimeException("Vertex frame buffer setup failure. OpenGL code "+gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
	}
	gpu.getGl().glClear(GL3.GL_COLOR_BUFFER_BIT);
    }//end constructor
    
 public void process(GL3 gl, int opaqueRenderListLogicalVec4Offset, int numPrimitives){
     final int relevantVertexBufferWidth = ((int)(VERTEX_BUFFER_WIDTH/3))*3;
	vertexProgram.use();
	vertexFrameBuffer.bindToDraw();
	vertexProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);
	gpu.memoryManager.get().bindToUniform(0, vertexProgram,
		vertexProgram.getUniform("rootBuffer"));
	objectProcessingStage.getCamMatrixTexture()  .bindToTextureUnit(1, gl);
	objectProcessingStage.getNoCamMatrixTexture().bindToTextureUnit(2, gl);
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glViewport(0, 0, 
		relevantVertexBufferWidth, 
		(int)Math.ceil((double)(numPrimitives*3)/(double)relevantVertexBufferWidth));//256*256 = 65536, max we can handle.
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);//Opaque
	//Cleanup
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
 }

/**
 * @return the vertexFrameBuffer
 */
public GLFrameBuffer getVertexFrameBuffer() {
    return vertexFrameBuffer;
}

/**
 * @return the vertexProgram
 */
public GLProgram getVertexProgram() {
    return vertexProgram;
}

/**
 * @return the vertexXYTexture
 */
public GLTexture getVertexXYTexture() {
    return vertexXYTexture;
}

/**
 * @return the vertexUVTexture
 */
public GLTexture getVertexUVTexture() {
    return vertexUVTexture;
}

/**
 * @return the vertexWTexture
 */
public GLTexture getVertexWTexture() {
    return vertexWTexture;
}

/**
 * @return the vertexZTexture
 */
public GLTexture getVertexZTexture() {
    return vertexZTexture;
}

/**
 * @return the vertexTextureIDTexture
 */
public GLTexture getVertexTextureIDTexture() {
    return vertexTextureIDTexture;
}

/**
 * @return the vertexNormXYTexture
 */
public GLTexture getVertexNormXYTexture() {
    return vertexNormXYTexture;
}

/**
 * @return the vertexNormZTexture
 */
public GLTexture getVertexNormZTexture() {
    return vertexNormZTexture;
}
}//end VertexProcessingStage
