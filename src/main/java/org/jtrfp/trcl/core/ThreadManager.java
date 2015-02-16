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
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.Animator;

public final class ThreadManager {
    public static final int RENDER_FPS 			= 60;
    public static final int GAMEPLAY_FPS 		= RENDER_FPS;
    public static final int RENDERLIST_REFRESH_FPS 	= 1;
    private final TR 			tr;
    private final Timer 		lightweightTimer 	= new Timer("LightweightTimer");
    private final Timer 		gameplayTimer 		= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private Thread 			renderingThread;
    private Animator			animator;
    private boolean[] 			paused = new boolean[]{false};
    private final ArrayList<Renderer>	renderers = new ArrayList<Renderer>();
    public final ThreadPoolExecutor	threadPool 			= 
	    new ThreadPoolExecutor(
		    30,
		    65,
		    1,
		    TimeUnit.DAYS,new ArrayBlockingQueue<Runnable>(30000,true));
    public final ThreadPoolExecutor	gpuMemAccessThreadPool 		= 
	    new ThreadPoolExecutor(
		    30,
		    65,
		    1,
		    TimeUnit.DAYS,new ArrayBlockingQueue<Runnable>(30000,true));
    public final Object			gameStateLock			= new Object();
    public final Queue<TRFutureTask<?>> pendingGPUMemAccessTasks   = new ArrayBlockingQueue<TRFutureTask<?>>(30000,true);
    public final Queue<TRFutureTask<?>> activeGPUMemAccessTasks    = new ArrayBlockingQueue<TRFutureTask<?>>(30000,true);
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
    
    private void gameplay() {
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	try{// NotReadyException
	final List<WorldObject> vl = 
		tr.mainRenderer.
		getRealtime().
		currentRenderList().
		getRealtime().
		getVisibleWorldObjectList();
	boolean alreadyVisitedPlayer=false;
	final Game game = tr.getGame();
	if(game==null)
	    return;
	synchronized(paused){
	synchronized(gameStateLock){
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo;
	    synchronized(vl){if(!vl.isEmpty())wo = vl.get(i);else break;}//TODO: This is slow.
	    boolean multiplePlayer=false;
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), game
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		if(wo instanceof Player){
		    if(alreadyVisitedPlayer){
			multiplePlayer=true;
		    }else alreadyVisitedPlayer=true;
		}//end if(Player)
		if(!multiplePlayer&&!paused[0])wo.tick(tickTimeInMillis);
	 }// end for(worldObjects)
	}//end sync(gameStateLock)
	if(game.getPlayer()!=null && !paused[0])
	    tr.getCollisionManager().performCollisionTests();
	}// end sync(paused)
	}catch(NotReadyException e){System.out.println("ThreadManager: Not ready");}
	lastGameplayTickTime = tickTimeInMillis;
    }// end gameplay()
    
    

    
    
    public <T> TRFutureTask<T> submitToGPUMemAccess(Callable<T> c){
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,c);
	synchronized(currentGPUMemAccessTaskSubmitter){
	    currentGPUMemAccessTaskSubmitter.get().submit(result);}
	return result;
    }//end submitToGPUMemAccess(...)
    
    public <T> GLFutureTask<T> submitToGL(Callable<T> c){
	final GLFutureTask<T> result = new GLFutureTask<T>(tr,c);
	if(isGLThread())
	    if(tr.gpu.get().getGl().getContext().isCurrent()){
		result.run();
		return result;
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
		try{
		if (counter++ % Math.ceil(RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
		 if(System.currentTimeMillis()<
			 tr.
			 mainRenderer.get().
			 nextVisCalcTime.get())
		     return;
		 tr.mainRenderer.get().visibilityCalc();}
		if(tr.getGame()!=null)
		 if(tr.getGame().getPlayer()!=null)gameplay();
		}catch(Exception e){tr.showStopper(e);}
	    }}, 0, 1000/GAMEPLAY_FPS);
	animator = new Animator(tr.getRootWindow().getCanvas());
	animator.start();
	tr.getRootWindow().addWindowListener(new WindowAdapter(){
	    @Override
	    public void windowClosing(WindowEvent e){
		System.out.println("WindowClosing...");
		animator.stop();
		System.out.println("glExecutorThread.join()...");
		System.out.println("ThreadManager: WindowClosing Done.");
	    }
	});
	tr.getRootWindow().getCanvas().addGLEventListener(new GLEventListener() {
	    @Override
	    public void init(final GLAutoDrawable drawable) {
		System.out.println("GLEventListener.init()");
	    }//end init()

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
	    }
	    
	    @Override
	    public void display(GLAutoDrawable drawable) {
		renderingThread=Thread.currentThread();
		renderingThread.setName("display()");
		attemptRender();
	    }//end display()

	    @Override
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
	    while (!activeGPUMemAccessTasks.isEmpty()) {
		if (!activeGPUMemAccessTasks.peek().isDone())
		    return;// Abort. Not ready to go yet.
		activeGPUMemAccessTasks.poll();
	    }// end while(!empty)
	     // /////// activeGPUMemAccessTasks should be empty beyond this
	    assert activeGPUMemAccessTasks.isEmpty() : "ThreadManager.activeGPUMemAccessTasks intolerably not empty.";
	     // point ////////
	    for (Renderer renderer : renderers) {
		renderer.render();
	    }// end for(renderers)
	    synchronized (currentGPUMemAccessTaskSubmitter) {
		currentGPUMemAccessTaskSubmitter
			.set(activeGPUMemAccessTaskSubmitter);
	    }
	    while (!pendingGPUMemAccessTasks.isEmpty())
		activeGPUMemAccessTaskSubmitter.submit(pendingGPUMemAccessTasks
			.poll());
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
	for(TRFutureTask t: pendingGPUMemAccessTasks)
	    System.err.println(t);
	System.err.println("active tasks:");
	for(TRFutureTask t: activeGPUMemAccessTasks)
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
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,callable);
	result.setHandleException(handleException);
	threadPool.submit(result);
	return result;
    }

    public void registerRenderer(Renderer r){
	if(r==null)
	    throw new NullPointerException("Passed Renderer intolerably null.");
	if(!renderers.contains(r))
	    renderers.add(r);
    }//end registerRenderer(...)
    
    public void deregisterRenderer(Renderer r){
	if(r==null)
	    throw new NullPointerException("Passed Renderer intolerably null.");
	renderers.remove(r);
    }//end deregisterRenderer
}// end ThreadManager
