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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.SwingUtilities;

public class StateListenerSupport {
    private double oldState;
    private final ControllerSource controllerSource;
    private final HashSet<StateListener> stateListeners = new HashSet<StateListener>(32);
    
    public StateListenerSupport(ControllerSource controllerSource){
	this.controllerSource = controllerSource;
    }
 public synchronized boolean addStateListener(StateListener stateListener){
     return stateListeners.add(stateListener);
 }//end addStateListener(...)
 public synchronized boolean removeStateListener(StateListener stateListener){
     return stateListeners.remove(stateListener);
 }//end removeStateListener()
 public Collection<StateListener> getStateListeners(){
     return Collections.unmodifiableCollection(stateListeners);
 }//end getStateListeners()
 public synchronized void fireStateChange(double newState){
     if(newState==oldState)
	 return;
     for(StateListener sl:stateListeners)
	 sl.stateChanged(controllerSource, newState);
     oldState = newState;
 }//end fireStateChange()
}//end StateListenerSupport
