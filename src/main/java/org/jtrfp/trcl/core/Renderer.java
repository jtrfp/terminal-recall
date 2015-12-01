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
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.media.opengl.GL3;

import org.apache.commons.collections4.collection.PredicatedCollection;
import org.apache.commons.collections4.functors.InstanceofPredicate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.GridCubeProximitySorter;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CachedAdapter;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.Reporter;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.prop.SkyCube;
import org.jtrfp.trcl.tools.Util;

import com.ochafik.util.Adapter;
import com.ochafik.util.listenable.AdaptedCollection;

public final class Renderer {
    private final	RendererFactory		factory;
    //private 		RenderableSpacePartitioningGrid rootGrid;
    private final	GridCubeProximitySorter proximitySorter = new GridCubeProximitySorter();
    private		GLFrameBuffer		renderingTarget;
    private 		boolean 		initialized = false;
    private final 	GPU 			gpu;
    public final 	TRFutureTask<RenderList> renderList;
    private 		int			frameNumber;
    private 		long			lastTimeMillis;
    private		double			meanFPS;
                        float[]			cameraMatrixAsFlatArray	   = new float[16];
                        float	[]		camRotationProjectionMatrix= new float[16];
    private		TRFutureTask<Void>	relevanceUpdateFuture,relevanceCalcTask;
    private 		SkyCube			skyCube;
    final 		AtomicLong		nextRelevanceCalcTime = new AtomicLong(0L);
    //private final	CollisionManager        collisionManager;
    private		Camera			camera = null;
    private final	PredicatedCollection<Positionable> relevantPositioned;
    private final	Reporter		reporter;
    private final	ThreadManager		threadManager;
    private volatile boolean			oneShotBehavior = false;
    private final       String                  debugName;
    private volatile boolean keepAlive = false;
    
    public Renderer(final RendererFactory factory, World world, final ThreadManager threadManager, final Reporter reporter/*, CollisionManager collisionManagerFuture*/, final ObjectListWindow objectListWindow, String debugName) {
	this.factory         = factory;
	this.gpu             = factory.getGPU();
	this.reporter        =reporter;
	this.threadManager   =threadManager;
	this.debugName       =debugName;
	//this.collisionManager=collisionManagerFuture;
	//BUG: Circular dependency... setCamera needs relevantPositioned, relevantPostioned needs renderer, renderer needs camera
	Camera camera = world.newCamera();//TODO: Remove after redesign.
	//setCamera(tr.getWorld().newCamera());//TODO: Use after redesign
	System.out.println("...Done.");
	System.out.println("Initializing RenderList...");
	renderList = new TRFutureTask<RenderList>(new Callable<RenderList>(){
	    @Override
	    public RenderList call() throws Exception {
		return new RenderList(gpu, Renderer.this, objectListWindow, threadManager, reporter);
	    }});threadManager.threadPool.submit(renderList);
	
	skyCube = new SkyCube(gpu);
	relevantPositioned =
		    PredicatedCollection.predicatedCollection(
			    new AdaptedCollection<PositionedRenderable,Positionable>(renderList.get().getVisibleWorldObjectList(),Util.bidi2Backward(castingAdapter),Util.bidi2Forward(castingAdapter)),
			    new InstanceofPredicate(PositionedRenderable.class));
     assert camera!=null;
	setCamera(camera);//TODO: Remove after redesign
    }//end constructor
    
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
    
    private void ensureInit() {
	if (initialized)
	    return;
	gpu.memoryManager.get().map();
	initialized = true;
    }// end ensureInit()

    private void fpsTracking() {
	frameNumber++;
	if ((frameNumber %= 20) == 0) {
	    final long dT = System.currentTimeMillis() - lastTimeMillis;
		if(dT<=0)return;
		final int fps = (int)(20.*(1000. / (double)dT));
	    reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" FPS", "" + fps);
	    final Collection<PositionedRenderable> coll = renderList.get().getVisibleWorldObjectList();
	    synchronized(coll){
	    reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" numVisibleObjects", coll.size()+"");
	    reporter.report("org.jtrfp.trcl.core.Renderer."+debugName+" rootGrid", getCamera().getRootGrid().toString());
	    }
	    lastTimeMillis = System.currentTimeMillis();
	}
	
    }//end fpsTracking()
    
    public void setCamera(Camera toUse){
	if(this.camera!=null)
	    this.camera.getFlatRelevanceCollection().removeTarget(relevantPositioned, true);
	this.camera=toUse;
	toUse.getFlatRelevanceCollection().addTarget(relevantPositioned, true);
    }
    
    public final Callable<?> render = new Callable<Void>(){
	@Override
	public Void call() throws Exception {
		final GL3 gl = gpu.getGl();
		try{	ensureInit();
			 final RenderList rl = renderList.getRealtime();
			
			if(oneShotBehavior){
			    if(!keepAlive)
				return null;
			    keepAlive=false;
			    }
			rl.render(gl);
			rl.sendToGPU(gl);
			//Make sure memory on the GPU is up-to-date by flushing stale pages to GPU mem.
			gpu.memoryManager.getRealtime().flushStalePages();
			// Update texture codepages
			gpu.textureManager.getRealtime().vqCodebookManager.getRealtime().refreshStaleCodePages();
			fpsTracking();
		}catch(NotReadyException e){}
	    return null;
	}};
    
    public void setSunVector(Vector3D sv){
	factory.getDeferredProgram().use();
	factory.getSunVectorUniform().set((float)sv.getX(),(float)sv.getY(),(float)sv.getZ());
	gpu.defaultProgram();
    }

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

    public Renderer setSunColor(final Color color) {
	gpu.submitToGL(new Callable<Void>(){
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
	gpu.submitToGL(new Callable<Void>(){
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
    

    public RendererFactory getRendererFactory() {
	return factory;
    }

    public TRFutureTask<RenderList> getRenderList() {
	return renderList;
    }
    
    public Camera getCamera() {
	return camera;
    }

    public void keepAlive() {
	keepAlive=true;
    }

    /**
     * @return the oneShotBehavior
     */
    public boolean isOneShotBehavior() {
        return oneShotBehavior;
    }

    /**
     * @param oneShotBehavior the oneShotBehavior to set
     */
    public void setOneShotBehavior(boolean oneShotBehavior) {
        this.oneShotBehavior = oneShotBehavior;
    }

    public boolean isKeepAlive() {
	return keepAlive;
    }
}//end Renderer
