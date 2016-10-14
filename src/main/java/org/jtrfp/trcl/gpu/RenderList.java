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
package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.VerboseExecutorService;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionUnpacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.DecoupledCollectionActionDispatcher;
import org.jtrfp.trcl.coll.ImplicitBiDiAdapter;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;
import org.jtrfp.trcl.coll.RedundancyReportingCollection;
import org.jtrfp.trcl.core.NotReadyException;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
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
    private final	GPU			gpu;
    private final	Renderer		renderer;
    private final	RendererFactory		rFactory;
    private final	ObjectListWindow	objectListWindow;
    private final       ThreadManager           threadManager;
    private         	Reporter		reporter;
    private final 	IntBuffer 		previousViewport;
    private final	IntArrayVariableList    renderList;
    private final	ListActionTelemetry<VEC4Address> renderListTelemetry 
    						= new ListActionTelemetry<VEC4Address>();
    public static final ExecutorService         RENDER_LIST_EXECUTOR = new VerboseExecutorService(Executors.newSingleThreadExecutor());
    private final	IndexList<VEC4Address>	opaqueIL, transIL, unoccludedIL;
    private final	DecoupledCollectionActionDispatcher<PositionedRenderable>
    						relevantPositionedRenderables = new DecoupledCollectionActionDispatcher<PositionedRenderable>(new HashSet<PositionedRenderable>(), RENDER_LIST_EXECUTOR);
    private final	PartitionedList<VEC4Address>
    						renderListPoolNEW = new PartitionedList<VEC4Address>(renderListTelemetry);
    private final	CollectionAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>
    	opaqueODAddrsColl    = new CollectionAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(opaqueIL     = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),opaqueODAdapter),
    	transODAddrsColl     = new CollectionAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(transIL      = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),transODAdapter ), 
    	unoccludedODAddrsColl= new CollectionAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler(unoccludedIL = new IndexList<VEC4Address>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),unoccludedODAddrAdapter);

    
    private             MatrixWindow            matrixWindowContext;
    private final       ObjectListWindow        objectListWindowContext;
    
    public RenderList(final GPU gpu, final Renderer renderer, final ObjectListWindow objectListWindow, ThreadManager threadManager) {
	// Build VAO
	this.objectListWindow = objectListWindow;
	this.objectListWindowContext = (ObjectListWindow)objectListWindow.newContextWindow();
	final IntBuffer ib    = IntBuffer.allocate(1);
	this.threadManager    = threadManager;
	this.gpu              = gpu;
	this.renderer         = renderer;
	this.rFactory         = renderer.getRendererFactory();
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=this.objectListWindow.create();
	this.renderList                 = new IntArrayVariableList(this.objectListWindow.opaqueIDs,renderListIdx);
	
	relevantPositionedRenderables.addTarget(opaqueODAddrsColl, true);
	relevantPositionedRenderables.addTarget(transODAddrsColl, true);
	relevantPositionedRenderables.addTarget(unoccludedODAddrsColl, true);
	relevantPositionedRenderables.addTarget(new RedundancyReportingCollection<PositionedRenderable>(), true);
	
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
	//final Renderer renderer = tr.mainRenderer;
	final int size = Math.min(objectListWindow.numPages(),hostRenderListPageTable.length);
	//////// Workaround for AMD bug where element zero always returns zero in frag. Shift up one.
	for (int i = 0; i < size-1; i++) {
	    hostRenderListPageTable[i+1] = objectListWindow.logicalPage2PhysicalPage(i);
	}// end for(hostRenderListPageTable.length)
	rFactory.getObjectProcessingStage().sendRenderListPageTable(hostRenderListPageTable);
	
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
	//final MatrixWindow matrixWindow = (MatrixWindow)gpu.matrixWindow.get().newContextWindow();
	final MatrixWindow matrixWindow = getMatrixWindowContext();
	synchronized(threadManager.gameStateLock){
	synchronized(relevantPositionedRenderables){
	for (PositionedRenderable renderable:relevantPositionedRenderables) {
	    //if(renderable instanceof WorldObject)
	//	((WorldObject)renderable).setMatrixWindow(matrixWindow);
	    try{renderable.updateStateToGPU(renderer, matrixWindow);}
	     catch(NotReadyException e){}//Simply not ready
	}//end for(relevantPositionedRenderables)
	final Camera camera = renderer.getCamera();
	camera.getCompleteMatrixAsFlatArray(renderer.cameraMatrixAsFlatArray);
	camera.getProjectionRotationMatrixAsFlatArray(renderer.camRotationProjectionMatrix);
	}}
	matrixWindow.flush();
    }//end updateStatesToGPU
    
    private void updateRenderListToGPU(){
	if(renderListTelemetry.isModified()){
	    try{
	    RENDER_LIST_EXECUTOR.submit(new Callable<Void>(){
		@Override
		public Void call() {
		    //Defragment
		    final ObjectListWindow objectListWindow = objectListWindowContext;
		    opaqueIL    .defragment();
		    transIL     .defragment();
		    unoccludedIL.defragment();
		    numOpaqueBlocks     = opaqueIL    .delegateSize();
		    numTransparentBlocks= transIL     .delegateSize();
		    numUnoccludedTBlocks= unoccludedIL.delegateSize();
		    renderList.rewind();
		    //final Set<VEC4Address> redundancyChecker = new HashSet<VEC4Address>();
		    //for(VEC4Address addr:renderListTelemetry)
			//if(!redundancyChecker.add(addr))
			//    new Exception("updateRenderList() found redundant item: "+addr).printStackTrace();
		    renderListTelemetry.drainListStateTo(renderList);
		    objectListWindow.flush();
		    return null;
		}}).get();
	    }catch(Exception e){e.printStackTrace();}
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
    
    protected Reporter getReporter(){
	return reporter;
    }
    
    public void render(final GL3 gl) throws NotReadyException {
	if(!sentPageTable)sendRenderListPageTable();
	final Reporter reporter = getReporter();
	final int renderListLogicalVec4Offset = ((objectListWindow.getObjectSizeInBytes()*renderListIdx)/16);
	final int primsPerBlock = GPU.GPU_VERTICES_PER_BLOCK/3;
	final int numPrimitives = (numTransparentBlocks+numOpaqueBlocks+numUnoccludedTBlocks)*primsPerBlock;
	saveWindowViewportState(gl);
	gl.glEnableVertexAttribArray(0);
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
	
	if (frameCounter == 0 && reporter != null) {
	    threadManager.submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    reporter.report(
			    "org.jtrfp.trcl.core."+renderer.getDebugName()+".RenderList.numOpaqueBlocks",
			    "" + opaqueIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+renderer.getDebugName()+".RenderList.numTransparentBlocks",
			    "" + transIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+renderer.getDebugName()+".RenderList.numUnoccludedTransparentBlocks",
			    "" + unoccludedIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+renderer.getDebugName()+".RenderList.approxNumSceneTriangles",
			    "" + ((opaqueIL.size()+transIL.size()+unoccludedIL.size())*GPU.GPU_VERTICES_PER_BLOCK)/3);
		    
		    int index = 0;
		    for(PositionedRenderable pr : relevantPositionedRenderables)
		     reporter.report(
			    "org.jtrfp.trcl.core."+renderer.getDebugName()+".RenderList.relevantObjects."+(index++), 
			    "" + pr);
		    return null;
		}});
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
	gl.glBlendColor(4f, 4f, 4f, 4f);// upshift 2 bits
	
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
	rFactory.getLayerAccumulatorTexture0().bindToTextureUnit(6,gl);
	vps.getVertexTextureIDTexture   ().bindToTextureUnit(7,gl);
	rFactory.getPrimitiveUVZWTexture().bindToTextureUnit(8,gl);
	rFactory.getPrimitiveNormTexture().bindToTextureUnit(9, gl);
	rFactory.getLayerAccumulatorTexture1().bindToTextureUnit(10,gl);
	
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
	gl.glDisableVertexAttribArray(0);
	
	//INTERMEDIATE ERASE
	rFactory.getOpaqueFrameBuffer().bindToDraw();
	gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
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
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> opaqueODAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){
		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			return new CollectionActionDispatcher<VEC4Address>(new HashSet<VEC4Address>());
		    else {return value.getOpaqueObjectDefinitionAddresses();}
		}
	    });
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> transODAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){
		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			return new CollectionActionDispatcher<VEC4Address>(new HashSet<VEC4Address>());
		    else {return value.getTransparentObjectDefinitionAddresses();}
		}
	    });
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> unoccludedODAddrAdapter =
	    new ImplicitBiDiAdapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){

		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<VEC4Address> result = new CollectionActionDispatcher<VEC4Address>(new HashSet<VEC4Address>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest()){
			value.getOpaqueObjectDefinitionAddresses().addTarget(result, true);
			value.getTransparentObjectDefinitionAddresses().addTarget(result, true);
			}//end if(unoccluded)
		    return result;
		}
	    });

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    protected MatrixWindow getMatrixWindowContext() {
	if(matrixWindowContext == null)
	    matrixWindowContext = (MatrixWindow)gpu.matrixWindow.get().newContextWindow();
        return matrixWindowContext;
    }

    protected void setMatrixWindowContext(MatrixWindow matrixWindowContext) {
        this.matrixWindowContext = matrixWindowContext;
    }
}// end RenderList
