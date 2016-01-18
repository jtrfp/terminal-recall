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
import org.jtrfp.trcl.core.TR;

public class PortalExit extends WorldObject {
    private Camera controlledCamera;
    private WeakReference<SpacePartitioningGrid> rootGrid;

    public PortalExit(TR tr) {
	super(tr);
    }
    
    public void updateObservationParams(double [] relativePosition, Rotation rotation, Vector3D controllingHeading, Vector3D controllingTop){
	//Apply position
	controlledCamera.setPosition(rotation.applyTo(new Vector3D(relativePosition)).add(new Vector3D(getPosition())));
	//Apply vector
	controlledCamera.setHeading(rotation.applyTo(controllingHeading));
	controlledCamera.setTop    (rotation.applyTo(controllingTop));
	//Update
	controlledCamera.notifyPositionChange();
    }//end updateObservationParams(...)

    /**
     * @return the controlledCamera
     */
    public Camera getControlledCamera() {
        return controlledCamera;
    }

    public void activate() {
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
    public void setRootGrid(SpacePartitioningGrid rootGrid) {
        this.rootGrid = new WeakReference<SpacePartitioningGrid>(rootGrid);
    }

    public void deactivate() {
	controlledCamera.setRootGrid(null);
	//Do nothing.
    }
    
    @Override
    public boolean supportsLoop(){
	return false;
    }

    public void setControlledCamera(Camera controlledCamera) {
        this.controlledCamera = controlledCamera;
    }

}//end PortalExit
