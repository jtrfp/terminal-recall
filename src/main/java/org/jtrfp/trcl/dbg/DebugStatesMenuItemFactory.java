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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.Reporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DebugStatesMenuItemFactory {
    private static final String [] MENU_PATH = new String[]{"Debug","Debug States..."};
    private Reporter reporter;
    private TR tr;

    private ActionListener menuItemListener;

    private void proposeInit(){
	if(getReporter() == null || getTr() == null || getMenuItemListener() != null)
	    return;
	setMenuItemListener(new MenuItemListener());
	final MenuSystem ms = getTr().getMenuSystem();
	ms.addMenuItem(MENU_PATH);
	ms.addMenuItemListener(menuItemListener, MENU_PATH);
	ms.setMenuItemEnabled(true, MENU_PATH);
    }//end proposeInit()

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
	proposeInit();
    }

    public TR getTr() {
	return tr;
    }

    @Autowired
    public void setTr(TR tr) {
	this.tr = tr;
	proposeInit();
    }

    public ActionListener getMenuItemListener() {
	return menuItemListener;
    }

    public void setMenuItemListener(ActionListener menuItemListener) {
	this.menuItemListener = menuItemListener;
    }
}//end DebugStatesMenuItemFactory
