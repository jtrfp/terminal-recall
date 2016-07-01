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

import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.RollBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory;
import org.jtrfp.trcl.gui.DefaultControllerConfiguration;
import org.jtrfp.trcl.miss.GamePauseFactory;
import org.jtrfp.trcl.miss.SatelliteViewFactory;
import org.springframework.stereotype.Component;

/**
 * Default (initial config) for XBox 360 pad (controller)
 * control mapping for Terminal Velocity / Fury3
 * @author Chuck Ritola
 *
 */

@Component
public class XBox360PadDefaultConfFactory implements FeatureFactory<ControllerMapper> {
public static class XBox360PadDefaultConf extends DefaultControllerConfiguration {
    public XBox360PadDefaultConf(){
	super();
	this.setIntendedController("Microsoft X-Box 360 pad");
	this.getEntryMap().put ("A"     ,new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,"A"     ,1,0 ));
	this.getEntryMap().put ("X"     ,new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,"X"     ,1,0 ));
	this.getEntryMap().put ("Y"     ,new ConfEntry(ViewSelectFactory.INSTRUMENTS_VIEW             ,"Y"     ,1,0 ));
	this.getEntryMap() .put("Mode"  ,new ConfEntry(SatelliteViewFactory.SATELLITE_TOGGLE           ,"Mode"  ,1,0 ));
	this.getEntryMap() .put("Select",new ConfEntry(ViewSelectFactory.VIEW                          ,"Select",1,0 ));
	this.getEntryMap() .put("Start" ,new ConfEntry(GamePauseFactory.PAUSE                         ,"Start" ,1,0 ));
	this.getEntryMap() .put("x"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,"x"     ,-1,0));
	this.getEntryMap() .put("y"     ,new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,"y"     ,-1,0));
	this.getEntryMap() .put("z"     ,new ConfEntry(UserInputThrottleControlBehavior.THROTTLE      ,"z"     ,-1,0));
	this.getEntryMap().put("Left Thumb"    , new ConfEntry(RollBehavior.ROLL                ,"Left Thumb"  ,-1,0 ));
	this.getEntryMap().put("Right Thumb"   , new ConfEntry(RollBehavior.ROLL                ,"Right Thumb" ,1,0 ));
    }//end constructor

    @Override
    public void destruct(ControllerMapper target) {
	// TODO Auto-generated method stub
	
    }
}//end XBox360PadDefaultConf

@Override
public Feature<ControllerMapper> newInstance(ControllerMapper target) {
    return new XBox360PadDefaultConf();
}

@Override
public Class<ControllerMapper> getTargetClass() {
    return ControllerMapper.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return XBox360PadDefaultConf.class;
}
}//end XBox360PadDefaultConfFactory