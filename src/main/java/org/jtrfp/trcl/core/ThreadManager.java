package org.jtrfp.trcl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

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
    private ArrayList<Runnable> runWhenFirstStarted = new ArrayList<Runnable>();
    private boolean firstRun = true;
    private final ConcurrentLinkedQueue<Runnable> mappedOperationQueue = new ConcurrentLinkedQueue<Runnable>();
    private Thread renderingThread;

    private int counter = 0;

    ThreadManager(TR tr) {
	this.tr = tr;
	renderingAnimator = new FPSAnimator((GLCanvas) tr.getGPU()
		.getComponent(), RENDER_FPS);
    }// end constructor

    private void gameplay() {
	// Ticks
	final long tickTimeInMillis = System.currentTimeMillis();
	timeInMillisSinceLastGameTick = tickTimeInMillis - lastGameplayTickTime;
	List<WorldObject> vl = tr.getCollisionManager().getCurrentlyActiveVisibilityList();
	for (int i = 0; i<vl.size(); i++) {
	    final WorldObject wo = vl.get(i);
	    if (wo.isActive()
		    && (TR.twosComplimentDistance(wo.getPosition(), tr
			    .getPlayer().getPosition()) < CollisionManager.MAX_CONSIDERATION_DISTANCE)
		    || wo instanceof VisibleEverywhere)
		wo.tick(tickTimeInMillis);
	}// end for(worldObjects)
	tr.getCollisionManager().performCollisionTests();
	lastGameplayTickTime = tickTimeInMillis;
    }// end gameplay()

    private void visibilityCalc() {
	tr.getRenderer().updateVisibilityList();
	tr.getCollisionManager().updateVisibilityList();
    }

    public void start() {
	tr.getGPU().addGLEventListener(new GLEventListener() {
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
		renderingThread=Thread.currentThread();
		if (firstRun) {
		    for (Runnable r : runWhenFirstStarted) {
			r.run();
		    }//end for(runnable)
		}//end if(firstRun)
		Thread.currentThread().setPriority(RENDERING_PRIORITY);
		ThreadManager.this.tr.getGPU().getMemoryManager().map();
		while(!mappedOperationQueue.isEmpty()){
		    final Runnable r = mappedOperationQueue.poll();
		    r.run();
		    synchronized(r){r.notifyAll();}
		}
		if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS) == 0)
		    visibilityCalc();
		gameplay();
		ThreadManager.this.tr.getRenderer().render();
		firstRun = false;
	    }

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
	    }
	});
	renderingAnimator.start();
	lastGameplayTickTime = System.currentTimeMillis();
    }// end constructor

    public void enqueueGLOperation(Runnable r){
	if(Thread.currentThread()!=renderingThread)mappedOperationQueue.add(r);
	else r.run();
    }
    public void blockingEnqueueGLOperation(Runnable r){
	if(Thread.currentThread()!=renderingThread){
	    synchronized(r){
	    mappedOperationQueue.add(r);
	    try{r.wait();}catch(Exception e){e.printStackTrace();}}}
	else {r.run();}
    }
    
    public long getElapsedTimeInMillisSinceLastGameTick() {
	return timeInMillisSinceLastGameTick;
    }

    public Timer getGameplayTimer() {
	return gameplayTimer;
    }

    public void addRunnableWhenFirstStarted(Runnable r) {
	runWhenFirstStarted.add(r);
    }
}// end ThreadManager
