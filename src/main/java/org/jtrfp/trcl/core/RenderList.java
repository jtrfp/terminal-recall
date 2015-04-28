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

import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.coll.ListActionAdapter;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.IntArrayVariableList;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.pool.PartitionedIndexPool;
import org.jtrfp.trcl.pool.PartitionedIndexPoolImpl;

public class RenderList {
    public static final int 	NUM_SUBPASSES 		= 4;
    public static final int	NUM_BLOCKS_PER_SUBPASS 	= 1024 * 4;
    public static final int	NUM_BLOCKS_PER_PASS 	= NUM_BLOCKS_PER_SUBPASS
	    						* NUM_SUBPASSES;
    public static final int	NUM_RENDER_PASSES 	= 2;// Opaque + transparent //TODO: This is no longer the case

    private final 	TR 			tr;
    private 		int[] 			hostRenderListPageTable;
    private 	 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks,
    						numTransparentBlocks,
    						numUnoccludedTBlocks;
    private final	int			renderListIdx;
    private		long			rootBufferReadFinishedSync;
    private final	Renderer		renderer;
    private final	RendererFactory		rFactory;
    private final	ArrayList<WorldObject>	nearbyWorldObjects = new ArrayList<WorldObject>();
    private final 	IntBuffer 		previousViewport;
    private final	IntArrayVariableList    renderList;
    private final	ListActionTelemetry<VEC4Address> renderListTelemetry 
    						= new ListActionTelemetry<VEC4Address>();
    private final	PartitionedIndexPool<VEC4Address>renderListPool;
    private final	PartitionedIndexPool.Partition<VEC4Address>
    						opaquePartition,
    						transparentPartition,
    						unoccludedTPartition;
    private final	ListActionAdapter<PartitionedIndexPool.Entry<VEC4Address>,VEC4Address>	
    						renderingIndices;
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
		final Collection<VEC4Address> opOD = wo.getOpaqueObjectDefinitionAddresses();
		final Collection<VEC4Address> trOD = wo.getTransparentObjectDefinitionAddresses();
		 if(isUnoccluded){
			    numUnoccludedTBlocks += trOD.size();
				if(trOD.size()>0)
				     synchronized(unoccludedTPartition)
				      {for(VEC4Address od:trOD)unoccludedTPartition.newEntry(od);}
			    numUnoccludedTBlocks += opOD.size();
				if(opOD.size()>0)
				     synchronized(unoccludedTPartition)
				      {for(VEC4Address od:opOD)unoccludedTPartition.newEntry(od);}
			}//end if(trOD)
		    else{
			numTransparentBlocks += trOD.size();
			if(trOD.size()>0)
			     synchronized(transparentPartition)
			      {for(VEC4Address od:trOD)transparentPartition.newEntry(od);}
			numOpaqueBlocks += opOD.size();
			if(opOD.size()>0)
			 synchronized(opaquePartition)
			  {for(VEC4Address od:opOD)opaquePartition.newEntry(od);}
		    }//end if(occluded)
	    }//end if(WorldObject)
	}// end submit(...)
	
	@Override
	public void submit(Collection<PositionedRenderable> items) {
	    synchronized(items){
		for(PositionedRenderable r:items){submit(r);}
	    }//end for(items)
	}//end submit(...)
    };

    public RenderList(final GL3 gl, final Renderer renderer, final TR tr) {
	// Build VAO
	final IntBuffer ib = IntBuffer.allocate(1);
	this.tr = tr;
	this.renderer = renderer;
	this.rFactory = renderer.getRendererFactory();
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=tr.objectListWindow.get().create();
	this.renderList                 = new IntArrayVariableList(tr.objectListWindow.get().opaqueIDs,renderListIdx);
	this.renderListPool		= new PartitionedIndexPoolImpl<VEC4Address>();
	this.opaquePartition            = renderListPool.newPartition();
	this.transparentPartition       = renderListPool.newPartition();
	this.unoccludedTPartition       = renderListPool.newPartition();
	this.renderingIndices		= new ListActionAdapter<PartitionedIndexPool.Entry<VEC4Address>,VEC4Address>(new PartitionedIndexPool.EntryAdapter<VEC4Address>(VEC4Address.ZERO));
	renderListPool.getFlatEntries().addTarget(renderingIndices, true);//Convert entries to Integers
	renderingIndices.getOutput().addTarget(renderListTelemetry, true);//Pipe Integers to renderList
	
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
	//final Renderer renderer = tr.mainRenderer.get();
	final int size = olWindow.numPages();
	//////// Workaround for AMD bug where element zero always returns zero in frag. Shift up one.
	for (int i = 0; i < size-1; i++) {
	    hostRenderListPageTable[i+1] = olWindow.logicalPage2PhysicalPage(i);
	}// end for(hostRenderListPageTable.length)
	final GLProgram objectProgram = rFactory.getObjectProgram();
	objectProgram.use();
	objectProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);
	final GLProgram depthQueueProgram = rFactory.getDepthQueueProgram();
	depthQueueProgram.use();
	final GLProgram primaryProgram = rFactory.getOpaqueProgram();
	primaryProgram.use();
	final GLProgram vertexProgram = rFactory.getVertexProgram();
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
    
    private void updateRenderListToGPU(){
	if(renderListTelemetry.isModified()){
	    renderListPool.defragment(0);
	    renderList.rewind();
	    renderListTelemetry.drainListStateTo(renderList);
	}//end if(modified)
    }//end updateRenderingListToGPU()

    public void sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	updateStatesToGPU();
	updateRenderListToGPU();
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
	
	// OBJECT STAGE
	final GLProgram objectProgram = rFactory.getObjectProgram();
	objectProgram.use();
	objectProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);
	
	gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	objectProgram.getUniform("cameraMatrix").set4x4Matrix(renderer.getCameraMatrixAsFlatArray(), true);
	rFactory.getObjectFrameBuffer().bindToDraw();
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
    	}//end variable scope
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
	///// VERTEX STAGE
	final int relevantVertexBufferWidth = ((int)(RendererFactory.VERTEX_BUFFER_WIDTH/3))*3;
	final GLProgram vertexProgram = rFactory.getVertexProgram();
	vertexProgram.use();
	rFactory.getVertexFrameBuffer().bindToDraw();
	vertexProgram.getUniform("logicalVec4Offset").setui(opaqueRenderListLogicalVec4Offset);
	gpu.memoryManager.get().bindToUniform(0, vertexProgram,
		vertexProgram.getUniform("rootBuffer"));
	rFactory.getCamMatrixTexture().bindToTextureUnit(1, gl);
	rFactory.getNoCamMatrixTexture().bindToTextureUnit(2, gl);
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
	
	///// PRIMITIVE STAGE
	//Almost like a geometry shader, except writing lookup textures for each primitive.
	rFactory.getPrimitiveProgram().use();
	rFactory.getPrimitiveFrameBuffer().bindToDraw();
	rFactory.getVertexXYTexture().bindToTextureUnit(0, gl);
	rFactory.getVertexWTexture().bindToTextureUnit(1, gl);
	rFactory.getVertexZTexture().bindToTextureUnit(2, gl);
	rFactory.getVertexUVTexture().bindToTextureUnit(3, gl);
	rFactory.getVertexNormXYTexture().bindToTextureUnit(4, gl);
	rFactory.getVertexNormZTexture().bindToTextureUnit(5, gl);
	gl.glDisable(GL3.GL_PROGRAM_POINT_SIZE);//Asserts that point size is set only from CPU
	gl.glPointSize(2*RendererFactory.PRIMITIVE_BUFFER_OVERSAMPLING);//2x2 frags
	gl.glViewport(0, 0, 
		RendererFactory.PRIMITIVE_BUFFER_WIDTH*RendererFactory.PRIMITIVE_BUFFER_OVERSAMPLING, 
		RendererFactory.PRIMITIVE_BUFFER_HEIGHT*RendererFactory.PRIMITIVE_BUFFER_OVERSAMPLING);
	
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
	final GLProgram primaryProgram = rFactory.getOpaqueProgram();
	primaryProgram.use();
	rFactory.getVertexXYTexture().bindToTextureUnit(1, gl);
	rFactory.getVertexUVTexture().bindToTextureUnit(2, gl);
	rFactory.getVertexTextureIDTexture().bindToTextureUnit(3, gl);
	rFactory.getVertexZTexture().bindToTextureUnit(4, gl);
	rFactory.getVertexWTexture().bindToTextureUnit(5, gl);
	rFactory.getVertexNormXYTexture().bindToTextureUnit(6, gl);
	rFactory.getVertexNormZTexture().bindToTextureUnit(7, gl);
	rFactory.getOpaqueFrameBuffer().bindToDraw();
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
	
	if(rFactory.isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	
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
	
	// Cleanup
	gpu.defaultProgram();
	gpu.defaultFrameBuffers();
	gpu.defaultTIU();
	gpu.defaultTexture();
	
	// DEPTH QUEUE DRAW
	// DRAW
	final GLProgram depthQueueProgram = rFactory.getDepthQueueProgram();
	depthQueueProgram.use();
	rFactory.getDepthQueueFrameBuffer().bindToDraw();
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glEnable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_DEPTH_CLAMP);
	gl.glDepthRange(0, 1);
	gl.glDepthFunc(GL3.GL_LEQUAL);
	
	// Thanks to: http://www.andersriggelsen.dk/glblendfunc.php
	//Set up float shift queue blending
	gl.glEnable(GL3.GL_BLEND);
	gl.glBlendFunc(GL3.GL_ONE, GL3.GL_CONSTANT_COLOR);
	gl.glBlendEquation(GL3.GL_FUNC_ADD);
	gl.glBlendColor(16f, 16f, 16f, 16f);// upshift 4 bits
	
	//object, root, depth, xy
	rFactory.getOpaqueDepthTexture().bindToTextureUnit(1,gl);
	rFactory.getVertexXYTexture().bindToTextureUnit(2, gl);
	//renderer.getVertexUVTexture().bindToTextureUnit(3, gl);
	rFactory.getVertexTextureIDTexture().bindToTextureUnit(4, gl);
	rFactory.getVertexZTexture().bindToTextureUnit(5, gl);
	rFactory.getVertexWTexture().bindToTextureUnit(6, gl);
	rFactory.getVertexNormXYTexture().bindToTextureUnit(7, gl);
	rFactory.getVertexNormZTexture().bindToTextureUnit(8, gl);
	
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
	
	// DEFERRED STAGE
	if(rFactory.isBackfaceCulling())gl.glDisable(GL3.GL_CULL_FACE);
	final GLProgram deferredProgram = rFactory.getDeferredProgram();
	deferredProgram.use();
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_BLEND);
	
	final GLFrameBuffer renderTarget = renderer.getRenderingTarget();
	if(renderTarget!=null)
	    renderTarget.bindToDraw();
	else gpu.defaultFrameBuffers();
	
	gpu.memoryManager.get().bindToUniform(0, deferredProgram,
		    deferredProgram.getUniform("rootBuffer"));
	renderer.getSkyCube().getSkyCubeTexture().bindToTextureUnit(1,gl);
	/// 2 UNUSED
	gpu.textureManager.get().vqCodebookManager.get().getESTuTvTexture().bindToTextureUnit(3,gl);
	gpu.textureManager.get().vqCodebookManager.get().getRGBATexture().bindToTextureUnit(4,gl);
	rFactory.getOpaquePrimitiveIDTexture().bindToTextureUnit(5,gl);
	rFactory.getLayerAccumulatorTexture().bindToTextureUnit(6,gl);
	rFactory.getVertexTextureIDTexture().bindToTextureUnit(7,gl);
	rFactory.getPrimitiveUVZWTexture().bindToTextureUnit(8,gl);
	rFactory.getPrimitiveNormTexture().bindToTextureUnit(9, gl);
	
	deferredProgram.getUniform("bypassAlpha").setui(!renderer.getCamera().isFogEnabled()?1:0);
	deferredProgram.getUniform("projectionRotationMatrix")
		.set4x4Matrix(renderer.getCamRotationProjectionMatrix(), true);
	//Execute the draw to a screen quad
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 36);
	//Cleanup
	gl.glDisable(GL3.GL_BLEND);
	
	// DEPTH QUEUE ERASE
	rFactory.getDepthQueueFrameBuffer().bindToDraw();
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	gpu.defaultFrameBuffers();
	//Cleanup
	gl.glDepthMask(true);
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultFrameBuffers();
	
	//INTERMEDIATE ERASE
	rFactory.getOpaqueFrameBuffer().bindToDraw();
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
	/*synchronized(opaqueObjectDefs){
	    opaqueObjectDefs.clear();}
	synchronized(transparentObjectDefs){
	    transparentObjectDefs.clear();}
	synchronized(unoccludedTObjectDefs){
	    unoccludedTObjectDefs.clear();}*/
	synchronized(opaquePartition){
	 opaquePartition.removeAllEntries();}//TODO: Further-optimize partitions' removeAllEntries...
	synchronized(transparentPartition){
	 transparentPartition.removeAllEntries();}
	synchronized(unoccludedTPartition){
	 unoccludedTPartition.removeAllEntries();}
    }//end reset()
    
    public List<WorldObject> getVisibleWorldObjectList(){
	return nearbyWorldObjects;
    }

    public int getAttribDummyID() {
	return dummyBufferID;
    }
}// end RenderList
