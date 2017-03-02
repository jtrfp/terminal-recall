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

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.gui.ConfigWindowFactory.ConfigWindow;
import org.springframework.stereotype.Component;

@Component
public class ControllerConfigTabFactory implements FeatureFactory<ConfigWindow>{
    public class ControllerConfigTab implements ConfigurationTab, Feature<ConfigWindow> {
	private ControllerConfigPanel panel;
	//private ControllerConfigTabConf conf;
	private ControllerMapper mapper;
	
	public ControllerConfigTab(){
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
	/*
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

	public void setConfigBean(ControllerConfigTabConf cfg) {
	    conf=cfg;
	    //readFromConfigBean();
	}
	
	public ControllerConfigTabConf getConfigBean() {
	    if(conf==null)
		setConfigBean(null);
	    return conf;
	}
	*/
	@Override
	public void apply(ConfigWindow target) {
	    mapper = Features.get(Features.getSingleton(), ControllerMapper.class);
	    final ControllerSinks cInputs = Features.get(mapper, ControllerSinks.class);
	    panel = new ControllerConfigPanel(mapper,cInputs);
	    target.registerConfigTab(this);
	    //readFromConfigBean();
	}

	@Override
	public void destruct(ConfigWindow target) {
	    // TODO Auto-generated method stub

	}
    }//end ControllerConfigTab

    @Override
    public Feature<ConfigWindow> newInstance(ConfigWindow target) {
	return new ControllerConfigTab();
    }

    @Override
    public Class<ConfigWindow> getTargetClass() {
	return ConfigWindow.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ControllerConfigTab.class;
    }//end ControllerConfigTab
    /*
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
    */
}//end ControlleConfigTabFactory
