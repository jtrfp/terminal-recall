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

package org.jtrfp.trcl.conf;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jtrfp.trcl.conf.FeatureConfigWindowFactory.FeatureConfigWindow;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class SettingsMenuItemFactory implements FeatureFactory<MenuSystem> {
    public static final String []   CONFIG_MENU_PATH = new String[]{"File","Settings..."};
    public class ConfigMenu implements Feature<MenuSystem>{
	private final ConfigMenuItemListener	configMenuItemListener = new ConfigMenuItemListener();
	@Getter
	private FeatureConfigFrame configWindow;
	private TR tr;

	@Override
	public void apply(MenuSystem target) {
	    tr           = Features.get(Features.getSingleton(), TR.class);
	    configWindow = Features.get(getTr(), FeatureConfigWindow.class).getFeatureConfigWindow();
	    target.addMenuItem(MenuSystem.MIDDLE, CONFIG_MENU_PATH);
	    target.setMenuItemEnabled(true, CONFIG_MENU_PATH);
	    target.addMenuItemListener(configMenuItemListener, CONFIG_MENU_PATH);
	}

	@Override
	public void destruct(MenuSystem target) {
	    // TODO Auto-generated method stub

	}

	private class ConfigMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		final Point oldPos = configWindow.getRootFrame().getLocation();
		if(oldPos.getX() == 0 && oldPos.getY() == 0) {
		    final Point screenXY = Features.get(getTr(), RootWindow.class).getLocationOnScreen();
		    configWindow.getRootFrame().setLocation(screenXY);
		}
		getConfigWindow().refreshAndShow();
	    }//end actionPerformed(...)
	}//end ConfigMenuItemListener
	
	private TR getTr(){
	    return tr;
	}

    }//end Feature

    @Override
    public Feature<MenuSystem> newInstance(MenuSystem target) {
	return new ConfigMenu();
    }

    @Override
    public Class<MenuSystem> getTargetClass() {
	return MenuSystem.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return ConfigMenu.class;
    }
}//end ConfigMenuItemFactory
