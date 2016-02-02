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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gpu.GLExecutor;
import org.jtrfp.trcl.gpu.ProvidesGLThread;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.FPSAnimator;

public final class ThreadManager implements GLExecutor{
    public static final int RENDER_FPS 			= 60;
    public static final int GAMEPLAY_FPS 		= 60;
    public static final int RENDERLIST_REFRESH_FPS 	= 1;
    private final TR 			tr;
    private final Timer 		lightweightTimer 	= new Timer("LightweightTimer");
    private final Timer 		gameplayTimer 		= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private Thread 			renderingThread;
    private FPSAnimator			animator;
    private boolean[] 			paused = new boolean[]{false};
    public final ThreadPoolExecutor	threadPool 			= 
	    new ThreadPoolExecutor(
		    20,
		    20,
		    1,
		    TimeUnit.DAYS,new ArrayBlockingQueue<Runnable>(30000,true));
    public final ThreadPoolExecutor	gpuMemAccessThreadPool 		= 
	    new ThreadPoolExecutor(
		    30,
		    30,
		    1,
		    TimeUnit.DAYS,new ArrayBlockingQueue<Runnable>(30000,true));
    
    public final Object			   gameStateLock	   = new Object();
    public final Queue<TRFutureTask<?>>    pendingGPUMemAccessTasks= new ArrayBlockingQueue<TRFutureTask<?>>(30000,true);
    public final Queue<TRFutureTask<?>>    activeGPUMemAccessTasks = new ArrayBlockingQueue<TRFutureTask<?>>(30000,true);
    public final ArrayList<Callable<?>>repeatingGPUMemAccessTasks  = new ArrayList<Callable<?>>();
    public final List<Callable<?>>repeatingGLTasks	           = Collections.synchronizedList(new ArrayList<Callable<?>>());
    
    private final Submitter<TRFutureTask<?>> pendingGPUMemAccessTaskSubmitter = new AbstractSubmitter<TRFutureTask<?>>(){
	@Override
	public void submit(TRFutureTask<?> item) {
		pendingGPUMemAccessTasks.add(item);
	}//end submit(...)
    };
    private final Submitter<TRFutureTask<?>> activeGPUMemAccessTaskSubmitter = new AbstractSubmitter<TRFutureTask<?>>(){
	@Override
	public void submit(TRFutureTask<?> item) {
		activeGPUMemAccessTasks.add(item);
		gpuMemAccessThreadPool.submit(item);
	}//end submit(...)
    };
    private AtomicReference<Submitter<TRFutureTask<?>>>	currentGPUMemAccessTaskSubmitter 
    	= new AtomicReference<Submitter<TRFutureTask<?>>>(activeGPUMemAccessTaskSubmitter);
    private final long startupTimeMillis = System.currentTimeMillis();
    
