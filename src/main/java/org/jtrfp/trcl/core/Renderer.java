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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.GridCubeProximitySorter;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.prop.SkyCube;

public final class Renderer {
    private final	RendererFactory		factory;
    private 		RenderableSpacePartitioningGrid rootGrid;
    private final	GridCubeProximitySorter proximitySorter = new GridCubeProximitySorter();
    private		GLFrameBuffer		renderingTarget;
    private 		boolean 		initialized = false;
    private final 	GPU 			gpu;
    public final 	TRFutureTask<RenderList> renderList;
    private 		int			frameNumber;
    private 		long			lastTimeMillis;
    private		double			meanFPS;
    private		float[]			cameraMatrixAsFlatArray		= new float[16];
    private volatile	float	[]		camRotationProjectionMatrix = new float[16];
    private		TRFutureTask<Void>	relevanceUpdateFuture,relevanceCalcTask;
    private 		SkyCube			skyCube;
    final 		AtomicLong		nextRelevanceCalcTime = new AtomicLong(0L);
    private		CollisionManager	collisionManager;
    private final	CollectionActionDispatcher<Camera>cameras = new CollectionActionDispatcher<Camera>(new ArrayList<Camera>());
    private		Camera			masterCamera = null;
    
    public Renderer(final RendererFactory factory) {
	this.factory = factory;
	this.gpu     = factory.getGPU();
	final TR tr  = gpu.getTr();
	addCamera(tr.getWorld().newCamera());//TODO: Remove after redesign.
	final GL3 gl = gpu.getGl();
	
	System.out.println("...Done.");
	System.out.println("Initializing RenderList...");
	renderList = new TRFutureTask<RenderList>(tr,new Callable<RenderList>(){
	    @Override
	    public RenderList call() throws Exception {
		return new RenderList(gl, Renderer.this, tr);
	    }});tr.getThreadManager().threadPool.submit(renderList);
	
	skyCube = new SkyCube(tr);
    }//end constructor

    private void ensureInit() {
	if (initialized)
	    return;
	gpu.memoryManager.get().map();
	initialized = true;
    }// end ensureInit()

    private void fpsTracking() {
	frameNumber++;
	final long dT = (long) (System.currentTimeMillis() - lastTimeMillis);
	if(dT<=0)return;
	final int fps = (int)(1000L / dT);
	meanFPS = meanFPS*.9+(double)fps*.1;
	if ((frameNumber %= 20) == 0) {
	    gpu.getTr().getReporter()
		    .report("org.jtrfp.trcl.core.Renderer.FPS", "" + meanFPS);
	    final Collection<PositionedRenderable> coll = renderList.get().getVisibleWorldObjectList();
	    synchronized(coll){
	    gpu.getTr().getReporter()
	    	.report("org.jtrfp.trcl.core.Renderer.numVisibleObjects", coll.size());}
	}
	lastTimeMillis = System.currentTimeMillis();
    }//end fpsTracking()
    
    private RenderList oneFrameLaggedRenderList;
    
    public final Callable<?> render = new Callable<Void>(){
	@Override
	public Void call() throws Exception {
		final GL3 gl = gpu.getGl();
		try{	ensureInit();
			if(oneFrameLaggedRenderList==null)
			 oneFrameLaggedRenderList = renderList.getRealtime();
			
			oneFrameLaggedRenderList.render(gl);
			oneFrameLaggedRenderList   = renderList.getRealtime();
			oneFrameLaggedRenderList.sendToGPU(gl);
			cameraMatrixAsFlatArray    = getCamera().getCompleteMatrixAsFlatArray();//TODO
			camRotationProjectionMatrix= getCamera().getProjectionRotationMatrixAsFlatArray();//TODO
			//Make sure memory on the GPU is up-to-date by flushing stale pages to GPU mem.
			gpu.memoryManager.getRealtime().flushStalePages();
			// Update texture codepages
			gpu.textureManager.getRealtime().vqCodebookManager.getRealtime().refreshStaleCodePages();
			fpsTracking();
		}catch(NotReadyException e){}
	    return null;
	}};
    
    public void temporarilyMakeImmediatelyRelevant(final PositionedRenderable pr){
	if(pr instanceof WorldObject)
	    gpu.getTr().getCollisionManager().getCurrentlyActiveCollisionList().add((WorldObject)pr);
	
	renderList.get().getSubmitter().submit((WorldObject)pr);//TODO: Refactor back to PositionedRenderable
    }//end temporarilyMakeImmediatelyRelevant(...)
    
