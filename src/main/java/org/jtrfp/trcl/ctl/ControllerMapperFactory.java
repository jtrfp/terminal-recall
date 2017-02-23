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

package org.jtrfp.trcl.ctl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.springframework.stereotype.Component;

@Component
public class ControllerMapperFactory implements FeatureFactory<Features> {
  public class ControllerMapper implements Feature<Features>{
    private final Collection<InputDevice> inputDevices = new ArrayList<InputDevice>();
    private final Set<MappingListener<ControllerSource,ControllerMapping>> mappingListeners = new HashSet<MappingListener<ControllerSource,ControllerMapping>>();
    private final Map<ControllerSource,ControllerMapping> map = new HashMap<ControllerSource,ControllerMapping>();
    private final Map<String,ControllerConfiguration> recommendedDefaultConfigurations = new HashMap<String,ControllerConfiguration>();
    
    public void registerInputDevice(InputDevice dev){
	inputDevices.add(dev);
    }
    
    public void registerInputDevices(Collection<InputDevice> dev){
	inputDevices.addAll(dev);
    }
    
    public Collection<InputDevice> getInputDevices(){
	return inputDevices;
    }//end getInputDevices()
    /**
     * Multiple sources may feed the same input, though their behavior is undefined if they are of different types
     * i.e. button vs trigger vs axis
     * @param controllerSource
     * @param controllerSink
     * @since Nov 12, 2015
     */
    public void mapControllerSourceToInput(ControllerSource controllerSource, ControllerSink controllerSink, double scale, double offset){
	final ControllerMapping mapping = new ControllerMapping(controllerSource,controllerSink,scale,offset);
	map.put(controllerSource, mapping);
	controllerSource.addPropertyChangeListener(mapping);
	fireMappedEvent(controllerSource, mapping);
    }//end mapControllerSourceToInput
    
    public boolean unmapControllerSource(ControllerSource controllerSource){
	final ControllerMapping controllerMapping = map.get(controllerSource);
	if(controllerMapping==null)
	    return false; //Was never here to begin with
	map.remove(controllerSource);
	controllerSource.removePropertyChangeListener(controllerMapping);
	return true;
    }
    
    private void fireMappedEvent(ControllerSource cs, ControllerMapping mapping){
	for(MappingListener<ControllerSource,ControllerMapping> l:mappingListeners)
	    l.mapped(cs, mapping);
    }// end fireMappedEvent(...)
    

    public boolean addMappingListener(MappingListener<ControllerSource,ControllerMapping> l, boolean populate) {
	boolean result =  mappingListeners.add(l);
	if(populate){
	    for(Entry<ControllerSource,ControllerMapping> entry:map.entrySet()){
		l.mapped(entry.getKey(), entry.getValue());
	    }//end for(entries)
	}//end if(populate)
	return result;
    }//end addMappingListener(...)
    
    public boolean removeMappingListener(MappingListener<ControllerSource,ControllerMapping> l){
	boolean result = mappingListeners.remove(l);
	return result;
    }//end removeMappingListener(...)

    public ControllerConfiguration getRecommendedDefaultConfiguration(
	    InputDevice inputDevice) {
	ControllerConfiguration result = recommendedDefaultConfigurations.get(inputDevice.getName());
	if(result==null)
	    result = recommendedDefaultConfigurations.get("fallback "+inputDevice.getClass().getName());
	if(result!=null){
	    try{result = (ControllerConfiguration)BeanUtils.cloneBean(result);}
	    catch(Exception e){e.printStackTrace();}
	}//end if(!null)
	return result;
    }//end getRecommendedDefaultConfiguration()

    @Override
    public void apply(Features target) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void destruct(Features target) {
	// TODO Auto-generated method stub
	
    }

    public void registerDefaultConfiguration(
	    ControllerConfiguration config) {
	recommendedDefaultConfigurations.put(config.getIntendedController(), config);
    }
 
}//end ControllerMapper

@Override
public Feature<Features> newInstance(Features target) {
    return new ControllerMapper();
}

@Override
public Class<Features> getTargetClass() {
    return Features.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return ControllerMapper.class;
}
}//end ControllerMapperFactory