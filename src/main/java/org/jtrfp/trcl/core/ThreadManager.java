package org.jtrfp.trcl.core;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.FPSAnimator;


public class ThreadManager
	{
	public static final int RENDER_FPS=60;
	public static final int GAMEPLAY_FPS=RENDER_FPS;
	public static final int RENDERLIST_REFRESH_FPS=5;
	public static final int RENDERING_PRIORITY=6;
	public static final int SOUND_PRIORITY=8;
	private final TR tr;
	private final FPSAnimator renderingAnimator;
	private final Timer gameplayTimer = new Timer("GameplayTimer");
	private long lastGameplayTickTime=0;
	private long timeInMillisSinceLastGameTick=0L;
	
	private int counter=0;
	
	ThreadManager(TR tr)
		{this.tr=tr;
		renderingAnimator = new FPSAnimator((GLCanvas)tr.getGPU().getComponent(),RENDER_FPS);
		tr.getGPU().addGLEventListener(new GLEventListener()
			{
			@Override
			public void init(GLAutoDrawable drawable)
				{}

			@Override
			public void dispose(GLAutoDrawable drawable)
				{}

			@Override
			public void display(GLAutoDrawable drawable)
				{
				Thread.currentThread().setPriority(RENDERING_PRIORITY);
				if(counter++%(RENDER_FPS/RENDERLIST_REFRESH_FPS)==0)visibilityCalc();
				gameplay();
				ThreadManager.this.tr.getRenderer().render();
				}

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y,
					int width, int height)
				{display(drawable);}
			});
		
		}//end constructor
	
	private void gameplay(){
	 // Ticks
		final long tickTimeInMillis = System.currentTimeMillis();
		timeInMillisSinceLastGameTick=tickTimeInMillis-lastGameplayTickTime;
			List<WorldObject> vl = tr.getCollisionManager().getVisibilityList();
		    	for(WorldObject wo:vl){
		    	    if(wo.isActive()&&
				    (TR.twosComplimentDistance(wo.getPosition(), tr.getPlayer().getPosition())
				    <CollisionManager.MAX_CONSIDERATION_DISTANCE)||wo instanceof VisibleEverywhere)
				       wo.tick(tickTimeInMillis);
		    	    }//end for(worldObjects)
		    	  tr.getCollisionManager().performCollisionTests();
		lastGameplayTickTime=tickTimeInMillis;
	}//end gameplay()
	
	private void visibilityCalc(){
		tr.getRenderer().updateVisibilityList();
		tr.getCollisionManager().updateVisibilityList();
	}
	
	public void start(){
		renderingAnimator.start();
		lastGameplayTickTime=System.currentTimeMillis();
		}//end constructor
	
	public long getElapsedTimeInMillisSinceLastGameTick(){return timeInMillisSinceLastGameTick;}

	public Timer getGameplayTimer() {
	    return gameplayTimer;
	}
	}//end ThreadManager
