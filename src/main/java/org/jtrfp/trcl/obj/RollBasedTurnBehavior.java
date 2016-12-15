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

package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;

public class RollBasedTurnBehavior extends Behavior {
    private RollPitchYawBehavior rollBehavior;
    private RotationalMomentumBehavior rotationalMomentumBehavior;
    private double turnFactor    = .001;
    private double pitchUpFactor = .9;

    @Override
    public void tick(long tickTimeMillis){
	final RollPitchYawBehavior rollBehavior = getRollBehavior();
	final RotationalMomentumBehavior rotationalMomentumBehavior 
	                                = getRotationalMomentumBehavior();
	final double currentRollTheta   = rollBehavior.getCurrentRollTheta();
	final double turnFactor         = getTurnFactor();
	final double turnAccelleration  = turnFactor * currentRollTheta;
	final double pitchUpAccelleration = turnAccelleration * getPitchUpFactor();
	
	rotationalMomentumBehavior.accellerateEquatorialMomentum(turnAccelleration);
	rotationalMomentumBehavior.accelleratePolarMomentum(pitchUpAccelleration);
    }//end tick(...)

    public RollPitchYawBehavior getRollBehavior() {
	if(rollBehavior == null)
	    rollBehavior = getParent().probeForBehavior(RollPitchYawBehavior.class);
        return rollBehavior;
    }

    public RollBasedTurnBehavior setRollBehavior(RollPitchYawBehavior rollBehavior) {
        this.rollBehavior = rollBehavior;
        return this;
    }

    public double getTurnFactor() {
        return turnFactor;
    }

    public RollBasedTurnBehavior setTurnFactor(double turnFactor) {
        this.turnFactor = turnFactor;
        return this;
    }

    public RotationalMomentumBehavior getRotationalMomentumBehavior() {
	if(rotationalMomentumBehavior == null)
	    setRotationalMomentumBehavior(getParent().probeForBehavior(RotationalMomentumBehavior.class));
        return rotationalMomentumBehavior;
    }

    public RollBasedTurnBehavior setRotationalMomentumBehavior(
    	RotationalMomentumBehavior rotationalMomentumBehavior) {
        this.rotationalMomentumBehavior = rotationalMomentumBehavior;
        return this;
    }

    public double getPitchUpFactor() {
        return pitchUpFactor;
    }

    public void setPitchUpFactor(double pitchUpFactor) {
        this.pitchUpFactor = pitchUpFactor;
    }
}//end RollBasedTurnBehavior
