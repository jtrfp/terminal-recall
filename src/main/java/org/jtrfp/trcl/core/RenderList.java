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
package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

public class RenderList {
    public static final int 	NUM_SUBPASSES 		= 4;
    public static final int	NUM_BLOCKS_PER_SUBPASS 	= 1024 * 4;
    public static final int	NUM_BLOCKS_PER_PASS 	= NUM_BLOCKS_PER_SUBPASS
	    						* NUM_SUBPASSES;
    public static final int	NUM_RENDER_PASSES 	= 2;// Opaque + transparent

    private final 	TR 			tr;
    private 		int[] 			hostRenderListPageTable;
    private 	 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks;
    private 		int 			numTransparentBlocks;
    private 		int 			opaqueIndex = 0, blendIndex = 0;
    private final	int			renderListIdx;
    private		long			rootBufferReadFinishedSync;
    private 	 	GLUniform 		renderListPageTable,
    /*	  */					cameraMatrixUniform,
    /*    */					rootBuffer,
    /*	  */					dqCameraMatrixUniform,
    /*	  */					dqRenderListPageTable,
    /*					*/	matrixRootBuffer,
    /*					*/	objectRenderListPageTable,
    /*					*/	objectCameraMatrix;
    private final	GLFrameBuffer		intermediateFrameBuffer,
    						depthQueueFrameBuffer,
    						objectFrameBuffer;
    private final	GLTexture		intermediateDepthTexture,
    /*    	*/				intermediateColorTexture,
    /*    	*/				intermediateNormTexture,
    /*		*/				intermediateTextureIDTexture,
    /*		*/				depthQueueTexture,
    /*		*/				objectBufferTexture;
    private final	GLProgram		depthQueueProgram,objectProgram;
    private final	ArrayList<WorldObject>	nearbyWorldObjects = new ArrayList<WorldObject>();
    private final 	IntBuffer 		previousViewport;
    private final 	Submitter<PositionedRenderable> 
    						submitter = new Submitter<PositionedRenderable>() {
	@Override
	public void submit(PositionedRenderable item) {
	    if (item instanceof WorldObject) {
		final WorldObject wo = (WorldObject)item;
		if (!wo.isActive()) {
		    return;
		}
		synchronized(nearbyWorldObjects)
		 {nearbyWorldObjects.add(wo);}
		if(!wo.isVisible())return;
	    }//end if(WorldObject)
	    final ByteBuffer opOD = item.getOpaqueObjectDefinitionAddresses();
	    final ByteBuffer trOD = item
		    .getTransparentObjectDefinitionAddresses();
	    
	    numOpaqueBlocks += opOD.capacity() / 4;
	    numTransparentBlocks += trOD.capacity() / 4;
	    
	    tr.objectListWindow.get().opaqueIDs.set(renderListIdx, opaqueIndex, opOD);//TODO: Shouldn't this have its own index and not zero?
	    opaqueIndex += opOD.capacity();
	    tr.objectListWindow.get().blendIDs.set(renderListIdx, blendIndex, trOD);
	    blendIndex += trOD.capacity();
	}// end submit(...)

	@Override
	public void submit(Collection<PositionedRenderable> items) {
	    synchronized(items){
		for(PositionedRenderable r:items){submit(r);}
	    }//end for(items)
	}//end submit(...)
    };

