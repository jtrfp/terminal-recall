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
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.BriefingScreen;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.gpu.GLProgram;
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
    private 		int 			numOpaqueBlocks,
    						numTransparentBlocks,
    						numUnoccludedTBlocks;
    private final	int			renderListIdx;
    private		long			rootBufferReadFinishedSync;
    private final	Renderer		renderer;
    private final	ArrayList<WorldObject>	nearbyWorldObjects = new ArrayList<WorldObject>();
    private final 	IntBuffer 		previousViewport;
    private final	ArrayList<ByteBuffer>	opaqueObjectDefs = new ArrayList<ByteBuffer>(),
	    					transparentObjectDefs = new ArrayList<ByteBuffer>(),
	    					unoccludedTObjectDefs = new ArrayList<ByteBuffer>();
    private final 	Submitter<PositionedRenderable> 
    						submitter = new Submitter<PositionedRenderable>() {
	@Override
	public void submit(PositionedRenderable item) {
	    boolean isUnoccluded = false;
	    if (item instanceof WorldObject) {
		final WorldObject wo = (WorldObject)item;
		if (!wo.isActive())
		    return;
		synchronized(nearbyWorldObjects)
		 {nearbyWorldObjects.add(wo);}
		if(!wo.isVisible())return;
		isUnoccluded = ((WorldObject)item).isImmuneToOpaqueDepthTest();
	    }//end if(WorldObject)
	    final ByteBuffer opOD = item.getOpaqueObjectDefinitionAddresses();
	    final ByteBuffer trOD = item.getTransparentObjectDefinitionAddresses();
	    
	    numOpaqueBlocks += opOD.capacity() / 4;
	    if(opOD.capacity()>0)
		     synchronized(opaqueObjectDefs)
		      {opaqueObjectDefs.add(opOD);}
	    
	    if(isUnoccluded){
		final WorldObject wo = (WorldObject)item;
		    numUnoccludedTBlocks += trOD.capacity() / 4;
			if(trOD.capacity()>0)
			     synchronized(unoccludedTObjectDefs)
			      {unoccludedTObjectDefs.add(trOD);}
		}//end if(trOD)
	    else{numTransparentBlocks += trOD.capacity() / 4;
	    if(trOD.capacity()>0)
		     synchronized(transparentObjectDefs)
		      {transparentObjectDefs.add(trOD);}
	    }//end if(trOD)
	}// end submit(...)

	@Override
	public void submit(Collection<PositionedRenderable> items) {
	    synchronized(items){
		for(PositionedRenderable r:items){submit(r);}
	    }//end for(items)
	}//end submit(...)
    };
    
    void flushObjectDefsToGPU(){
	int byteIndex=0;
	synchronized(opaqueObjectDefs){
	 for(ByteBuffer bb:opaqueObjectDefs){
	    synchronized(bb){
	     bb.clear();
	     tr.objectListWindow.get().opaqueIDs.set(renderListIdx, byteIndex, bb);
	      byteIndex += bb.capacity();}
	 }}
	synchronized(transparentObjectDefs){
	 for(ByteBuffer bb:transparentObjectDefs){
	    synchronized(bb){
	     bb.clear();
	     tr.objectListWindow.get().opaqueIDs.set(renderListIdx, byteIndex, bb);
	     byteIndex += bb.capacity();}
	 }}
	synchronized(unoccludedTObjectDefs){
		 for(ByteBuffer bb:unoccludedTObjectDefs){
		    synchronized(bb){
		     bb.clear();
		     tr.objectListWindow.get().opaqueIDs.set(renderListIdx, byteIndex, bb);
		     byteIndex += bb.capacity();}
		 }}
    }//end flushObjectDefsToGPU()

    public RenderList(final GL3 gl, final Renderer renderer, final TR tr) {
	// Build VAO
	final IntBuffer ib = IntBuffer.allocate(1);
	this.tr = tr;
	this.renderer = renderer;
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=tr.objectListWindow.get().create();
	final TRFuture<Void> task0 = tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		gl.glGenBuffers(1, ib);
		ib.clear();
		dummyBufferID = ib.get();
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, 1, null, GL3.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 1, GL3.GL_BYTE, false, 0, 0);
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
		return null;
	    }
	});//end task0
	
	hostRenderListPageTable = new int[((ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS
		* RenderList.NUM_RENDER_PASSES)
		/ PagedByteBuffer.PAGE_SIZE_BYTES)*3];
	
	task0.get();
    }// end constructor
    
    private void sendRenderListPageTable(){
	final ObjectListWindow olWindow = RenderList.this.tr
		    .objectListWindow.get();
	final Renderer renderer = tr.renderer.get();
	final int size = olWindow.numPages();
	//////// Workaround for AMD bug where element zero always returns zero in frag. Shift up one.
	for (int i = 0; i < size-1; i++) {
	    hostRenderListPageTable[i+1] = olWindow.logicalPage2PhysicalPage(i);
	    //System.out.print(" "+hostRenderListPageTable[i+1]);
	}// end for(hostRenderListPageTable.length)
	System.out.println();
	final GLProgram objectProgram = renderer.objectProgram;
	objectProgram.use();
	objectProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);
	final GLProgram depthQueueProgram = renderer.getDepthQueueProgram();
	depthQueueProgram.use();
	final GLProgram primaryProgram = renderer.getOpaqueProgram();
	primaryProgram.use();
	final GLProgram vertexProgram = renderer.getVertexProgram();
	vertexProgram.use();
	vertexProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);
	tr.gpu.get().defaultProgram();
	sentPageTable=true;
    }

    private static int frameCounter = 0;

    private void updateStatesToGPU() {
	synchronized(tr.getThreadManager().gameStateLock){
	synchronized(nearbyWorldObjects){
	final int size=nearbyWorldObjects.size();
	for (int i=0; i<size; i++) 
	    nearbyWorldObjects.get(i).updateStateToGPU();
	}}
    }//end updateStatesToGPU

    public void sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	updateStatesToGPU();
    }//end sendToGPU
    
    private boolean sentPageTable=false;
    
    private void revertViewportToWindow(GL3 gl){
	gl.glViewport(
	 previousViewport.get(0), 
	 previousViewport.get(1), 
	 previousViewport.get(2), 
	 previousViewport.get(3));
    }//end revertViewportToWindow()
    
    public void render(final GL3 gl) throws NotReadyException {
	if(!sentPageTable)sendRenderListPageTable();
	final GPU gpu = tr.gpu.get();
	final ObjectListWindow olWindow = tr.objectListWindow.get();
	final int opaqueRenderListLogicalVec4Offset = ((olWindow.getObjectSizeInBytes()*renderListIdx)/16);
	final int primsPerBlock = GPU.GPU_VERTICES_PER_BLOCK/3;
	final int numPrimitives = (numTransparentBlocks+numOpaqueBlocks+numUnoccludedTBlocks)*primsPerBlock;
	
	renderer.getSkyCube().render(this,gl);
	
	// OBJECT STAGE
	final GLProgram objectProgram = renderer.getObjectProgram();
	objectProgram.use();
	objectProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);
	
	gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	objectProgram.getUniform("cameraMatrix").set4x4Matrix(renderer.getCameraMatrixAsFlatArray(), true);
	renderer.getObjectFrameBuffer().bindToDraw();
	gl.glGetIntegerv(GL3.GL_VIEWPORT, previousViewport);
	gl.glViewport(0, 0, 1024, 128);
	gpu.memoryManager.get().bindToUniform(0, objectProgram,
		objectProgram.getUniform("rootBuffer"));
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDisable(GL3.GL_LINE_SMOOTH);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glLineWidth(1);
	{//Start variable scope
	 int remainingBlocks = numOpaqueBlocks+numTransparentBlocks+numUnoccludedTBlocks;
    	 int numRows = (int)Math.ceil(remainingBlocks/256.);
    	 for(int i=0; i<numRows; i++){
    	     gl.glDrawArrays(GL3.GL_LINE_STRIP, i*257, (remainingBlocks<=256?remainingBlocks:256)+1);
    	     remainingBlocks -= 256;
    	 }
    	 /*
    	 final int opaqueVtxOffset = numOpaqueRows*257;
    	 int numTransRows = (int)Math.ceil(numTransparentBlocks/256.);
    	 remainingBlocks = numTransparentBlocks;
    	 */
    	 /*
    	 //Need to shift over to the transparent list and then reverse-compensate for the vertex offset
    	 objectProgram.getUniform("logicalVec4Offset").setui(
    		 transRenderListLogicalVec4Offset-
    		 (numOpaqueBlocks/4));//Each block entry is 4 bytes, or 1/4 of a VEC4
    	 */
    	 /*
    	 for(int i=0; i<numTransRows; i++){
    	     gl.glDrawArrays(GL3.GL_LINE_STRIP, opaqueVtxOffset+i*257, (remainingBlocks<=256?remainingBlocks:256)+1);
    	     remainingBlocks -= 256;
    	 }*/
    	}//end variable scope
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
	///// VERTEX STAGE
	final int relevantVertexBufferWidth = ((int)(Renderer.VERTEX_BUFFER_WIDTH/3))*3;
	final GLProgram vertexProgram = renderer.getVertexProgram();
	vertexProgram.use();
	renderer.getVertexFrameBuffer().bindToDraw();
	vertexProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);
	gpu.memoryManager.get().bindToUniform(0, vertexProgram,
		vertexProgram.getUniform("rootBuffer"));
	renderer.getCamMatrixTexture().bindToTextureUnit(1, gl);
	renderer.getNoCamMatrixTexture().bindToTextureUnit(2, gl);
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glViewport(0, 0, 
		relevantVertexBufferWidth, 
		(int)Math.ceil((double)(numPrimitives*3)/(double)relevantVertexBufferWidth));//256*256 = 65536, max we can handle.
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);//Opaque
	//gl.glViewport(0, (NUM_BLOCKS_PER_PASS*GPU.GPU_VERTICES_PER_BLOCK)/relevantVertexBufferWidth, relevantVertexBufferWidth, 256);
	//gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);//Transparent
	//Cleanup
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
	
	///// PRIMITIVE STAGE
	//Almost like a geometry shader, except writing lookup textures for each primitive.
	renderer.getPrimitiveProgram().use();
	renderer.getPrimitiveFrameBuffer().bindToDraw();
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);//TODO: See if this can be removed
	renderer.getVertexXYTexture().bindToTextureUnit(0, gl);
	renderer.getVertexWTexture().bindToTextureUnit(1, gl);
	renderer.getVertexZTexture().bindToTextureUnit(2, gl);
	renderer.getVertexUVTexture().bindToTextureUnit(3, gl);
	renderer.getVertexNormXYTexture().bindToTextureUnit(4, gl);
	renderer.getVertexNormZTexture().bindToTextureUnit(5, gl);
	gl.glDisable(GL3.GL_PROGRAM_POINT_SIZE);//Asserts that point size is set only from CPU
	gl.glPointSize(2*Renderer.PRIMITIVE_BUFFER_OVERSAMPLING);//2x2 frags
	gl.glViewport(0, 0, 
		Renderer.PRIMITIVE_BUFFER_WIDTH*Renderer.PRIMITIVE_BUFFER_OVERSAMPLING, 
		Renderer.PRIMITIVE_BUFFER_HEIGHT*Renderer.PRIMITIVE_BUFFER_OVERSAMPLING);
	
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_BLEND);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_CULL_FACE);
	
	//Everything
	gl.glDrawArrays(GL3.GL_POINTS, 0, (numTransparentBlocks+numOpaqueBlocks+numUnoccludedTBlocks)*primsPerBlock);
	//Cleanup
	gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
	gl.glPointSize(1);
	revertViewportToWindow(gl);
	
	gl.glDepthMask(true);
	// OPAQUE.DRAW STAGE
	final GLProgram primaryProgram = tr.renderer.get().getOpaqueProgram();
	primaryProgram.use();
	renderer.getVertexXYTexture().bindToTextureUnit(1, gl);
	renderer.getVertexUVTexture().bindToTextureUnit(2, gl);
	renderer.getVertexTextureIDTexture().bindToTextureUnit(3, gl);
	renderer.getVertexZTexture().bindToTextureUnit(4, gl);
	renderer.getVertexWTexture().bindToTextureUnit(5, gl);
	renderer.getVertexNormXYTexture().bindToTextureUnit(6, gl);
	renderer.getVertexNormZTexture().bindToTextureUnit(7, gl);
	renderer.getOpaqueFrameBuffer().bindToDraw();
	final int numOpaqueVertices = numOpaqueBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	final int numTransparentVertices = numTransparentBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	final int numUnoccludedVertices = numUnoccludedTBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	// Turn on depth write, turn off transparency
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_LESS);
	gl.glEnable(GL3.GL_DEPTH_TEST);
	gl.glEnable(GL3.GL_DEPTH_CLAMP);
	//gl.glDepthRange((BriefingScreen.MAX_Z_DEPTH+1)/2, 1);
	
	if(tr.renderer.get().isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	//final int verticesPerSubPass	= (NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK);
	//final int numSubPasses		= (numOpaqueVertices / verticesPerSubPass) + 1;
	//int remainingVerts		= numOpaqueVertices;
	
	if (frameCounter == 0) {
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numOpaqueBlocks",
		    "" + numOpaqueBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numTransparentBlocks",
		    "" + numTransparentBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numUnoccludedTransparentBlocks",
		    "" + numUnoccludedTBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.approxNumSceneTriangles",
		    "" + ((numOpaqueBlocks+numTransparentBlocks)*GPU.GPU_VERTICES_PER_BLOCK)/3);
	}
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numOpaqueVertices);
	/*
	for (int sp = 0; sp < numSubPasses; sp++) {
	    final int numVerts = remainingVerts <= verticesPerSubPass ? remainingVerts
		    : verticesPerSubPass;
	    remainingVerts -= numVerts;
	    final int newOffset = sp
		    * NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK;
	    gl.glDrawArrays(GL3.GL_TRIANGLES, newOffset, numVerts);
	}// end for(subpasses)
	*/
	// Cleanup
	gpu.defaultProgram();
	gpu.defaultFrameBuffers();
	gpu.defaultTIU();
	gpu.defaultTexture();
	
	// DEPTH QUEUE DRAW
	// DRAW
	final GLProgram depthQueueProgram = renderer.getDepthQueueProgram();
	depthQueueProgram.use();
	renderer.getDepthQueueFrameBuffer().bindToDraw();
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glEnable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_DEPTH_CLAMP);
	gl.glDepthRange(0, 1);
	//gl.glEnable(GL3.GL_SAMPLE_MASK);
	gl.glDepthFunc(GL3.GL_LEQUAL);
	//gl.glDisable(GL3.GL_MULTISAMPLE);
	//gl.glStencilFunc(GL3.GL_EQUAL, 0x1, 0xFF);
	//gl.glStencilOp(GL3.GL_DECR, GL3.GL_DECR, GL3.GL_DECR);
	//gl.glSampleMaski(0, 0xFF);
	
	// Thanks to: http://www.andersriggelsen.dk/glblendfunc.php
	//Set up float shift queue blending
	gl.glEnable(GL3.GL_BLEND);
	gl.glBlendFunc(GL3.GL_ONE, GL3.GL_CONSTANT_COLOR);
	gl.glBlendEquation(GL3.GL_FUNC_ADD);
	gl.glBlendColor(16f, 16f, 16f, 16f);// upshift 4 bits
	
	//object, root, depth, xy
	renderer.getOpaqueDepthTexture().bindToTextureUnit(1,gl);
	renderer.getVertexXYTexture().bindToTextureUnit(2, gl);
	//renderer.getVertexUVTexture().bindToTextureUnit(3, gl);
	renderer.getVertexTextureIDTexture().bindToTextureUnit(4, gl);
	renderer.getVertexZTexture().bindToTextureUnit(5, gl);
	renderer.getVertexWTexture().bindToTextureUnit(6, gl);
	renderer.getVertexNormXYTexture().bindToTextureUnit(7, gl);
	renderer.getVertexNormZTexture().bindToTextureUnit(8, gl);
	
	gl.glDrawArrays(GL3.GL_TRIANGLES, numOpaqueVertices, numTransparentVertices);
	//UNOCCLUDED TRANSPARENT
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDrawArrays(GL3.GL_TRIANGLES, numOpaqueVertices+numTransparentVertices, numUnoccludedVertices);
	
	//Cleanup
	gl.glDisable(GL3.GL_BLEND);
	gpu.defaultProgram();
	gpu.defaultFrameBuffers();
	gpu.defaultTIU();
	gpu.defaultTexture();
	
	// FENCE
	rootBufferReadFinishedSync = gl.glFenceSync(GL3.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	
	//gl.glDisable(GL3.GL_MULTISAMPLE);
	//gl.glStencilFunc(GL3.GL_ALWAYS, 0xFF, 0xFF);
	//gl.glDisable(GL3.GL_STENCIL_TEST);
	
	// DEFERRED STAGE
	gl.glDepthMask(true);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glEnable(GL3.GL_BLEND);
	if(tr.renderer.get().isBackfaceCulling())gl.glDisable(GL3.GL_CULL_FACE);
	final GLProgram deferredProgram = tr.renderer.get().getDeferredProgram();
	deferredProgram.use();
	gl.glEnable(GL3.GL_BLEND);
	gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
	gl.glBlendEquation(GL3.GL_FUNC_ADD);
	gpu.defaultFrameBuffers();
	gpu.memoryManager.get().bindToUniform(0, deferredProgram,
		    deferredProgram.getUniform("rootBuffer"));
	/// 1 UNUSED
	/// 2 UNUSED
	/// 3 UNUSED
	gpu.textureManager.get().vqCodebookManager.get().getRGBATexture().bindToTextureUnit(4,gl);
	renderer.getOpaquePrimitiveIDTexture().bindToTextureUnit(5,gl);
	renderer.getLayerAccumulatorTexture().bindToTextureUnit(6,gl);
	renderer.getVertexTextureIDTexture().bindToTextureUnit(7,gl);
	renderer.getPrimitiveUVZWTexture().bindToTextureUnit(8,gl);
	renderer.getPrimitiveNormTexture().bindToTextureUnit(9, gl);
	
	deferredProgram.getUniform("bypassAlpha").setui(!renderer.getCamera().isFogEnabled()?1:0);
	//Execute the draw to a screen quad
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
	//Cleanup
	gl.glDisable(GL3.GL_BLEND);
	
	// DEPTH QUEUE ERASE
	renderer.getDepthQueueFrameBuffer().bindToDraw();
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	gpu.defaultFrameBuffers();
	//Cleanup
	gl.glDepthMask(true);
	//gl.glDisable(GL3.GL_MULTISAMPLE);
	//gl.glDisable(GL3.GL_SAMPLE_MASK);
	//gl.glDisable(GL3.GL_STENCIL_TEST);
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultFrameBuffers();
	
	//INTERMEDIATE ERASE
	renderer.getOpaqueFrameBuffer().bindToDraw();
	gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
	gl.glFlush();
	gl.glWaitSync(rootBufferReadFinishedSync, 0, GL3.GL_TIMEOUT_IGNORED);
    }// end render()

    public Submitter<PositionedRenderable> getSubmitter() {
	return submitter;
    }

    public void reset() {
	numOpaqueBlocks 	= 0;
	numTransparentBlocks 	= 0;
	numUnoccludedTBlocks	= 0;
	synchronized(nearbyWorldObjects)
	 {nearbyWorldObjects.clear();}
	synchronized(opaqueObjectDefs){
	    opaqueObjectDefs.clear();}
	synchronized(transparentObjectDefs){
	    transparentObjectDefs.clear();}
	synchronized(unoccludedTObjectDefs){
	    unoccludedTObjectDefs.clear();}
    }//end reset()
    
    public List<WorldObject> getVisibleWorldObjectList(){
	return nearbyWorldObjects;
    }

    public int getAttribDummyID() {
	return dummyBufferID;
    }
}// end RenderList
