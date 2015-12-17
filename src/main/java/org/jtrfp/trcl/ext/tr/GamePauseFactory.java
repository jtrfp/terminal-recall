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
package org.jtrfp.trcl.ext.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.SatelliteViewFactory.SatelliteView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GamePauseFactory implements FeatureFactory<Mission>  {
    private final TR tr;
    public static final String PAUSE = "Pause";
    public static final String [] PAUSE_MENU_PATH = new String[] {"Game","Pause"}; 
    private final ControllerInput pause;
    private final MenuSystem menuSystem;
    public interface PauseDisabledState{}
    
    @Autowired
    public GamePauseFactory(TR tr, ControllerInputs inputs, MenuSystem menuSystem){
	this.tr         = tr;
	pause           = inputs.getControllerInput(PAUSE);
	this.menuSystem = menuSystem;
    }//end constructor'
    
    public class GamePause implements Feature<Mission>{
	private boolean paused = false;
	private final ControllerListener    controllerListener       = new ControllerListener();
	private final MenuSelectionListener menuSelectionListener    = new MenuSelectionListener();
	private final RunStateListener      runStateListener         = new RunStateListener();
	private WeakReference<Mission>      mission;
	private final PropertyChangeSupport pcs                      = new PropertyChangeSupport(this);

	@Override
	public void apply(Mission mission) {
	    pause.addPropertyChangeListener(controllerListener);
	    menuSystem.addMenuItem(PAUSE_MENU_PATH);
	    menuSystem.addMenuItemListener(menuSelectionListener, PAUSE_MENU_PATH);
	    tr.addPropertyChangeListener(TR.RUN_STATE, runStateListener);
	    this.mission = new WeakReference<Mission>(mission);
	}

	@Override
	public void destruct(Mission target) {
	    tr.removePropertyChangeListener(TR.RUN_STATE, runStateListener);
	    menuSystem.removeMenuItemListener(menuSelectionListener, PAUSE_MENU_PATH);
	    menuSystem.removeMenuItem(PAUSE_MENU_PATH);
	    pause.removePropertyChangeListener(controllerListener);
	}
	
	private class ControllerListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if((Double)(evt.getNewValue())>.7)
		    proposePause(!paused);
	    }
	}//end ControllerListener
	
	private class MenuSelectionListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		proposePause(!paused);
	    }
	}//end MenuSelectionListener
	
	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Object newValue = evt.getNewValue();
		menuSystem.setMenuItemEnabled(
			newValue instanceof Mission.GameplayState
			, PAUSE_MENU_PATH);
	    }
	}//end RunStateListener
	
	private void proposePause(boolean newState){
	    final Object runState = tr.getRunState();
	    if(runState instanceof Mission.PlayerActivity &&
		    !(runState instanceof PauseDisabledState)){
		setPaused(newState);
	    }//end if(PlayerActivity)
	}//end proposePauseToggle()

	public boolean isPaused() {
	    return paused;
	}

	public void setPaused(boolean paused) {
	    if(this.paused==paused)
		return;
	    final boolean oldValue = this.paused;
	    this.paused = paused;
	    pcs.firePropertyChange(PAUSE, oldValue, paused);
	    Mission mission = this.mission.get();
	    if(mission!=null)
		if(paused)
		 ((TVF3Game)mission.getGame()).getUpfrontDisplay().submitPersistentMessage("Paused--F3 to Resume ");
		else
		    ((TVF3Game)mission.getGame()).getUpfrontDisplay().removePersistentMessage();
	    tr.getThreadManager().setPaused(paused);
	    tr.soundSystem.get() .setPaused(paused);
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

	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}
	
    }//end GamePause

    @Override
    public Feature<Mission> newInstance(Mission target) {
	return new GamePause();
    }

    @Override
    public Class<Mission> getTargetClass() {
	return Mission.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GamePause.class;
    }
}//end GamePause
