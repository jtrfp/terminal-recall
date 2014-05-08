package org.jtrfp.trcl.core;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.Animator;

public class ThreadManager {
    public static final int RENDER_FPS 			= 60;
    public static final int GAMEPLAY_FPS 		= RENDER_FPS;
    public static final int RENDERLIST_REFRESH_FPS 	= 5;
    public static final int RENDERING_PRIORITY 		= 6;
    public static final int SOUND_PRIORITY 		= 8;
    private final TR 			tr;
    private final Animator 		renderingAnimator;
    private final Timer 		gameplayTimer 			= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private final Object		renderThisFrameLock		= new Object();
    private Thread 			renderingThread;
    private volatile boolean		renderThisFrame=true;	
    public final ExecutorService	threadPool 			= 
	    new ThreadPoolExecutor(20,35,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(200));
    private final ConcurrentLinkedQueue<FutureTask> 	
    	mappedOperationQueue 		= new ConcurrentLinkedQueue<FutureTask>();
    private final ScheduledThreadPoolExecutor gameplayScheduler = new ScheduledThreadPoolExecutor(1,new ThreadFactory(){
	@Override
	public Thread newThread(Runnable runnable) {
	    final Thread thisThread = new Thread(runnable);
	    thisThread.setName("gameplaySchedulerThread");
	    thisThread.setPriority(RENDERING_PRIORITY);
	    return thisThread;
	}
    });

    ThreadManager(final TR tr) {
	this.tr = tr;
	renderingAnimator = new Animator(tr.getRootWindow().getCanvas());
	final Thread gameplayThread = new Thread(new Runnable(){

	    @Override
	    public void run() {
		try{
		while(true){
		    Thread.sleep(1/GAMEPLAY_FPS);
			synchronized(renderThisFrameLock){
			    while(renderThisFrame){
			    renderThisFrameLock.wait();}
			}//end sync(renderThisFrameLock)
			//if(tr.getPlayer()!=null)gameplay();
		    renderThisFrame = true;
		}}catch(InterruptedException e){}
		catch(Exception e){tr.showStopper(e);}
	    }});
	gameplayThread.start();
	start();
    }// end constructor

    private void gameplay() {
	//System.out.println("gameplay()");
	if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
		visibilityCalc();}
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	if(tr.renderer.isDone() && tr.getPlayer()!=null){
	    if(!tr.renderer.get().currentRenderList().isDone())
		return;
	}else return;
	//System.out.println("getVisibleObjectList()");
	List<WorldObject> vl = tr.renderer.get().currentRenderList().get().getVisibleWorldObjectList();
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo = vl.get(i);
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), tr
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		//System.out.println(wo.getClass().getName());
		wo.tick(tickTimeInMillis);
	}// end for(worldObjects)
	//System.out.println("performCollisionTests()");
	if(tr.getPlayer()!=null){
	   // tr.getPlayer().tick(tickTimeInMillis);
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
		    ThreadManager.this.tr.gpu.get().memoryManager.get().map(); }}//end if(mapped)
		while(!mappedOperationQueue.isEmpty()){
		    mappedOperationQueue.poll().run();
		    renderingThread.setName("OpenGL display()");
		    //synchronized(r){r.notifyAll();}
		}//end while(mappedOperationQueue)
		//Render this frame if necessary
		//System.out.println("display()");
		if(renderThisFrame){
		if(tr.renderer.isDone()){
		    if(tr.getPlayer()!=null)gameplay();
		    //System.out.println("FRAME.");
		    ThreadManager.this.tr.renderer.get().render();

		    synchronized(renderThisFrameLock){
			renderThisFrame=false;
			    renderThisFrameLock.notifyAll();
			 }
		    }////end if(renderer.isDone)
		}//end if(renderThisFrame)
		
	    }//end display()

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	renderingAnimator.start();
	lastGameplayTickTime = System.currentTimeMillis();
    }// end start()

    public <T> TRFutureTask<T> enqueueGLOperation(Callable<T> r){
	final TRFutureTask<T> t = new TRFutureTask<T>(tr,r);
	if(Thread.currentThread()!=renderingThread)mappedOperationQueue.add(t);
	else t.run();
	return t;
	}
    
    public long getElapsedTimeInMillisSinceLastGameTick() {
	return timeInMillisSinceLastGameTick;
    }

    public Timer getGameplayTimer() {
	return gameplayTimer;
    }
}// end ThreadManager
