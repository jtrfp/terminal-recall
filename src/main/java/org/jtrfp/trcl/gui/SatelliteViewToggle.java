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

package org.jtrfp.trcl.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jtrfp.trcl.core.TR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jogamp.newt.event.KeyEvent;

@Component
public class SatelliteViewToggle {
    private static final String satKey = "SATELLITE_KEY";

    @Autowired
    public SatelliteViewToggle(TR tr){
	final MenuSystem menuSystem = tr.getMenuSystem();
	final JCheckBoxMenuItem view_sat = menuSystem.getView_sat();
	Action satelliteKeyAction = new AbstractAction("SATELLITE_VIEW_KEY"){
	    private static final long serialVersionUID = -6843605846847411702L;
	    @Override
	    public void actionPerformed(ActionEvent l) {
		if(view_sat.isEnabled())
		    view_sat.doClick();
	    }};
	    view_sat.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0), satKey);
	    view_sat.getActionMap().put(satKey, satelliteKeyAction);
    }//end constructor
}//end SatelliteViewToggle