    public void updateRelevanceList(boolean mandatory) {
	if(relevanceUpdateFuture!=null){
	    if(!relevanceUpdateFuture.isDone()){
		if(!mandatory){System.out.println("Renderer.updateVisibilityList() !done");return;}
		else {}
		}
	    relevanceUpdateFuture.get();
	    }//end if(visibilityUpdateFuture!=null)
	relevanceUpdateFuture = gpu.getTr().getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() {
		try{
		proximitySorter.setCenter(getCamera().getCameraPosition().toArray());
		synchronized(gpu.getTr().getThreadManager().gameStateLock){
		 rootGrid.cubesWithinRadiusOf(
			getCamera().getCameraPosition().add(
				getCamera().getLookAtVector().scalarMultiply(
					getCamera().getViewDepth() / 2.1)),
					proximitySorter
			);
		}//end sync(gameStateLock)
		CollectionActionDispatcher<PositionedRenderable> visible = renderList.get().getVisibleWorldObjectList();
		synchronized(visible){visible.repopulate(proximitySorter.getRenderables());}
		proximitySorter.reset();
		}catch(Exception e){e.printStackTrace();}
		return null;
	    }//end pool run()
	});
    }// end updateRelevanceList()
    
    public void setSunVector(Vector3D sv){
	factory.getDeferredProgram().use();
	factory.getSunVectorUniform().set((float)sv.getX(),(float)sv.getY(),(float)sv.getZ());
	gpu.defaultProgram();
    }

    /**
     * @return the rootGrid
     */
    public RenderableSpacePartitioningGrid getRootGrid() {
	return rootGrid;
    }

    /**
     * @param rootGrid
     *            the rootGrid to set
     */
    public void setRootGrid(RenderableSpacePartitioningGrid rootGrid) {
	this.rootGrid = rootGrid;
	if(getCamera().getContainingGrid()!=null)
	    getCamera().getContainingGrid().remove(getCamera());
	rootGrid.add(getCamera());//TODO: Remove later
    }
    
    public Camera getCamera(){//TODO: Remove, but a lot of outside invocations depend on it.
	return cameras.iterator().next();
    }

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

    public Renderer setSunColor(final Color color) {
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		factory.getDeferredProgram().use();
		factory.getDeferredProgram().getUniform("sunColor").set(color.getRed()/128f, color.getGreen()/128f, color.getBlue()/128f);
		gpu.defaultProgram();
		return null;
	    }
	}).get();
	return this;
    }

    public Renderer setAmbientLight(final Color color) {
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		factory.getDeferredProgram().use();
		factory.getDeferredProgram().getUniform("ambientLight").set(color.getRed()/128f, color.getGreen()/128f, color.getBlue()/128f);
		gpu.defaultProgram();
		return null;
	    }
	}).get();
	return this;
    }//end setAmbientLight

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
    
    private final Object relevanceUpdateLock = new Object();
    
    public void relevanceCalc(final boolean mandatory) {
	final long currTimeMillis = System.currentTimeMillis();
	if(relevanceCalcTask!=null && !mandatory){
	    if(!relevanceCalcTask.isDone())
		{System.out.println("visiblityCalc() !done. Return...");return;}}
	relevanceCalcTask = gpu.getTr().getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		synchronized(relevanceUpdateLock){
		 updateRelevanceList(mandatory);
		 if(collisionManager!=null)
		  collisionManager.updateCollisionList();
		 //Nudge of 10ms to compensate for drift of the timer task
		 nextRelevanceCalcTime.set((currTimeMillis-10L)+(1000/ThreadManager.RENDERLIST_REFRESH_FPS));
		 }//end sync(visibilityUpdateLock)
		return null;
	    }});
    }//end visibilityCalc()
    
    public void relevanceCalc(){
	relevanceCalc(false);
    }

    /**
     * @return the collisionManager
     */
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    /**
     * @param collisionManager the collisionManager to set
     */
    public Renderer setCollisionManager(CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
        return this;
    }
    
    public Renderer addCamera(Camera c){
	if(!cameras.contains(c))
	 cameras.add(c);
	return this;
    }
    
    public Renderer removeCamera(Camera c){
	cameras.remove(c);
	return this;
    }

    public RendererFactory getRendererFactory() {
	return factory;
    }

    public TRFutureTask<RenderList> getRenderList() {
	return renderList;
    }

    public Collection<Camera> getCameras() {
	return cameras;
    }

    public Camera getMasterCamera() {
	if(masterCamera==null && !cameras.isEmpty())
	    masterCamera = cameras.iterator().next();
	return masterCamera;
    }

    /**
     * @param masterCamera the masterCamera to set
     */
    public void setMasterCamera(Camera masterCamera) {
        this.masterCamera = masterCamera;
    }
}//end Renderer
