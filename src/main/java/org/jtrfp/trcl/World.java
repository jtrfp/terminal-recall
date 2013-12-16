/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.objects.WorldObject;

import com.jogamp.opengl.util.glsl.ShaderState;

public final class World extends RenderableSpacePartitioningGrid implements GLEventListener
	{
	double sizeX, sizeY, sizeZ;
	private static final int blockGranularity = 8;// Dim-Segments per diameter. should
	//double gridBlockSize;
	Color fogColor = Color.black;
	LinkedList<TickListener> tickListeners = new LinkedList<TickListener>();
	TR tr;
	RenderableSpacePartitioningGrid rootGrid;
	boolean drawBackdrop = true;

	private long lastTimeMillis;

	private int frameNumber = 0;

	boolean firstRun = true;

	KeyStatus keyStatus;
	
	//private final Renderer renderer;

	public World(double sizeX, double sizeY, double sizeZ,
			double cameraViewDepth, TR tr)
		{
		super(sizeX,sizeY,sizeZ,cameraViewDepth / (double) blockGranularity,cameraViewDepth);
		this.tr = tr;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		//this.camera=new Camera(tr);
		tr.getGPU().takeGL();
		tr.getRenderer().getCamera().setViewDepth(cameraViewDepth);
		tr.getGPU().releaseGL();
		// Create the grid
		rootGrid = new RenderableSpacePartitioningGrid(this){};
		tr.getRenderer().setRootGrid(rootGrid);
		Camera camera = tr.getRenderer().getCamera();
		camera.setCameraPosition(new Vector3D(camera.getCameraPosition().getX(),
				sizeY / 3.15, camera.getCameraPosition().getZ()));
		GlobalObjectList.poke();
		keyStatus = tr.getKeyStatus();
		}// end constructor

	public void addTickListener(TickListener l)
		{
		tickListeners.add(l);
		}

	public void setCameraDirection(ObjectDirection dir)
		{
		tr.getRenderer().getCamera().setLookAtVector(dir.getHeading());
		tr.getRenderer().getCamera().setUpVector(dir.getTop());
		}

	public RenderableSpacePartitioningGrid getRootGrid()
		{return rootGrid;}

	@Override
	public final void display(GLAutoDrawable drawable)
		{
		Thread.currentThread().setPriority(8);
		fpsTracking();
		
		updateCameraMovement();
		// Update GPU
		GlobalDynamicTextureBuffer.getTextureBuffer().map();
		PrimitiveList.tickAnimators();
		// Ticks
		long tickTimeInMillis = System.currentTimeMillis();
		for (TickListener l : tickListeners)
			{l.tick(tickTimeInMillis);}
		tr.getRenderer().render();
		}// end display()

	private void fpsTracking()
		{
		frameNumber++;
		if ((frameNumber %= 20) == 0)
			{
			System.out
					.println((1000. / (double) (System.currentTimeMillis() - lastTimeMillis))
							+ " FPS");
			}
		lastTimeMillis = System.currentTimeMillis();
		}

	private void updateCameraMovement()
		{
		final double manueverSpeed = 20. / (double) tr.getGPU().getFrameRate();
		final double nudgeUnit = TR.mapSquareSize / 9.;
		final double angleUnit = Math.PI * .015 * manueverSpeed;

		boolean positionChanged = false, lookAtChanged = false;
		// double
		// newX=getCameraPosition().getX(),newY=getCameraPosition().getY(),newZ=getCameraPosition().getZ();
		final Camera camera = tr.getRenderer().getCamera();
		Vector3D newCamPos = camera.getCameraPosition();
		Vector3D newLookAt = camera.getLookAtVector();
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
	
	@Override
	public void dispose(GLAutoDrawable arg0)
		{
		}

	@Override
	public void init(GLAutoDrawable drawable)
		{
		}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4)
		{}

	/*public ObjectDirection getCameraDirection()
		{
		return new ObjectDirection(camera.getLookAtVector(), camera.getUpVector());
		}*/

	/**
	 * @return the fogColor
	 */
	public Color getFogColor()
		{return fogColor;}

	/**
	 * @param fogColor
	 *            the fogColor to set
	 */
	public void setFogColor(Color fogColor)
		{this.fogColor = fogColor;}

	/**
	 * @return the tr
	 */
	public TR getTr()
		{
		return tr;
		}

	/**
	 * @param tr
	 *            the tr to set
	 */
	public void setTr(TR tr)
		{
		this.tr = tr;
		}
	}// World
