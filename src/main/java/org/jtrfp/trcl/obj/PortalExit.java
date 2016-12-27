/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.obj;

import java.lang.ref.WeakReference;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory.TR;

public class PortalExit extends WorldObject {
    private Camera controlledCamera;
    private final double [] controlledPosition= new double[3];
    private final double [] controlledHeading = new double[3];
    private final double [] controlledTop     = new double[3];
    private WeakReference<SpacePartitioningGrid> rootGrid;

    public PortalExit() {
	super();
    }
    //TODO: Optimize for less garbage
    public synchronized void updateObservationParams(double [] relativePosition, Rotation rotation, Vector3D controllingHeading, Vector3D controllingTop){
	final Camera controlledCamera = getControlledCamera();
	//Apply position
	final Vector3D newPos = rotation.applyTo(new Vector3D(relativePosition)).add(new Vector3D(getPosition()));
	controlledPosition[0] = newPos.getX();
	controlledPosition[1] = newPos.getY();
	controlledPosition[2] = newPos.getZ();
	
	//Apply vector
	final Vector3D newHdg = rotation.applyTo(controllingHeading);
	controlledHeading[0] = newHdg.getX();
	controlledHeading[1] = newHdg.getY();
	controlledHeading[2] = newHdg.getZ();
	
	final Vector3D newTop = rotation.applyTo(controllingTop);
	controlledTop[0]    =  newTop.getX();
	controlledTop[1]    =  newTop.getY();
	controlledTop[2]    =  newTop.getZ();
	
	if(controlledCamera == null)
	    return;
	
	//Update
	controlledCamera.setPosition(newPos);
	controlledCamera.setHeading(newHdg);
	controlledCamera.setTop    (newTop);
	controlledCamera.notifyPositionChange();
    }//end updateObservationParams(...)

    /**
     * @return the controlledCamera
     */
    public Camera getControlledCamera() {
        return controlledCamera;
    }

    public synchronized void activate() {
	if(rootGrid==null)
	    return;
	SpacePartitioningGrid grid = rootGrid.get();
	if(grid!=null)
	 controlledCamera.setRootGrid(grid);
	else
	    throw new IllegalStateException("RootGrid intolerably null.");
    }

    /**
     * @return the rootGrid
     */
    public SpacePartitioningGrid getRootGrid() {
        return rootGrid.get();
    }

    /**
     * @param rootGrid the rootGrid to set
     */
    public synchronized void setRootGrid(SpacePartitioningGrid rootGrid) {
        this.rootGrid = new WeakReference<SpacePartitioningGrid>(rootGrid);
    }

    public synchronized void deactivate() {
	controlledCamera.setRootGrid(null);
	//Do nothing.
    }
    
    @Override
    public boolean supportsLoop(){
	return false;
    }

    public synchronized void setControlledCamera(Camera controlledCamera) {
        this.controlledCamera = controlledCamera;
    }
    public double[] getControlledPosition() {
        return controlledPosition;
    }
    public double[] getControlledHeading() {
        return controlledHeading;
    }
    public double[] getControlledTop() {
        return controlledTop;
    }

}//end PortalExit
