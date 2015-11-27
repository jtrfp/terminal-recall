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

package org.jtrfp.trcl.io;

import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.ext.tr.GamePause;
import org.jtrfp.trcl.ext.tr.ViewSelect;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.jtrfp.trcl.gui.SatelliteViewToggle;
import org.springframework.stereotype.Component;


/**
 * Default (initial config) for Keyboard
 * control mapping for Terminal Velocity / Fury3
 * @author Chuck Ritola
 *
 */

@Component
public class KeyboardDefaultConf extends ControllerConfiguration {

    public KeyboardDefaultConf(){
	super();
	this.setIntendedController("Keyboard");
	
	this.getEntryMap().put("SPACE", new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,"SPACE",1,0 ));
	this.getEntryMap().put("TAB"  , new ConfEntry(SatelliteViewToggle.SATELLITE_TOGGLE           ,"TAB"  ,1,0 ));
	this.getEntryMap().put("LEFT" , new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"LEFT" ,1,0 ));
	this.getEntryMap().put("RIGHT", new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"RIGHT",-1,0));
	this.getEntryMap().put("UP"   , new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"UP"   ,1,0 ));
	this.getEntryMap().put("DOWN" , new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"DOWN" ,-1,0));
	this.getEntryMap().put("A"    , new ConfEntry(UserInputThrottleControlBehavior.THROTTLE_DELTA,"A"    ,1,0 ));
	this.getEntryMap().put("Z"    , new ConfEntry(UserInputThrottleControlBehavior.THROTTLE_DELTA,"Z"    ,-1,0));
	this.getEntryMap().put("P"    , new ConfEntry(GamePause.PAUSE                                ,"P"    ,1,0 ));
	this.getEntryMap().put("V"    , new ConfEntry(ViewSelect.VIEW                                ,"V"    ,1,0 ));
	this.getEntryMap().put("F"    , new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,"F"    ,1,0 ));
    }//end constructor
}//end KeyboardDefaultConf
