package org.jtrfp.trcl.core;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.opengl.util.FPSAnimator;


public class ThreadManager
	{
	public static final int RENDER_FPS=60;
	public static final int GAMEPLAY_FPS=60;
	public static final int RENDERLIST_REFRESH_FPS=5;
	public static final int RENDERING_PRIORITY=6;
	public static final int GAMEPLAY_PRIORITY=7;
	public static final int SOUND_PRIORITY=8;
	private final TR tr;
	private final FPSAnimator renderingAnimator;
	private final Timer gameplayTimer = new Timer("GameplayTimer");
	private final Timer visibilityCalculationTimer = new Timer("RenderListRefreshTimer");
	public static final Object GAME_OBJECT_MODIFICATION_LOCK = new Object();
	private long lastGameplayTickTime=0;
	private long timeInMillisSinceLastGameTick=0L;
	
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
				ThreadManager.this.tr.getRenderer().render();
				}

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y,
					int width, int height)
				{display(drawable);}
			});
		
		}//end constructor
	
	public void start(){
		renderingAnimator.start();
		lastGameplayTickTime=System.currentTimeMillis();
		gameplayTimer.scheduleAtFixedRate(new TimerTask()
			{@Override
			public void run()
				{Thread.currentThread().setPriority(GAMEPLAY_PRIORITY);
				// Ticks
				final long tickTimeInMillis = System.currentTimeMillis();
				timeInMillisSinceLastGameTick=tickTimeInMillis-lastGameplayTickTime;
				synchronized(GAME_OBJECT_MODIFICATION_LOCK)
					{List<WorldObject> vl = tr.getCollisionManager().getVisibilityList();
				    	for(WorldObject wo:vl)
						{if(wo.isVisible()&&
						    (TR.twosComplimentDistance(wo.getPosition(), tr.getPlayer().getPosition())
						    <CollisionManager.MAX_CONSIDERATION_DISTANCE)||wo.getPosition()==WorldObject.EVERYWHERE)
						        wo.tick(tickTimeInMillis);}
				    			tr.getCollisionManager().performCollisionTests();}
				lastGameplayTickTime=tickTimeInMillis;
				}//end run()
			}, 0, 1000/GAMEPLAY_FPS);
		visibilityCalculationTimer.scheduleAtFixedRate(new TimerTask()
			{@Override
			public void run()
				{Thread.currentThread().setPriority(RENDERING_PRIORITY);
				tr.getRenderer().updateVisibilityList();
				tr.getCollisionManager().updateVisibilityList();
				}
			}, 0, 1000/RENDERLIST_REFRESH_FPS);
		//CLEANUP
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run()
				{visibilityCalculationTimer.cancel();
				gameplayTimer.cancel();
				}
			});
		}//end constructor
	
	public long getElapsedTimeInMillisSinceLastGameTick(){return timeInMillisSinceLastGameTick;}
	}//end ThreadManager
