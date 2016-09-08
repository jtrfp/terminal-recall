/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.snd;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL2ES2;

import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.obj.RelevantEverywhere;

public class MusicPlaybackEvent extends AbstractSoundEvent implements RelevantEverywhere {
    private final GPUResidentMOD mod;
    private final boolean loop;
    private static final int SETUP_PADDING_SECS = 2;//Ample time to set up next loop.
    private double nextLoopTimeSeconds;
    private TRFutureTask<Void> lastApply;
    private volatile boolean isPlaying = false;
    private AtomicBoolean firstRun = new AtomicBoolean(true);

    private MusicPlaybackEvent(
	    Factory origin, GPUResidentMOD mod, boolean loop, SoundEvent parent) {
	super(0L, 0L, origin, parent);
	//this.songStartTimeBufferFrames=songStartTimeBufferFrames;
	this.deactivate();
	this.mod=mod;
	this.loop=loop;
    }//end constructor
    
    public void play(){
	if(!isPlaying){
	    activate();
	    firstRun.set(true);
	    nextLoopTimeSeconds=Features.get(getOrigin().getTR(),SoundSystemFeature.class).getCurrentFrameBufferTimeCounter();
	    isPlaying=true;}
    }
    
    public void stop(){
	destroy();
	isPlaying=false;
    }//end stop()

    @Override
    public void apply(GL2ES2 gl, final double bufferStartTimeSeconds) {// Non-blocking.
	if(lastApply!=null)
	    if(!lastApply.isDone())
		return;
	if(loop || firstRun.get())
	  if(bufferStartTimeSeconds > nextLoopTimeSeconds-SETUP_PADDING_SECS)
	     lastApply = getOrigin().getTR().getThreadManager().submitToThreadPool(new Callable<Void>(){
	     @Override
	     public Void call() throws Exception {
		// Set the song up
		mod.apply(MusicPlaybackEvent.this.nextLoopTimeSeconds,MusicPlaybackEvent.this,
			Features.get(getOrigin().getTR(),TRConfiguration.class).getModStereoWidth());
		MusicPlaybackEvent.this.nextLoopTimeSeconds+=mod.getSongLengthInRealtimeSeconds();
		firstRun.set(false);
		return null;
	     }//end call()
	    });//end submit()
    }//end apply()
    
    public static class Factory extends AbstractSoundEvent.Factory{

	protected Factory(TR tr) {
	    super(tr);
	}

	@Override
	public void apply(GL2ES2 gl, Collection<SoundEvent> events,
		double bufferStartTimeSeconds) {
	    for(SoundEvent event:events)
		event.apply(gl, bufferStartTimeSeconds);
	}//end apply(...)
	
	
	public MusicPlaybackEvent create(GPUResidentMOD mod, boolean loop){
	    return create(mod,loop,null);
	}
	
	protected MusicPlaybackEvent create(GPUResidentMOD mod,boolean loop, MusicPlaybackEvent parent){
	    return new MusicPlaybackEvent(this,mod,loop,parent);
	}//end create(...)
    }//end Factory

}//end MusicPlaybackEvent
