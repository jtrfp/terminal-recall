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

package org.jtrfp.trcl.core;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerMapper {
    private final ControllerInputs controllerInputs;
    private HashMap<ControllerSource,ControllerInput> controllerMap = new HashMap<ControllerSource,ControllerInput>();
    private final CollectionActionDispatcher<InputDevice> inputDevices = new CollectionActionDispatcher<InputDevice>();
    
    @Autowired
    public ControllerMapper(ControllerInputs controllerInputs){
	this.controllerInputs = controllerInputs;
    }//end constructor
    
    public CollectionActionDispatcher<InputDevice> getInputDevices(){
	return inputDevices;
    }//end getInputDevices()
    
    public void registerInputDevice(InputDevice toRegister){
	inputDevices.add(toRegister);
    }//end registerInputDevice
    
    /**
     * Multiple sources may feed the same input, though their behavior is undefined if they are of different types
     * i.e. button vs trigger vs axis
     * @param controllerSource
     * @param controllerInput
     * @since Nov 12, 2015
     */
    public void mapControllerSourceToInput(ControllerSource controllerSource, ControllerInput controllerInput, double scale, double offset){
	controllerSource.addStateListener(new ControllerInputStateChangeSetter(controllerInput,scale,offset));
    }//end mapControllerSourceToInput
    
    private class ControllerInputStateChangeSetter implements StateListener{
	private final ControllerInput controllerInput;
	private final double scale;
	private final double offset;
	public ControllerInputStateChangeSetter(ControllerInput controllerInput, double scale, double offset) {
	    this.controllerInput = controllerInput;
	    this.scale           = scale;
	    this.offset          = offset;
	}

	@Override
	public void stateChanged(ControllerSource source, double value) {
		controllerInput.setState(value*scale+offset);
	}
    }//end ControllerInputStateChangeSetter
 
}//end ControllerMapper
