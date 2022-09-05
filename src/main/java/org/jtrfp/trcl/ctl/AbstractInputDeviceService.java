/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.ctl;

import java.util.Collection;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration.ConfEntry;
import org.jtrfp.trcl.gui.DefaultControllerConfiguration;

public abstract class AbstractInputDeviceService implements InputDeviceService, Feature<ControllerMapper> {
    private ControllerMapper target;
    private ControllerSinks sinks;
    
    @Override
    public void registerDefaultConfiguration(DefaultControllerConfiguration<?> defaultConfigToRegister){
	final String intendedControllerName   = defaultConfigToRegister.getIntendedController();
	final ControllerSinks sinks = getSinks();
	final InputDevice inputDevice = getTarget().getInputDeviceByName(intendedControllerName);
	final ControllerMapper mapper = getTarget();
	if( inputDevice != null ){
	    final Collection<ConfEntry> confs = defaultConfigToRegister.getEntryMap().values();
	    for( ConfEntry conf : confs )
	        mapper.mapControllerSourceToInput(
	        	inputDevice.getSourceByName(conf.getName()), 
	        	sinks.getSink(conf.getDest()), 
	        	ControllerMapping.PRIORITY_DEFAULT, 
	        	conf.getScale(), conf.getOffset());
	}//end if( inputDevice )
    }//end registerDefaultConfiguration(...)
    
    @Override
    public void registerFallbackConfiguration(ControllerConfiguration fallbackConfigToRegister){
	final ControllerSinks sinks = getSinks();
	final ControllerMapper mapper = getTarget();
	for( InputDevice inputDevice : getInputDevices() ){
	    final Collection<ConfEntry> confs = fallbackConfigToRegister.getEntryMap().values();
	    for( ConfEntry conf : confs ){
		final ControllerSource controllerSource = inputDevice.getSourceByName(conf.getName());
		if(controllerSource != null ) //It might not exist on this machine.
	            mapper.mapControllerSourceToInput(
	        	controllerSource, 
	        	sinks.getSink(conf.getDest()), 
	        	ControllerMapping.PRIORITY_FALLBACK, 
	        	conf.getScale(), conf.getOffset());
	        }//end for( confs )
	}//end for(input devices)
    }//end registerFallbackConfiguration
    
    @Override
    public void apply(ControllerMapper target){
	setTarget(target);
	final Collection<InputDevice> inputDevices = getInputDevices();
	target.registerInputDevices(inputDevices);
    }//end apply()

    protected ControllerMapper getTarget() {
        return target;
    }

    protected void setTarget(ControllerMapper target) {
        this.target = target;
    }

    protected ControllerSinks getSinks() {
	if(sinks == null)
	    sinks = Features.get(target, ControllerSinks.class);
        return sinks;
    }

    protected void setSinks(ControllerSinks sinks) {
        this.sinks = sinks;
    }

}//end AbstractInputDeviceService
