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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.objects.WorldObject;

import com.jogamp.opengl.util.glsl.ShaderState;

public final class World implements GLEventListener
	{
	double sizeX, sizeY, sizeZ, cameraViewDepth;

	Vector3D lookAtVector = new Vector3D(0, 0, 1);

	Vector3D upVector = new Vector3D(0, 1, 0);

	private final int blockGranularity = 8;// Dim-Segments per diameter. should
											// exceed 2.

	double gridBlockSize;

	Vector3D cameraPosition = new Vector3D(50000, 0, 50000);

	Color fogColor = Color.black;

	LinkedList<TickListener> tickListeners = new LinkedList<TickListener>();

	TR tr;

	RenderableSpacePartitioningGrid rootGrid;

	GLProgram shaderProgram;

	boolean drawBackdrop = true;

	ShaderState worldShaderState = new ShaderState();

	RealMatrix projectionMatrix;

	RealMatrix cameraMatrix;

	RenderList renderList;

	private long lastTimeMillis;

	private int frameNumber = 0;

	boolean firstRun = true;

	KeyStatus keyStatus;

	public World(double sizeX, double sizeY, double sizeZ,
			double cameraViewDepth, TR tr)
		{
		this.tr = tr;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.cameraViewDepth = cameraViewDepth;
		// Create the grid
		gridBlockSize = cameraViewDepth / (double) blockGranularity;
		rootGrid = new RenderableSpacePartitioningGrid(this)
			{
			};
		setCameraPosition(new Vector3D(getCameraPosition().getX(),
				sizeY / 3.15, getCameraPosition().getZ()));
		GlobalObjectList.poke();
		keyStatus = tr.getKeyStatus();
		}// end constructor

	public void addTickListener(TickListener l)
		{
		tickListeners.add(l);
		}

	public void setCameraDirection(ObjectDirection dir)
		{
		this.setLookAtVector(dir.getHeading());
		upVector = dir.getTop();
		}

	public RenderableSpacePartitioningGrid getRootGrid()
		{
		return rootGrid;
		}

	@Override
	public final void display(GLAutoDrawable drawable)
		{
		Thread.currentThread().setPriority(8);
		GL3 gl = tr.getGPU().takeGL();
		if (firstRun)
			{
			fauxInit(gl);
			return;
			}
		fpsTracking();

		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		// gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		updateCameraMovement();
		calculateCameraMatrix(gl);
		renderVisibleObjects(gl);
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
		Vector3D newCamPos = this.getCameraPosition();
		Vector3D newLookAt = getLookAtVector();
		if (keyStatus.isPressed(KeyEvent.VK_UP))
			{
			newCamPos = newCamPos.add(getLookAtVector().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_DOWN))
			{
			newCamPos = newCamPos.subtract(getLookAtVector().scalarMultiply(
					nudgeUnit * manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_UP))
			{
			newCamPos = newCamPos.add(upVector.scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}
		if (keyStatus.isPressed(KeyEvent.VK_PAGE_DOWN))
			{
			newCamPos = newCamPos.subtract(upVector.scalarMultiply(nudgeUnit
					* manueverSpeed));
			positionChanged = true;
			}

		Rotation turnRot = new Rotation(upVector, angleUnit);

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
			this.setLookAtVector(newLookAt);
		if (positionChanged)
			setCameraPosition(newCamPos);
		}

	private void renderVisibleObjects(GL3 gl)
		{
		// Update GPU
		GlobalDynamicTextureBuffer.getTextureBuffer().map(gl);
		PrimitiveList.tickAnimators();
		// Ticks
		long tickTimeInMillis = System.currentTimeMillis();
		for (TickListener l : tickListeners)
			{l.tick(tickTimeInMillis);}
		rootGrid.itemsWithinRadiusOf(
				getCameraPosition().add(
						lookAtVector.scalarMultiply(cameraViewDepth / 2.1)),
				renderList.getSubmitter());
		renderList.sendToGPU(gl);
		GlobalDynamicTextureBuffer.getTextureBuffer().unmap(gl);
		
		// Render objects
		renderList.render(gl);
		}// renderVisibleObjects

	private void calculateCameraMatrix(GL3 gl)
		{
		// CAMERA
		Vector3D eyeLoc = getCameraPosition();

		Vector3D aZ = getLookAtVector().negate();
		Vector3D aX = upVector.crossProduct(aZ).normalize();
		Vector3D aY = aZ.crossProduct(aX);

		RealMatrix rM = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ -aX.getX(), aY.getX(), aZ.getX(), 0 }, new double[]
				{ -aX.getY(), aY.getY(), aZ.getY(), 0 }, new double[]
				{ -aX.getZ(), aY.getZ(), aZ.getZ(), 0 }, new double[]
				{ 0, 0, 0, 1 } });

		RealMatrix tM = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ 1, 0, 0, -eyeLoc.getX() }, new double[]
				{ 0, 1, 0, -eyeLoc.getY() }, new double[]
				{ 0, 0, 1, -eyeLoc.getZ() }, new double[]
				{ 0, 0, 0, 1 } });

		setCameraMatrix(projectionMatrix.multiply(rM.multiply(tM)));
		}// end calculatateCameraMatrix(...)

	@Override
	public void dispose(GLAutoDrawable arg0)
		{
		}

	@Override
	public void init(GLAutoDrawable drawable)
		{
		}

	private void fauxInit(GL3 gl)
		{
		System.out.println("World.init() start.");
		setupFixedPipelineBehavior(gl);
		ByteArrayOutputStream shaderOS = new ByteArrayOutputStream();
		PrintStream shaderLog = new PrintStream(shaderOS);

		try {
			System.out.println("buildShaderProgram...");
			buildShaderProgram(shaderLog);
			System.out.println("setupProjectionMatrix...");
			setupProjectionMatrix(gl);
			System.out.println("uploadDataToGPU...");
			uploadDataToGPU(gl);
			System.out.println("bindBuffersAndTextures...");
			bindBuffersAndTextures(gl, shaderLog);

			float fogRed = (float) fogColor.getRed() / 255f;
			float fogGreen = (float) fogColor.getGreen() / 255f;
			float fogBlue = (float) fogColor.getBlue() / 255f;
			
			shaderProgram.getUniform(gl,"fogStart").set((float) (cameraViewDepth * 1.2) / 5f);
			shaderProgram.getUniform(gl,"fogEnd").set((float) (cameraViewDepth * 1.5) * 1.3f);
			shaderProgram.getUniform(gl,"fogColor").set(fogRed, fogGreen, fogBlue);
			}
		catch (Exception e)
			{
			shaderLog.flush();
			e.printStackTrace();
			System.exit(1);
			}
		firstRun = false;
		System.out.println("");
		}

	private void bindBuffersAndTextures(GL3 gl, PrintStream shaderLog)
		{try{
			GlobalDynamicTextureBuffer.getTextureBuffer().bindToUniform(gl, 1, shaderProgram, "rootBuffer");
			shaderProgram.getUniform(gl, "textureMap").set((int)0);//Texture unit 0 mapped to textureMap
			}
		catch (RuntimeException e)
			{e.printStackTrace();}
		GLTexture.specifyTextureUnit(gl, 0);
		Texture.getGlobalTexture().bind(gl);
		if(!shaderProgram.validate())
			{
			System.out.println(shaderProgram.getInfoLog());
			System.exit(1);
			}
		System.out.println("Initializing RenderList...");
		renderList = new RenderList(gl, shaderProgram);
		System.out.println("...Done.");
		}
	
	private void uploadDataToGPU(GL3 gl)
		{
		GlobalDynamicTextureBuffer.getTextureBuffer().map(gl);
		System.out.println("Uploading vertex data to GPU...");
		TriangleList.uploadAllListsToGPU(gl);
		System.out.println("...Done.");
		System.out.println("Uploading object defintion data to GPU...");
		WorldObject.uploadAllObjectDefinitionsToGPU();
		System.out.println("...Done.");
		System.out.println("\t...World.init() complete.");
		GlobalDynamicTextureBuffer.getTextureBuffer().unmap(gl);
		}

	private void buildShaderProgram(PrintStream shaderLog)
			throws IOException
		{
		GLVertexShader vertexShader = tr.getGPU().newVertexShader();
		GLFragmentShader fragmentShader = tr.getGPU().newFragmentShader();
		shaderProgram = tr.getGPU().newProgram();
		
		Parser p = new Parser();
		vertexShader.setSource(p.readUTF8FileToString(new File("texturedVertexShader.glsl")));
		fragmentShader.setSource(p.readUTF8FileToString(new File("texturedFragShader.glsl")));
		shaderProgram.attachShader(vertexShader);
		shaderProgram.attachShader(fragmentShader);
		shaderProgram.link();
		shaderProgram.use();
		}

	private void setupProjectionMatrix(GL3 gl)
		{
		final float fov = 70f;// In degrees
		final float aspect = (float) tr.getFrame().getWidth()
				/ (float) tr.getFrame().getHeight();
		final float zF = (float) (cameraViewDepth * 1.5);// 1.5 because the
															// visibility is
															// pushed forward
															// from the lookAt
		final float zN = (float) (TR.mapSquareSize / 10);
		final float f = (float) (1. / Math.tan(fov * Math.PI / 360.));
		projectionMatrix = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ f / aspect, 0, 0, 0 }, new double[]
				{ 0, f, 0, 0 }, new double[]
				{ 0, 0, (zF + zN) / (zN - zF), -1f }, new double[]
				{ 0, 0, (2f * zF * zN) / (zN - zF), 0 } }).transpose();
		}// end setupProjectionMatrix()

	private void setupFixedPipelineBehavior(GL3 gl)
		{
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClearColor(0f, 0f, 0f, 0f);
		}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4)
		{}

	public ObjectDirection getCameraDirection()
		{
		return new ObjectDirection(this.lookAtVector, this.upVector);
		}

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

	/**
	 * @return the lookAtVector
	 */
	public Vector3D getLookAtVector()
		{
		return lookAtVector;
		}

	/**
	 * @param lookAtVector
	 *            the lookAtVector to set
	 */
	public void setLookAtVector(Vector3D lookAtVector)
		{
		this.lookAtVector = lookAtVector;
		}

	/**
	 * @return the cameraPosition
	 */
	public Vector3D getCameraPosition()
		{
		return cameraPosition;
		}

	/**
	 * @param cameraPosition
	 *            the cameraPosition to set
	 */
	public void setCameraPosition(Vector3D cameraPosition)
		{
		this.cameraPosition = cameraPosition;
		}

	/**
	 * @return the cameraMatrix
	 */
	public RealMatrix getCameraMatrix()
		{
		return cameraMatrix;
		}

	/**
	 * @param cameraMatrix
	 *            the cameraMatrix to set
	 */
	private void setCameraMatrix(RealMatrix cameraMatrix)
		{
		this.cameraMatrix = cameraMatrix;
		}
	}// World
