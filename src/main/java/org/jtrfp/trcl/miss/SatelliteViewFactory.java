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

package org.jtrfp.trcl.miss;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ctl.ControllerSink;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.miss.GamePauseFactory.GamePause;
import org.springframework.stereotype.Component;

@Component
public class SatelliteViewFactory implements FeatureFactory<Mission> {
    public static final String SATELLITE_TOGGLE = "Sat View";
    public static final String [] VIEW_MENU_PATH = new String [] {"View","Satellite"};
    public interface SatelliteViewState extends Mission.OverworldState, GamePauseFactory.PauseDisabledState{};
    
    public SatelliteViewFactory(){
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
    public Class<SatelliteView> getFeatureClass() {
	return SatelliteView.class;
    }
    
    public class SatelliteView implements Feature<Mission> {
	public static final String SATELLITE_VIEW = "satelliteView";
	
	private final   SatelliteViewMenuItemListener menuItemListener = new SatelliteViewMenuItemListener();
	private final   RunStateListener              runStateListener = new RunStateListener();
	private final   SatelliteControlInputListener satelliteControl = new SatelliteControlInputListener();
	private final   PausePropertyChangeListener   pausePropertyChangeListener = new PausePropertyChangeListener();
	private         WeakReference<Mission>        mission;
	private boolean                               satelliteView    = false;
	private boolean                               enabled          = false;
	private final   PropertyChangeSupport         pcs = new PropertyChangeSupport(this);
	private         ControllerSink               satelliteToggleInput;
	private         GamePause                     gamePause;
	private         MenuSystem                    menuSystem;
	
	public MenuSystem getMenuSystem() {
	        return menuSystem;
	    }
	
	@Override
	public void apply(Mission target) {
	    final ControllerMapper mapper = Features.get(Features.getSingleton(), ControllerMapper.class);
	    final ControllerSinks inputs = Features.get(mapper, ControllerSinks.class);
	    satelliteToggleInput = inputs.getSink(SATELLITE_TOGGLE);
	    setMission(target);
	    setGamePause(Features.get(target, GamePause.class));
	    final Frame frame = getTr().getRootWindow();
	    setMenuSystem(Features.get(frame, MenuSystem.class));
	    gamePause.addPropertyChangeListener(GamePauseFactory.PAUSE, pausePropertyChangeListener);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, VIEW_MENU_PATH);
	    menuSystem.addMenuItemListener(menuItemListener, VIEW_MENU_PATH);
	    getTr().addPropertyChangeListener(TRFactory.RUN_STATE, runStateListener);
	    satelliteToggleInput.addPropertyChangeListener(satelliteControl);
	}

	@Override
	public void destruct(Mission target) {
	    gamePause.removePropertyChangeListener(GamePauseFactory.PAUSE, pausePropertyChangeListener);
	    getTr().removePropertyChangeListener(TRFactory.RUN_STATE, runStateListener);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.removeMenuItemListener(menuItemListener, VIEW_MENU_PATH);
	    satelliteToggleInput.removePropertyChangeListener(satelliteControl);
	    menuSystem.removeMenuItem(VIEW_MENU_PATH);
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
		reEvaluateEnabledState();
	    }//end propertyChange(...)
	}//end PropertyChangeListener
	
	private class PausePropertyChangeListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		reEvaluateEnabledState();
	    }
	}//end PausePropertyChangeListener
	
	private void reEvaluateEnabledState(){
	    Object runState = getTr().getRunState();
	    boolean enabled;
	     enabled  = !(runState instanceof Mission.TunnelState);
	     enabled &= !(runState instanceof Mission.ChamberState);
	     enabled &= runState   instanceof Mission.PlayerActivity;
	     enabled &= !getGamePause().isPaused();
	    setEnabled(enabled);
	}//end reEvaluateEnabledState()

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
	    final boolean oldValue = this.satelliteView;
	    if(satelliteView == oldValue)
		return;
	    this.satelliteView = satelliteView;
	    if(satelliteView)
	     getTr().setRunState(new SatelliteViewState(){});
	    else
	     getTr().setRunState(new Mission.OverworldState(){});
	    pcs.firePropertyChange(SATELLITE_VIEW,oldValue,satelliteView);
	    getMission().setSatelliteView(satelliteView);
	}

	public boolean isEnabled() {
	    return enabled;
	}

	public void setEnabled(boolean enabled) {
	    if(this.enabled==enabled)
		return;
	    this.enabled = enabled;
	    getMenuSystem().setMenuItemEnabled(enabled, VIEW_MENU_PATH);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}
	

	    public TR getTr() {
	        return getMission().getTr();
	    }

	    public GamePause getGamePause() {
	        return gamePause;
	    }

	    public void setGamePause(GamePause gamePause) {
	        this.gamePause = gamePause;
	    }

	    public void setMenuSystem(MenuSystem menuSystem) {
	        this.menuSystem = menuSystem;
	    }
    }//end SatelliteView

}//end SatelliteViewFactory
