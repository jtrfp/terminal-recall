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

package org.jtrfp.trcl.core;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class CreditsMenuItemFactory implements FeatureFactory<MenuSystem> {
    private static final String [] MENU_ITEM_PATH = {"Help","About Terminal Recall..."};
    public static class CreditsMenuItem implements Feature<MenuSystem> {
	@Getter @Setter
	private RootWindow rootWindow;
	@Getter(lazy=true)
	private final CreditsDisplayWindow creditsWindow = new CreditsDisplayWindow();

	@Override
	public void apply(MenuSystem target) {
	    target.addMenuItem(MenuSystem.MIDDLE, MENU_ITEM_PATH);
	    target.addMenuItemListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    showCreditsWindow();
		}}, MENU_ITEM_PATH);
	    
	    target.setMenuItemEnabled(true, MENU_ITEM_PATH);
	}

	@Override
	public void destruct(MenuSystem target) {
	    target.removeMenuItem(MENU_ITEM_PATH);
	}
	
	private void showCreditsWindow() {
	    final CreditsDisplayWindow cw = getCreditsWindow();
	    final RootWindow rw = getRootWindow();
	    final Point rwLoc = rw.getLocation();
	    final Point rwCtr = new Point((int)(rwLoc.getX()+rw.getWidth()/2), (int)(rwLoc.getY()+rw.getHeight()/2));
	    final Point newCWPos = new Point((int)(rwCtr.getX()-cw.getWidth()/2), (int)(rwCtr.getY()-cw.getHeight()/2));
	    cw.setLocation(newCWPos);
	    cw.setVisible(true);
	}//end showCreditsWindow()

    }//end CreditsMenuItem

    @Override
    public Feature<MenuSystem> newInstance(MenuSystem target)
	    throws FeatureNotApplicableException {
	final CreditsMenuItem result = new CreditsMenuItem();
	result.setRootWindow(Features.getByPath(Features.getSingleton(), RootWindow.class, TR.class));
	return result;
    }

    @Override
    public Class<MenuSystem> getTargetClass() {
	return MenuSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return CreditsMenuItem.class;
    }
}//end CreditsMenuItemFactory
