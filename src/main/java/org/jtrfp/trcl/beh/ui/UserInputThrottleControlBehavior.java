/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.prop.VetoableChangeListenable;

public class UserInputThrottleControlBehavior extends Behavior implements PlayerControlBehavior, VetoableChangeListenable {
    private double nudgeUnit = 40000;
    //CONTROL NAMES
    public static final String THROTTLE_DELTA= "Throttle Delta";
    public static final String THROTTLE      = "Throttle";
    
    //PROPERTIES
    public static final String THROTTLE_CTL_STATE = "throttleCtlState";
    
    private final ControllerInput throttleDelta,throttleCtl;
    private final VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    private double throttleCtlState;
    private ThrottleControlListener throttleControlListener = new ThrottleControlListener();
    
    public UserInputThrottleControlBehavior(ControllerInputs controllerInputs){
	throttleDelta= controllerInputs.getControllerInput(THROTTLE_DELTA);
	throttleCtl  = controllerInputs.getControllerInput(THROTTLE);
	
	throttleCtl  .addPropertyChangeListener(throttleControlListener);
    }//end constructor
    
    private class ThrottleControlListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final double newState = (Double)evt.getNewValue();
	    final Propelled p = getParent().probeForBehavior(Propelled.class);
	    final double range = p.getMaxPropulsion()-p.getMinPropulsion();
		try{setThrottleCtlState(range*newState+p.getMinPropulsion());}
		 catch(PropertyVetoException e){}
	}//end propertyChange(...)
    }//end ThrottleControlListener
    
    @Override
    public void tick(long timeInMillis){
	applyDelta();
    }//end _tick(...)
    
    private void applyDelta(){
	final Propelled p = getParent().probeForBehavior(Propelled.class);
	double propulsion  = p.getPropulsion();
	try{setThrottleCtlState(propulsion + nudgeUnit * throttleDelta.getState());}
	 catch(PropertyVetoException e){}
    }//end applyDelta
    /**
     * @return the nudgeUnit
     */
    public double getNudgeUnit() {
        return nudgeUnit;
    }
    /**
     * @param nudgeUnit the nudgeUnit to set
     */
    public void setNudgeUnit(double nudgeUnit) {
        this.nudgeUnit = nudgeUnit;
    }

    @Override
    public void addVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener) {
	vcs.addVetoableChangeListener(propertyName, listener);
    }

    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener) {
	vcs.addVetoableChangeListener(listener);
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
	return vcs.getVetoableChangeListeners();
    }

    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(
	    String propertyName) {
	return vcs.getVetoableChangeListeners(propertyName);
    }

    @Override
    public void removeVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener) {
	vcs.removeVetoableChangeListener(propertyName, listener);
    }

    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
	vcs.removeVetoableChangeListener(listener);
    }

    public double getThrottleCtlState() {
        return throttleCtlState;
    }

    public void setThrottleCtlState(double newState) throws PropertyVetoException {
	final double oldState = this.throttleCtlState;
	vcs.fireVetoableChange(THROTTLE_CTL_STATE, oldState, newState);
        this.throttleCtlState = newState;
        getParent().
         probeForBehavior(Propelled.class).
         setPropulsion(newState);
    }//end setThrottleCtlState(...)
}//end UseInputThrottleControlBehavior
