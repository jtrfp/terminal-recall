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

package org.jtrfp.trcl.flow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.ControllerInput;
import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SatelliteViewFactory implements FeatureFactory<Mission> {
    public static final String SATELLITE_TOGGLE = "Sat View";
    public static final String [] VIEW_MENU_PATH = new String [] {"View","Satellite"};
    private final ControllerInput satelliteToggleInput;
    private final MenuSystem menuSystem;
    private final TR tr;
    
    @Autowired
    public SatelliteViewFactory(TR tr, MenuSystem menuSystem, ControllerInputs inputs){
	this.menuSystem=menuSystem;
	this.tr = tr;
	satelliteToggleInput = inputs.getControllerInput(SATELLITE_TOGGLE);
    }

    @Override
    public Feature<Mission> newInstance(Mission target) {
	return new SatelliteView();
    }

    @Override
    public Class<Mission> getTargetClass() {
	return Mission.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SatelliteView.class;
    }

    public MenuSystem getMenuSystem() {
        return menuSystem;
    }
    
    class SatelliteView implements Feature<Mission> {
	private final   SatelliteViewMenuItemListener menuItemListener = new SatelliteViewMenuItemListener();
	private final   RunStateListener              runStateListener = new RunStateListener();
	private final   SatelliteControlInputListener satelliteControl = new SatelliteControlInputListener();
	private         WeakReference<Mission>        mission;
	private boolean                               satelliteView    = false;
	private boolean                               enabled          = false;
	
	@Override
	public void apply(Mission target) {
	    setMission(target);
	    menuSystem.addMenuItem(VIEW_MENU_PATH);
	    menuSystem.addMenuItemListener(menuItemListener, VIEW_MENU_PATH);
	    tr.addPropertyChangeListener(TR.RUN_STATE, runStateListener);
	    satelliteToggleInput.addPropertyChangeListener(satelliteControl);
	}

	@Override
	public void destruct(Mission target) {
	    menuSystem.removeMenuItemListener(menuItemListener, VIEW_MENU_PATH);
	    menuSystem.removeMenuItem(VIEW_MENU_PATH);
	    tr.removePropertyChangeListener(runStateListener);
	    satelliteToggleInput.removePropertyChangeListener(satelliteControl);
	}
	
	private class SatelliteViewMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		setSatelliteView(!isSatelliteView());
	    }
	}//end SatelliteViewListener
	
	private class SatelliteControlInputListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final double newValue = (Double)evt.getNewValue();
		if(isEnabled() && newValue >= .7)
		    setSatelliteView(!isSatelliteView());
	    }//end propertyChange(...)
	}//end SatelliteControlInputListener
	
	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		boolean enabled;
		enabled  = !(newValue instanceof Mission.TunnelState);
		enabled &= !(newValue instanceof Mission.ChamberState);
		enabled &= newValue   instanceof Mission.PlayerActivity;
		setEnabled(enabled);
	    }//end propertyChange(...)
	}//end PropertyChangeListener

	public Mission getMission() {
	    return mission.get();
	}

	public void setMission(Mission mission) {
	    this.mission = new WeakReference<Mission>(mission);
	}

	public boolean isSatelliteView() {
	    return satelliteView;
	}

	public void setSatelliteView(boolean satelliteView) {
	    this.satelliteView = satelliteView;
	    getMission().setSatelliteView(satelliteView);
	}

	public boolean isEnabled() {
	    return enabled;
	}

	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	    menuSystem.setMenuItemEnabled(enabled, VIEW_MENU_PATH);
	}
    }//end Mission

    public TR getTr() {
        return tr;
    }

}//end SatelliteViewFactory
