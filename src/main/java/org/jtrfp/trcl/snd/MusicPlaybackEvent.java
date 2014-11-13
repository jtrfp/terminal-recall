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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.obj.VisibleEverywhere;

public class MusicPlaybackEvent extends AbstractSoundEvent implements VisibleEverywhere {
    private final GPUResidentMOD mod;
    private final boolean loop;
    private static final int SETUP_PADDING_FRAMES = 1024*16;//Ample time to set up next loop.
    private long nextLoopTimeFrames;
    private TRFutureTask<Void> lastApply;
    private volatile boolean isPlaying = false;

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
	    nextLoopTimeFrames=getOrigin().getTR().soundSystem.get().getCurrentBufferFrameCounter();
	    isPlaying=true;}
    }
    
    public void stop(){
	destroy();
	isPlaying=false;
    }//end stop()

    @Override
    public void apply(GL3 gl, final long bufferStartTimeFrames) {// Non-blocking.
	if(lastApply!=null)
	    if(!lastApply.isDone())
		return;
	lastApply = getOrigin().getTR().getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		// Set the song up
		if(loop){
		    if(bufferStartTimeFrames > nextLoopTimeFrames-SETUP_PADDING_FRAMES){
			mod.apply(MusicPlaybackEvent.this.nextLoopTimeFrames,MusicPlaybackEvent.this,
				getOrigin().getTR().getTrConfig()[0].getModStereoWidth());
			MusicPlaybackEvent.this.nextLoopTimeFrames+=mod.getSongLengthInBufferFrames();
		    }//end if(time to loop)
		}//end if(loop)
		return null;
	    }//end call()
	});//end submit()
    }//end apply()
    
    public static class Factory extends AbstractSoundEvent.Factory{

	protected Factory(TR tr) {
	    super(tr);
	}

	@Override
	public void apply(GL3 gl, Collection<SoundEvent> events,
		long bufferStartTimeFrames) {
	    for(SoundEvent event:events)
		event.apply(gl, bufferStartTimeFrames);
	}//end apply(...)
	
	
	public MusicPlaybackEvent create(GPUResidentMOD mod, boolean loop){
	    return create(mod,loop,null);
	}
	
	protected MusicPlaybackEvent create(GPUResidentMOD mod,boolean loop, MusicPlaybackEvent parent){
	    return new MusicPlaybackEvent(this,mod,loop,parent);
	}//end create(...)
    }//end Factory

}//end MusicPlaybackEvent
