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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

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
    private static final int	OPAQUE_PASS 		= 0;
    private static final int	BLEND_PASS 		= 1;

    private final 	TR 			tr;
    private 		int[] 			hostRenderListPageTable;
    private 	 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks;
    private 		int 			numTransparentBlocks;
    private 		int 			modulusUintOffset;
    private 		int 			opaqueIndex = 0, blendIndex = 0;
    private 	 	GLUniform 		renderListOffsetUniform,
    /*    */	    				renderListPageTable,
    /*    */					useTextureMap,
    /*	  */					cameraMatrixUniform,
    /*    */					rootBuffer;
    private final	GLFrameBuffer		intermediateFrameBuffer,
    						depthQueueFrameBuffer;
    private final	GLTexture		intermediateDepthTexture,
    /*    	*/				intermediateColorTexture,
    /*    	*/				intermediateNormTexture,
    /*		*/				intermediateTextureIDTexture,
    /*		*/				depthQueueTexture;
    private final	GLProgram		depthQueueProgram;
    private final	ArrayList<WorldObject>	nearbyWorldObjects = new ArrayList<WorldObject>();
    private final 	Submitter<PositionedRenderable> 
    						submitter = new Submitter<PositionedRenderable>() {
	@Override
	public void submit(PositionedRenderable item) {
	    if (item instanceof WorldObject) {
		final WorldObject wo = (WorldObject)item;
		if (!wo.isActive()) {
		    return;
		}
		nearbyWorldObjects.add(wo);
		if(!wo.isVisible())return;
	    }//end if(WorldObject)
	    final ByteBuffer opOD = item.getOpaqueObjectDefinitionAddresses();
	    final ByteBuffer trOD = item
		    .getTransparentObjectDefinitionAddresses();
	    numOpaqueBlocks += opOD.capacity() / 4;
	    numTransparentBlocks += trOD.capacity() / 4;
	    tr.objectListWindow.get().opaqueIDs.set(0, opaqueIndex, opOD);
	    opaqueIndex += opOD.capacity();
	    tr.objectListWindow.get().blendIDs.set(0, blendIndex, trOD);
	    blendIndex += trOD.capacity();
	}// end submit(...)

	@Override
	public void submit(Collection<PositionedRenderable> items) {
	    synchronized(items){
		for(PositionedRenderable r:items){submit(r);}
	    }//end for(items)
	}//end submit(...)
    };

    public RenderList(final GL3 gl,final GLProgram primaryProgram,
	    final GLProgram deferredProgram, final GLProgram depthQueueProgram, 
	    final GLFrameBuffer intermediateFrameBuffer, 
	    final GLTexture intermediateColorTexture, final GLTexture intermediateDepthTexture,
	    final GLTexture intermediateNormTexture, final GLTexture intermediateTextureIDTexture,
	    final GLFrameBuffer depthQueueFrameBuffer, final GLTexture depthQueueTexture,
	    final TR tr) {
	// Build VAO
	final IntBuffer ib = IntBuffer.allocate(1);
	this.tr = tr;
	this.intermediateColorTexture=intermediateColorTexture;
	this.intermediateDepthTexture=intermediateDepthTexture;
	this.intermediateFrameBuffer=intermediateFrameBuffer;
	this.intermediateNormTexture=intermediateNormTexture;
	this.intermediateTextureIDTexture=intermediateTextureIDTexture;
	this.depthQueueFrameBuffer=depthQueueFrameBuffer;
	this.depthQueueProgram=depthQueueProgram;
	this.depthQueueTexture=depthQueueTexture;
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
		renderListOffsetUniform = primaryProgram.getUniform("renderListOffset");
		renderListPageTable = primaryProgram.getUniform("renderListPageTable");
		useTextureMap = primaryProgram.getUniform("useTextureMap");
		cameraMatrixUniform = primaryProgram.getUniform("cameraMatrix");
		
		rootBuffer = deferredProgram.getUniform("rootBuffer");
		return null;
	    }
	});//end task0
	
	hostRenderListPageTable = new int[ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS
		* RenderList.NUM_RENDER_PASSES
		/ PagedByteBuffer.PAGE_SIZE_BYTES];
	
	final ObjectListWindow olWindow = RenderList.this.tr
		    .objectListWindow.get();
	final Renderer renderer = tr.renderer.get();
	task0.get();
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		for (int i = 0; i < hostRenderListPageTable.length; i++) {
		    hostRenderListPageTable[i] = olWindow.logicalPage2PhysicalPage(i);
		}// end for(hostRenderListPageTable.length)
		depthQueueProgram.use();
		depthQueueProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);//TODO: Cache or consolidate
		renderer.getPrimaryProgram().use();
		renderListPageTable.setArrayui(hostRenderListPageTable);
		modulusUintOffset = (olWindow
			.getPhysicalAddressInBytes(0) % PagedByteBuffer.PAGE_SIZE_BYTES) / 4;
		return null;
	    }
	}).get();
    }// end constructor

    private static int frameCounter = 0;

    private void updateStatesToGPU() {
	/*for (int i = 0; i < renderablesIndex; i++) {
	    renderables[i].updateStateToGPU();
	}*/
	for (int i = 0; i < nearbyWorldObjects.size(); i++) {
	    nearbyWorldObjects.get(i).updateStateToGPU();
	}
    }//end updateStatesToGPU

    public void sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	updateStatesToGPU();
    }//end sendToGPU
    
    public void render(GL3 gl) {
	// OPAQUE STAGE
	tr.renderer.get().getPrimaryProgram().use();
	final float [] matrixAsFlatArray = tr.renderer.get().getCamera().getMatrixAsFlatArray();
	cameraMatrixUniform.set4x4Matrix(matrixAsFlatArray,true);
	useTextureMap.set((int)0);
	intermediateFrameBuffer.bindToDraw();
	gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
	final int numOpaqueVertices = numOpaqueBlocks
		* (GPU.GPU_VERTICES_PER_BLOCK+1);
	final int numTransparentVertices = numTransparentBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	// Turn on depth write, turn off transparency
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_LESS);
	if(tr.renderer.get().isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	// renderModeUniform.set(OPAQUE_PASS);
	final int verticesPerSubPass = (NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK);
	final int numSubPasses = (numOpaqueVertices / verticesPerSubPass) + 1;
	int remainingVerts = numOpaqueVertices;

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
	    final int newOffset = modulusUintOffset + sp
		    * NUM_BLOCKS_PER_SUBPASS;// newOffset is in uints
	    renderListOffsetUniform.setui(newOffset);
	    gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVerts);
	}// end for(subpasses)
	
	// DEPTH QUEUE STAGE
	if(tr.getTrConfig().isUsingNewTexturing()){
	    //ERASE
	    tr.renderer.get().depthErasureProgram.use();
	    gl.glDisable(GL3.GL_CULL_FACE);
	    depthQueueFrameBuffer.bindToDraw();
	    gl.glEnable(GL3.GL_MULTISAMPLE);
	    gl.glEnable(GL3.GL_SAMPLE_MASK);
	    gl.glDepthFunc(GL3.GL_ALWAYS);
	    gl.glDepthMask(false);
	    gl.glEnable(GL3.GL_STENCIL_TEST);
	    for(int i = 0; i < Renderer.DEPTH_QUEUE_SIZE; i++) {
	      gl.glStencilFunc(GL3.GL_ALWAYS, i+1, 0xff);
	      gl.glStencilOp(GL3.GL_REPLACE, GL3.GL_REPLACE, GL3.GL_REPLACE);
	      gl.glSampleMaski(0, 0x1 << i);
	      gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
	    }
	    gl.glSampleMaski(0, 0xFF);
	    //DRAW
	    depthQueueProgram.use(); //TODO: Nothing shows up
	    //gl.glDepthFunc(GL3.GL_GREATER);
	    //gl.glDisable(GL3.GL_STENCIL_TEST);
	    //gl.glDisable(GL3.GL_SAMPLE_MASK);
	    //gl.glEnable(GL3.GL_STENCIL_TEST);
	    gl.glDisable(GL3.GL_MULTISAMPLE);
	    gl.glStencilFunc(GL3.GL_EQUAL, 0x1, 0xFF);
	    gl.glStencilOp(GL3.GL_DECR, GL3.GL_DECR, GL3.GL_DECR);
	    gl.glSampleMaski(0, 0xFF);
	    GLTexture.specifyTextureUnit(gl, 0);
	    intermediateDepthTexture.bind(gl);
	    depthQueueProgram.getUniform("cameraMatrix").set4x4Matrix(matrixAsFlatArray, true);//TODO: Consolidate or abbreviate
	    depthQueueProgram.getUniform("renderListOffset").setui(modulusUintOffset + NUM_BLOCKS_PER_PASS);
	    tr.gpu.get().memoryManager.get().bindToUniform(4, depthQueueProgram,
		    depthQueueProgram.getUniform("rootBuffer"));
	    gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numTransparentVertices);
	}//end if(isUsingNewTexturing())
	
	gl.glEnable(GL3.GL_MULTISAMPLE);
	gl.glStencilFunc(GL3.GL_ALWAYS, 0xFF, 0xFF);//NEW
	gl.glDisable(GL3.GL_STENCIL_TEST);
	
	// DEFERRED STAGE
	gl.glDepthMask(true);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	if(tr.renderer.get().isBackfaceCulling())gl.glDisable(GL3.GL_CULL_FACE);
	final GLProgram deferredProgram = tr.renderer.get().getDeferredProgram();
	deferredProgram.use();
	gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);// Zero means
						    // "Draw to screen"
	//GLTexture.specifyTextureUnit(gl, 0);
	//    Texture.getGlobalTexture().bind(gl);
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
	
	if(!tr.getTrConfig().isUsingNewTexturing()){
	    // TRANSPARENT
	    // Turn off depth write, turn on transparency
	    tr.renderer.get().getPrimaryProgram().use();
	    useTextureMap.set((int)1);
	    gl.glDepthMask(false);
	    gl.glDepthFunc(GL3.GL_LESS);
	    gl.glEnable(GL3.GL_BLEND);
	    // ////////
	    // gl.glDepthFunc(GL3.GL_ALWAYS);
	    // ///////
	    renderListOffsetUniform.setui(modulusUintOffset + NUM_BLOCKS_PER_PASS);
	    // renderModeUniform.set(BLEND_PASS);
	    gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numTransparentVertices);
	    // ////////
	    // gl.glDepthFunc(GL3.GL_LESS);
	    // ////////
	    gl.glDepthMask(true);
	}//end if(!isUsingNewTexturing())
	
    }// end render()

    public Submitter<PositionedRenderable> getSubmitter() {
	return submitter;
    }

    public void reset() {
	numOpaqueBlocks = 0;
	numTransparentBlocks = 0;
	blendIndex = 0;
	opaqueIndex = 0;
	nearbyWorldObjects.clear();
    }//end reset()
    
    public List<WorldObject> getVisibleWorldObjectList(){
	return nearbyWorldObjects;
    }
}// end RenderList
