/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2022 Chuck Ritola
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
import java.awt.event.WindowEvent;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class ExitMenuItemFactory implements FeatureFactory<MenuSystem> {
    public static final String []   EXIT_MENU_PATH = new String[]  {"File","Exit"};
    
    public class ExitMenuItem implements Feature<MenuSystem>{
	private final ExitMenuItemListener	exitMenuItemListener   = new ExitMenuItemListener();

	@Override
	public void apply(MenuSystem target) {
	    target.addMenuItem(MenuSystem.END, EXIT_MENU_PATH);
	    target.setMenuItemEnabled(true, EXIT_MENU_PATH);
	    target.addMenuItemListener(exitMenuItemListener, EXIT_MENU_PATH);
	    target.setMenuPosition(MenuSystem.BEGINNING, "File");
	}

	@Override
	public void destruct(MenuSystem target) {}
	
	private class ExitMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		final RootWindow rw = Features.getByPath(Features.getSingleton(), RootWindow.class, TR.class);
		rw.dispatchEvent(new WindowEvent(rw, WindowEvent.WINDOW_CLOSING));
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
