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
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.ControllerMapper;
import org.jtrfp.trcl.core.InputDevice;
import org.jtrfp.trcl.gui.ControllerConfigTab.ControllerConfigTabConf;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerConfigTab implements ConfigurationTab<ControllerConfigTabConf> {
    private final ControllerConfigPanel panel;
    private ControllerConfigTabConf conf;
    private final ControllerMapper mapper;
    
    @Autowired(required=false)
    public ControllerConfigTab(Collection<InputDevice> inputs, ControllerMapper mapper, ControllerInputs cInputs){
	this.panel = new ControllerConfigPanel(inputs,mapper,cInputs);
	this.mapper = mapper;
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
    
    public static class ControllerConfigTabConf {
	private HashMap<String, ControllerConfiguration> controllerConfigurations;

	public HashMap<String, ControllerConfiguration> getControllerConfigurations() {
	    if(controllerConfigurations == null)
		controllerConfigurations = new HashMap<String, ControllerConfiguration>();
	    return controllerConfigurations;
	}

	public void setControllerConfigurations(
		HashMap<String, ControllerConfiguration> controllerConfigurations) {
	    this.controllerConfigurations = controllerConfigurations;
	}
    }//end ControllerConfigTabConf

    @Override
    public Class<ControllerConfigTabConf> getConfigBeanClass() {
	return ControllerConfigTabConf.class;
    }

    @Override
    public void setConfigBean(ControllerConfigTabConf cfg) {
	conf=cfg;
	readFromConfigBean();
    }
    
    private void readFromConfigBean(){
	if(conf==null)
	    conf = new ControllerConfigTabConf();
	final ControllerConfigTabConf config = getConfigBean();
	
	for(ControllerInputDevicePanel p: panel.getControllerInputDevicePanels()){
	    final InputDevice id = p.getInputDevice();
	    ControllerConfiguration controllerConfiguration = config.getControllerConfigurations().get(id.getName());
	    //No config present. Create a new one.
	    if(controllerConfiguration==null){
		    controllerConfiguration = mapper.getRecommendedDefaultConfiguration(p.getInputDevice());
		    if(controllerConfiguration==null){
			controllerConfiguration = new ControllerConfiguration();
			controllerConfiguration.setIntendedController(p.getInputDevice().getName());
			}
		    //Add the new config
		    config.getControllerConfigurations().put(id.getName(), controllerConfiguration);
		    }//end if(null)
	    p.setControllerConfiguration(controllerConfiguration);
	}//end for(ControllerInputDevicePanels)
    }//end readFromConfigBean()

    @Override
    public ControllerConfigTabConf getConfigBean() {
	if(conf==null)
	    setConfigBean(null);
	return conf;
    }

}//end ControllerTab
