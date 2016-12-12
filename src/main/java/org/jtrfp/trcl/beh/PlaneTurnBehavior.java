/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

public class PlaneTurnBehavior extends Behavior {
 private RotationalMomentumBehavior rotationalMomentumBehavior;
 private double noseUpCompensation = 1.5;
 private double tiltFactor         = 1;
 private double turnFactor         = 0;
 
 @Override
 public void tick(long tickTimeMillis){
     final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
     rotationalMomentumBehavior.accellerateEquatorialMomentum(turnFactor);
     rotationalMomentumBehavior.accellerateLateralMomentum   (-turnFactor * tiltFactor);
     rotationalMomentumBehavior.accelleratePolarMomentum     (Math.abs(turnFactor * tiltFactor * noseUpCompensation));
 }

public RotationalMomentumBehavior getRotationalMomentumBehavior() {
    return rotationalMomentumBehavior;
}

public void setRotationalMomentumBehavior(
	RotationalMomentumBehavior rotationalMomentumBehavior) {
    this.rotationalMomentumBehavior = rotationalMomentumBehavior;
}

public double getNoseUpCompensation() {
    return noseUpCompensation;
}

public void setNoseUpCompensation(double noseUpCompensation) {
    this.noseUpCompensation = noseUpCompensation;
}

public double getTurnFactor() {
    return turnFactor;
}

public void setTurnFactor(double turnFactor) {
    this.turnFactor = turnFactor;
}
}//end PlaneTurnBehavior
