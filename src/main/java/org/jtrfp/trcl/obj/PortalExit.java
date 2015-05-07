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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;

public class PortalExit extends WorldObject {
    private final Camera controlledCamera;

    public PortalExit(TR tr, Camera cameraToControl) {
	super(tr);
	this.controlledCamera=cameraToControl;
    }
    
    public void updateObservationParams(double [] relativePosition, Rotation relativeHeadingTop){
	//Apply position
	Vect3D.add(this.getPosition(), relativePosition, controlledCamera.getPosition());
	//Apply vector
	controlledCamera.setHeading(relativeHeadingTop.applyTo(getHeading()));
	controlledCamera.setTop    (relativeHeadingTop.applyTo(getTop()));
	//Update
	controlledCamera.notifyPositionChange();
    }//end updateObservationParams(...)

    /**
     * @return the controlledCamera
     */
    public Camera getControlledCamera() {
        return controlledCamera;
    }

}//end PortalExit
