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

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.HasQuantifiableSupply;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputs;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SamplePlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class AfterburnerBehavior extends Behavior implements HasQuantifiableSupply {
    public static final String IGNITION_SOUND   = "BLAST7.WAV";
    public static final String EXTINGUISH_SOUND = "SHUT-DN7.WAV";
    boolean firstDetected=true;
    private double fuelRemaining=0;
    private double formerMax,formerProp,newMax;
    private final ControllerInput afterburnerCtl;
    public static final String AFTERBURNER = "Afterburner";
    private SoundTexture ignitionSound, extinguishSound;
    
    public AfterburnerBehavior(ControllerInputs inputs){
	afterburnerCtl = inputs.getControllerInput(AFTERBURNER);
    }
    @Override
    public void tick(long tickTimeMillis){
	WorldObject p = getParent();
	if(afterburnerCtl.getState()>.7){
	    if(firstDetected){
		afterburnerOnTransient(p);
		firstDetected=false;
		fuelRemaining-=((double)p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/
			(double)Powerup.AFTERBURNER_TIME_PER_UNIT_MILLIS);
	    }//end if(firstDetected)
	    p.probeForBehavior(Propelled.class).setPropulsion(newMax).setMaxPropulsion(newMax);
	}//end if(F)
	else{
	    if(firstDetected==false)
	    	{afterburnerOffTransient(p);}
	    firstDetected=true;
	}//end else{}
    }//end _tick
    
    private void afterburnerOnTransient(WorldObject p){
	//Save former max, former propulsion
	//TODO: Ignition SFX, start sustain SFX
	ignitionSFX();
	Propelled prop = p.probeForBehavior(Propelled.class);
	formerMax=prop.getMaxPropulsion();
	formerProp=prop.getPropulsion();
	newMax=formerMax*3;
    }//end afterburnerOnTriansient(...)
    
    private void ignitionSFX(){
	final SoundTexture ignitionSound = getIgnitionSound();
	if(ignitionSound != null){
	    final WorldObject parent = getParent();
	    final TR              tr = parent.getTr();
	    final SoundSystem soundSystem = tr.soundSystem.get();
	    final SamplePlaybackEvent playbackEvent 
	        = soundSystem.getPlaybackFactory().
	            create(ignitionSound, new double[]{
	        	    SoundSystem.DEFAULT_SFX_VOLUME,
	        	    SoundSystem.DEFAULT_SFX_VOLUME});
	    soundSystem.enqueuePlaybackEvent(playbackEvent);
	}//end if(ignitionSound)
    }//end ignitionSFX()
    
    private void extinguishSFX(){
	final SoundTexture extinguishSound = getExtinguishSound();
	if(extinguishSound != null){
	    final WorldObject parent = getParent();
	    final TR              tr = parent.getTr();
	    final SoundSystem soundSystem = tr.soundSystem.get();
	    final SamplePlaybackEvent playbackEvent 
	        = soundSystem.getPlaybackFactory().
	            create(extinguishSound, new double[]{
	        	    SoundSystem.DEFAULT_SFX_VOLUME,
	        	    SoundSystem.DEFAULT_SFX_VOLUME});
	    soundSystem.enqueuePlaybackEvent(playbackEvent);
	}//end if(ignitionSound)
    }//end ignitionSFX()
    
    private void afterburnerOffTransient(WorldObject p){
	//TODO: De-Ignition SFX, end sustain SFX
	extinguishSFX();
	Propelled prop = p.probeForBehavior(Propelled.class);
	prop.setMaxPropulsion(formerMax);
	prop.setPropulsion(formerProp);
    }//end afterburnerOffTransient(...)

    @Override
    public void addSupply(double amount) {
	fuelRemaining+=amount;
    }//end addSupply(...)

    @Override
    public double getSupply() {
	return fuelRemaining;
    }//end getSupply()
    public SoundTexture getIgnitionSound() {
        return ignitionSound;
    }
    public AfterburnerBehavior setIgnitionSound(SoundTexture ignitionSound) {
        this.ignitionSound = ignitionSound;
        return this;
    }
    public SoundTexture getExtinguishSound() {
        return extinguishSound;
    }
    public AfterburnerBehavior setExtinguishSound(SoundTexture extinguishSound) {
        this.extinguishSound = extinguishSound;
        return this;
    }
}//end AfterburnerBehavior
