/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.collection.PredicatedCollection;
import org.apache.commons.collections4.functors.InstanceofPredicate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.VerboseExecutorService;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionUnpacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.DecoupledCollectionActionDispatcher;
import org.jtrfp.trcl.coll.ImplicitBiDiAdapter;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;
import org.jtrfp.trcl.coll.RedundancyReportingCollection;
import org.jtrfp.trcl.core.NotReadyException;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.mem.IntArrayVariableList;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.pool.IndexList;
import org.jtrfp.trcl.prop.SkyCube;
import org.jtrfp.trcl.tools.Util;

import com.jogamp.opengl.GL3;
import com.ochafik.util.Adapter;
import com.ochafik.util.CollectionAdapter;
import com.ochafik.util.listenable.AdaptedCollection;

public final class Renderer {
    private       	RendererFactory		rendererFactory;
    //private 		RenderableSpacePartitioningGrid rootGrid;
    //private final	GridCubeProximitySorter proximitySorter = new GridCubeProximitySorter();
    private		GLFrameBuffer		renderingTarget;
    private 		boolean 		initialized = false;
    private     	GPU 			gpu;
    //public      	TRFutureTask<RenderList> renderList;
    private 		int			frameNumber;
    private 		long			lastTimeMillis;
    //private		double			meanFPS;
                        float[]			cameraMatrixAsFlatArray	   = new float[16];
                        float	[]		camRotationProjectionMatrix= new float[16];
    //private		TRFutureTask<Void>	relevanceUpdateFuture,relevanceCalcTask;
    private 		SkyCube			skyCube;
    final 		AtomicLong		nextRelevanceCalcTime = new AtomicLong(0L);
    //private final	CollisionManager        collisionManager;
    private		Camera			camera = null;
    private        	PredicatedCollection<Positionable> relevantPositioned;
    private      	Reporter		reporter;
    private     	ThreadManager		threadManager;
    private             String                  debugName;
    private boolean                             enabled = false;
    private             World                   world;
    private             ObjectListWindow        objectListWindow;
    private             Byte                    stencilID = null;
    
    public static final int 	NUM_SUBPASSES 		= 4;
    public static final int	NUM_BLOCKS_PER_SUBPASS 	= 1024 * 4;
    public static final int	NUM_BLOCKS_PER_PASS 	= NUM_BLOCKS_PER_SUBPASS
	    						* NUM_SUBPASSES;
    public static final int	NUM_RENDER_PASSES 	= 2;// Opaque + transparent //TODO: This is no longer the case

