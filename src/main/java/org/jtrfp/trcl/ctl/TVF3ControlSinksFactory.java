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

import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.RollBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory;
import org.jtrfp.trcl.miss.GamePauseFactory;
import org.jtrfp.trcl.miss.SatelliteViewFactory;
import org.springframework.stereotype.Component;

@Component
public class TVF3ControlSinksFactory
	implements FeatureFactory<ControllerSinks> {
    
    public static class TVF3ControlSinks implements Feature<ControllerSinks> {

	@Override
	public void apply(ControllerSinks target) {
	    target.getSink(UserInputWeaponSelectionBehavior.FIRE);
	    target.getSink(AfterburnerBehavior.AFTERBURNER);
	    target.getSink(ViewSelectFactory.INSTRUMENTS_VIEW);
	    target.getSink(SatelliteViewFactory.SATELLITE_TOGGLE);
	    target.getSink(ViewSelectFactory.VIEW);
	    target.getSink(GamePauseFactory.PAUSE);
	    target.getSink(UserInputRudderElevatorControlBehavior.RUDDER);
	    target.getSink(UserInputRudderElevatorControlBehavior.ELEVATOR);
	    target.getSink(UserInputThrottleControlBehavior.THROTTLE);
	    target.getSink(RollBehavior.ROLL);
	}

	@Override
	public void destruct(ControllerSinks target) {}
	
    }//end TVF3ControlSinks

    @Override
    public Feature<ControllerSinks> newInstance(ControllerSinks target)
	    throws FeatureNotApplicableException {
	return new TVF3ControlSinks();
    }

    @Override
    public Class<ControllerSinks> getTargetClass() {
	return ControllerSinks.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TVF3ControlSinks.class;
    }

}//end TVF3ControlSinksFactory
