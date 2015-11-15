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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;

import org.jtrfp.trcl.flow.JVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GamepadInputDevice implements InputDevice {
    private final ControllerMapper mapper;
    private final ArrayList<GamepadControllerSource> controllerSources;
    private final ControllerEnvironment controllerEnvironment;
    private final Controller controller;
    private final net.java.games.input.EventQueue eventQueue;
    private final HashMap<net.java.games.input.Component,GamepadControllerSource> controllerSourceMap 
            = new HashMap<net.java.games.input.Component,GamepadControllerSource>();
    private final HashMap<String,GamepadControllerSource>    nameMap = new HashMap<String,GamepadControllerSource>();
    private final GamepadEventThread gamepadEventThread = new GamepadEventThread();
    @Autowired
    public GamepadInputDevice(ControllerMapper mapper){
	net.java.games.input.EventQueue eq = null;
	try{final JVM jvm = new JVM();//TODO: windows, os x
	    final File file = jvm.loadFromJarToFile("/libjinput-linux64.so");
	    System.setProperty("net.java.games.input.librarypath", file.getParentFile().getAbsolutePath());
	}catch(Exception e){e.printStackTrace();}
	ArrayList<GamepadControllerSource> newControllerSources = null;
	this.controllerEnvironment = ControllerEnvironment.getDefaultEnvironment();
	Controller c = null;
	for(Controller controller:controllerEnvironment.getControllers()){
	    if(controller.getType()==Controller.Type.GAMEPAD){
		controller.setEventQueueSize(256);
		eq = controller.getEventQueue();
		c = controller;
		System.out.println("CONTROLLER: "+controller.getClass().getName());
		newControllerSources = new ArrayList<GamepadControllerSource>(controller.getComponents().length);
		//System.out.println("Rumblers: "+controller.getRumblers().length);
		//controller.getRumblers()[0].rumble(1f);
		for(net.java.games.input.Component comp : controller.getComponents()){
		    GamepadControllerSource gcs = new GamepadControllerSource(comp);
		    newControllerSources.add(gcs);
		    controllerSourceMap.put(comp, gcs);
		    nameMap.put(comp.getName(),gcs);
		    System.out.println("Component found: "+comp.getName());
		    }
	    }//end if(GAMEPAD)
	}//end for(controller types)
	this.controller = c;
	this.controllerSources = newControllerSources;
	this.mapper = mapper;
	this.eventQueue = eq;
	//controllerSourceMap.put(f.getInt(null),new GamepadControllerSource(stripVKPrefix(f.getName())));
	mapper.registerInputDevice(this);
	if(controller!=null)
	 gamepadEventThread.start();
    }//end constructor
    
    private class GamepadControllerSource implements ControllerSource {
	private final StateListenerSupport sls = new StateListenerSupport(this);
	private final net.java.games.input.Component component;
	
	public GamepadControllerSource(net.java.games.input.Component component){
	    this.component=component;
	}//end constructor
	
	@Override
	public String getName() {
	    return component.getName();
	}

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
	
	void notifyStateChange(final double value){
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    sls.fireStateChange(value);
		}});
	}//end notifyStateChange(...)
    }//end GamepadControllerSource
    
    @Override
    public Collection<? extends ControllerSource> getControllerSources() {
	return controllerSources;
    }
    @Override
    public String getName() {
	return controller.getName();
    }
    @Override
    public String getVendor() {
	return "[Unknown]";
    }
    @Override
    public String getDetailedDescription() {
	return "Provided through OS -> jInput -> TRCL GamepadInputDevice.";
    }
    
    private class GamepadEventThread extends Thread{
	private final Event event = new Event();
	@Override
	public void run(){
	    Thread.currentThread().setName("GamepadEventThread");
	    while(true){
		while(!eventQueue.getNextEvent(event))
		    try{Thread.sleep(20);
		    controller.poll();
		    }catch(InterruptedException e){}
		controllerSourceMap.get(event.getComponent()).notifyStateChange(event.getValue());
	    }//end while(true)
	}//end run()
    }//end GamepadEventThread

    public ControllerSource getGamepadControllerSource(String string) {
	return nameMap.get(string);
    }

}//end GamepadInputDevice
