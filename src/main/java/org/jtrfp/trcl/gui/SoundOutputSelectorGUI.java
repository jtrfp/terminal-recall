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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;

import org.jtrfp.trcl.snd.AudioDevice;
import org.jtrfp.trcl.snd.AudioDriver;
import org.jtrfp.trcl.snd.AudioOutput;

import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;

public class SoundOutputSelectorGUI extends SoundOutputSelector {

    /**
     * Create the panel.
     */
    public SoundOutputSelectorGUI() {
	super();
    	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    	
    	final DefaultComboBoxModel<AudioDriver> driverSelectCBM = new DefaultComboBoxModel<AudioDriver>();
    	final JComboBox<AudioDriver> driverSelectCB = new JComboBox<AudioDriver>(driverSelectCBM);
    	add(driverSelectCB);
    	
    	final DefaultComboBoxModel<AudioDevice> deviceSelectCBM = new DefaultComboBoxModel<AudioDevice>();
    	final JComboBox<AudioDevice> deviceSelectCB = new JComboBox<AudioDevice>(deviceSelectCBM);
    	add(deviceSelectCB);
    	
    	final DefaultComboBoxModel<AudioOutput> audioOutputCBM = new DefaultComboBoxModel<AudioOutput>();
    	JComboBox<AudioOutput> audioOutputCB = new JComboBox<AudioOutput>(audioOutputCBM);
    	add(audioOutputCB);
    	
    	JButton testButton = new JButton("Test");
    	testButton.setIcon(new ImageIcon(SoundOutputSelectorGUI.class.getResource("/org/freedesktop/tango/22x22/devices/audio-card.png")));
    	add(testButton);
    	
    	for(AudioDriver ao:SoundOutputSelector.outputDrivers)
    	    ((DefaultComboBoxModel<AudioDriver>)(driverSelectCB.getModel())).addElement(ao);
    	SoundOutputSelector.outputDrivers.addCollectionListener(new CollectionListener<AudioDriver>(){
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
	
	// Init
	for(AudioDevice o:deviceList)
		if(o.getOutputs().size()>0)
		    deviceSelectCBM.addElement(o);
	
	deviceList.addCollectionListener(new CollectionListener<AudioDevice>(){
	    @Override
	    public void collectionChanged(CollectionEvent<AudioDevice> evt) {
		switch(evt.getType()){
		case ADDED:
		    for(AudioDevice o:evt.getElements())
			if(o.getOutputs().size()>0)// Empty devices are useless here.
			    deviceSelectCBM.addElement(o);
		    break;
		case REMOVED:
		    for(AudioDevice o:evt.getElements())
			deviceSelectCBM.removeElement(o);
		    break;
		case UPDATED:
		    break;//???
		default:
		    break;
		}
	    }});
	
	// Init
	for (AudioOutput o : outputList)
	    audioOutputCBM.addElement(o);
	
	outputList.addCollectionListener(new CollectionListener<AudioOutput>(){
	    @Override
	    public void collectionChanged(CollectionEvent<AudioOutput> evt) {
		switch(evt.getType()){
		case ADDED:
		    for(AudioOutput o:evt.getElements())
			audioOutputCBM.addElement(o);
		    break;
		case REMOVED:
		    for(AudioOutput o:evt.getElements())
			audioOutputCBM.removeElement(o);
		    break;
		case UPDATED:
		    break;//???
		default:
		    break;
		}
	    }});
    }//end SoundInputSelectorGUI()

}//end SoundOutputSelector
