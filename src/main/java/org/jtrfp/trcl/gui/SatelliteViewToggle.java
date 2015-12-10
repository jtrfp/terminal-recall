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

package org.jtrfp.trcl.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.ControllerInput;
import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.ControllerMapper;
import org.jtrfp.trcl.core.TR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SatelliteViewToggle {
    public static final String SATELLITE_TOGGLE = "Sat View";
    private final ControllerInput satelliteToggleInput;
    //private final JCheckBoxMenuItem view_sat;

    @Autowired
    public SatelliteViewToggle(TR tr, ControllerMapper mapper, ControllerInputs inputs){
	satelliteToggleInput = inputs.getControllerInput(SATELLITE_TOGGLE);
	//satelliteToggleInput.addPropertyChangeListener(new SatelliteToggleListener());
	final MenuSystem menuSystem = tr.getMenuSystem();
	//view_sat = menuSystem.getView_sat();
    }//end constructor
    
    /*
    private class SatelliteToggleListener implements PropertyChangeListener{
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    if(view_sat.isEnabled() && ((Double)evt.getNewValue())>.9)
			    view_sat.doClick();
		}});
	}//end propertyChange(...)
    }//end SatelliteToggleListener
    */
}//end SatelliteViewToggle
