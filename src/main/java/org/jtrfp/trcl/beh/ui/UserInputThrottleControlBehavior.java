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
package org.jtrfp.trcl.beh.ui;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.obj.Propelled;

public class UserInputThrottleControlBehavior extends Behavior implements PlayerControlBehavior {
    private double nudgeUnit = 40000;
    public static final String THROTTLE_DELTA= "Throttle Delta";
    public static final String THROTTLE      = "Throttle";
    private final ControllerInput throttleDelta,throttle;
    
    public UserInputThrottleControlBehavior(ControllerInputs controllerInputs){
	throttleDelta= controllerInputs.getControllerInput(THROTTLE_DELTA);
	throttle     = controllerInputs.getControllerInput(THROTTLE);
    }
    
    @Override
    public void tick(long timeInMillis){
	Propelled p=getParent().probeForBehavior(Propelled.class);
	final double range = p.getMaxPropulsion()-p.getMinPropulsion();
	double propulsion = p.getPropulsion();
	double throt = throttle.getState();
	if(throt!=0)
	    propulsion = range*throt+p.getMinPropulsion();
	else{
	    propulsion += nudgeUnit*throttleDelta.getState();
	}
	p.setPropulsion(propulsion);
    }//end _tick(...)
    /**
     * @return the nudgeUnit
     */
    public double getNudgeUnit() {
        return nudgeUnit;
    }
    /**
     * @param nudgeUnit the nudgeUnit to set
     */
    public void setNudgeUnit(double nudgeUnit) {
        this.nudgeUnit = nudgeUnit;
    }
}//end UseInputThrottleControlBehavior
