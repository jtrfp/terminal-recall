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
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;

public final class World extends RenderableSpacePartitioningGrid
	{
	double sizeX, sizeY, sizeZ;
	private static final int blockGranularity = 8;// Dim-Segments per diameter. should
	//double gridBlockSize;
	Color fogColor = Color.black;
	private final List<TickListener> tickListeners = new LinkedList<TickListener>();
	TR tr;
	boolean drawBackdrop = true;
	private long lastTimeMillis;
	private int frameNumber = 0;

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
		//rootGrid = new RenderableSpacePartitioningGrid(this){};
		Camera camera = tr.getRenderer().getCamera();
		camera.setCameraPosition(new Vector3D(camera.getCameraPosition().getX(),
				sizeY / 3.15, camera.getCameraPosition().getZ()));
		GlobalObjectList.poke();
		}// end constructor

	public void addTickListener(TickListener l)
		{tickListeners.add(l);}

	public void setCameraDirection(ObjectDirection dir)
		{
		tr.getRenderer().getCamera().setLookAtVector(dir.getHeading());
		tr.getRenderer().getCamera().setUpVector(dir.getTop());
		}

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

	/**
	 * @return the tickListeners
	 */
	public List<TickListener> getTickListeners()
		{
		return tickListeners;
		}
	}// World
