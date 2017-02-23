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
public class ControllerSinksFactory implements FeatureFactory<ControllerMapper> {
    public class ControllerSinks implements Feature<ControllerMapper> {
	private final HashMap<String,ControllerSink>    sinks     = new HashMap<String,ControllerSink>(32);
	private final CollectionActionDispatcher<String> sinkNames = new CollectionActionDispatcher<String>(new HashSet<String>());
	/**
	 * Obtains a control sink of the specified name or creates and registers a new one if not available.
	 * @param name
	 * @return
	 * @since Nov 12, 2015
	 */
	public ControllerSink getSink(final String name){
	    ControllerSink result;
	    if(!sinks.containsKey(name)){
		result = new DefaultControllerSink(name);
		sinks.put(name, result);
		sinkNames.add(name);
	    }else result = sinks.get(name);
	    return result;
	}//end getSink(...)

	public Set<Entry<String,ControllerSink>> getSinks(){
	    return sinks.entrySet();
	}

	public CollectionActionDispatcher<String> getSinkNames(){
	    return sinkNames;
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
    
    private static class DefaultControllerSink implements ControllerSink{
	    private final String controllerName;
	    private       double state = 0;
	    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	    public DefaultControllerSink(String controllerName){
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
		pcs.firePropertyChange(ControllerSink.STATE, this.state, newState);
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
	}//end DefaultControllerSink

    @Override
    public Feature<ControllerMapper> newInstance(ControllerMapper target) {
	return new ControllerSinks();
    }

    @Override
    public Class<ControllerMapper> getTargetClass() {
	return ControllerMapper.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ControllerSinks.class;
    }
}//end ControllerSinksFactory