    public RenderList(final GL3 gl,final GLProgram objectProgram, final GLProgram primaryProgram,
	    final GLProgram deferredProgram, final GLProgram depthQueueProgram, 
	    final GLFrameBuffer intermediateFrameBuffer, GLFrameBuffer objectFrameBuffer,
	    final GLTexture intermediateColorTexture, final GLTexture intermediateDepthTexture,
	    final GLTexture intermediateNormTexture, final GLTexture intermediateTextureIDTexture,
	    final GLFrameBuffer depthQueueFrameBuffer, final GLTexture depthQueueTexture,
	    final GLTexture objectBufferTexture, final TR tr) {
	// Build VAO
	final IntBuffer ib = IntBuffer.allocate(1);
	this.tr = tr;
	this.objectProgram		=objectProgram;
	this.intermediateColorTexture	=intermediateColorTexture;
	this.intermediateDepthTexture	=intermediateDepthTexture;
	this.intermediateFrameBuffer	=intermediateFrameBuffer;
	this.intermediateNormTexture	=intermediateNormTexture;
	this.intermediateTextureIDTexture=intermediateTextureIDTexture;
	this.depthQueueFrameBuffer	=depthQueueFrameBuffer;
	this.depthQueueProgram		=depthQueueProgram;
	this.depthQueueTexture		=depthQueueTexture;
	this.objectFrameBuffer		=objectFrameBuffer;
	this.objectBufferTexture	=objectBufferTexture;
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=tr.objectListWindow.get().create();
	final TRFuture<Void> task0 = tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		gl.glGenBuffers(1, ib);
		ib.clear();
		dummyBufferID = ib.get();
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, 1, null, GL3.GL_DYNAMIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 1, GL3.GL_BYTE, false, 0, 0);
		renderListPageTable = primaryProgram.getUniform("renderListPageTable");
		dqRenderListPageTable = depthQueueProgram.getUniform("renderListPageTable");
		objectRenderListPageTable = objectProgram.getUniform("renderListPageTable");
		//cameraMatrixUniform = primaryProgram.getUniform("cameraMatrix");
		//dqCameraMatrixUniform = depthQueueProgram.getUniform("cameraMatrix");
		objectCameraMatrix = objectProgram.getUniform("cameraMatrix");
		
