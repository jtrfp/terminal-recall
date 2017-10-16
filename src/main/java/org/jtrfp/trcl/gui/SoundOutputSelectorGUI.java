/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2017 Chuck Ritola
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.concurrent.Executor;

import javax.sound.sampled.AudioFormat;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jtrfp.trcl.snd.AudioDevice;
import org.jtrfp.trcl.snd.AudioDriver;
import org.jtrfp.trcl.snd.AudioOutput;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundSystemOutputConfig;

public class SoundOutputSelectorGUI extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -6539874860140467626L;
    private JComboBox<String> driverSelectCB, deviceSelectCB, audioOutputCB, audioFormatCB;
    private Executor executor;
    private volatile boolean eventInProgress = false;

    /**
     * Create the panel.
     */
    public SoundOutputSelectorGUI() {
	super();
	
    	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    	
    	final DefaultComboBoxModel driverSelectCBM = new DefaultComboBoxModel();
    	driverSelectCB = new JComboBox(driverSelectCBM);
    	add(driverSelectCB);
    	
    	final DefaultComboBoxModel deviceSelectCBM = new DefaultComboBoxModel();
    	deviceSelectCB = new JComboBox(deviceSelectCBM);
    	add(deviceSelectCB);
    	
    	final DefaultComboBoxModel audioOutputCBM = new DefaultComboBoxModel();
    	audioOutputCB = new JComboBox(audioOutputCBM);
    	add(audioOutputCB);
    	
    	final DefaultComboBoxModel audioFormatCBM = new DefaultComboBoxModel();
    	audioFormatCB = new JComboBox(audioFormatCBM);
    	add(audioFormatCB);
    	
    	JButton testButton = new JButton("Test");
    	testButton.setHorizontalAlignment(SwingConstants.LEFT);
    	testButton.setIcon(new ImageIcon(SoundOutputSelectorGUI.class.getResource("/org/freedesktop/tango/22x22/devices/audio-card.png")));
    	//add(testButton);//TODO: Implement
    	/*
    	final Object driverSelection = driverSelectCB.getSelectedItem();
    	if(driverSelection != null)
    	    setActiveDriver((AudioDriver)driverSelection);
	driverSelectCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent ie) {
		if(ie.getStateChange()==ItemEvent.SELECTED)
		    SoundOutputSelectorGUI.this.setActiveDriver((AudioDriver)ie.getItem());
	    }});
	
	final AudioDevice deviceSelection = (AudioDevice)deviceSelectCB.getSelectedItem();
    	if(deviceSelection != null)
    	    setActiveDevice((AudioDevice)deviceSelection);
	deviceSelectCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent ie) {
		if(ie.getStateChange()==ItemEvent.SELECTED)
		    SoundOutputSelectorGUI.this.setActiveDevice((AudioDevice)ie.getItem());
	    }});
	
	final AudioFormat outputSelection = (AudioFormat)audioOutputCB.getSelectedItem();
    	if(outputSelection != null)
    	    setActiveOutput((AudioOutput)outputSelection);
	audioOutputCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent ie) {
		if(ie.getStateChange()==ItemEvent.SELECTED)
		    SoundOutputSelectorGUI.this.setActiveOutput((AudioOutput)ie.getItem());
	    }});
	
	final AudioFormat formatSelection = (AudioFormat)audioFormatCB.getSelectedItem();
    	if(formatSelection != null)
    	    setActiveFormat((AudioFormat)formatSelection);
	audioFormatCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent ie) {
		if(ie.getStateChange()==ItemEvent.SELECTED)
		    SoundOutputSelectorGUI.this.setActiveFormat((AudioFormat)ie.getItem());
	    }});
	*/
    	/*
	// Init
	for(AudioDevice o:deviceList)
		if(o.getOutputs().size()>0)
		    deviceSelectCBM.addElement(o);
	*/
    	/*
	deviceList.addCollectionListener(new CollectionListener<AudioDevice>(){
	    @Override
	    public void collectionChanged(CollectionEvent<AudioDevice> evt) {
		switch(evt.getType()){
		case ADDED:
		    for(AudioDevice o:evt.getElements())
			if(o.getOutputs().size()>0){// Empty devices are useless here.
			    deviceSelectCBM.addElement(o);
			    deviceSelectCB.setSelectedIndex(0);
			    }
		    break;
		case REMOVED:
		    for(AudioDevice o:evt.getElements()){
			deviceSelectCBM.removeElement(o);
			if(deviceSelectCBM.getSize()>0)
			    deviceSelectCB.setSelectedIndex(0);
		    }
		    break;
		case UPDATED:
		    break;//???
		default:
		    break;
		}
	    }});
	*/
	// Init
    	/*
	for (AudioOutput o : outputList)
	    audioOutputCBM.addElement(o);
	*/
    	/*
	outputList.addCollectionListener(new CollectionListener<AudioOutput>(){
	    @Override
	    public void collectionChanged(CollectionEvent<AudioOutput> evt) {
		switch(evt.getType()){
		case ADDED:
		    for(AudioOutput o:evt.getElements()){
			audioOutputCBM.addElement(o);
			audioOutputCB.setSelectedIndex(0);
		    }
		    break;
		case REMOVED:
		    for(AudioOutput o:evt.getElements()){
			audioOutputCBM.removeElement(o);
			if(audioOutputCBM.getSize()>0)
			    audioOutputCB.setSelectedIndex(0);
		    }
		    break;
		case UPDATED:
		    break;//???
		default:
		    break;
		}
	    }});
	*/
	// Init
    	/*
	for (AudioFormat o : formatList)
	    audioFormatCBM.addElement(o);

	formatList.addCollectionListener(new CollectionListener<AudioFormat>() {
	    @Override
	    public void collectionChanged(CollectionEvent<AudioFormat> evt) {
		switch (evt.getType()) {
		case ADDED:
		    for (AudioFormat o : evt.getElements()){
			audioFormatCBM.addElement(o);
			audioFormatCB.setSelectedIndex(0);
		    }
		    break;
		case REMOVED:
		    for (AudioFormat o : evt.getElements()){
			audioFormatCBM.removeElement(o);
			if(audioFormatCBM.getSize()>0)
			    audioFormatCB.setSelectedIndex(0);
		    }
		    break;
		case UPDATED:
		    break;// ???
		default:
		    break;
		}
	    }
	});//end addCollectionListener<AudioFormat>
	
	////ACTIVE SELECTION UPDATES
	this.addPropertyChangeListener(ACTIVE_DRIVER, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		driverSelectCBM.setSelectedItem(evt.getNewValue());
	    }});
	this.addPropertyChangeListener(ACTIVE_DEVICE, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		deviceSelectCBM.setSelectedItem(evt.getNewValue());
	    }});
	this.addPropertyChangeListener(ACTIVE_OUTPUT, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		audioOutputCBM.setSelectedItem(evt.getNewValue());
	    }});
	this.addPropertyChangeListener(ACTIVE_FORMAT, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		audioFormatCBM.setSelectedItem(evt.getNewValue());
	    }});
	*/
    }//end SoundInputSelectorGUI()
    
    private AudioDriver getSelectedDriver() {
	try{
	    //Repopulate the device list
	    final String driverName = driverSelectCB.getSelectedItem().toString();
	    final AudioDriver driver = (AudioDriver)Class.forName(driverName).newInstance();
	    return driver;
	}catch(Exception e){e.printStackTrace(); return null;}
    }//end getCurrentDriver()
    
    private AudioDevice getSelectedDevice() {
	//Select the device
	final String deviceByName = deviceSelectCB.getSelectedItem().toString();
	final AudioDevice selectedDevice = getSelectedDriver().getDeviceByName(deviceByName);
	return selectedDevice;
    }//end getCurrentDevice()
    
    private AudioOutput getSelectedPort() {
	final String portByName = audioOutputCB.getSelectedItem().toString();
	final AudioOutput selectedOutput = getSelectedDevice().getOutputByName(portByName);
	return selectedOutput;
    }//end getSelectedPort()
    
    private AudioFormat getSelectedFormat() {
	final String formatByName = audioFormatCB.getSelectedItem().toString();
	final AudioFormat selectedFormat = getSelectedPort().getFormatFromUniqueName(formatByName);
	return selectedFormat;
    }//end getSelectedFormat()
    
    public void init(final SoundSystem soundSystem) {
	soundSystem.addPropertyChangeListener(new PropertyChangeListener(){

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(eventInProgress)
		    return;
		final Object newValue = evt.getNewValue();
		eventInProgress = true;

		if( evt.getPropertyName() == SoundSystem.OUTPUT_CONFIG && newValue instanceof SoundSystemOutputConfig) {
		    final SoundSystemOutputConfig conf = (SoundSystemOutputConfig)newValue;
		    //Populate the boxes
		    //Set new driver
		    final String driverName = conf.getDriverByName();
		    driverSelectCB.getModel().setSelectedItem(driverName);

		    try{
			//Repopulate the device list
			final AudioDriver driver = (AudioDriver)Class.forName(driverName).newInstance();
			final Collection<AudioDevice> devices = driver.getDevices();
			final DefaultComboBoxModel devLM = (DefaultComboBoxModel)deviceSelectCB.getModel();
			devLM.removeAllElements();
			for( AudioDevice device : devices )
			    devLM.addElement(device.getUniqueName());
			//Select the device
			final String deviceByName = conf.getDeviceByName();
			deviceSelectCB.setSelectedItem(deviceByName);
			final AudioDevice selectedDevice = driver.getDeviceByName(deviceByName);

			//Repopulate the port list
			final Collection<? extends AudioOutput> outputs = selectedDevice.getOutputs();
			final DefaultComboBoxModel outputLM = (DefaultComboBoxModel)audioOutputCB.getModel();
			outputLM.removeAllElements();
			for( AudioOutput output : outputs )
			    outputLM.addElement(output.getUniqueName());
			//Select the output port
			final String portByName = conf.getPortByName();
			audioOutputCB.setSelectedItem(portByName);
			final AudioOutput selectedOutput = selectedDevice.getOutputByName(portByName);

			//Repopulate format list
			final AudioFormat [] formats = selectedOutput.getFormats();//TODO: Should return Collection
			final DefaultComboBoxModel formatLM = (DefaultComboBoxModel)audioFormatCB.getModel();
			formatLM.removeAllElements();
			for( AudioFormat format : formats )
			    formatLM.addElement(format.toString());
			
			//Prepare to fire a propagating event
			eventInProgress = false;
			
			//Select the output port
			final String formatByName = conf.getFormatByName();
			audioFormatCB.setSelectedItem(formatByName);
			//final AudioFormat selectedFormat = selectedOutput.getFormatFromUniqueName(formatByName);
		    }
		    catch(Exception e){e.printStackTrace();}
		}//end if( OUTPUT_CONFIG )

		eventInProgress = false;
	    }});//end PropertyChangeListener
	
	driverSelectCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() != ItemEvent.SELECTED)
		    return;
		try{
		    //Repopulate the device list
		    final AudioDriver driver = getSelectedDriver();
		    final Collection<AudioDevice> devices = driver.getDevices();
		    final DefaultComboBoxModel devLM = (DefaultComboBoxModel)deviceSelectCB.getModel();
		    devLM.removeAllElements();
		    for( AudioDevice device : devices )
			devLM.addElement(device.getUniqueName());
		    if(devices.isEmpty()){
			final DefaultComboBoxModel outputLM = (DefaultComboBoxModel)audioOutputCB.getModel();
			outputLM.removeAllElements();
			}
		}catch(Exception ex) {ex.printStackTrace();}
	    }});
	
	deviceSelectCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() != ItemEvent.SELECTED)
		    return;
		try{
		    //Repopulate the port list
		    final AudioDevice selectedDevice = getSelectedDevice();
		    final Collection<? extends AudioOutput> outputs = selectedDevice.getOutputs();
		    final DefaultComboBoxModel outputLM = (DefaultComboBoxModel)audioOutputCB.getModel();
		    outputLM.removeAllElements();
		    for( AudioOutput output : outputs )
			outputLM.addElement(output.getUniqueName());
		    if(outputs.isEmpty()){
			final DefaultComboBoxModel formatLM = (DefaultComboBoxModel)audioFormatCB.getModel();
			formatLM.removeAllElements();
			}
		}catch(Exception ex) {ex.printStackTrace();}
	    }});
	
	audioOutputCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() != ItemEvent.SELECTED)
		    return;
		try{
		    //Repopulate format list
		    final AudioOutput selectedPort = getSelectedPort();
		    final AudioFormat [] formats = selectedPort.getFormats();
		    final DefaultComboBoxModel formatLM = (DefaultComboBoxModel)audioFormatCB.getModel();
		    formatLM.removeAllElements();
		    for( AudioFormat format : formats )
			formatLM.addElement(format.toString());
		}catch(Exception ex) {ex.printStackTrace();}
	    }});
	
	audioFormatCB.addItemListener(new ItemListener(){
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if(eventInProgress || e.getStateChange() != ItemEvent.SELECTED)
		    return;
		eventInProgress = true;
		final SoundSystemOutputConfig config = new SoundSystemOutputConfig();
		config.setDriverByName(driverSelectCB.getSelectedItem().toString());
		config.setDeviceByName(deviceSelectCB.getSelectedItem().toString());
		config.setPortByName  (audioOutputCB .getSelectedItem().toString());
		config.setFormatByName(audioFormatCB .getSelectedItem().toString());
		soundSystem.setOutputConfig(config);
		eventInProgress = false;
	    }});
	
	final DefaultComboBoxModel driverLM = (DefaultComboBoxModel)driverSelectCB.getModel();
	final Collection<String> audioDriverNames = soundSystem.getAudioDriverNames();
	for(String audioDriverName : audioDriverNames )
	    driverLM.addElement(audioDriverName);
	/*
	soundSystem.getAudioDriverNames().addTarget(
		new ComboBoxPropertyBinding(driverSelectCB, soundSystem, SoundSystem.DRIVER_BY_NAME).getComboBoxContents(),
		true);
	
	final ComboBoxPropertyBinding deviceBinding = new ComboBoxPropertyBinding(deviceSelectCB,soundSystem,  SoundSystem.DEVICE_BY_NAME);
	final ComboBoxPropertyBinding outputBinding = new ComboBoxPropertyBinding(audioOutputCB ,soundSystem,  SoundSystem.OUTPUT_BY_NAME);
	final ComboBoxPropertyBinding formatBinding = new ComboBoxPropertyBinding(audioFormatCB ,soundSystem,  SoundSystem.FORMAT_BY_NAME);
	
	deviceBinding.setExecutor(getExecutor());
	outputBinding.setExecutor(getExecutor());
	formatBinding.setExecutor(getExecutor());
	
	soundSystem.addPropertyChangeListener(SoundSystem.ACTIVE_DEVICE, new PropertyChangeListener (){

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Collection<String> coll = outputBinding.getComboBoxContents();
		final Object newValue = evt.getNewValue();
		if( newValue instanceof AudioDevice ){
		    final String oldSelection = soundSystem.getFormatByName();
		    AudioDevice dev = (AudioDevice)newValue;
		    coll.clear();
		    for(AudioOutput output : dev.getOutputs())
			coll.add(output.getUniqueName());
		    //Try to retain the old selection if available
		    if(oldSelection != null)
		        audioOutputCB.setSelectedItem(oldSelection);
		    }//end if(AudioDevice)
	    }});
	soundSystem.addPropertyChangeListener(SoundSystem.ACTIVE_OUTPUT, new PropertyChangeListener (){

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Collection<String> coll = formatBinding.getComboBoxContents();
		final Object newValue = evt.getNewValue();
		if( newValue instanceof AudioOutput ){
		    final String oldSelection = soundSystem.getFormatByName();
		    AudioOutput output = (AudioOutput)newValue;
		    coll.clear();
		    for(AudioFormat fmt : output.getFormats())
			if(SoundSystem.isAcceptableFormat(fmt))
			    coll.add(fmt.toString());
		    //Try to retain the old selection if available
		    if(oldSelection != null)
		        audioFormatCB.getModel().setSelectedItem(oldSelection);
		    }//end if(AudioOutput)
	    }});
	soundSystem.addPropertyChangeListener(SoundSystem.ACTIVE_DRIVER, new PropertyChangeListener (){

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Collection<String> coll = deviceBinding.getComboBoxContents();
		final Object newValue = evt.getNewValue();
		if( newValue instanceof AudioDriver ){
		    final String oldSelection = soundSystem.getDeviceByName();
		    AudioDriver driver = (AudioDriver)newValue;
		    coll.clear();
		    for(AudioDevice dev : driver.getDevices())
			coll.add(dev.getUniqueName());
		    //Try to retain the old selection if available
		    if(oldSelection != null)
		        deviceSelectCB.getModel().setSelectedItem(oldSelection);
		    }//end if( AudioDriver )
	    }});
	    
	    */
	/*
	soundSystem.getAudioDriverNames().addTarget(new CollectionListener<AudioDriver>(){
	    @Override
	    public void collectionChanged(CollectionEvent<AudioDriver> evt) {
		switch(evt.getType()){
		case ADDED:
		    for(AudioDriver ao:evt.getElements())
			driverSelectCBM.addElement(ao);
		    break;
		case REMOVED:
		    for(AudioDriver ao:evt.getElements())
			driverSelectCBM.removeElement(ao);
		    break;
		case UPDATED:
		    break;//???
		default:
		    break;
		}
	    }});
	*/
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}//end SoundOutputSelector
