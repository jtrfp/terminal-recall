/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;

public final class World {
    public static final ExecutorService relevanceExecutor = new VerboseExecutorService(Executors.newSingleThreadExecutor());
    protected static Future<Thread> relevanceThread = relevanceExecutor.submit(new Callable<Thread>(){
	@Override
	public Thread call() throws Exception {
	    return Thread.currentThread();
	}});
    public double sizeX, sizeY, sizeZ, viewDepth, gridBlockSize, cameraViewDepth;
    private final TR tr;
    private static final int blockGranularity       = 8;//Dim segs / diameter //TODO: Remove
    public static final int CUBE_GRANULARITY        = (int)(TRFactory.mapSquareSize*8);
    public static final int WORLD_WIDTH_CUBES       = (int)Math.round(TRFactory.mapWidth / CUBE_GRANULARITY);
    public static final Vector3D RELEVANT_EVERYWHERE= Vector3D.NaN;

    public World(double sizeX, double sizeY, double sizeZ,
	    double cameraViewDepth, TR tr) {
	this.sizeX = sizeX;
	this.sizeY = sizeY;
	this.sizeZ = sizeZ;
	this.tr    = tr;
	this.cameraViewDepth = cameraViewDepth;
	this.gridBlockSize   = cameraViewDepth / (double) blockGranularity;
	this.viewDepth       = cameraViewDepth;
	
    }// end constructor
    
    public RootGrid newRootGrid(){
	return new RootGrid(sizeX,sizeY,sizeZ,gridBlockSize,cameraViewDepth);
    }
    
    public Camera newCamera(){
	final Camera camera = new Camera(tr);
	camera.setViewDepth(cameraViewDepth);
	camera.setPosition(new Vector3D(camera.getCameraPosition().getX(),
		sizeY / 3.15, camera.getCameraPosition().getZ()));
	return camera;
    }

    public int getCubeGranularity() {
	return CUBE_GRANULARITY;
    }
}// World
