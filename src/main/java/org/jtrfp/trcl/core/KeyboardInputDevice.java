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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyboardInputDevice implements InputDevice {
    private final Map<Integer,KeyControllerSource> controllerSourceMap = new HashMap<Integer,KeyControllerSource>();
    
    public KeyboardInputDevice(){
	final Field [] fields = KeyEvent.class.getDeclaredFields();
	for(Field f:fields)
	    if(Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("VK_"))
		try{controllerSourceMap.put(f.getInt(null),new KeyControllerSource(stripVKPrefix(f.getName())));}
	catch(Exception e){e.printStackTrace();}
	KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new DefaultKeyEventListener());
    }//end constructor
    
    private String stripVKPrefix(String input){
	if(input.contains("VK_"))
	 return    input.substring(3);
	return input;
    }//end stripVKPrefix()
    
    KeyControllerSource getKeyControllerSource(int eventID){
	try{return controllerSourceMap.get(eventID);}
	catch(IndexOutOfBoundsException e){return null;}
    }//end getKeyControllerSource
    
    private class DefaultKeyEventListener implements KeyEventDispatcher{
	@Override
	public boolean dispatchKeyEvent(KeyEvent evt) {
	    final KeyControllerSource kcs = getKeyControllerSource(evt.getKeyCode());
	    if(kcs==null)
		return false;
	    if(evt.getID()==KeyEvent.KEY_PRESSED)
		{kcs.notifyPressed();return false;}
	    else if(evt.getID()==KeyEvent.KEY_RELEASED)
		{kcs.notifyReleased();return false;}
	    return false;
	}//end dispatchKeyEvent
    }//end DefaultKeyEventListener
    
    private class KeyControllerSource implements ControllerSource{
	private final StateListenerSupport sls = new StateListenerSupport(this);
	private final String name;
	
	public KeyControllerSource(String name){
	    this.name=name;
	}//end constructor

	@Override
	public boolean addStateListener(StateListener stateListener) {
	    return sls.addStateListener(stateListener);
	}

	@Override
	public boolean removeStateListener(StateListener stateListener) {
	    return sls.removeStateListener(stateListener);
	}

	@Override
	public Collection<StateListener> getStateListeners() {
	    return sls.getStateListeners();
	}

	@Override
	public String getName() {
	    return name;
	}
	
	public void notifyPressed(){
	    sls.fireStateChange(1);
	}
	
	public void notifyReleased(){
	    sls.fireStateChange(0);
	}
    }//end KeyControllerSource
    
    @Override
    public Collection<? extends ControllerSource> getControllerSources() {
	return controllerSourceMap.values();
    }
    
    @Override
    public String getName() {
	return "Keyboard Input";
    }

    @Override
    public String getVendor() {
	return "jTRFP.org";
    }

    @Override
    public String getDetailedDescription() {
	return "Adds keyboard control support using AWT.";
    }
}//end KeyboardInputDevice
