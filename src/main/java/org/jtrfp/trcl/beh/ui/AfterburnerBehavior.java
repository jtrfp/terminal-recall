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

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.HasQuantifiableSupply;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ctl.ControllerInput;
import org.jtrfp.trcl.ctl.ControllerInputsFactory.ControllerInputs;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.SatelliteViewFactory;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.LoopingSoundEvent;
import org.jtrfp.trcl.snd.SamplePlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class AfterburnerBehavior extends Behavior implements HasQuantifiableSupply, PlayerControlBehavior {
    public static final String IGNITION_SOUND   = "BLAST7.WAV";
    public static final String EXTINGUISH_SOUND = "SHUT-DN7.WAV";
    public static final String LOOP_SOUND       = "ENGINE4.WAV";
    private double fuelRemaining=0;
    private double formerMax,formerProp,newMax;
    private final ControllerInput afterburnerCtl;
    public static final String AFTERBURNER = "Afterburner";
    private SoundTexture ignitionSound, extinguishSound, loopSound;
    private LoopingSoundEvent afterburnerLoop;
    private final RunStateListener     runStateListener     = new RunStateListener();
    private final FiringVetoListener   firingVetoListener   = new FiringVetoListener();
    private final ABControlListener    abControlListener    = new ABControlListener();
    private final ThrottleVetoListener throttleVetoListener = new ThrottleVetoListener();
    
    // HARD REFERENCES; DO NOT REMOVE
    private PropertyChangeListener weakRunStateListener, weakAbControlListener, weakThrottleVetoListener;
    
    private boolean afterburning = false;
    private boolean installedVetoListeners = false;
    
    public AfterburnerBehavior(ControllerInputs inputs){
	afterburnerCtl = inputs.getControllerInput(AFTERBURNER);
	afterburnerCtl.addPropertyChangeListener(weakAbControlListener = new WeakPropertyChangeListener(abControlListener,afterburnerCtl));
    }//end constructor
    
    private class ABControlListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object runState = getParent().getTr().getRunState();
	    if(!isEnabled() || 
		    !(runState instanceof Mission.GameplayState) || 
		    runState instanceof SatelliteViewFactory.SatelliteViewState ||
		    runState instanceof Mission.SatelliteState ||//TODO: Supports old system, remove later
		    runState instanceof Mission.Briefing)
		{setAfterburning(false);return;}
	    final double newValue = (Double)evt.getNewValue();
	    final double oldValue = (Double)evt.getOldValue();
	    if(newValue == oldValue)
		return;
	    if     (newValue >= .7 && oldValue < .7)
		setAfterburning(true);
	    else if(newValue <  .7 && oldValue >= .7)
		setAfterburning(false);
	}//end propertyChange(...)
    }//end ABControlListener
    
    private class FiringVetoListener implements VetoableChangeListener{
	@Override
	public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
	    final Object newValue = evt.getNewValue();
	    if(isAfterburning() && newValue == Boolean.TRUE)
		throw new PropertyVetoException(null, evt);
	}//end vetoableChange(...)
    }//end FiringVetoListener
    
    private class ThrottleVetoListener implements VetoableChangeListener{
	@Override
	public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
	    if(isAfterburning())
		throw new PropertyVetoException(null, evt);
	}//end vetoableChange(...)
    }//end ThrottleVetoListener
    
    private class RunStateListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object newValue = evt.getNewValue();
	    //Ensure the afterburner sound isn't looping on completion of mission.
	    if(!(newValue instanceof Mission.PlayerActivity))
		ensureLoopDestroyed();
	}//end propertyChange(...)
    }//end RunModeListener
    
    @Override
    public void setParent(WorldObject newParent){
	super.setParent(newParent);
	final TR tr = newParent.getTr();
	weakRunStateListener = new WeakPropertyChangeListener(runStateListener, tr);
	tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener);
    }//end setParent(...)
    
    @Override
    public void tick(long tickTimeMillis){
	if(!installedVetoListeners)
	    installVetoListeners();
	final WorldObject p = getParent();
	if(isAfterburning())
	    fuelRemaining-=((double)p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/
			(double)Powerup.AFTERBURNER_TIME_PER_UNIT_MILLIS);
    }//end tick
    
    private void installVetoListeners(){
	probeForBehaviors(new ProjectileFiringBehaviorSubmitter(), ProjectileFiringBehavior.class);
	probeForBehavior(UserInputThrottleControlBehavior.class).addVetoableChangeListener(UserInputThrottleControlBehavior.THROTTLE_CTL_STATE,throttleVetoListener);
	installedVetoListeners = true;
    }//end installVetoListeners()
    
    private class ProjectileFiringBehaviorSubmitter extends AbstractSubmitter<ProjectileFiringBehavior>{
	@Override
	public void submit(ProjectileFiringBehavior item) {
	    item.addVetoableChangeListener(ProjectileFiringBehavior.PENDING_FIRING,firingVetoListener);
	}
    }//end ProjectileFiringBehaviorSubmitter
    
    private void afterburnerOnTransient(WorldObject p){
	//Save former max, former propulsion
	ignitionSFX();
	startLoop();
	Propelled prop = p.probeForBehavior(Propelled.class);
	formerMax=prop.getMaxPropulsion();
	formerProp=prop.getPropulsion();
	newMax=formerMax*3;
	p.probeForBehavior(Propelled.class).
	 setMaxPropulsion(newMax).
	 setPropulsion(newMax);
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
	stopLoop();
	extinguishSFX();
	Propelled prop = p.probeForBehavior(Propelled.class);
	prop.setMaxPropulsion(formerMax);
	prop.setPropulsion(formerProp);
    }//end afterburnerOffTransient(...)
    
    private void startLoop(){
	if(loopSound == null)
	    return;
	final WorldObject parent = getParent();
	    final TR              tr = parent.getTr();
	    final SoundSystem soundSystem = tr.soundSystem.get();
	    if(afterburnerLoop != null)
		ensureLoopDestroyed();
	    afterburnerLoop 
	        = soundSystem.getLoopFactory().
	            create(loopSound, new double[]{
	        	    SoundSystem.DEFAULT_SFX_VOLUME,
	        	    SoundSystem.DEFAULT_SFX_VOLUME});
	    soundSystem.enqueuePlaybackEvent(afterburnerLoop);
    }//end startLoop()
    
    private void ensureLoopDestroyed(){
	if(afterburnerLoop == null)
	    return;
	afterburnerLoop.destroy();
	afterburnerLoop = null;
    }
    
    private void stopLoop(){
	ensureLoopDestroyed();
    }

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
    public SoundTexture getLoopSound() {
        return loopSound;
    }
    public AfterburnerBehavior setLoopSound(SoundTexture loopSound) {
        this.loopSound = loopSound;
        return this;
    }

    public boolean isAfterburning() {
        return afterburning;
    }

    public void setAfterburning(boolean newValue) {
	final boolean oldValue = this.afterburning;
	if(newValue == oldValue)
	    return;
	final WorldObject parent = getParent();
	
        this.afterburning = newValue;
	
	if(newValue)
	    afterburnerOnTransient(parent);
	else
	    afterburnerOffTransient(parent);
    }//end setAfterburning(...)
    
    @Override
    public Behavior setEnable(boolean enable){
	if(isAfterburning() && this.isEnabled() && !enable)
	    setAfterburning(false);
	super.setEnable(enable);
	return this;
    }
}//end AfterburnerBehavior
