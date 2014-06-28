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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.Animator;

public final class ThreadManager {
    public static final int RENDER_FPS 			= 60;
    public static final int GAMEPLAY_FPS 		= RENDER_FPS;
    public static final int RENDERLIST_REFRESH_FPS 	= 5;
    private final TR 			tr;
    private final Timer 		lightweightTimer 	= new Timer("LightweightTimer");
    private final Timer 		gameplayTimer 		= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private Thread 			renderingThread;
    private Animator			animator;
    public final ExecutorService	threadPool 			= 
	    new ThreadPoolExecutor(20,35,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(200));
    private TRFutureTask<Void>		visibilityCalcTask;
    
    ThreadManager(final TR tr) {
	this.tr = tr;
	start();
    }// end constructor
    
    private void gameplay() {
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	if(tr.renderer.isDone() && tr.getPlayer()!=null){
	    if(!tr.renderer.get().currentRenderList().isDone())
		return;
	}else return;
	List<WorldObject> vl = tr.renderer.get().currentRenderList().get().getVisibleWorldObjectList();
	boolean alreadyVisitedPlayer=false;
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo = vl.get(i);
	    boolean multiplePlayer=false;
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), tr
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		if(wo instanceof Player){
		    if(alreadyVisitedPlayer){
			multiplePlayer=true;
			/*
			new RuntimeException("ALREADY VISITED PLAYER").printStackTrace();//TODO: Remove
			Player p = (Player)wo;
			List<PositionListener>pcls = p.getPositionListeners();
			for(PositionListener pcl:pcls){
			    System.out.println("PositionListener "+pcl);
			}//end for(pcls)
			*/
		    }else alreadyVisitedPlayer=true;
		}
		if(!multiplePlayer)wo.tick(tickTimeInMillis);
	}// end for(worldObjects)
	if(tr.getPlayer()!=null){
	    tr.getCollisionManager().performCollisionTests();
	}
	lastGameplayTickTime = tickTimeInMillis;
    }// end gameplay()

    private void visibilityCalc() {
	if(tr.renderer==null)return;
	if(!tr.renderer.isDone())return;
	tr.renderer.get().updateVisibilityList();
	if(visibilityCalcTask!=null){
	    if(!visibilityCalcTask.isDone())return;
	}
	visibilityCalcTask = new TRFutureTask<Void>(tr,new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		tr.getCollisionManager().updateCollisionList();
		return null;
	    }});
	threadPool.submit(visibilityCalcTask);
    }//end visibilityCalc()
    public <T> TRFuture<T> submitToGL(Callable<T> c){
	final GLFutureTask<T> result = new GLFutureTask<T>(tr,c);
	if(Thread.currentThread()!=renderingThread)
	    result.enqueue();
	else result.run();
	return result;
    }
    
    public <T> TRFutureTask<T> submitToThreadPool(Callable<T> c){
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,c);
	threadPool.submit(result);
	return result;
    }

    private void start() {
	gameplayTimer.schedule(new TimerTask(){
	    @Override
	    public void run() {
				if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
					visibilityCalc();}
				    if(tr.getPlayer()!=null)gameplay();
		
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
		if(tr.renderer!=null){
		    if(tr.renderer.isDone()){
			if(ThreadManager.this.tr.renderer.isDone())ThreadManager.this.tr.renderer.get().render();
		    	}////end if(renderer.isDone)
		    }//end if(!null)
	    }//end display()

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	lastGameplayTickTime = System.currentTimeMillis();
    }// end start()
    
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
}// end ThreadManager
