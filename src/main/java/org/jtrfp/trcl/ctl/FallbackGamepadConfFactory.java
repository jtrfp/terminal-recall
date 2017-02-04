package org.jtrfp.trcl.ctl;

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

import java.util.Map;

import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.RollBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.XBox360PadDefaultConfFactory.XBox360PadDefaultConf;
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
public class FallbackGamepadConfFactory implements FeatureFactory<ControllerMapper> {
public static class FallbackGamepadConf extends DefaultControllerConfiguration {
    public FallbackGamepadConf(){
	super();
	this.setIntendedController("fallback GamepadInputDevice");
	final Map<String,ConfEntry> eMap = getEntryMap();
	final String bA = net.java.games.input.Component.Identifier.Button.A.getName();
	final String bX = net.java.games.input.Component.Identifier.Button.X.getName();
	final String bY = net.java.games.input.Component.Identifier.Button.Y.getName();
	final String bMode = net.java.games.input.Component.Identifier.Button.MODE.getName();
	final String bSelect = net.java.games.input.Component.Identifier.Button.SELECT.getName();
	final String bStart = net.java.games.input.Component.Identifier.Button.START.getName();
	final String analogX = net.java.games.input.Component.Identifier.Axis.X.getName();
	final String analogY = net.java.games.input.Component.Identifier.Axis.Y.getName();
	final String analogZ = net.java.games.input.Component.Identifier.Axis.Z.getName();
	final String bThumbL = net.java.games.input.Component.Identifier.Button.LEFT_THUMB.getName();
	final String bThumbR = net.java.games.input.Component.Identifier.Button.RIGHT_THUMB.getName();
	eMap.put (bA     ,new ConfEntry(UserInputWeaponSelectionBehavior.FIRE          ,bA     ,1,0 ));
	eMap.put (bX     ,new ConfEntry(AfterburnerBehavior.AFTERBURNER                ,bX     ,1,0 ));
	eMap.put (bY     ,new ConfEntry(ViewSelectFactory.INSTRUMENTS_VIEW             ,bY     ,1,0 ));
	eMap .put(bMode  ,new ConfEntry(SatelliteViewFactory.SATELLITE_TOGGLE          ,bMode  ,1,0 ));
	eMap .put(bSelect,new ConfEntry(ViewSelectFactory.VIEW                         ,bSelect,1,0 ));
	eMap .put(bStart ,new ConfEntry(GamePauseFactory.PAUSE                         ,bStart ,1,0 ));
	eMap .put(analogX     ,new ConfEntry(UserInputRudderElevatorControlBehavior.RUDDER  ,analogX     ,-1,0));
	eMap .put(analogY     ,new ConfEntry(UserInputRudderElevatorControlBehavior.ELEVATOR,analogY     ,-1,0));
	eMap .put(analogZ     ,new ConfEntry(UserInputThrottleControlBehavior.THROTTLE      ,analogZ     ,-0.5,0.5));
	eMap.put(bThumbL      , new ConfEntry(RollBehavior.ROLL                ,bThumbL  ,-1,0 ));
	eMap.put(bThumbR      , new ConfEntry(RollBehavior.ROLL                ,bThumbR ,1,0 ));
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