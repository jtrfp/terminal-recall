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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.ObjectDirection;

public final class World extends RenderableSpacePartitioningGrid
	{
	double sizeX;
	public double sizeY;
	double sizeZ;
	private static final int blockGranularity = 8;// Dim-Segments per diameter. should
	private Color fogColor = Color.black;
	private final TR tr;

	public World(double sizeX, double sizeY, double sizeZ,
			double cameraViewDepth, TR tr)
		{
		super(sizeX,sizeY,sizeZ,cameraViewDepth / (double) blockGranularity,cameraViewDepth);
		this.tr = tr;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		tr.getRenderer().getCamera().setViewDepth(cameraViewDepth);
		Camera camera = tr.getRenderer().getCamera();
		camera.setPosition(new Vector3D(camera.getCameraPosition().getX(),
				sizeY / 3.15, camera.getCameraPosition().getZ()));
		GlobalObjectList.poke();
		}// end constructor

	public void setCameraDirection(ObjectDirection dir)
		{tr.getRenderer().getCamera().setLookAtVector(dir.getHeading());
		tr.getRenderer().getCamera().setUpVector(dir.getTop());}

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
		{if(fogColor==null)throw new NullPointerException("Color is intolerably null.");
	    	this.fogColor = fogColor;}

	/**
	 * @return the tr
	 */
	public TR getTr()
		{return tr;}
	}// World
