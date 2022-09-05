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

package org.jtrfp.trcl.ctl;

import org.jtrfp.trcl.BriefingScreen;
import org.jtrfp.trcl.Crosshairs;
import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.RollBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.KeyboardInputDeviceServiceFactory.KeyboardInputDeviceService;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory;
import org.jtrfp.trcl.gui.DefaultControllerConfiguration;
import org.jtrfp.trcl.miss.GamePauseFactory;
import org.jtrfp.trcl.miss.SatelliteViewFactory;
import org.springframework.stereotype.Component;


/**
 * Default (initial config) for Keyboard
 * control mapping for Terminal Velocity / Fury3
 * @author Chuck Ritola
 *
 */

@Component
public class KeyboardDefaultConfFactory implements FeatureFactory<KeyboardInputDeviceService> {
    public static class KeyboardDefaultConf extends DefaultControllerConfiguration<KeyboardInputDeviceService> {

	public KeyboardDefaultConf(){
	    super();
	    this.setIntendedController("Keyboard");

	    this.getEntryMap().put("SPACE", new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,"SPACE",1,0 ));
	    this.getEntryMap().put("TAB"  , new ConfEntry(SatelliteViewFactory.SATELLITE_TOGGLE           ,"TAB"  ,1,0 ));
	    this.getEntryMap().put("LEFT" , new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"LEFT" ,1,0 ));
	    this.getEntryMap().put("RIGHT", new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"RIGHT",-1,0));
	    this.getEntryMap().put("UP"   , new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"UP"   ,1,0 ));
	    this.getEntryMap().put("DOWN" , new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"DOWN" ,-1,0));
	    this.getEntryMap().put("A"    , new ConfEntry(UserInputThrottleControlBehavior.THROTTLE_DELTA,"A"    ,1,0 ));
	    this.getEntryMap().put("Z"    , new ConfEntry(UserInputThrottleControlBehavior.THROTTLE_DELTA,"Z"    ,-1,0));
	    this.getEntryMap().put("P"    , new ConfEntry(GamePauseFactory.PAUSE                                ,"P"    ,1,0 ));
	    this.getEntryMap().put("F3"    , new ConfEntry(GamePauseFactory.PAUSE                                ,"F3"  ,1,0 ));
	    this.getEntryMap().put("V"    , new ConfEntry(ViewSelectFactory.VIEW                                ,"V"    ,1,0 ));
	    this.getEntryMap().put("C"    , new ConfEntry(ViewSelectFactory.INSTRUMENTS_VIEW                    ,"C"    ,1,0 ));
	    this.getEntryMap().put("F"    , new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,"F"    ,1,0 ));
	    this.getEntryMap().put("HOME"    , new ConfEntry(RollBehavior.ROLL                ,"HOME"    ,-1,0 ));
	    this.getEntryMap().put("PAGE_UP" , new ConfEntry(RollBehavior.ROLL                ,"PAGE_UP"    ,1,0 ));
	    this.getEntryMap().put("ENTER", new ConfEntry(BriefingScreen.NEXT_SCREEN_CTL      ,"ENTER"    ,1,0 ));
	    this.getEntryMap().put("X"    , new ConfEntry(Crosshairs.TOGGLE_CROSSHAIRS        ,"X"    ,1,0 ));
	}//end constructor

	@Override
	public void destruct(KeyboardInputDeviceService target) {
	    // TODO Auto-generated method stub

	}
    }//end KeyboardDefaultConf

    @Override
    public Feature<KeyboardInputDeviceService> newInstance(KeyboardInputDeviceService target) {
	return new KeyboardDefaultConf();
    }

    @Override
    public Class<KeyboardInputDeviceService> getTargetClass() {
	return KeyboardInputDeviceService.class;
    }

    @Override
    public Class<KeyboardDefaultConf> getFeatureClass() {
	return KeyboardDefaultConf.class;
    }
}//end KeyboardDefaultConfFactory