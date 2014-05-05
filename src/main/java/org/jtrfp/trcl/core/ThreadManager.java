package org.jtrfp.trcl.core;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.FPSAnimator;

public class ThreadManager {
    public static final int RENDER_FPS = 60;
    public static final int GAMEPLAY_FPS = RENDER_FPS;
    public static final int RENDERLIST_REFRESH_FPS = 5;
    public static final int RENDERING_PRIORITY = 6;
    public static final int SOUND_PRIORITY = 8;
    private final TR tr;
    private final FPSAnimator renderingAnimator;
    private final Timer gameplayTimer = new Timer("GameplayTimer");
    private long lastGameplayTickTime = 0;
    private long timeInMillisSinceLastGameTick = 0L;
    private final ConcurrentLinkedQueue<FutureTask> mappedOperationQueue = new ConcurrentLinkedQueue<FutureTask>();
    public final ExecutorService threadPool = new ThreadPoolExecutor(20,35,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(200));
    private Thread renderingThread;
    private int counter = 0;

    ThreadManager(final TR tr) {
	this.tr = tr;
	renderingAnimator = new FPSAnimator(tr.getRootWindow().getCanvas(),
		RENDER_FPS);
	start();
    }// end constructor

    private void gameplay() {
	// Ticks
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	if(tr.renderer.isDone()){
	    if(!tr.renderer.get().getCurrentRenderList().isDone()){
		return;
	    }
	}else return;
	List<WorldObject> vl = tr.renderer.get().getCurrentRenderList().get().getVisibleWorldObjectList();
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo = vl.get(i);
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), tr
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		wo.tick(tickTimeInMillis);
	}// end for(worldObjects)
	if(tr.getPlayer()!=null){
	    tr.getPlayer().tick(tickTimeInMillis);
	    tr.getCollisionManager().performCollisionTests();
	}
	lastGameplayTickTime = tickTimeInMillis;
    }// end gameplay()

    private void visibilityCalc() {
	tr.renderer.get().updateVisibilityList();
	tr.getCollisionManager().updateCollisionList();
    }
    public <T> TRFuture<T> submitToGL(Callable<T> c){
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,c);
	if(Thread.currentThread()!=renderingThread)mappedOperationQueue.add(result);
	else result.run();
	return result;
    }
    
    public <T> TRFuture<T> submitToThreadPool(Callable<T> c){
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,c);
	threadPool.submit(result);
	return result;
    }

    private void start() {
	tr.getRootWindow().getCanvas().addGLEventListener(new GLEventListener() {
	    @Override
	    public void init(GLAutoDrawable drawable) {
		System.out.println("GLEventListener.init()");
	    }

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
	    }

	    @Override
	    public void display(GLAutoDrawable drawable) {
		Thread.currentThread().setName("OpenGL display()");
		Thread.currentThread().setPriority(RENDERING_PRIORITY);
		renderingThread=Thread.currentThread();
		//if GPU not yet available, mapping not possible.
		if(ThreadManager.this.tr.gpu.isDone()){
		    if(ThreadManager.this.tr.gpu.get().memoryManager.isDone()){
		    ThreadManager.this.tr.gpu.get().memoryManager.get().map();}}
		while(!mappedOperationQueue.isEmpty()){
		    final Runnable r = mappedOperationQueue.poll();
		    r.run();
		    synchronized(r){r.notifyAll();}
		}//end while(mappedOperationQueue)
		//Wait for everyone to finish.
		if(tr.renderer.isDone()){
		    if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
			visibilityCalc();}
		    gameplay();
		    ThreadManager.this.tr.renderer.get().render();
		}
	    }//end display()

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	renderingAnimator.start();
	lastGameplayTickTime = System.currentTimeMillis();
    }// end constructor

    public <T> TRFutureTask<T> enqueueGLOperation(Callable<T> r){
	final TRFutureTask<T> t = new TRFutureTask<T>(tr,r);
	/*
	if(firstRun){
	    final boolean startedWithGL = tr.getGPU().getGl().getContext().isCurrent();
	    if(!startedWithGL){
		Threading.invokeOnOpenGLThread(false, t);}
	    else t.run();
	    return t;
	}//end if(firstRun)
	*/
	if(Thread.currentThread()!=renderingThread)mappedOperationQueue.add(t);
	else t.run();
	return t;
    }
    /*public void blockingEnqueueGLOperation(Runnable r){
	if(Thread.currentThread()!=renderingThread){
	    synchronized(r){
	    mappedOperationQueue.add(r);
	    try{r.wait();}catch(Exception e){e.printStackTrace();}}}
	else {r.run();}
    }*/
    
    public long getElapsedTimeInMillisSinceLastGameTick() {
	return timeInMillisSinceLastGameTick;
    }

    public Timer getGameplayTimer() {
	return gameplayTimer;
    }
}// end ThreadManager
