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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;

public final class World {
    public double sizeX, sizeY, sizeZ, viewDepth, gridBlockSize;
    private final Renderer renderer;
    private static final int blockGranularity = 8;//Dim segs / diameter

    public World(double sizeX, double sizeY, double sizeZ,
	    double cameraViewDepth, TR tr) {
	this.sizeX = sizeX;
	this.sizeY = sizeY;
	this.sizeZ = sizeZ;
	this.gridBlockSize = cameraViewDepth / (double) blockGranularity;
	this.renderer=tr.mainRenderer.get();
	this.viewDepth = cameraViewDepth;
	renderer.getCamera().setViewDepth(cameraViewDepth);
	Camera camera = renderer.getCamera();
	camera.setPosition(new Vector3D(camera.getCameraPosition().getX(),
		sizeY / 3.15, camera.getCameraPosition().getZ()));
    }// end constructor
}// World
