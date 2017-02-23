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
package org.jtrfp.trcl.beh.ui;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.ctl.ControllerSink;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.obj.Player;

public class UserInputRudderElevatorControlBehavior extends Behavior implements PlayerControlBehavior {
    public static final String RUDDER   = "Rudder";
    public static final String ELEVATOR = "Elevator";
    
    private final ControllerSink rudder, elevator;
    
    private  double accellerationFactor=.0005;
    public UserInputRudderElevatorControlBehavior(ControllerSinks controllerInputs){
	super();
	rudder =   controllerInputs.getSink(RUDDER);
	elevator = controllerInputs.getSink(ELEVATOR);
    }
    @Override
    public void tick(long tickTimeMillis){
	final Player p = (Player)getParent();
	final RotationalMomentumBehavior rmb = p.probeForBehavior(RotationalMomentumBehavior.class);
	final double elevatorState = elevator.getState();
	final double rudderState   = rudder.getState();
	rmb.accelleratePolarMomentum(-2.*Math.PI*accellerationFactor*1.2*elevatorState);
	//Tilt
	rmb.accellerateLateralMomentum(rudderState*-2.*Math.PI*accellerationFactor*.8);
	//Turn
	rmb.accellerateEquatorialMomentum(rudderState*2*Math.PI*accellerationFactor);
    }//end UserInputRudderElevatorControlBehavior
    /**
     * @return the accellerationFactor
     */
    public double getAccellerationFactor() {
        return accellerationFactor;
    }
    /**
     * @param accellerationFactor the accellerationFactor to set
     */
    public void setAccellerationFactor(double accellerationFactor) {
        this.accellerationFactor = accellerationFactor;
    }
}//end UserInputRudderElevatorControlBeahvior
