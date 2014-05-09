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
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.swing.SwingUtilities;

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
    private final Timer 		gameplayTimer 			= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private Thread 			renderingThread;
    private volatile boolean		running				=true;
    private Thread			glExecutorThread;
    public final ExecutorService	threadPool 			= 
	    new ThreadPoolExecutor(20,35,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(200));
    private final ConcurrentLinkedQueue<FutureTask> 	
    	mappedOperationQueue 		= new ConcurrentLinkedQueue<FutureTask>();
    ThreadManager(final TR tr) {
	this.tr = tr;
	final Thread gameplayThread = new Thread(new Runnable(){
	    @Override
	    public void run() {
		try{
		Thread.currentThread().setName("gameplayThread");
		while(running){
		    Thread.sleep(1000/GAMEPLAY_FPS);
		    if(tr.getPlayer()!=null)gameplay();
		    submitToGL(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
			    if(tr.renderer.isDone()){
				    ThreadManager.this.tr.renderer.get().render();
				    }////end if(renderer.isDone)
			    return null;
			}}).get();
		    SwingUtilities.invokeAndWait(new Runnable(){
			@Override
			public void run() {
			    tr.getRootWindow().getCanvas().repaint();
			}});
		}}catch(InterruptedException e){}
		catch(Exception e){tr.showStopper(e);}
	    }});
	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
	    @Override
	    public void run() {
		running=false;
		glExecutorThread.interrupt();
		gameplayThread.interrupt();
	    }}));
	//gameplayThread.setDaemon(true);
	gameplayThread.start();
	start();
    }// end constructor

    private void gameplay() {
	if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
		visibilityCalc();}
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	if(tr.renderer.isDone() && tr.getPlayer()!=null){
	    if(!tr.renderer.get().currentRenderList().isDone())
		return;
	}else return;
	List<WorldObject> vl = tr.renderer.get().currentRenderList().get().getVisibleWorldObjectList();
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo = vl.get(i);
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), tr
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		wo.tick(tickTimeInMillis);
	}// end for(worldObjects)
	if(tr.getPlayer()!=null){
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
	if(Thread.currentThread()!=renderingThread){
	    synchronized(mappedOperationQueue){
	    final boolean wasEmpty = mappedOperationQueue.isEmpty();
	    mappedOperationQueue.add(result);
	    if(wasEmpty)mappedOperationQueue.notifyAll();}
	}//end if(current thread is GL)
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
	    public void init(final GLAutoDrawable drawable) {
		System.out.println("GLEventListener.init()");
		final GLContext context = drawable.getContext();
		glExecutorThread = new Thread(new Runnable(){
		    @Override
		    public void run() {
			Thread.currentThread().setName("glExecutorThread");
			try{
			while(running){
			    renderingThread=Thread.currentThread();
			    synchronized(mappedOperationQueue){
				if(mappedOperationQueue.isEmpty()){
				    drawable.getContext().release();
				    mappedOperationQueue.wait();
				}//end if(!glTasksWaiting)
			    }//end sync(glTasksWaiting)
			    //Execute the tasks
			    try{context.makeCurrent();}catch(NullPointerException e){break;}
				//if GPU not yet available, mapping not possible.
				if(ThreadManager.this.tr.gpu.isDone()){
				    if(ThreadManager.this.tr.gpu.get().memoryManager.isDone()){
				    ThreadManager.this.tr.gpu.get().memoryManager.get().map(); }}
				while(!mappedOperationQueue.isEmpty()){
				    mappedOperationQueue.poll().run();
				    renderingThread.setName("glExecutorThread");
				}//end while(mappedOperationQueue)
			}}catch(InterruptedException e){}
			catch(Exception e){tr.showStopper(e);}
			if(context.isCurrent())context.release();
		    }});
		glExecutorThread.setDaemon(true);
		glExecutorThread.start();
	    }//end init()

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
	    }

	    @Override
	    public void display(GLAutoDrawable drawable) {
		Thread.currentThread().setName("OpenGL display()");
		
		
		
	    }//end display()

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	//renderingAnimator.start();
	lastGameplayTickTime = System.currentTimeMillis();
    }// end start()
    
    public long getElapsedTimeInMillisSinceLastGameTick() {
	return timeInMillisSinceLastGameTick;
    }

    public Timer getGameplayTimer() {
	return gameplayTimer;
    }
}// end ThreadManager
