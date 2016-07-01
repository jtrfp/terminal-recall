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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.springframework.stereotype.Component;

@Component
public class ControllerInputsFactory implements FeatureFactory<ControllerMapper> {
    public class ControllerInputs implements Feature<ControllerMapper> {
	private final HashMap<String,ControllerInput>    inputs     = new HashMap<String,ControllerInput>(32);
	private final CollectionActionDispatcher<String> inputNames = new CollectionActionDispatcher<String>(new HashSet<String>());
	/**
	 * Obtains an input of the specified name or creates and registers a new one if not available.
	 * @param name
	 * @return
	 * @since Nov 12, 2015
	 */
	public ControllerInput getControllerInput(final String name){
	    ControllerInput result;
	    if(!inputs.containsKey(name)){
		result = new DefaultControllerInput(name);
		inputs.put(name, result);
		inputNames.add(name);
	    }else result = inputs.get(name);
	    return result;
	}//end declareInput(...)

	public Set<Entry<String,ControllerInput>> getInputs(){
	    return inputs.entrySet();
	}

	public CollectionActionDispatcher<String> getInputNames(){
	    return inputNames;
	}
	
	@Override
	public void apply(ControllerMapper target) {
	    // TODO Auto-generated method stub
	    
	}

	@Override
	public void destruct(ControllerMapper target) {
	    // TODO Auto-generated method stub
	    
	}
    }//end ControllerInputs
    
    private static class DefaultControllerInput implements ControllerInput{
	    private final String controllerName;
	    private       double state = 0;
	    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	    public DefaultControllerInput(String controllerName){
		this.controllerName=controllerName;
	    }//end constructor
	    @Override
	    public double getState() throws IllegalStateException {
		return state;
	    }//end getState()
	    @Override
	    public String getName() {
		return controllerName;
	    }//end getName()
	    @Override
	    public void setState(double newState) {
		pcs.firePropertyChange(ControllerInput.STATE, this.state, newState);
		this.state = newState;
	    }//end setState(...)
	    @Override
	    public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	    }
	    @Override
	    public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	    }
	}//end DefaultControllerInput

    @Override
    public Feature<ControllerMapper> newInstance(ControllerMapper target) {
	return new ControllerInputs();
    }

    @Override
    public Class<ControllerMapper> getTargetClass() {
	return ControllerMapper.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ControllerInputs.class;
    }
}//end ControllerInputsFactory