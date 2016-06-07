/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.dbg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.Reporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DebugStatesMenuItemFactory implements FeatureFactory<MenuSystem> {
    private static final String [] MENU_PATH = new String[]{"Debug","Debug States..."};
    private Reporter reporter;

    private ActionListener menuItemListener;
    
    public class DebugStatesMenuItemFeature implements Feature<MenuSystem> {

	@Override
	public void apply(MenuSystem target) {
	    setMenuItemListener(new MenuItemListener());
	    final MenuSystem ms = target;
	    ms.addMenuItem(MENU_PATH);
	    ms.addMenuItemListener(menuItemListener, MENU_PATH);
	    ms.setMenuItemEnabled(true, MENU_PATH);
	}

	@Override
	public void destruct(MenuSystem target) {
	    // TODO Auto-generated method stub
	}
	
    }//end DebugStatesMenuItemFeature

    private class MenuItemListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent evt) {
	    final Reporter rep = getReporter();
	    rep.setVisible(true);
	}
    }//end MenuItemListener

    public Reporter getReporter() {
	return reporter;
    }

    @Autowired
    public void setReporter(Reporter reporter) {
	this.reporter = reporter;
    }

    public ActionListener getMenuItemListener() {
	return menuItemListener;
    }

    public void setMenuItemListener(ActionListener menuItemListener) {
	this.menuItemListener = menuItemListener;
    }

    @Override
    public Feature<MenuSystem> newInstance(
	    MenuSystem target) {
	return new DebugStatesMenuItemFeature();
    }

    @Override
    public Class<MenuSystem> getTargetClass() {
	return MenuSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return DebugStatesMenuItemFeature.class;
    }
}//end DebugStatesMenuItemFactory
