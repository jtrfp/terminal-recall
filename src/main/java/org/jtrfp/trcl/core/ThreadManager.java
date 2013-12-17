package org.jtrfp.trcl.core;

import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.TickListener;
import org.jtrfp.trcl.objects.WorldObject;

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
	private final Timer renderListRefreshTimer = new Timer("RenderListRefreshTimer");
	public static final Object GAME_OBJECT_MODIFICATION_LOCK = new Object();
	
	ThreadManager(TR tr)
		{
		this.tr=tr;
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
	
	public void start()
		{
		renderingAnimator.start();
		gameplayTimer.scheduleAtFixedRate(new TimerTask()
			{@Override
			public void run()
				{
				Thread.currentThread().setPriority(GAMEPLAY_PRIORITY);
				// Ticks
				long tickTimeInMillis = System.currentTimeMillis();
				synchronized(GAME_OBJECT_MODIFICATION_LOCK)
					{updateCameraMovement();
					for (TickListener l : ThreadManager.this.tr.getWorld().getTickListeners())
						{l.tick(tickTimeInMillis);}
					}
				}//end run()
			}, 0, 1000/GAMEPLAY_FPS);
		renderListRefreshTimer.scheduleAtFixedRate(new TimerTask()
			{@Override
			public void run()
				{Thread.currentThread().setPriority(RENDERING_PRIORITY);
				tr.getRenderer().updateVisibilityList();
				}
			}, 0, 1000/RENDERLIST_REFRESH_FPS);
		//CLEANUP
		Runtime.getRuntime().addShutdownHook(new Thread()
			{
			@Override
			public void run()
				{renderListRefreshTimer.cancel();
				gameplayTimer.cancel();
				}
			});
		}//end constructor
	
	private void updateCameraMovement()
		{
		final double manueverSpeed = 20. / (double) ThreadManager.RENDER_FPS;
		final double nudgeUnit = TR.mapSquareSize / 9.;
		final double angleUnit = Math.PI * .015 * manueverSpeed;
		
		boolean positionChanged = false, lookAtChanged = false;
		// double
		// newX=getCameraPosition().getX(),newY=getCameraPosition().getY(),newZ=getCameraPosition().getZ();
		final Camera camera = tr.getRenderer().getCamera();
		Vector3D newCamPos = camera.getCameraPosition();
		Vector3D newLookAt = camera.getLookAtVector();
		final KeyStatus keyStatus = tr.getKeyStatus();
		if (keyStatus.isPressed(KeyEvent.VK_UP))
			{
			newCamPos = newCamPos.add(camera.getLookAtVector().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_DOWN))
			{
			newCamPos = newCamPos.subtract(camera.getLookAtVector().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_UP))
			{
			newCamPos = newCamPos.add(camera.getUpVector().scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_DOWN))
			{
			newCamPos = newCamPos.subtract(camera.getUpVector().scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}

		Rotation turnRot = new Rotation(camera.getUpVector(), angleUnit);

		if (keyStatus.isPressed(KeyEvent.VK_LEFT))
			{
			newLookAt = turnRot.applyInverseTo(newLookAt);
			lookAtChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_RIGHT))
			{
			newLookAt = turnRot.applyTo(newLookAt);
			lookAtChanged = true;
			}

		// Loop correction
		if (WorldObject.LOOP)
			{
			if (newCamPos.getX() > TR.mapWidth)
				newCamPos = newCamPos.subtract(new Vector3D(TR.mapWidth, 0, 0));
			if (newCamPos.getY() > TR.mapWidth)
				newCamPos = newCamPos.subtract(new Vector3D(0, TR.mapWidth, 0));
			if (newCamPos.getZ() > TR.mapWidth)
				newCamPos = newCamPos.subtract(new Vector3D(0, 0, TR.mapWidth));

			if (newCamPos.getX() < 0)
				newCamPos = newCamPos.add(new Vector3D(TR.mapWidth, 0, 0));
			if (newCamPos.getY() < 0)
				newCamPos = newCamPos.add(new Vector3D(0, TR.mapWidth, 0));
			if (newCamPos.getZ() < 0)
				newCamPos = newCamPos.add(new Vector3D(0, 0, TR.mapWidth));
			}

		if (lookAtChanged)
			camera.setLookAtVector(newLookAt);
		if (positionChanged)
			camera.setCameraPosition(newCamPos);
		}
	}//end ThreadManager
