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
package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class SteadilyRotating extends Behavior {
    private double rotationPeriodMillis=10*1000;
    private double rotationPhase=0;
    @Override
    public void _tick(long tickTime){
	final WorldObject thisObject=getParent();
	final double theta=((double)tickTime/rotationPeriodMillis)*Math.PI*2+rotationPhase;
	thisObject.setHeading(new Vector3D(
		Math.sin(theta),
		0,
		Math.cos(theta)));
    }//end _tick(...)
    /**
     * @return the rotationPeriodMillis
     */
    public double getRotationPeriodMillis() {
        return rotationPeriodMillis;
    }
    /**
     * @param rotationPeriodMillis the rotationPeriodMillis to set
     */
    public SteadilyRotating setRotationPeriodMillis(double rotationPeriodMillis) {
        this.rotationPeriodMillis = rotationPeriodMillis;
        return this;
    }
    public SteadilyRotating setRotationPhase(double phaseInRadians) {
	rotationPhase=phaseInRadians;
	return this;
    }
}//end SlowlyRotating
