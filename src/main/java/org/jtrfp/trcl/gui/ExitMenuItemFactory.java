/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jMenuSystemFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.springframework.stereotype.Component;

@Component
public class ExitMenuItemFactory implements FeatureFactory<MenuSystem> {
    public static final String []   EXIT_MENU_PATH = new String[]  {"File","Exit"};
    
    public class ExitMenuItem implements Feature<MenuSystem>{
	private final ExitMenuItemListener	exitMenuItemListener   = new ExitMenuItemListener();

	@Override
	public void apply(MenuSystem target) {
	    target.addMenuItem(EXIT_MENU_PATH);
	    target.setMenuItemEnabled(true, EXIT_MENU_PATH);
	    target.addMenuItemListener(exitMenuItemListener, EXIT_MENU_PATH);
	}

	@Override
	public void destruct(MenuSystem target) {
	    // TODO Auto-generated method stub
	    
	}
	
	private class ExitMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	}//end ExitMenuItemListener
	
    }//end ExitMenuItem

    @Override
    public Feature<MenuSystem> newInstance(MenuSystem target) {
	return new ExitMenuItem();
    }

    @Override
    public Class<MenuSystem> getTargetClass() {
	return MenuSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ExitMenuItem.class;
    }

}//end ExitMenuItemFactory
