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

import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.ControllerMapper;
import org.jtrfp.trcl.core.InputDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerConfigTab implements ConfigurationTab {
    private final ControllerConfigPanel panel;
    
    @Autowired(required=false)
    public ControllerConfigTab(Collection<InputDevice> inputs, ControllerMapper mapper, ControllerInputs cInputs){
	this.panel = new ControllerConfigPanel(inputs,mapper,cInputs);
    }

    @Override
    public String getTabName() {
	return "Control";
    }

    @Override
    public JComponent getContent() {
	return panel;
    }

    @Override
    public ImageIcon getTabIcon() {
	return new ImageIcon(ControllerConfigTab.class.getResource("/org/freedesktop/tango/22x22/devices/input-gaming.png"));
    }

}//end ControllerTab
