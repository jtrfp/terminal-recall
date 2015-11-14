/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.core;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Inputs {
 
 @Autowired
 public Inputs(ControllerMapper mapper, KeyboardInputDevice kid, TR tr){
     final ControllerInput elevator = tr.getControllerInputs().getInput(UserInputRudderElevatorControlBehavior.ELEVATOR);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_UP)  , elevator, 1, 0);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_DOWN), elevator, -1, 0);
     
     final ControllerInput rudder = tr.getControllerInputs().getInput(UserInputRudderElevatorControlBehavior.RUDDER);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_LEFT) , rudder, 1, 0);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_RIGHT), rudder, -1, 0);
     
     final ControllerInput throttleKeys = tr.getControllerInputs().getInput(UserInputThrottleControlBehavior.THROTTLE_DELTA);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_A) , throttleKeys, 1, 0);
     mapper.mapControllerSourceToInput(kid.getKeyControllerSource(KeyEvent.VK_Z), throttleKeys, -1, 0);
     
 }//end constructor
}//end Inputs
