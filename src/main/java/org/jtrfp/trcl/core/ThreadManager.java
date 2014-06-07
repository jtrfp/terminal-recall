package org.jtrfp.trcl.core;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLRunnable;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PositionListener;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

public final class ThreadManager {
    public static final int RENDER_FPS 			= 60;
    public static final int GAMEPLAY_FPS 		= RENDER_FPS;
    public static final int RENDERLIST_REFRESH_FPS 	= 5;
    //public static final int RENDERING_PRIORITY 		= 6;
    //public static final int SOUND_PRIORITY 		= 8;
    private final TR 			tr;
    private final Timer 		gameplayTimer 			= new Timer("GameplayTimer");
    private long 			lastGameplayTickTime 		= 0;
    private long 			timeInMillisSinceLastGameTick 	= 0L;
    private int 			counter 			= 0;
    private Thread 			renderingThread;
    private volatile boolean		running				=true;
    private Thread			glExecutorThread;
    private Animator			animator;
    public final ExecutorService	threadPool 			= 
	    new ThreadPoolExecutor(20,35,10,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(200));
    private TRFuture<Void> 		renderTask;
    private TRFutureTask<Void>		visibilityCalcTask;
    private volatile TRFutureTask<Void> gpuUpdateTask;
    private final ConcurrentLinkedQueue<FutureTask> 	
    	mappedOperationQueue 		= new ConcurrentLinkedQueue<FutureTask>();
    private final AtomicInteger _counter = new AtomicInteger();
    private final AtomicLong _lastFPSTime = new AtomicLong();
    private final AtomicLong _timeInDisplay = new AtomicLong();
    
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
	final TRFutureTask<T> result = new TRFutureTask<T>(tr,c);
	if(Thread.currentThread()!=renderingThread){
	    //System.out.println("Enqueue...");
	    tr.getRootWindow().getCanvas().invoke(false, new GLRunnable(){
		@Override
		public boolean run(GLAutoDrawable drawable) {
		    //System.out.println("Executing...");
		    result.run();
		    //System.out.println("Executed.");
		    return true;
		}
	    });
	    //synchronized(mappedOperationQueue){
	    //final boolean wasEmpty = mappedOperationQueue.isEmpty();
	    //mappedOperationQueue.add(result);
	    //if(wasEmpty)mappedOperationQueue.notifyAll();}
	    
	}//end if(current thread is GL)
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
		//drawable.getContext().getGL().glFinish();//TODO: Remove
		long startTimeMillis = System.currentTimeMillis();
		//renderingThread=Thread.currentThread();
		final int _count = _counter.incrementAndGet();
		/*
		if(System.currentTimeMillis() > _lastFPSTime.get()+1000){
		    _counter.set(0);
		    System.out.println(_count+" FPS timeInDisplay="+_timeInDisplay.get()+"ms");
		    _lastFPSTime.set(System.currentTimeMillis());
		}//end if(1 second)
		*/
		//final GLContext context = drawable.getContext();
		//if(context.isCurrent())context.release();
		//Thread.currentThread().setPriority(RENDERING_PRIORITY-1);
		
		final long _renderStartTick=System.currentTimeMillis();
		//Thread.currentThread().setName("OpenGL display()");
		//if(gpuUpdateTask!=null)gpuUpdateTask.get();
		//if(counter%50==0)System.out.println("mappedOperationQueue size="+mappedOperationQueue.size());
		    //Schedule the rendering pass
		    /*renderTask = submitToGL(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
			    if(tr.renderer==null)return null;
			    if(tr.renderer.isDone()){
				    if(ThreadManager.this.tr.renderer.isDone())ThreadManager.this.tr.renderer.get().render();
				    }////end if(renderer.isDone)
			    return null;
			}});*/
		    //if(counter%50==0)System.out.println("Enqueue overhead: "+(System.currentTimeMillis()-_renderStartTick));
		    //renderTask.get();//TODO: Remove
		    //While the rendering pass is being executed, consider doing the visiblity calc.
		   /* gpuUpdateTask = ThreadManager.this.submitToThreadPool(new Callable<Void>(){
			    @Override
			    public Void call() throws Exception {*/
				if (counter++ % (RENDER_FPS / RENDERLIST_REFRESH_FPS ) == 0){
					visibilityCalc();}
				    if(tr.getPlayer()!=null)gameplay();
		/*		return null;
			    }
			});*/
		    //Barrier for completed rendering
		    //renderTask.get();//TODO: Uncomment
		    //context.makeCurrent();
		   // drawable.getContext().getGL().glFinish();//TODO: Remove
		    //_timeInDisplay.set(System.currentTimeMillis()-startTimeMillis);
		    //drawable.getContext().getGL().glFinish();//TODO: Remove
		    //if(counter%50==0)System.out.println("Wait-for-Render-Finish time: "+(System.currentTimeMillis()-_renderStartTick));
	    }}, 0, 1000/GAMEPLAY_FPS);
	animator = new Animator(tr.getRootWindow().getCanvas());
	animator.start();
	tr.getRootWindow().addWindowListener(new WindowAdapter(){
	    @Override
	    public void windowClosing(WindowEvent e){
		System.out.println("WindowClosing...");
		//glExecutorThread.interrupt();
		animator.stop();
		//running=false;
		System.out.println("glExecutorThread.join()...");
		//try{glExecutorThread.join();}
		//catch(InterruptedException ex){ex.printStackTrace();}
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
		if(tr.renderer.isDone()){
		    if(ThreadManager.this.tr.renderer.isDone())ThreadManager.this.tr.renderer.get().render();
		    }////end if(renderer.isDone)
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

    public Timer getGameplayTimer() {
	return gameplayTimer;
    }
}// end ThreadManager
