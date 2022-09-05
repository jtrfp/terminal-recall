/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.ctl;

import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.GamepadInputDeviceServiceFactory.GamepadInputDeviceService;
import org.jtrfp.trcl.gui.DefaultControllerConfiguration;
import org.springframework.stereotype.Component;

/**
 * Default (initial config) for Saitek Cyborg Evo (controller)
 * control mapping for Terminal Velocity / Fury3
 * @author Chuck Ritola
 *
 */

@Component
public class SaitekCyborgEvoDefaultConfFactory implements FeatureFactory<GamepadInputDeviceService> {
public static class SaitekCyborgEvo extends DefaultControllerConfiguration<GamepadInputDeviceService> {
    public SaitekCyborgEvo(){
	super();
	this.setIntendedController("Saitek Cyborg Evo");
	this.getEntryMap().put ("Button 0",new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,"Button 0"     ,1,0 ));
	//this.getEntryMap().put ("X"     ,new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,"X"     ,1,0 ));
	//this.getEntryMap().put ("Y"     ,new ConfEntry(ViewSelectFactory.INSTRUMENTS_VIEW             ,"Y"     ,1,0 ));
	//this.getEntryMap() .put("Mode"  ,new ConfEntry(SatelliteViewFactory.SATELLITE_TOGGLE           ,"Mode"  ,1,0 ));
	//this.getEntryMap() .put("Select",new ConfEntry(ViewSelectFactory.VIEW                          ,"Select",1,0 ));
	//this.getEntryMap() .put("Start" ,new ConfEntry(GamePauseFactory.PAUSE                         ,"Start" ,1,0 ));
	this.getEntryMap() .put("X Axis"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"X Axis"     ,-1,0));
	this.getEntryMap() .put("Y Axis"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"Y Axis"     ,-1,0));
	this.getEntryMap() .put("Z Axis"     ,new ConfEntry(UserInputThrottleControlBehavior.THROTTLE      ,"Z Axis"     ,-1,0));
	//this.getEntryMap().put("Left Thumb"    , new ConfEntry(RollBehavior.ROLL                ,"Left Thumb"  ,-1,0 ));
	//this.getEntryMap().put("Right Thumb"   , new ConfEntry(RollBehavior.ROLL                ,"Right Thumb" ,1,0 ));
    }//end constructor

    @Override
    public void destruct(GamepadInputDeviceService target) {
	// TODO Auto-generated method stub
	
    }
}//end SaitekCyborgEvo

@Override
public Feature<GamepadInputDeviceService> newInstance(GamepadInputDeviceService target) {
    return new SaitekCyborgEvo();
}

@Override
public Class<GamepadInputDeviceService> getTargetClass() {
    return GamepadInputDeviceService.class;
}

@Override
public Class<SaitekCyborgEvo> getFeatureClass() {
    return SaitekCyborgEvo.class;
}
}//end SaitekCyborgEvoDefaultConfFactory