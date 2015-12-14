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
import org.jtrfp.trcl.ext.tr.GamePauseFactory;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.jtrfp.trcl.miss.SatelliteViewFactory;
import org.springframework.stereotype.Component;

/**
 * Default (initial config) for XBox 360 pad (controller)
 * control mapping for Terminal Velocity / Fury3
 * @author Chuck Ritola
 *
 */

@Component
public class XBox360PadDefaultConf extends ControllerConfiguration {
    public XBox360PadDefaultConf(){
	super();
	this.setIntendedController("Microsoft X-Box 360 pad");
	this.getEntryMap().put ("A"     ,new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,"A"     ,1,0 ));
	this.getEntryMap().put ("X"     ,new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,"X"     ,1,0 ));
	this.getEntryMap() .put("Mode"  ,new ConfEntry(SatelliteViewFactory.SATELLITE_TOGGLE           ,"Mode"  ,1,0 ));
	this.getEntryMap() .put("Select",new ConfEntry(ViewSelectFactory.VIEW                          ,"Select",1,0 ));
	this.getEntryMap() .put("Start" ,new ConfEntry(GamePauseFactory.PAUSE                         ,"Start" ,1,0 ));
	this.getEntryMap() .put("x"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"x"     ,-1,0));
	this.getEntryMap() .put("y"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"y"     ,-1,0));
	this.getEntryMap() .put("z"     ,new ConfEntry(UserInputThrottleControlBehavior.THROTTLE      ,"z"     ,-1,0));
    }//end constructor
}//end XBox360PadDefaultConf
