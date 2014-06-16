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

import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.obj.WorldObject;

public class SpinAccellerationBehavior extends Behavior {
    private double spinAccelleration=.004;
    private SpinMode spinMode=SpinMode.EQUATORIAL;
    
    @Override
    public void _tick(long millis){
	final WorldObject p = getParent();
	final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	switch(spinMode){
	 case LATERAL:rmb.accellerateLateralMomentum(spinAccelleration);break;
	 case POLAR:rmb.accelleratePolarMomentum(spinAccelleration);break;
	 case EQUATORIAL:rmb.accellerateEquatorialMomentum(spinAccelleration);break;
	 }//end switch(spinMode)
    }//end _tick

public enum SpinMode{
    POLAR,
    EQUATORIAL,
    LATERAL
 }//end SpinMode

/**
 * @return the spinAccelleration
 */
public double getSpinAccelleration() {
    return spinAccelleration;
}

/**
 * @param spinAccelleration the spinAccelleration to set
 */
public SpinAccellerationBehavior setSpinAccelleration(double spinAccelleration) {
    this.spinAccelleration = spinAccelleration;
    return this;
}

/**
 * @return the spinMode
 */
public SpinMode getSpinMode() {
    return spinMode;
}

/**
 * @param spinMode the spinMode to set
 */
public SpinAccellerationBehavior setSpinMode(SpinMode spinMode) {
    this.spinMode = spinMode;
    return this;
}
}//end SpinAlongHeading
