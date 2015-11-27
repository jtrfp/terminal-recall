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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.ControllerMapper;
import org.jtrfp.trcl.core.InputDevice;

public class ControllerConfigPanel extends JPanel {
    private static final long serialVersionUID = -7100861763976731950L;
    private final Collection<ControllerInputDevicePanel> controllerInputDevicePanels = new ArrayList<ControllerInputDevicePanel>();
    
    public ControllerConfigPanel(Collection<InputDevice> inputs, ControllerMapper mapper, ControllerInputs ci){
	super();
	setLayout(new BorderLayout(0, 0));
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	add(tabbedPane, BorderLayout.CENTER);
	for(InputDevice id:inputs){
	    if(!id.getControllerSources().isEmpty()){
		final ControllerInputDevicePanel panel = new ControllerInputDevicePanel(id,ci,mapper);
		controllerInputDevicePanels.add(panel);
		tabbedPane.addTab(id.getName(), null, panel, id.getDetailedDescription());
	    }//end if(present)
	}//end for(inputs)
    }//end constructor

    public Collection<ControllerInputDevicePanel> getControllerInputDevicePanels() {
        return controllerInputDevicePanels;
    }
}//end ControllerConfigPanel