    ThreadManager(final TR tr) {
	this.tr = tr;
	threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler(){
	    @Override
	    public void rejectedExecution(Runnable r, ThreadPoolExecutor exec) {
		try{Thread.sleep(150);
		 exec.execute(r);
		}//Wait 150ms
		catch(InterruptedException e){e.printStackTrace();}
	    }});
	gpuMemAccessThreadPool.setRejectedExecutionHandler(new RejectedExecutionHandler(){
	    @Override
	    public void rejectedExecution(Runnable r, ThreadPoolExecutor exec) {
		try{Thread.sleep(150);
		 exec.execute(r);
		}//Wait 150ms
		catch(InterruptedException e){e.printStackTrace();}
	    }});
	threadPool.prestartAllCoreThreads();
	gpuMemAccessThreadPool.prestartAllCoreThreads();
	start();
    }// end constructor
    
    private final ArrayList<PositionedRenderable> visibilityListBuffer = new ArrayList<PositionedRenderable>();
    
    private void gameplay() {
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	boolean alreadyVisitedPlayer=false;
	final Game game = tr.getGame();
	TRFuture [] renderers = new TRFuture[]{tr.mainRenderer/* ,tr.secondaryRenderer*/ };//TODO: This is hacky.
	try{// NotReadyException
	visibilityListBuffer.clear();
	synchronized(paused){
	synchronized(gameStateLock){
	    for(TRFuture<Renderer> r:renderers){
		if(r==null)return;
		Renderer renderer = r.getRealtime();
		if(renderer.isEnabled()){
		    final Collection<PositionedRenderable> vl = 
				renderer.
				getRenderList().
				getRealtime().
				getVisibleWorldObjectList();
		    synchronized(vl)
			 {visibilityListBuffer.addAll(vl);}
			    for (PositionedRenderable pr:visibilityListBuffer) {
			    boolean multiplePlayer=false;
			    final WorldObject wo = (WorldObject)pr;
			    if (wo.isActive() || wo instanceof RelevantEverywhere)
				if(wo instanceof Player){
				    if(alreadyVisitedPlayer)
					multiplePlayer=true;
				    else alreadyVisitedPlayer=true;
				}//end if(Player)
				if(!multiplePlayer&&!paused[0])
				    wo.tick(tickTimeInMillis);
			 }// end for(worldObjects)
		}//end if(renderer active)
		renderer.getCamera().tick(tickTimeInMillis);
	    }//end for(renderers)
	}//end sync(gameStateLock)//relevance changes outside of this cause errors!
	//if(game.getPlayer()!=null && !paused[0])
	    //tr.getCollisionManager().performCollisionTests();
	    tr.getCollisionManager().newPerformCollisionTests();
	}// end sync(paused)
	}catch(NotReadyException e){/*System.out.println("ThreadManager: Not ready");*/}
	lastGameplayTickTime = tickTimeInMillis;
    }// end gameplay()
    
    public <T> TRFutureTask<T> submitToGPUMemAccess(Callable<T> c){
	final TRFutureTask<T> result = new TRFutureTask<T>(c,tr);
	synchronized(currentGPUMemAccessTaskSubmitter){
	    currentGPUMemAccessTaskSubmitter.get().submit(result);}
	return result;
    }//end submitToGPUMemAccess(...)
    
    public <T> GLFutureTask<T> submitToGL(Callable<T> c){
	final GLFutureTask<T> result = new GLFutureTask<T>(tr.getRootWindow().getCanvas(),c);
	if(isGLThread())
	    if(tr.gpu.get().getGl().getContext().isCurrent()){
		result.run();
		return result;
	    }else{
		tr.gpu.get().getGl().getContext().makeCurrent();
		result.run();
		tr.gpu.get().getGl().getContext().release();
	    }
	result.enqueue();
	return result;
    }//end submitToGL(...)
    
    public boolean isGLThread(){
	return Thread.currentThread()==renderingThread;
    }
    
    public <T> TRFutureTask<T> submitToThreadPool(Callable<T> c){
	return submitToThreadPool(true,c);
    }//end submitToThreadPool(...)

    private void start() {
	gameplayTimer.schedule(new TimerTask(){
	    @Override
	    public void run() {
		if(tr.mainRenderer==null)
		    return;
		try{gameplay();
		}catch(Exception e){tr.showStopper(e);}
	    }}, 0, 1000/GAMEPLAY_FPS);
	animator = new FPSAnimator(tr.getRootWindow().getCanvas(),RENDER_FPS);
	animator.start();
	tr.getRootWindow().addWindowListener(new WindowAdapter(){
	    @Override
	    public void windowClosing(WindowEvent e){
		System.out.println("WindowClosing...");
		gameplayTimer.cancel();
		animator.stop();
		System.out.println("glExecutorThread.join()...");
		System.out.println("ThreadManager: WindowClosing Done.");
	    }
	});
	tr.getRootWindow().getCanvas().addGLEventListener(new GLEventListener() {
	    @Override
	    @ProvidesGLThread
	    public void init(final GLAutoDrawable drawable) {
		System.out.println("GLEventListener.init()");
	    }//end init()

	    @Override
	    @ProvidesGLThread
	    public void dispose(GLAutoDrawable drawable) {
	    }
	    
	    @Override
	    @ProvidesGLThread
	    public void display(GLAutoDrawable drawable) {
		renderingThread=Thread.currentThread();
		renderingThread.setName("display()");
		attemptRender();
	    }//end display()

	    @Override
	    @ProvidesGLThread
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	lastGameplayTickTime = System.currentTimeMillis();
    }// end start()
    
    private void attemptRender() {
	// Swap submitters
	try {
	    synchronized (currentGPUMemAccessTaskSubmitter) {
		currentGPUMemAccessTaskSubmitter
			.set(pendingGPUMemAccessTaskSubmitter);
	    }//end sync()
	    while (!activeGPUMemAccessTasks.isEmpty())
		if (!activeGPUMemAccessTasks.peek().isDone())
		    return;// Abort. Not ready to go yet.
		else activeGPUMemAccessTasks.poll();
	     ///////// activeGPUMemAccessTasks should be empty beyond this
	    assert activeGPUMemAccessTasks.isEmpty() : "ThreadManager.activeGPUMemAccessTasks intolerably not empty.";
	    //// GL ONLY
	    synchronized(repeatingGLTasks){
	     for(Callable<?> c:repeatingGLTasks)
		c.call();}
	    synchronized (currentGPUMemAccessTaskSubmitter) {
		currentGPUMemAccessTaskSubmitter
			.set(activeGPUMemAccessTaskSubmitter);
	    }
	    while (!pendingGPUMemAccessTasks.isEmpty())
		activeGPUMemAccessTaskSubmitter.submit(pendingGPUMemAccessTasks
			.poll());
	    for(Callable<?> c:repeatingGPUMemAccessTasks)
		this.submitToGPUMemAccess(c);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }// end attemptRender()
    
    public long getElapsedTimeInMillisSinceLastGameTick() {
	return timeInMillisSinceLastGameTick;
    }
    
    /**
     * General-use Timer to be used for short, non-timing-critical operations.
     * @return
     * @since Jun 8, 2014
     */
    public Timer getLightweightTimer() {
	return lightweightTimer;
    }
    
    public static final ThreadLocal<TRFutureTask<?>> trFutureTaskIdentities = new ThreadLocal<TRFutureTask<?>>();
    public void notifyGPUMemAccessFault() {
	System.err.println("pending tasks:");
	for(TRFutureTask<?> t: pendingGPUMemAccessTasks)
	    System.err.println(t);
	System.err.println("active tasks:");
	for(TRFutureTask<?> t: activeGPUMemAccessTasks)
	    System.err.println(t);
	System.err.println("This task: "+trFutureTaskIdentities.get());
	tr.showStopper(new RuntimeException("Writing to GPU while rendering!"));
    }//end notifyGPUMemAccessFault()

    public long getMillisSinceStartup() {
	return System.currentTimeMillis()-startupTimeMillis;
    }

    public void setPaused(boolean paused) {
	synchronized(this.paused)
	 {this.paused[0]=paused;}
    }

    public <T>TRFutureTask<T> submitToThreadPool(boolean handleException,
	    Callable<T> callable) {
	final TRFutureTask<T> result = new TRFutureTask<T>(callable,handleException?tr:null);
	threadPool.submit(result);
	return result;
    }
    public void addRepeatingGLTask(Callable<?> task){
	if(task==null)
	    throw new NullPointerException("Passed task intolerably null.");
	synchronized(repeatingGLTasks){
	 if(repeatingGLTasks.contains(task))
	    return;
	 repeatingGLTasks.add(task);}
    }//end addRepeatingGLTask(...)
    
    public void removeRepeatingGLTask(Callable<?> task){
	if(task==null)
	    throw new NullPointerException("Passed task intolerably null.");
	repeatingGLTasks.remove(task);
    }//end removeRepeatingGLTask(...)
    
    public void addRepeatingGPUMemTask(Callable<?> task){
	if(task==null)
	    throw new NullPointerException("Passed task intolerably null.");
	if(repeatingGPUMemAccessTasks.contains(task))
	    return;
	repeatingGPUMemAccessTasks.add(task);
    }//end addRepeatingGPUMemTask(...)
    
    public void removeRepeatingGPUMemTask(Callable<?> task){
	if(task==null)
	    throw new NullPointerException("Passed task intolerably null.");
	repeatingGPUMemAccessTasks.remove(task);
    }//end removeRepeatingGPUMemTask(...)
}// end ThreadManager
