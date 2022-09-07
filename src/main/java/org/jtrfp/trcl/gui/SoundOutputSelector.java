/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.JPanel;

import org.jtrfp.trcl.snd.AudioDevice;
import org.jtrfp.trcl.snd.AudioDriver;
import org.jtrfp.trcl.snd.AudioOutput;

import com.ochafik.util.listenable.DefaultListenableCollection;
import com.ochafik.util.listenable.ListenableCollection;

public class SoundOutputSelector extends JPanel{
    /**
     * 
     */
    private static final long serialVersionUID = 5353360244801171372L;

    //// BEAN PROPERTIES
    public static final String  ACTIVE_DRIVER=	"activeDriver",
	    			ACTIVE_DEVICE=	"activeDevice",
	    			ACTIVE_OUTPUT=	"activeOutput",
	    			ACTIVE_FORMAT=  "activeFormat";
    
    //// VARS
    private AudioDriver         activeDriver;
    private AudioDevice		activeDevice;
    private AudioOutput		activeOutput;
    private AudioFormat		activeFormat;
    
    public final ListenableCollection<AudioDevice> deviceList 
    	= new DefaultListenableCollection<AudioDevice>(new HashSet<AudioDevice>());
    public final ListenableCollection<AudioOutput> outputList 
	= new DefaultListenableCollection<AudioOutput>(new HashSet<AudioOutput>());
    public final ListenableCollection<AudioFormat> formatList 
	= new DefaultListenableCollection<AudioFormat>(new HashSet<AudioFormat>());
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public SoundOutputSelector(){
	super();
	this.addPropertyChangeListener(ACTIVE_DRIVER, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		deviceList.clear();
		deviceList.addAll(((AudioDriver)evt.getNewValue()).getDevices());
	    }});
	this.addPropertyChangeListener(ACTIVE_DEVICE, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		outputList.clear();
		outputList.addAll(((AudioDevice)evt.getNewValue()).getOutputs());
	    }});
	this.addPropertyChangeListener(ACTIVE_OUTPUT, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		formatList.clear();
		for(AudioFormat f:((AudioOutput)evt.getNewValue()).getFormats())
		    if(isAcceptableFormat(f))
			formatList.add(f);
	    }});
    }//end constructor
    
    /*public void applySettings(TRConfiguration config){
	config.setActiveSoundDriver(activeDriver.getClass().getName());
	config.setActiveAudioDevice(activeDevice.getUniqueName());
	config.setActiveAudioOutput(activeOutput.getUniqueName());
	config.setActiveAudioFormat(activeFormat.toString());
    }*/
    /*
    public void readToPanel(TRConfiguration config){
	String driverClassName = config.getActiveAudioDriver();
	if(driverClassName==null) driverClassName = "org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput";
	Class<?> driverClass;
	try{driverClass = Class.forName(driverClassName);}
	catch(ClassNotFoundException e){
	    driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;}
	if(!AudioDriver.class.isAssignableFrom(driverClass))
	    driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;
	try{setActiveDriver((AudioDriver)driverClass.newInstance());}
	catch(Exception e){e.printStackTrace();return;}
	final AudioDriver activeDriver = getActiveDriver();
	
	String deviceName = config.getActiveAudioDevice();
	AudioDevice activeDevice=null;
	if(deviceName!=null)
	 activeDevice = activeDriver.getDeviceByName(deviceName);
	if(activeDevice!=null){
	 setActiveDevice(activeDevice);
	 System.out.println("activeDevice="+activeDevice);
	 String outputName = config.getActiveAudioOutput();
	 AudioOutput ao = activeDevice.getOutputByName(outputName);
	 System.out.println("activeOutput="+ao);
	 if(ao!=null) activeDriver.setOutput(ao);
	 else activeDriver.setOutput(ao = activeDevice.getDefaultOutput());
	 
	 if(ao!=null){
	     String formatName = config.getActiveAudioFormat();
	     AudioFormat fmt=null;
	     if(formatName!=null)
		 fmt = ao.getFormatFromUniqueName(formatName);
	     if(fmt!=null)
		 activeDriver.setFormat(fmt);
	     else if(ao.getFormats().length>0)
		 activeDriver.setFormat(ao.getFormats()[0]);
	  }//end if(ao!=null)
	 }//end if(activeDevice!=null)
    }//end loadFromTRConfig
*/
    /**
     * @return the activeDriver
     */
    public AudioDriver getActiveDriver() {
        return activeDriver;
    }

    /**
     * @param activeDriver the activeDriver to set
     */
    public void setActiveDriver(AudioDriver activeDriver) {
	pcs.firePropertyChange(ACTIVE_DRIVER, this.activeDriver, activeDriver);
        this.activeDriver = activeDriver;
    }

    /**
     * @return the activeOutput
     */
    public Object getActiveDevice() {
        return activeDevice;
    }

    /**
     * @param activeDevice the activeOutput to set
     */
    public void setActiveDevice(AudioDevice activeDevice) {
	pcs.firePropertyChange(ACTIVE_DEVICE, this.activeDevice, activeDevice);
        this.activeDevice = activeDevice;
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @return the activeLine
     */
    public AudioOutput getActiveOutput() {
        return activeOutput;
    }

    /**
     * @param activeOutput the activeLine to set
     */
    public void setActiveOutput(AudioOutput activeOutput) {
	pcs.firePropertyChange(ACTIVE_OUTPUT, this.activeOutput, activeOutput);
        this.activeOutput = activeOutput;
    }
    
    protected boolean isAcceptableFormat(AudioFormat f){
	return  f.getChannels()==2 &&
		f.getEncoding()==Encoding.PCM_SIGNED &&
		f.getSampleSizeInBits() % 8 == 0;
    }

    /**
     * @return the activeFormat
     */
    public AudioFormat getActiveFormat() {
        return activeFormat;
    }

    /**
     * @param activeFormat the activeFormat to set
     */
    public void setActiveFormat(AudioFormat activeFormat) {
	pcs.firePropertyChange(ACTIVE_FORMAT, this.activeFormat, activeFormat);
        this.activeFormat = activeFormat;
    }
    
}//end soundOutputSelector
