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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class KeyboardInputDeviceService implements InputDeviceService {
    private Collection<InputDevice> inputDevices = new ArrayList<InputDevice>();
    
    public KeyboardInputDeviceService(){
	inputDevices.add(new KeyboardInputDevice());
    }//end constructor
    
    private class KeyboardInputDevice implements InputDevice{
	private final Map<Integer,KeyControllerSource> controllerSourceMap = new HashMap<Integer,KeyControllerSource>();
	    private final HashMap<String,KeyControllerSource> nameMap          = new HashMap<String,KeyControllerSource>();
	    
	    KeyboardInputDevice(){
		final Field [] fields = KeyEvent.class.getDeclaredFields();
		for(Field f:fields)
		    if(Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("VK_"))
			try{final String strippedName = stripVKPrefix(f.getName());
			    final KeyControllerSource kcs = new KeyControllerSource(strippedName);
			    controllerSourceMap.put(f.getInt(null),kcs);
			    nameMap            .put(strippedName  ,kcs);
			}
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
		private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private final String name;
		private boolean pressed = false;
		private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		private final KeyReleaseDispatcher keyReleaseDispatcher = new KeyReleaseDispatcher();
		private Future<?> timerFuture;
		
		public KeyControllerSource(String name){
		    this.name=name;
		}//end constructor

		@Override
		public void addPropertyChangeListener(PropertyChangeListener stateListener) {
		    pcs.addPropertyChangeListener(stateListener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener stateListener) {
		    pcs.removePropertyChangeListener(stateListener);
		}

		@Override
		public String getName() {
		    return name;
		}
		
		public void notifyPressed(){
		    if(!pressed){
	             if(timerFuture!=null)
		      timerFuture.cancel(false);
		     pcs.firePropertyChange(new PropertyChangeEvent(this, ControllerSource.STATE, 0., 1.));
		     }
		    pressed=true;
		}//end notifyPressed()
		
		public void notifyReleased(){
		    if(pressed){
			if(timerFuture!=null)
			 timerFuture.cancel(false);
			timerFuture = timer.schedule(keyReleaseDispatcher, 20, TimeUnit.MILLISECONDS);
			}
		    pressed=false;
		}//end notifyReleased()

		@Override
		public InputDevice getInputDevice() {
		    return KeyboardInputDevice.this;
		}
		
		private class KeyReleaseDispatcher implements Runnable{
		    @Override
		    public void run() {
			pcs.firePropertyChange(new PropertyChangeEvent(KeyControllerSource.this, ControllerSource.STATE, 1., 0.));
		    }
		}//end KeypressDispatcher
	    }//end KeyControllerSource
	    
	    @Override
	    public Collection<? extends ControllerSource> getControllerSources() {
		return controllerSourceMap.values();
	    }
	    
	    @Override
	    public String getName() {
		return "Keyboard";
	    }

	    @Override
	    public String getVendor() {
		return "jTRFP.org";
	    }

	    @Override
	    public String getDetailedDescription() {
		return "Adds keyboard control support using AWT.";
	    }

	    @Override
	    public ControllerSource getSourceByName(String name) {
		return nameMap.get(name);
	    }
    }//end KeyboardInputDevice

    @Override
    public String getAuthor() {
	return "jTRFP.org";
    }

    @Override
    public String getDescription() {
	return "AWT keyboard input";
    }

    @Override
    public Collection<InputDevice> getInputDevices() {
	return inputDevices;
    }
}//end KeyboardInputDevice