		rootBuffer = deferredProgram.getUniform("rootBuffer");
		//matrixRootBuffer = objectProgram.getUniform("rootBuffer");
		return null;
	    }
	});//end task0
	
	hostRenderListPageTable = new int[((ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS
		* RenderList.NUM_RENDER_PASSES)
		/ PagedByteBuffer.PAGE_SIZE_BYTES)*3];
	
	task0.get();
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		//sendRenderListPageTable();
		return null;
	    }
	}).get();
    }// end constructor
    
    private void sendRenderListPageTable(){
	final ObjectListWindow olWindow = RenderList.this.tr
		    .objectListWindow.get();
	final Renderer renderer = tr.renderer.get();
	final int size = olWindow.numPages();
	for (int i = 0; i < size; i++) {
	    hostRenderListPageTable[i] = olWindow.logicalPage2PhysicalPage(i);
	}// end for(hostRenderListPageTable.length)
	objectProgram.use();
	objectRenderListPageTable.setArrayui(hostRenderListPageTable);
	depthQueueProgram.use();
	dqRenderListPageTable.setArrayui(hostRenderListPageTable);
	renderer.getPrimaryProgram().use();
	renderListPageTable.setArrayui(hostRenderListPageTable);
	sentPageTable=true;
    }

    private static int frameCounter = 0;

    private float [] updateStatesToGPU() {
	synchronized(tr.getThreadManager().gameStateLock){
	synchronized(nearbyWorldObjects){
	final int size=nearbyWorldObjects.size();
	for (int i=0; i<size; i++) 
	    nearbyWorldObjects.get(i).updateStateToGPU();
	return tr.renderer.get().getCamera().getMatrixAsFlatArray();
	}}
    }//end updateStatesToGPU

    public float [] sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	return updateStatesToGPU();
    }//end sendToGPU
    
    private boolean sentPageTable=false;
    
    public void render(final GL3 gl, final float[] cameraMatrixAsFlatArray) {
	if(!sentPageTable)sendRenderListPageTable();
	final ObjectListWindow olWindow = tr.objectListWindow.get();
	final int renderListLogicalVec4Offset = ((olWindow.getObjectSizeInBytes()*renderListIdx)/16);
	objectProgram.use();
	objectProgram.getUniform("logicalVec4Offset").setui(renderListLogicalVec4Offset);
	gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	objectCameraMatrix.set4x4Matrix(cameraMatrixAsFlatArray, true);
	objectFrameBuffer.bindToDraw();
	gl.glGetIntegerv(GL3.GL_VIEWPORT, previousViewport);
	gl.glViewport(0, 0, 1024, 128);
	tr.gpu.get().memoryManager.get().bindToUniform(4, objectProgram,
		objectProgram.getUniform("rootBuffer"));
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	gl.glDisable(GL3.GL_CULL_FACE);
	
	int numRows = (int)Math.ceil(numOpaqueBlocks/256.);
	int remainingBlocks = numOpaqueBlocks;
	for(int i=0; i<numRows; i++){
	    gl.glDrawArrays(GL3.GL_LINE_STRIP, i*257, (remainingBlocks<=256?remainingBlocks:256)+1);
	    remainingBlocks -= 256;
	}
	
	final int rowOffset = NUM_BLOCKS_PER_PASS/256;
	final int vtxOffset = rowOffset*257;
	numRows = (int)Math.ceil(numTransparentBlocks/256.);
	remainingBlocks = numTransparentBlocks;
	for(int i=0; i<numRows; i++){
	    gl.glDrawArrays(GL3.GL_LINE_STRIP, vtxOffset+i*257, (remainingBlocks<=256?remainingBlocks:256)+1);
	    remainingBlocks -= 256;
	}
	
	///////////////////
	gl.glViewport(
		previousViewport.get(0), 
		previousViewport.get(1), 
		previousViewport.get(2), 
		previousViewport.get(3));//Cleanup
	gl.glDepthMask(true);
	// OPAQUE.DRAW STAGE
	tr.renderer.get().getPrimaryProgram().use();
	tr.renderer.get().getPrimaryProgram().getUniform("logicalVec4Offset").setui(renderListLogicalVec4Offset);
	//final float [] matrixAsFlatArray = tr.renderer.get().getCamera().getMatrixAsFlatArray();
	GLTexture.specifyTextureUnit(gl, 2);
	objectBufferTexture.bind(gl);
	//cameraMatrixUniform	.set4x4Matrix(cameraMatrixAsFlatArray,true);
	intermediateFrameBuffer	.bindToDraw();
	gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
	final int numOpaqueVertices = numOpaqueBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	final int numTransparentVertices = numTransparentBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	// Turn on depth write, turn off transparency
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_LESS);
	if(tr.renderer.get().isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	final int verticesPerSubPass	= (NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK);
	final int numSubPasses		= (numOpaqueVertices / verticesPerSubPass) + 1;
	int remainingVerts		= numOpaqueVertices;

	if (frameCounter == 0) {
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numOpaqueBlocks",
		    "" + numOpaqueBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numTransparentBlocks",
		    "" + numTransparentBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.approxNumSceneTriangles",
		    "" + ((numOpaqueBlocks+numTransparentBlocks)*GPU.GPU_VERTICES_PER_BLOCK)/3);
	}

	for (int sp = 0; sp < numSubPasses; sp++) {
	    final int numVerts = remainingVerts <= verticesPerSubPass ? remainingVerts
		    : verticesPerSubPass;
	    remainingVerts -= numVerts;
	    final int newOffset = sp
		    * NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK;
	    gl.glDrawArrays(GL3.GL_TRIANGLES, newOffset, numVerts);
	}// end for(subpasses)
	
	// DEPTH QUEUE DRAW
	tr.renderer.get().depthErasureProgram.use();
	gl.glDisable(GL3.GL_CULL_FACE);
	depthQueueFrameBuffer.bindToDraw();
	gl.glEnable(GL3.GL_SAMPLE_MASK);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	// DRAW
	depthQueueProgram.use();
	depthQueueProgram.getUniform("logicalVec4Offset").setui(renderListLogicalVec4Offset);
	gl.glDisable(GL3.GL_MULTISAMPLE);
	gl.glStencilFunc(GL3.GL_EQUAL, 0x1, 0xFF);
	gl.glStencilOp(GL3.GL_DECR, GL3.GL_DECR, GL3.GL_DECR);
	gl.glSampleMaski(0, 0xFF);
	GLTexture.specifyTextureUnit(gl, 0);
	intermediateDepthTexture.bind(gl);
	GLTexture.specifyTextureUnit(gl, 2);
	objectBufferTexture.bind(gl);
	/*dqCameraMatrixUniform.set4x4Matrix(
		cameraMatrixAsFlatArray, true);*/
	tr.gpu.get().memoryManager.get().bindToUniform(4, depthQueueProgram,
		depthQueueProgram.getUniform("rootBuffer"));
	gl.glDrawArrays(GL3.GL_TRIANGLES, NUM_BLOCKS_PER_PASS*GPU.GPU_VERTICES_PER_BLOCK, numTransparentVertices);
	
	// FENCE
	rootBufferReadFinishedSync = gl.glFenceSync(GL3.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	
	gl.glEnable(GL3.GL_MULTISAMPLE);
	gl.glStencilFunc(GL3.GL_ALWAYS, 0xFF, 0xFF);
	gl.glDisable(GL3.GL_STENCIL_TEST);
	
	// DEFERRED STAGE
	gl.glDepthMask(true);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	if(tr.renderer.get().isBackfaceCulling())gl.glDisable(GL3.GL_CULL_FACE);
	final GLProgram deferredProgram = tr.renderer.get().getDeferredProgram();
	deferredProgram.use();
	gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);// Zero means
						    // "Draw to screen"
	GLTexture.specifyTextureUnit(gl, 1);
	intermediateColorTexture.bind(gl);
	GLTexture.specifyTextureUnit(gl, 2);
	intermediateDepthTexture.bind(gl);
	GLTexture.specifyTextureUnit(gl, 3);
	intermediateNormTexture.bind(gl);
	tr.gpu.get().memoryManager.get().bindToUniform(4, deferredProgram,
		    rootBuffer);
	GLTexture.specifyTextureUnit(gl, 5);
	tr.gpu.get().textureManager.get().vqCodebookManager.get().getRGBATexture().bind();
	GLTexture.specifyTextureUnit(gl, 6);
	intermediateTextureIDTexture.bind();
	GLTexture.specifyTextureUnit(gl, 7);
	depthQueueTexture.bind();
	//Execute the draw to a screen quad
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
	
	// DEPTH QUEUE ERASE
	tr.renderer.get().depthErasureProgram.use();
	gl.glDisable(GL3.GL_CULL_FACE);
	depthQueueFrameBuffer.bindToDraw();
	gl.glEnable(GL3.GL_MULTISAMPLE);
	gl.glEnable(GL3.GL_SAMPLE_MASK);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	gl.glDepthMask(false);
	gl.glEnable(GL3.GL_STENCIL_TEST);
	for (int i = 0; i < Renderer.DEPTH_QUEUE_SIZE; i++) {
	    gl.glStencilFunc(GL3.GL_ALWAYS, i + 1, 0xff);
	    gl.glStencilOp(GL3.GL_REPLACE, GL3.GL_REPLACE, GL3.GL_REPLACE);
	    gl.glSampleMaski(0, 0x1 << i);
	    gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
	}
	gl.glDepthMask(true);
	//INTERMEDIATE ERASE
	intermediateFrameBuffer	.bindToDraw();
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
	gl.glFlush();
	gl.glWaitSync(rootBufferReadFinishedSync, 0, GL3.GL_TIMEOUT_IGNORED);
    }// end render()

    public Submitter<PositionedRenderable> getSubmitter() {
	return submitter;
    }

    public void reset() {
	numOpaqueBlocks 	= 0;
	numTransparentBlocks 	= 0;
	blendIndex 		= 0;
	opaqueIndex 		= 0;
	synchronized(nearbyWorldObjects)
	 {nearbyWorldObjects.clear();}
    }//end reset()
    
    public List<WorldObject> getVisibleWorldObjectList(){
	return nearbyWorldObjects;
    }
}// end RenderList