    private 		int[] 			hostRendererPageTable;
    private 	 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks,
    						numTransparentBlocks,
    						numOpaqueUnoccludedTBlocks,
    						numTransUnoccludedTBlocks;
    private        	int			renderListIdx;
    private     	IntBuffer 		previousViewport;
    private final	ListActionTelemetry<VEC4Address> objectListTelemetry 
    						= new ListActionTelemetry<>();
    public static final ExecutorService         RENDER_LIST_EXECUTOR = new VerboseExecutorService(Executors.newSingleThreadExecutor());
    private final	IndexList<VEC4Address>	opaqueIL, transIL, opaqueUnoccludedIL, transUnoccludedIL;
    private final	DecoupledCollectionActionDispatcher<PositionedRenderable>
    						relevantPositionedRenderables = new DecoupledCollectionActionDispatcher<>(new HashSet<PositionedRenderable>(), RENDER_LIST_EXECUTOR);
    private final	PartitionedList<VEC4Address>
    						renderListPoolNEW = new PartitionedList<>(objectListTelemetry);
    private final	CollectionAdapter<CollectionActionDispatcher<VEC4Address>,PositionedRenderable>
    	opaqueODAddrsColl    	   = new CollectionAdapter<>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler<>(opaqueIL                 = new IndexList<>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),opaqueODAdapter),
    	opaqueUnoccludedODAddrsColl= new CollectionAdapter<>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler<>(opaqueUnoccludedIL = new IndexList<>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),opaqueUnoccludedODAddrAdapter),
    	transODAddrsColl     	   = new CollectionAdapter<>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler<>(transIL          = new IndexList<>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),transODAdapter ), 
        transUnoccludedODAddrsColl = new CollectionAdapter<>(new CollectionActionUnpacker<VEC4Address>(new CollectionThreadDecoupler<>(transUnoccludedIL  = new IndexList<>(renderListPoolNEW.newSubList()),RENDER_LIST_EXECUTOR)),transUnoccludedODAddrAdapter);

    
    private             MatrixWindow            matrixWindowContext;
    private             ObjectListWindow        objectListWindowContext;
    private             IntArrayVariableList    indexList;
    
    private		Color			sunColor = Color.white, ambientColor = Color.gray;
    private		float			fogScalar=1;
    private		Vector3D		sunVector = new Vector3D(1,1,1).normalize();
    private		boolean			intermittent = false;
    
    
    private static final Adapter<Positionable,PositionedRenderable> castingAdapter = new Adapter<Positionable,PositionedRenderable>(){
	@Override
	public PositionedRenderable adapt(Positionable value)
		throws UnsupportedOperationException {
	    return (PositionedRenderable)value;
	}
	@Override
	public Positionable reAdapt(PositionedRenderable value)
		throws UnsupportedOperationException {
	    return (Positionable)value;
	}
    };//end castingAdapter
    
    public void ensureInit() {
	if (initialized)
	    return;
	Util.assertPropertiesNotNull(this, "gpu", "world", "threadManager");
	final World world = getWorld();
	final GPU gpu = getGpu();
	final ThreadManager threadManager = getThreadManager();
	Camera camera = world.newCamera();//TODO: Remove after redesign.
	camera.setDebugName(getDebugName());
	//setCamera(tr.getWorld().newCamera());//TODO: Use after redesign
	System.out.println("...Done.");
	/*System.out.println("Initializing RenderList...");
	renderList = new TRFutureTask<RenderList>(new Callable<RenderList>(){
	    @Override
	    public RenderList call() throws Exception {
		final RenderList rl = new RenderList(gpu, Renderer.this, getObjectListWindow(), getThreadManager());
		rl.setReporter(getReporter());
		return rl;
	    }});
	threadManager.threadPool.submit(renderList);*/

	if(getSkyCube() == null)
	    setSkyCube(new SkyCube(gpu));
	relevantPositioned =
		PredicatedCollection.predicatedCollection(
			new AdaptedCollection<PositionedRenderable,Positionable>(getVisibleWorldObjectList(),Util.bidi2Backward(castingAdapter),Util.bidi2Forward(castingAdapter)),
			new InstanceofPredicate(PositionedRenderable.class));
	setCamera(camera);
	assert camera!=null;
	gpu.memoryManager.get().map();

	// Build VAO
	this.objectListWindowContext = (ObjectListWindow)objectListWindow.newContextWindow();
	final IntBuffer ib    = IntBuffer.allocate(1);
	this.threadManager    = threadManager;
	this.gpu              = gpu;
	//this.rFactory         = renderer.getRendererFactory();
	this.previousViewport		=ByteBuffer.allocateDirect(4*4).order(ByteOrder.nativeOrder()).asIntBuffer();
	this.renderListIdx		=this.objectListWindow.create();
	this.indexList                 = new IntArrayVariableList(this.objectListWindowContext.opaqueIDs,renderListIdx);

	relevantPositionedRenderables.addTarget(opaqueODAddrsColl, true);
	relevantPositionedRenderables.addTarget(transODAddrsColl, true);
	relevantPositionedRenderables.addTarget(opaqueUnoccludedODAddrsColl, true);
	relevantPositionedRenderables.addTarget(transUnoccludedODAddrsColl, true);
	relevantPositionedRenderables.addTarget(new RedundancyReportingCollection<PositionedRenderable>(), true);
	
	final Future<Void> task0 = gpu.getGlExecutor().submitToGL(new GLExecutable<Void, GL3>(){
	    @Override
	    public Void execute(GL3 gl) throws Exception {
		//final GL3 gl = gpu.getGl();
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

	hostRendererPageTable = new int[((ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS
		* Renderer.NUM_RENDER_PASSES)
		/ PagedByteBuffer.PAGE_SIZE_BYTES)*3];

	try {task0.get();}
	catch(Exception e) {e.printStackTrace();}

	initialized = true;
    }// end ensureInit()

    private void fpsTracking() {
	final Reporter reporter = getReporter();
	if(reporter == null)
	    return;
	frameNumber++;
	final boolean isKeyFrame = (frameNumber % 20) == 0;
	if (isKeyFrame) {
	    final long dT = System.currentTimeMillis() - lastTimeMillis;
		if(dT<=0)return;
		final int fps = (int)(20.*(1000. / (double)dT));
	    reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" FPS", "" + fps);
	    final Collection<PositionedRenderable> coll = getVisibleWorldObjectList();
	    synchronized(coll){
	    reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" numVisibleObjects", coll.size()+"");
	    SpacePartitioningGrid<PositionedRenderable> spg = getCamera().getRootGrid();
	    if(spg!=null)
	     reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" rootGrid", spg.toString());
	    }
	    lastTimeMillis = System.currentTimeMillis();
	}//end if(key frame)
    }//end fpsTracking()
    
    public void setCamera(Camera toUse){
	final PredicatedCollection<Positionable> relevantPositioned = getRelevantPositioned();
	if(this.camera!=null)
	    this.camera.getFlatRelevanceCollection().removeTarget(relevantPositioned, true);
	this.camera=toUse;
	toUse.getFlatRelevanceCollection().addTarget(relevantPositioned, true);
    }
    
    private static final NotReadyException renderNRE = new NotReadyException();
    
    public final Callable<?> render = new Callable<Void>(){
	@Override
	public Void call() throws Exception {
	    final GL3 gl = gpu.getGl();
	    try{ensureInit();
	        if(!isEnabled())
	            throw renderNRE;
	        sendToGPU(gl);
	        //Make sure memory on the GPU is up-to-date by flushing stale pages to GPU mem.
	        gpu.memoryManager.getRealtime().flushStalePages();
	        render(gl);
	        // Update texture codepages
	        //TODO: Isn't this getting redundantly called between Renderers?!
	        gpu.textureManager.getRealtime().vqCodebookManager.refreshStaleCodePages(gl);
	        fpsTracking();
	    }catch(NotReadyException e){}
	    return null;
	}};

    /**
     * @return the rootGrid
     */
    /*
    public RenderableSpacePartitioningGrid getRootGrid() {
	return rootGrid;
    }
*/
    /**
     * @param rootGrid
     *            the rootGrid to set
     */
    /*
    public void setRootGrid(RenderableSpacePartitioningGrid rootGrid) {
	this.rootGrid = rootGrid;
	if(getCamera().getContainingGrid()!=null)
	    getCamera().getContainingGrid().remove(getCamera());
	rootGrid.add(getCamera());//TODO: Remove later
    }
    */

    /**
     * @return the cameraMatrixAsFlatArray
     */
    public float[] getCameraMatrixAsFlatArray() {
        return cameraMatrixAsFlatArray;
    }

    /**
     * @return the camRotationProjectionMatrix
     */
    public float[] getCamRotationProjectionMatrix() {
        return camRotationProjectionMatrix;
    }

    /**
     * @return the skyCube
     */
    public SkyCube getSkyCube() {
        return skyCube;
    }

    /**
     * @param skyCube the skyCube to set
     */
    public void setSkyCube(SkyCube skyCube) {
        this.skyCube = skyCube;
    }
    
    public void updateDeferredUniforms(GLProgram p) {
	p.getUniform("sunColor").set(sunColor.getRed()/128f, sunColor.getGreen()/128f, sunColor.getBlue()/128f);
	p.getUniform("ambientLight").set(ambientColor.getRed()/128f, ambientColor.getGreen()/128f, ambientColor.getBlue()/128f);
	rendererFactory.getSunVectorUniform().set((float)sunVector.getX(),(float)sunVector.getY(),(float)sunVector.getZ());
	rendererFactory.getFogScalarUniform().set((float)fogScalar);
    }//end updateDeferredUniforms(...)

    public Renderer setSunColor(final Color color) {
	this.sunColor = color;
	return this;
    }

    public Renderer setAmbientLight(final Color color) {
	this.ambientColor = color;
	return this;
    }//end setAmbientLight
    
    public Renderer setSunVector(Vector3D sv){
	this.sunVector = sv;
	return this;
    }
    
    public Renderer setFogScalar(float fogScalar) {
	this.fogScalar = fogScalar;
	return this;
    }

    /**
     * @return the renderingTarget
     */
    public GLFrameBuffer getRenderingTarget() {
        return renderingTarget;
    }

    /**
     * @param renderingTarget the renderingTarget to set
     */
    public Renderer setRenderingTarget(GLFrameBuffer renderingTarget) {
        this.renderingTarget = renderingTarget;
        return this;
    }
    
    //private final Object relevanceUpdateLock = new Object();

    public RendererFactory getRendererFactory() {
	return rendererFactory;
    }
    
    public Camera getCamera() {
	return camera;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
	if(this.enabled == enabled)
	    return;
	this.enabled = enabled;
	if(!isIntermittent()) {
	    if(isEnabled())
		threadManager.addRepeatingGLTask(render);
	    else
		threadManager.removeRepeatingGLTask(render);
	} else {
	    if(isEnabled())
		threadManager.addIntermittentGLTask(render);
	    else
		threadManager.removeIntermittentGLTask(render);
	}
	getCamera().setActive(isEnabled());
    }//end setEnabled(...)
    
    private void sendRendererPageTable(){
	//final Renderer renderer = tr.mainRenderer;
	final int size = Math.min(objectListWindow.numPages(),hostRendererPageTable.length);
	//////// Workaround for AMD bug where element zero always returns zero in frag. Shift up one.
	for (int i = 0; i < size-1; i++) {
	    hostRendererPageTable[i+1] = objectListWindow.logicalPage2PhysicalPage(i);
	}// end for(hostRenderListPageTable.length)
	final RendererFactory rFactory = getRendererFactory();
	rFactory.getObjectProcessingStage().sendRenderListPageTable(hostRendererPageTable);
	
	final GLProgram depthQueueProgram = rFactory.getDepthQueueProgram();
	depthQueueProgram.use();
	final GLProgram primaryProgram = rFactory.getOpaqueProgram();
	primaryProgram.use();
	final GLProgram vertexProgram = rFactory.getVertexProcessingStage().getVertexProgram();
	vertexProgram.use();
	vertexProgram.getUniform("renderListPageTable").setArrayui(hostRendererPageTable);
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
	    try{renderable.updateStateToGPU(this, matrixWindow);}
	     catch(NotReadyException e){}//Simply not ready
	}//end for(relevantPositionedRenderables)
	final Camera camera = getCamera();
	camera.getCompleteMatrixAsFlatArray(cameraMatrixAsFlatArray);
	camera.getProjectionRotationMatrixAsFlatArray(camRotationProjectionMatrix);
	}}
	matrixWindow.flush();
    }//end updateStatesToGPU
    
    private void updateObjectListToGPU(){
	if(objectListTelemetry.isModified()){
	    try{
	    RENDER_LIST_EXECUTOR.submit(new Callable<Void>(){
		@Override
		public Void call() {
		    //Defragment
		    final ObjectListWindow objectListWindow = objectListWindowContext;
		    opaqueIL    .defragment();
		    transIL     .defragment();
		    opaqueUnoccludedIL.defragment();
		    transUnoccludedIL .defragment();
		    numOpaqueBlocks     = opaqueIL    .delegateSize();
		    numTransparentBlocks= transIL     .delegateSize();
		    numOpaqueUnoccludedTBlocks= opaqueUnoccludedIL.delegateSize();
		    numTransUnoccludedTBlocks = transUnoccludedIL .delegateSize();
		    indexList.rewind();
		    //final Set<VEC4Address> redundancyChecker = new HashSet<VEC4Address>();
		    //for(VEC4Address addr:renderListTelemetry)
			//if(!redundancyChecker.add(addr))
			//    new Exception("updateRenderList() found redundant item: "+addr).printStackTrace();
		    objectListTelemetry.drainListStateTo(indexList);
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
	updateObjectListToGPU();
    }//end sendToGPU
    
    private boolean sentPageTable=false;
    
    private void saveWindowViewportState(GL3 gl){
	gl.glGetIntegerv(GL3.GL_VIEWPORT, previousViewport);
    }
    
    private void revertViewportState(GL3 gl){
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
	if(!sentPageTable)sendRendererPageTable();
	final Reporter reporter = getReporter();
	final RendererFactory rFactory = getRendererFactory();
	final int renderListLogicalVec4Offset = ((objectListWindow.getObjectSizeInBytes()*renderListIdx)/16);
	final int primsPerBlock = GPU.GPU_VERTICES_PER_BLOCK/3;
	final int numPrimitives = (
		numTransparentBlocks+
		numOpaqueBlocks+
		numOpaqueUnoccludedTBlocks+
		numTransUnoccludedTBlocks
		                  )*primsPerBlock;
	saveWindowViewportState(gl);
	gl.glEnableVertexAttribArray(0);
	// OBJECT STAGE
	
	rFactory.getObjectProcessingStage().process(gl,getCameraMatrixAsFlatArray(),
		renderListLogicalVec4Offset, numTransparentBlocks, numOpaqueBlocks, numOpaqueUnoccludedTBlocks, numTransUnoccludedTBlocks);
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
	gl.glDrawArrays(GL3.GL_POINTS, 0, (numTransparentBlocks+numOpaqueBlocks+numOpaqueUnoccludedTBlocks+numTransUnoccludedTBlocks)*primsPerBlock);
	//Cleanup
	gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
	gl.glPointSize(1);
	revertViewportState(gl);
	//gpu.defaultViewport();
	
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
	final int numOpaqueUnoccludedVertices = numOpaqueUnoccludedTBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	final int numTransUnoccludedVertices = numTransUnoccludedTBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	// Turn on depth write, turn off transparency
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_LESS);
	gl.glEnable(GL3.GL_DEPTH_TEST);
	gl.glEnable(GL3.GL_DEPTH_CLAMP);
	final Byte stencilID = getStencilID();
	if(stencilID != null){
	    gl.glEnable(GL3.GL_STENCIL_TEST);
	    gl.glStencilFunc(GL3.GL_EQUAL, stencilID, 0xFF);
	}else gl.glDisable(GL3.GL_STENCIL_TEST);
	//gl.glDepthRange((BriefingScreen.MAX_Z_DEPTH+1)/2, 1);
	
	if(rFactory.isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	
	if (frameCounter == 0 && reporter != null) {
	    threadManager.submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.numOpaqueBlocks",
			    "" + opaqueIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.numTransparentBlocks",
			    "" + transIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.numUnoccludedTransparentBlocks",
			    "" + transUnoccludedIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.numUnoccludedOpaqueBlocks",
			    "" + opaqueUnoccludedIL.size());
		    reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.approxNumSceneTriangles",
			    "" + ((opaqueIL.size()+transIL.size()+opaqueUnoccludedIL.size()+transUnoccludedIL.size())*GPU.GPU_VERTICES_PER_BLOCK)/3);
		    
		    int index = 0;
		    for(PositionedRenderable pr : relevantPositionedRenderables)
		     reporter.report(
			    "org.jtrfp.trcl.core."+getDebugName()+".Renderer.relevantObjects."+(index++), 
			    "" + pr);
		    return null;
		}});
	}
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numOpaqueVertices);
	//OPAQUE UNOCCLUDED
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_DEPTH_CLAMP);
	gl.glDisable(GL3.GL_CULL_FACE);
	gl.glDepthRange(0, 1);
	gl.glDepthFunc(GL3.GL_LEQUAL);
	gl.glDrawArrays(GL3.GL_TRIANGLES, numOpaqueVertices, numOpaqueUnoccludedVertices);
	
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
	
	gl.glDrawArrays(GL3.GL_TRIANGLES, numOpaqueVertices+numOpaqueUnoccludedVertices, numTransparentVertices);
	//UNOCCLUDED TRANSPARENT
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDrawArrays(GL3.GL_TRIANGLES, numOpaqueVertices+numOpaqueUnoccludedVertices+numTransparentVertices, numTransUnoccludedVertices);
	
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
	updateDeferredUniforms(deferredProgram);
	gl.glDepthMask(false);
	gl.glDisable(GL3.GL_DEPTH_TEST);
	gl.glDisable(GL3.GL_BLEND);
	
	final GLFrameBuffer renderTarget = getRenderingTarget();
	if(renderTarget!=null)
	    renderTarget.bindToDraw();
	else gpu.defaultFrameBuffers();
	
	gpu.memoryManager.get().bindToUniform(0, deferredProgram,
		    deferredProgram.getUniform("rootBuffer"));
	getSkyCube().getSkyCubeTexture(gl).bindToTextureUnit(1,gl);
	getRendererFactory().getPortalTexture().bindToTextureUnit(2,gl);
	gpu.textureManager.get().vqCodebookManager.getESTuTvTexture().bindToTextureUnit(3,gl);
	gpu.textureManager.get().vqCodebookManager.getRGBATexture().bindToTextureUnit(4,gl);
	rFactory.getOpaquePrimitiveIDTexture().bindToTextureUnit(5,gl);
	rFactory.getLayerAccumulatorTexture0().bindToTextureUnit(6,gl);
	vps.getVertexTextureIDTexture   ().bindToTextureUnit(7,gl);
	rFactory.getPrimitiveUVZWTexture().bindToTextureUnit(8,gl);
	rFactory.getPrimitiveNormLODTexture().bindToTextureUnit(9, gl);
	rFactory.getLayerAccumulatorTexture1().bindToTextureUnit(10,gl);
	
	deferredProgram.getUniform("bypassAlpha").setui(!getCamera().isFogEnabled()?1:0);
	deferredProgram.getUniform("projectionRotationMatrix")
		.set4x4Matrix(getCamRotationProjectionMatrix(), true);
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

    protected MatrixWindow getMatrixWindowContext() {
	if(matrixWindowContext == null)
	    matrixWindowContext = (MatrixWindow)gpu.matrixWindow.get().newContextWindow();
        return matrixWindowContext;
    }

    protected void setMatrixWindowContext(MatrixWindow matrixWindowContext) {
        this.matrixWindowContext = matrixWindowContext;
    }
    
    @Override
    public String toString(){
	return "Renderer debugName="+debugName+" hash="+hashCode();
    }

    public String getDebugName() {
        return debugName;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public RendererFactory getFactory() {
        return rendererFactory;
    }

    public void setFactory(RendererFactory factory) {
        this.rendererFactory = factory;
    }

    public PredicatedCollection<Positionable> getRelevantPositioned() {
        return relevantPositioned;
    }

    public void setRelevantPositioned(
    	PredicatedCollection<Positionable> relevantPositioned) {
        this.relevantPositioned = relevantPositioned;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    public ObjectListWindow getObjectListWindow() {
        return objectListWindow;
    }

    public void setObjectListWindow(ObjectListWindow objectListWindow) {
        this.objectListWindow = objectListWindow;
    }

    public void setRendererFactory(RendererFactory rendererFactory) {
        this.rendererFactory = rendererFactory;
    }

    public void setDebugName(String debugName) {
        this.debugName = debugName;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public Byte getStencilID() {
	return stencilID;
    }

    public void setStencilID(Byte stencilID) {
        this.stencilID = stencilID;
    }
    
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> opaqueODAdapter =
	    new ImplicitBiDiAdapter<>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){
		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			return new CollectionActionDispatcher<>(new HashSet<VEC4Address>());
		    else {return value.getOpaqueObjectDefinitionAddresses();}
		}
	    });
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> transODAdapter =
	    new ImplicitBiDiAdapter<>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){
		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest())
			return new CollectionActionDispatcher<>(new HashSet<VEC4Address>());
		    else {return value.getTransparentObjectDefinitionAddresses();}
		}
	    });
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> transUnoccludedODAddrAdapter =
	    new ImplicitBiDiAdapter<>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){

		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<VEC4Address> result = new CollectionActionDispatcher<>(new HashSet<VEC4Address>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest()){
			//value.getOpaqueObjectDefinitionAddresses().addTarget(result, true);
			value.getTransparentObjectDefinitionAddresses().addTarget(result, true);
			}//end if(unoccluded)
		    return result;
		}
	    });
    static final Adapter<CollectionActionDispatcher<VEC4Address>, PositionedRenderable> opaqueUnoccludedODAddrAdapter =
	    new ImplicitBiDiAdapter<>(null,new com.ochafik.util.listenable.Adapter<PositionedRenderable,CollectionActionDispatcher<VEC4Address>>(){

		@Override
		public CollectionActionDispatcher<VEC4Address> adapt(
			PositionedRenderable value) {
		    final CollectionActionDispatcher<VEC4Address> result = new CollectionActionDispatcher<>(new HashSet<VEC4Address>());
		    if(value instanceof WorldObject && ((WorldObject)value).isImmuneToOpaqueDepthTest()){
			value.getOpaqueObjectDefinitionAddresses().addTarget(result, true);
			//value.getTransparentObjectDefinitionAddresses().addTarget(result, true);
			}//end if(unoccluded)
		    return result;
		}
	    });

    public boolean isIntermittent() {
        return intermittent;
    }

    public void setIntermittent(boolean intermittent) {
        this.intermittent = intermittent;
    }
}//end Renderer
