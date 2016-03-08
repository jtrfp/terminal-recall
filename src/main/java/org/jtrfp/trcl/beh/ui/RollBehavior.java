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

package org.jtrfp.trcl.beh.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.miss.Mission;

public class RollBehavior extends Behavior implements ControlBehavior {
    public static final String ROLL = "Roll";
    private double rollStrength = .01;
    private double rollDelta    = 0;
    private final ControllerInput rollInput;
    private final RollInputChangeListener rollInputChangeListener = new RollInputChangeListener();
    
    public RollBehavior(ControllerInputs inputs){
	rollInput = inputs.getControllerInput(ROLL);
	rollInput.addPropertyChangeListener(rollInputChangeListener);
    }
    
    private class RollInputChangeListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final double newValue = (Double)evt.getNewValue();
	    setRollDelta(rollStrength * newValue);
	}
    }//end RollInputChangeListener
    
    @Override
    public void tick(long tickTimeMillis){
	if(isRollAppropriate())
	     getParent().probeForBehavior(RotationalMomentumBehavior.class).accellerateLateralMomentum(getRollDelta());
    }
    
    public double getRollDelta() {
        return rollDelta;
    }
    
    protected boolean isRollAppropriate(){
	final TR tr = getParent().getTr();
	final Object runState = tr.getRunState();
	return runState instanceof Mission.PlayerActivity &&
		!(runState instanceof Mission.SatelliteState);
    }//end isRollAppropriate
    
    public void setRollDelta(double rollDelta) {
        this.rollDelta = rollDelta;
    }
    public double getRollStrength() {
        return rollStrength;
    }
    public void setRollStrength(double rollStrength) {
        this.rollStrength = rollStrength;
    }
}//end RollBehavior
