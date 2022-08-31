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

package org.jtrfp.trcl.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.miss.Mission;
import org.springframework.stereotype.Component;

@Component
public class CrosshairsMenuItemFactory implements FeatureFactory<TVF3Game> {
    public static final String [] CROSSHAIRS_MENU_ITEM_PATH =  new String[]{"View","Crosshairs (Toggle)"};

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target)
	    throws FeatureNotApplicableException {
	return new CrosshairsMenuItemFeature();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return CrosshairsMenuItemFeature.class;
    }
    
    public static class CrosshairsMenuItemFeature implements Feature<TVF3Game>{
	private MenuItemListener menuItemListener = new MenuItemListener();
	private final GameStatePCL         gameStatePCL = new GameStatePCL();
	private WeakPropertyChangeListener weakStatePCL;
	private TVF3Game target;
	private MenuSystem menuSystem;
	private volatile boolean destructed = false;

	@Override
	public void apply(TVF3Game target) {
	    setTarget(target);
	    final TR tr =  target.getTr();
	    final RootWindow rootWindow = Features.get(tr, RootWindow.class);
	    final MenuSystem menuSystem = Features.get(rootWindow, MenuSystem.class);
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, CROSSHAIRS_MENU_ITEM_PATH);
	    menuSystem.addMenuItemListener(menuItemListener, CROSSHAIRS_MENU_ITEM_PATH);
	    this.menuSystem = menuSystem;
	    weakStatePCL = new WeakPropertyChangeListener(gameStatePCL, tr);
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakStatePCL);
	}//end apply(...)

	@Override
	public void destruct(TVF3Game target) {
	    destructed = true;
	    target.getTr().removePropertyChangeListener(weakStatePCL);
	    menuSystem.removeMenuItem(CROSSHAIRS_MENU_ITEM_PATH);
	}
	
	private class MenuItemListener implements ActionListener {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		final TVF3Game target = getTarget();
		final TR tr = target.getTr();
		final TRConfiguration trConfig = Features.get(tr, TRConfiguration.class);
		final boolean oldState = trConfig.isCrosshairsEnabled();
		trConfig.setCrosshairsEnabled(!oldState);
	    }
	}//end MenuItemListener

	protected TVF3Game getTarget() {
	    return target;
	}

	protected void setTarget(TVF3Game target) {
	    this.target = target;
	}
	
	private class GameStatePCL implements PropertyChangeListener {

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(destructed)
		    return;
		final Object newValue = evt.getNewValue();
		final boolean enable = newValue instanceof Mission.ActiveMissionState;
		menuSystem.setMenuItemEnabled(enable, CROSSHAIRS_MENU_ITEM_PATH);
	    }//end propertyChange(...)
	    
	}//end GamestatePCL
	
    }//end CrosshairsMenuItemFeature

}//end CrosshairsMenuItemFactory
