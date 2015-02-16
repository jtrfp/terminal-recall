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

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.ObjectDirection;

public final class World extends RenderableSpacePartitioningGrid {
    double sizeX;
    public double sizeY;
    double sizeZ;
    private final Renderer renderer;
    //private final AtomicBoolean	visibilityRefreshAlreadyRequested = new AtomicBoolean();
    private static final int blockGranularity = 8;// Dim-Segments per diameter.
						  // should
    private Color fogColor = Color.black;
    private final TR tr;

    public World(double sizeX, double sizeY, double sizeZ,
	    double cameraViewDepth, TR tr) {
	super(sizeX, sizeY, sizeZ, cameraViewDepth / (double) blockGranularity,
		cameraViewDepth);
	this.tr = tr;
	this.sizeX = sizeX;
	this.sizeY = sizeY;
	this.sizeZ = sizeZ;
	this.renderer=tr.mainRenderer.get();
	renderer.getCamera().setViewDepth(cameraViewDepth);
	Camera camera = renderer.getCamera();
	camera.setPosition(new Vector3D(camera.getCameraPosition().getX(),
		sizeY / 3.15, camera.getCameraPosition().getZ()));
    }// end constructor

    public void setCameraDirection(ObjectDirection dir) {
	renderer.getCamera().setLookAtVector(dir.getHeading());
	renderer.getCamera().setUpVector(dir.getTop());
    }
    
    @Override
    public void notifyBranchAdded(SpacePartitioningGrid b){//TODO: Optimized branch append.
	notifyBranchRemoved(b);
    }//end notifyBranchAdded(...)
    
    @Override
    public void notifyBranchRemoved(SpacePartitioningGrid b){
	//tr.getThreadManager().visibilityCalc();
	/*
	if(!visibilityRefreshAlreadyRequested.getAndSet(true)){
	    tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    tr.getThreadManager().visibilityCalc();
		    return null;
		}});
	}//end if(!alreadyRequested)
	*/
    }//end notifyBranchRemoved(...)

    /**
     * @return the fogColor
     */
    public Color getFogColor() {
	return fogColor;
    }

    /**
     * @return the tr
     */
    public TR getTr() {
	return tr;
    }
}// World
