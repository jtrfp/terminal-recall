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

package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SamplePlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class RandomSFXPlayback extends Behavior implements DeathListener {
    private long timeOfLastPlaybackMillis;
    private long minTimeBetweenPlaybacksMillis = 1000;
    private double playbackProbabilityPerSecond = .5;
    private long timeOfLastTick;
    private SoundTexture soundTexture;
    private boolean disableOnDeath = true;
    private double playbackSpeedVariation = .1;
    private double volumeScalar = SoundSystem.DEFAULT_SFX_VOLUME;
    
    @Override
    public void tick(long tickTimeMillis){
	if(soundTexture==null)
	    return;
	final long millisSinceLastPlayback =  tickTimeMillis - timeOfLastPlaybackMillis;
	if(millisSinceLastPlayback > minTimeBetweenPlaybacksMillis){
	    final long millisSinceLastTick = tickTimeMillis - timeOfLastTick;
	    final double secondsSinceLastTick = millisSinceLastTick / 1000.;
	    final double probabilityOfFailurePerSecond = 1.-playbackProbabilityPerSecond;
	    final double probabilityOfFailureInSecondsPassed 
	        = Math.pow(probabilityOfFailurePerSecond, secondsSinceLastTick);
	    
	    if(Math.random() > probabilityOfFailureInSecondsPassed){
		playSound();
		timeOfLastPlaybackMillis = tickTimeMillis;
	    }//end if(playback)
	    timeOfLastTick = tickTimeMillis;
	}//emd if(ok to proceed)
    }//end tick()
    
    @Override
    public void notifyDeath() {
	if(isDisableOnDeath())
	    setEnable(false);
    }//end notifyDeath()
    
    private void playSound(){
	final WorldObject parent = getParent();
	final TR tr = parent.getTr();
	final SoundSystem soundSystem = tr.soundSystem.get();
	final double playbackRatio = 1+(((Math.random()*2)-1)*getPlaybackSpeedVariation());
	//Play the sound
	SamplePlaybackEvent playbackEvent = soundSystem.
	  getPlaybackFactory().
	  create(soundTexture, getParent().getPositionWithOffset(), tr.mainRenderer.get().getCamera(), SoundSystem.DEFAULT_SFX_VOLUME,playbackRatio);
	soundSystem.enqueuePlaybackEvent(playbackEvent);
    }//end playSound()

    public long getMinTimeBetweenPlaybacksMillis() {
        return minTimeBetweenPlaybacksMillis;
    }

    public RandomSFXPlayback setMinTimeBetweenPlaybacksMillis(long minTimeBetweenPlaybacksMillis) {
        this.minTimeBetweenPlaybacksMillis = minTimeBetweenPlaybacksMillis;
        return this;
    }

    public double getPlaybackProbabilityPerSecond() {
        return playbackProbabilityPerSecond;
    }

    public RandomSFXPlayback setPlaybackProbabilityPerSecond(double playbackProbabilityPerSecond) {
        this.playbackProbabilityPerSecond = playbackProbabilityPerSecond;
        return this;
    }

    public SoundTexture getSoundTexture() {
        return soundTexture;
    }

    public RandomSFXPlayback setSoundTexture(SoundTexture soundTexture) {
        this.soundTexture = soundTexture;
        return this;
    }

    public boolean isDisableOnDeath() {
        return disableOnDeath;
    }

    public RandomSFXPlayback setDisableOnDeath(boolean disableOnDeath) {
        this.disableOnDeath = disableOnDeath;
        return this;
    }

    public double getPlaybackSpeedVariation() {
        return playbackSpeedVariation;
    }

    public RandomSFXPlayback setPlaybackSpeedVariation(double playbackSpeedVariation) {
        this.playbackSpeedVariation = playbackSpeedVariation;
        return this;
    }

    public double getVolumeScalar() {
        return volumeScalar;
    }

    public RandomSFXPlayback setVolumeScalar(double volumeScalar) {
        this.volumeScalar = volumeScalar;
        return this;
    }

}//end RandomSFXPlayback
