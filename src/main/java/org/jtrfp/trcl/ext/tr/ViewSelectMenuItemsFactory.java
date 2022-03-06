/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
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
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.RunStateHandler;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.ViewSelectFactory.ViewSelect;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.miss.Mission.PlayerActivity;
import org.jtrfp.trcl.miss.Mission.SatelliteState;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class ViewSelectMenuItemsFactory implements FeatureFactory<Game> {
    public static class ViewSelectMenuItems implements Feature<Game> {
	private static final String [] PATH_COCKPIT_VIEW = {"View","Pilot View","Cockpit"};
	private static final String [] PATH_CHASE_VIEW = {"View","Pilot View","Chase"};
	private static final String [] PATH_OUTSIDE_VIEW = {"View","Pilot View","Outside"};
	
	private static final String [] PATH_FULL_VIEW = {"View","Cockpit","Full"};
	private static final String [] PATH_HEADSUP_VIEW = {"View","Cockpit","Heads-Up"};
	private static final String [] PATH_INSTRUMENT_VIEW = {"View","Cockpit","Instrument"};
	
	@Getter
	private boolean destructed = false;
	@Getter@Setter
	private ViewSelect viewSelect;
	
	private PropertyChangeListener weakMenuItemsEnabledPCL; //HARD REFERENCE. DO NOT REMOVE.
	private PropertyChangeListener menuItemsEnabledPCL = new RunStateHandler() {

	    @Override
	    public void enteredRunState(Object oldState, Object newState) {
		enableMenuItems(true);
	    }

	    @Override
	    public void exitedRunState(Object oldState, Object newState) {
		if(destructed)
		    return;
		enableMenuItems(false);
	    }

	    @Override
	    public boolean isValidRunState(Object oldRunState,
		    Object newRunState) {
		return (newRunState instanceof PlayerActivity) && !(newRunState instanceof SatelliteState);
	    }};

	    private void enableMenuItems(boolean state) {
		final MenuSystem menuSystem = getMenuSystem();
		
		menuSystem.setMenuItemEnabled(state, PATH_COCKPIT_VIEW);
		menuSystem.setMenuItemEnabled(state, PATH_CHASE_VIEW);
		menuSystem.setMenuItemEnabled(state, PATH_OUTSIDE_VIEW);

		menuSystem.setMenuItemEnabled(state, PATH_FULL_VIEW);
		menuSystem.setMenuItemEnabled(state, PATH_HEADSUP_VIEW);
		menuSystem.setMenuItemEnabled(state, PATH_INSTRUMENT_VIEW);
	    }
	
	@Getter @Setter
	private TR tr;
	@Getter @Setter
	private MenuSystem menuSystem;
	
	public ViewSelectMenuItems() {}
	

	@Override
	public void apply(Game target) {
	    final MenuSystem menuSystem = getMenuSystem();
	    final TR tr = getTr();
	    
	    menuSystem.addMenuItem(.001, PATH_COCKPIT_VIEW);
	    menuSystem.addMenuItem(.002, PATH_CHASE_VIEW);
	    menuSystem.addMenuItem(.003, PATH_OUTSIDE_VIEW);
	    
	    menuSystem.addMenuItem(.001, PATH_FULL_VIEW);
	    menuSystem.addMenuItem(.002, PATH_HEADSUP_VIEW);
	    menuSystem.addMenuItem(.003, PATH_INSTRUMENT_VIEW);
	    
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakMenuItemsEnabledPCL = new WeakPropertyChangeListener(menuItemsEnabledPCL, tr));
	    
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setViewMode(vs.COCKPIT_VIEW);
		}}, PATH_COCKPIT_VIEW);
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setViewMode(vs.CHASE_VIEW);
		}}, PATH_CHASE_VIEW);
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setViewMode(vs.OUTSIDE_VIEW);
		}}, PATH_OUTSIDE_VIEW);
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setInstrumentMode(vs.FULL_COCKPIT);
		}}, PATH_FULL_VIEW);
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setInstrumentMode(vs.NO_INSTRUMENTS);
		}}, PATH_HEADSUP_VIEW);
	    menuSystem.addMenuItemListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    final ViewSelect vs = getViewSelect();
		    vs.setInstrumentMode(vs.HUD_INSTRUMENTS);
		}}, PATH_INSTRUMENT_VIEW);
	}//end apply(...)

	@Override
	public void destruct(Game target) {
	    destructed = true;
	    
	    menuSystem.removeMenuItem(PATH_COCKPIT_VIEW);
	    menuSystem.removeMenuItem(PATH_CHASE_VIEW);
	    menuSystem.removeMenuItem(PATH_OUTSIDE_VIEW);
	    
	    menuSystem.removeMenuItem(PATH_FULL_VIEW);
	    menuSystem.removeMenuItem(PATH_HEADSUP_VIEW);
	    menuSystem.removeMenuItem(PATH_INSTRUMENT_VIEW);
	    
	    getTr().removePropertyChangeListener(weakMenuItemsEnabledPCL);
	}//end destruct()

    }//end ViewSelectMenuItems

    @Override
    public Feature<Game> newInstance(Game target)
	    throws FeatureNotApplicableException {
	final ViewSelectMenuItems result = new ViewSelectMenuItems();
	result.setMenuSystem(Features.getByPath(Features.getSingleton(), MenuSystem.class, TR.class, RootWindow.class));
	result.setTr(Features.get(Features.getSingleton(), TR.class));
	result.setViewSelect(Features.get(target, ViewSelect.class));
	return result;
    }

    @Override
    public Class<Game> getTargetClass() {
	return Game.class;
    }

    @Override
    public Class<? extends Feature<Game>> getFeatureClass() {
	return ViewSelectMenuItems.class;
    }
}//end ViewSelectMenuItemsFactory
