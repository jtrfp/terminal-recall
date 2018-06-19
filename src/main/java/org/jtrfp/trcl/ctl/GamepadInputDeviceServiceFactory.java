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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.flow.JVM;
import org.springframework.stereotype.Component;

import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;

@Component
public class GamepadInputDeviceServiceFactory implements FeatureFactory<ControllerMapper> {
    private static final String operatingSystem = System.getProperty("os.name").toLowerCase();
    
    public class GamepadInputDeviceService extends AbstractInputDeviceService {
	private final List<InputDevice> inputDevices = new ArrayList<InputDevice>();
	private final ControllerEnvironment controllerEnvironment;

	public GamepadInputDeviceService(){
	    try{final JVM jvm = new JVM();
	    if(operatingSystem.contains("win")){
		jvm.loadFromJarToFile("/jinput-dx8_64.dll");
		jvm.loadFromJarToFile("/jinput-dx8.dll");
		jvm.loadFromJarToFile("/jinput-raw_64.dll");
		jvm.loadFromJarToFile("/jinput-raw.dll");
		jvm.loadFromJarToFile("/jinput-wintab.dll");
	    } else if(operatingSystem.contains("linux")){
		jvm.loadFromJarToFile("/libjinput-linux64.so");
		jvm.loadFromJarToFile("/libjinput-linux.so");
	    } else if(operatingSystem.contains("mac")){
		jvm.loadFromJarToFile("/libjinput-osx.jnilib");
	    } else
		System.err.println("Warning: Couldn't determine OS; jInput will likely fail to load.");
	    System.setProperty("net.java.games.input.librarypath", new File("DeleteMe").getAbsolutePath());
	    }catch(Exception e){e.printStackTrace();}
	    this.controllerEnvironment = ControllerEnvironment.getDefaultEnvironment();
	    if(controllerEnvironment.isSupported()){
		//Filter out the keyboards in favor of AWT's event-based tracking
		for(Controller controller:controllerEnvironment.getControllers()){
		    final Type type = controller.getType();
		    if(     type != Controller.Type.KEYBOARD && 
			    type != Controller.Type.MOUSE    &&
			    type != Controller.Type.TRACKBALL)
			inputDevices.add(new GamepadInputDevice(controller));
		}//end for(controller types)
	    }//end if(controllerEnvironment.isSupported())
	    else System.err.println("GamepadInputDeviceServiceFactory() WARNING: jInput indicates environment not supported. There will be no Gamepad controller support by this Feature.");
	}//end constructor

	private class GamepadInputDevice implements InputDevice{
	    private final List<GamepadControllerSource> controllerSources;
	    private final Controller controller;
	    private final net.java.games.input.EventQueue eventQueue;
	    private final HashMap<net.java.games.input.Component,GamepadControllerSource> controllerSourceMap 
	    = new HashMap<net.java.games.input.Component,GamepadControllerSource>();
	    private final HashMap<String,GamepadControllerSource>    nameMap = new HashMap<String,GamepadControllerSource>();
	    private final GamepadEventThread gamepadEventThread = new GamepadEventThread();

	    GamepadInputDevice(Controller controller){
		if(controller==null)
		    throw new NullPointerException("Passed Controller intolerably null.");
		this.controller = controller;
		controller.setEventQueueSize(256);
		eventQueue = controller.getEventQueue();
		System.out.println("CONTROLLER: "+controller.getClass().getName());
		controllerSources = new ArrayList<GamepadControllerSource>(controller.getComponents().length);
		//System.out.println("Rumblers: "+controller.getRumblers().length);
		//controller.getRumblers()[0].rumble(1f);
		for(net.java.games.input.Component comp : controller.getComponents()){
		    GamepadControllerSource gcs = new GamepadControllerSource(comp);
		    controllerSources.add(gcs);
		    controllerSourceMap.put(comp, gcs);
		    nameMap.put(comp.getName(),gcs);
		    System.out.println("Component found: "+comp.getName());
		}//end for(components)
		gamepadEventThread.start();
	    }//end consructor

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
		return "Provided through OS -> jInput -> TRCL GamepadInputDeviceService.";
	    }

	    private class GamepadEventThread extends Thread{
		private final Event event = new Event();
		@Override
		public void run(){
		    Thread.currentThread().setName("GamepadEventThread");
		    while(true){
			while(!eventQueue.getNextEvent(event))
			    try{Thread.sleep(20);
			    if(!controller.poll()){
				System.err.println("WARNING: Lost contact with controller: "+controller.getName()+". Escaping poll loop...");
				return;
				}
			    }catch(InterruptedException e){}
			controllerSourceMap.get(event.getComponent()).notifyPropertyChange(event.getValue());
		    }//end while(true)
		}//end run()
	    }//end GamepadEventThread

	    @Override
	    public ControllerSource getSourceByName(String name) {
		return nameMap.get(name);
	    }

	    private class GamepadControllerSource implements ControllerSource {
		private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private final net.java.games.input.Component component;
		private volatile double currentState = 0;

		public GamepadControllerSource(net.java.games.input.Component component){
		    this.component=component;
		}//end constructor

		@Override
		public String getName() {
		    return component.getName();
		}

		void notifyPropertyChange(final double value){
		    SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
			    pcs.firePropertyChange(new PropertyChangeEvent(GamepadControllerSource.this, ControllerSource.STATE, currentState, value));
			    currentState=value;
			}});

		}//end notifyPropertyChange(...)

		@Override
		public InputDevice getInputDevice() {
		    return GamepadInputDevice.this;
		}

		@Override
		public void addPropertyChangeListener(PropertyChangeListener l) {
		    pcs.addPropertyChangeListener(l);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener l) {
		    pcs.removePropertyChangeListener(l);
		}
	    }//end GamepadControllerSource
	}//end GamepadInputDevice

	@Override
	public String getAuthor() {
	    return "jTRFP.org / jInput";
	}

	@Override
	public String getDescription() {
	    return "jInput supplied controllers and head-trackers";
	}

	@Override
	public Collection<InputDevice> getInputDevices() {
	    return inputDevices;
	}

	@Override
	public void destruct(ControllerMapper target) {
	    // TODO Auto-generated method stub
	    
	}
    }//end GamepadInputDeviceService

    @Override
    public Feature<ControllerMapper> newInstance(ControllerMapper target) {
	return new GamepadInputDeviceService();
    }

    @Override
    public Class<ControllerMapper> getTargetClass() {
	return ControllerMapper.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GamepadInputDeviceService.class;
    }
}//end GamepadInputDeviceServiceFactory