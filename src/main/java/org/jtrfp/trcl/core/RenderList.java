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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.VerboseExecutorService;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionUnpacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.DecoupledCollectionActionDispatcher;
import org.jtrfp.trcl.coll.ImplicitBiDiAdapter;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.VertexProcessingStage;
import org.jtrfp.trcl.gui.Reporter;
import org.jtrfp.trcl.mem.IntArrayVariableList;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.pool.IndexList;

import com.ochafik.util.Adapter;
import com.ochafik.util.CollectionAdapter;

public class RenderList {
    public static final int 	NUM_SUBPASSES 		= 4;
    public static final int	NUM_BLOCKS_PER_SUBPASS 	= 1024 * 4;
    public static final int	NUM_BLOCKS_PER_PASS 	= NUM_BLOCKS_PER_SUBPASS
	    						* NUM_SUBPASSES;
    public static final int	NUM_RENDER_PASSES 	= 2;// Opaque + transparent //TODO: This is no longer the case

    private 		int[] 			hostRenderListPageTable;
    private 	 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks,
    						numTransparentBlocks,
    						numUnoccludedTBlocks;
    private final	int			renderListIdx;
    private		long			rootBufferReadFinishedSync;
    private final	GPU			gpu;
    private final	Renderer		renderer;
    private final	RendererFactory		rFactory;
    private final	ObjectListWindow	objectListWindow;
    private final       ThreadManager           threadManager;
    private final	Reporter		reporter;
    //private final	ArrayList<WorldObject>	nearbyWorldObjects = new ArrayList<WorldObject>();
    private final 	IntBuffer 		previousViewport;
    private final	IntArrayVariableList    renderList;
    private final	ListActionTelemetry<VEC4Address> renderListTelemetry 
    						= new ListActionTelemetry<VEC4Address>();
    //private final	PartitionedIndexPool<VEC4Address>renderListPool;
    /*private final	PartitionedIndexPool.Partition<VEC4Address>
    						opaquePartition,
    						transparentPartition,
    						unoccludedTPartition;*/
    private final	IndexList<VEC4Address>	opaqueIL, transIL, unoccludedIL;
    private final	DecoupledCollectionActionDispatcher<PositionedRenderable>
    						relevantPositionedRenderables = new DecoupledCollectionActionDispatcher<PositionedRenderable>(new HashSet<PositionedRenderable>(), new VerboseExecutorService(Executors.newSingleThreadExecutor()));
    private final	PartitionedList<VEC4Address>
    						renderListPoolNEW = new PartitionedList<VEC4Address>(renderListTelemetry);
    private final	CollectionAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>,PositionedRenderable>
    	opaqueODAddrsColl    = new CollectionAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>,PositionedRenderable>(new CollectionActionUnpacker<CollectionActionDispatcher<VEC4Address>>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(opaqueIL     = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),relevantPositionedRenderables.getExecutor()))),opaqueODAdapter),
    	transODAddrsColl     = new CollectionAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>,PositionedRenderable>(new CollectionActionUnpacker<CollectionActionDispatcher<VEC4Address>>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(transIL      = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),relevantPositionedRenderables.getExecutor()))),transODAdapter ), 
    	unoccludedODAddrsColl= new CollectionAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>,PositionedRenderable>(new CollectionActionUnpacker<CollectionActionDispatcher<VEC4Address>>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(unoccludedIL = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),relevantPositionedRenderables.getExecutor()))),unoccludedODAddrAdapter);

    public RenderList(final GPU gpu, final Renderer renderer, final ObjectListWindow objectListWindow, ThreadManager threadManager, Reporter reporter) {
	// Build VAO
	final IntBuffer ib = IntBuffer.allocate(1);
	this.reporter        = reporter;
	this.threadManager   = threadManager;
	this.gpu             = gpu;
	this.objectListWindow=objectListWindow;
	this.renderer        = renderer;
	this.rFactory        = renderer.getRendererFactory();
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=objectListWindow.create();
	this.renderList                 = new IntArrayVariableList(objectListWindow.opaqueIDs,renderListIdx);
	//this.renderListPool		= new PartitionedIndexPoolImpl<VEC4Address>();
	//this.opaquePartition            = renderListPool.newPartition();
	//this.transparentPartition       = renderListPool.newPartition();
	//this.unoccludedTPartition       = renderListPool.newPartition();
	//this.renderingIndices		= new ListActionAdapter<PartitionedIndexPool.Entry<VEC4Address>,VEC4Address>(new PartitionedIndexPool.EntryAdapter<VEC4Address>(VEC4Address.ZERO));
	//renderListPool.getFlatEntries().addTarget(renderingIndices, true);//Convert entries to Integers
	//((ListActionDispatcher)renderingIndices.getOutput()).addTarget(renderListTelemetry, true);//Pipe Integers to renderList
	
	relevantPositionedRenderables.addTarget(opaqueODAddrsColl, true);
	relevantPositionedRenderables.addTarget(transODAddrsColl, true);
	relevantPositionedRenderables.addTarget(unoccludedODAddrsColl, true);
	
	final TRFuture<Void> task0 = gpu.submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		final GL3 gl = gpu.getGl();
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
	//final Renderer renderer = tr.mainRenderer.get();
	final int size = objectListWindow.numPages();
	//////// Workaround for AMD bug where element zero always returns zero in frag. Shift up one.
	for (int i = 0; i < size-1; i++) {
	    hostRenderListPageTable[i+1] = objectListWindow.logicalPage2PhysicalPage(i);
	}// end for(hostRenderListPageTable.length)
	rFactory.getObjectProcessingStage().sendRenderListPageTable(hostRenderListPageTable);
	//final GLProgram objectProgram = rFactory.getObjectProgram();
	
	final GLProgram depthQueueProgram = rFactory.getDepthQueueProgram();
	depthQueueProgram.use();
	final GLProgram primaryProgram = rFactory.getOpaqueProgram();
	primaryProgram.use();
	final GLProgram vertexProgram = rFactory.getVertexProcessingStage().getVertexProgram();
	vertexProgram.use();
	vertexProgram.getUniform("renderListPageTable").setArrayui(hostRenderListPageTable);
	gpu.defaultProgram();
	sentPageTable=true;
    }

    private static int frameCounter = 0;

    private void updateStatesToGPU() {
	synchronized(threadManager.gameStateLock){
	synchronized(relevantPositionedRenderables){
	for (PositionedRenderable renderable:relevantPositionedRenderables) 
	    renderable.updateStateToGPU(renderer);
	    renderer.getCamera().getCompleteMatrixAsFlatArray(renderer.cameraMatrixAsFlatArray);//TODO
	    renderer.getCamera().getProjectionRotationMatrixAsFlatArray(renderer.camRotationProjectionMatrix);//TODO
	}}
    }//end updateStatesToGPU
    
    private final CyclicBarrier renderListExecutorBarrier = new CyclicBarrier(2);
    private void updateRenderListToGPU(){
	if(renderListTelemetry.isModified()){
	    relevantPositionedRenderables.getExecutor().execute(new Runnable(){
		@Override
		public void run() {
		    //Defragment
		    opaqueIL    .defragment();
		    transIL     .defragment();
		    unoccludedIL.defragment();
		    numOpaqueBlocks     = opaqueIL    .delegateSize();
		    numTransparentBlocks= transIL     .delegateSize();
		    numUnoccludedTBlocks= unoccludedIL.delegateSize();
		    renderList.rewind();
		    renderListTelemetry.drainListStateTo(renderList);
		    //System.out.println("RenderList.updateRenderListToGPU() performing on-demand update... "+renderList.size());
		    try{renderListExecutorBarrier.await();}catch(Exception e){e.printStackTrace();}
		}});
	    try{renderListExecutorBarrier.await();}catch(Exception e){e.printStackTrace();}
	}//end if(modified)
    }//end updateRenderingListToGPU()

    public void sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	updateStatesToGPU();
	updateRenderListToGPU();
    }//end sendToGPU
    
    private boolean sentPageTable=false;
    
    private void saveWindowViewportState(GL3 gl){
	gl.glGetIntegerv(GL3.GL_VIEWPORT, previousViewport);
    }
    
    private void revertViewportToWindow(GL3 gl){
	gl.glViewport(
	 previousViewport.get(0), 
	 previousViewport.get(1), 
	 previousViewport.get(2), 
	 previousViewport.get(3));
    }//end revertViewportToWindow()
    
    public void render(final GL3 gl) throws NotReadyException {
	if(!sentPageTable)sendRenderListPageTable();
	final int renderListLogicalVec4Offset = ((objectListWindow.getObjectSizeInBytes()*renderListIdx)/16);
	final int primsPerBlock = GPU.GPU_VERTICES_PER_BLOCK/3;
	final int numPrimitives = (numTransparentBlocks+numOpaqueBlocks+numUnoccludedTBlocks)*primsPerBlock;
	saveWindowViewportState(gl);
	// OBJECT STAGE
	
	rFactory.getObjectProcessingStage().process(gl,renderer.getCameraMatrixAsFlatArray(),
		renderListLogicalVec4Offset, numTransparentBlocks, numOpaqueBlocks, numUnoccludedTBlocks);
	//// VERTEX STAGE
	VertexProcessingStage vps = rFactory.getVertexProcessingStage();
	vps.process(gl, renderListLogicalVec4Offset, numPrimitives);
	///// PRIMITIVE STAGE
	//Almost like a geometry shader, except writing lookup textures for each primitive.
	rFactory.getPrimitiveProgram().use();
	rFactory.getPrimitiveFrameBuffer().bindToDraw();
	vps.getVertexXYTexture().bindToTextureUnit(0, gl);
	vps.getVertexWTexture().bindToTextureUnit(1, gl);
	vps.getVertexZTexture().bindToTextureUnit(2, gl);
	vps.getVertexUVTexture().bindToTextureUnit(3, gl);
	vps.getVertexNormXYTexture().bindToTextureUnit(4, gl);
	vps.getVertexNormZTexture().bindToTextureUnit(5, gl);
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
	vps.getVertexXYTexture().bindToTextureUnit(1, gl);
	vps.getVertexUVTexture().bindToTextureUnit(2, gl);
	vps.getVertexTextureIDTexture().bindToTextureUnit(3, gl);
	vps.getVertexZTexture().bindToTextureUnit(4, gl);
	vps.getVertexWTexture().bindToTextureUnit(5, gl);
	vps.getVertexNormXYTexture().bindToTextureUnit(6, gl);
	vps.getVertexNormZTexture().bindToTextureUnit(7, gl);
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
	    reporter.report(
		    "org.jtrfp.trcl.core.RenderList.numOpaqueBlocks",
		    "" + opaqueIL.size());
	    reporter.report(
		    "org.jtrfp.trcl.core.RenderList.numTransparentBlocks",
		    "" + transIL.size());
	    reporter.report(
		    "org.jtrfp.trcl.core.RenderList.numUnoccludedTransparentBlocks",
		    "" + unoccludedIL.size());
	    reporter.report(
		    "org.jtrfp.trcl.core.RenderList.approxNumSceneTriangles",
		    "" + ((opaqueIL.size()+transIL.size()+unoccludedIL.size())*GPU.GPU_VERTICES_PER_BLOCK)/3);
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
	vps.getVertexXYTexture().bindToTextureUnit(2, gl);
	//renderer.getVertexUVTexture().bindToTextureUnit(3, gl);
	vps.getVertexTextureIDTexture().bindToTextureUnit(4, gl);
	vps.getVertexZTexture().bindToTextureUnit(5, gl);
	vps.getVertexWTexture().bindToTextureUnit(6, gl);
	vps.getVertexNormXYTexture().bindToTextureUnit(7, gl);
	vps.getVertexNormZTexture().bindToTextureUnit(8, gl);
	
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
	renderer.getRendererFactory().getPortalTexture().bindToTextureUnit(2,gl);
	gpu.textureManager.get().vqCodebookManager.get().getESTuTvTexture().bindToTextureUnit(3,gl);
	gpu.textureManager.get().vqCodebookManager.get().getRGBATexture().bindToTextureUnit(4,gl);
	rFactory.getOpaquePrimitiveIDTexture().bindToTextureUnit(5,gl);
	rFactory.getLayerAccumulatorTexture().bindToTextureUnit(6,gl);
	vps.getVertexTextureIDTexture   ().bindToTextureUnit(7,gl);
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

    public void reset() {
	synchronized(relevantPositionedRenderables){
	 relevantPositionedRenderables.clear();
	 }//end sync(relevantObjects)
    }//end reset()
    
    public void repopulate(List<PositionedRenderable> renderables){
	synchronized(relevantPositionedRenderables){
	    relevantPositionedRenderables.clear();
	    relevantPositionedRenderables.addAll(renderables);
	}//end sync(relevantObjects)
    }
    
    public CollectionActionDispatcher<PositionedRenderable> getVisibleWorldObjectList(){
	return relevantPositionedRenderables;
    }

    public int getAttribDummyID() {
	return dummyBufferID;
    }
    static final Adapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>, PositionedRenderable> opaqueODAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>,PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>>(){
		@Override
		public CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> result = new CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>(new HashSet<CollectionActionDispatcher<VEC4Address>>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			  {}//Nothing
		    else {result.add(value.getOpaqueObjectDefinitionAddresses());}
		    return result;
		}
	    });
    static final Adapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>, PositionedRenderable> transODAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>, PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>>(){
		@Override
		public CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> result = new CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>(new HashSet<CollectionActionDispatcher<VEC4Address>>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			 {}//Nothing
		    else {result.add(value.getTransparentObjectDefinitionAddresses());}
		    return result;
		}
		
	    });
    static final Adapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>, PositionedRenderable> unoccludedODAddrAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>, PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>>(){

		@Override
		public CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>> result = new CollectionActionDispatcher<CollectionActionDispatcher<VEC4Address>>(new HashSet<CollectionActionDispatcher<VEC4Address>>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest()){
			result.add(value.getOpaqueObjectDefinitionAddresses());
			result.add(value.getTransparentObjectDefinitionAddresses());
			}//end if(unoccluded)
		    return result;
		}
	    });
}// end RenderList
