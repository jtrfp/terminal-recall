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

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GPU;

public class SamplePlaybackEvent extends AbstractSoundEvent {
    private final SoundTexture soundTexture;
    private final double[] pan;
    private final GLProgram soundProgram;
    private final double playbackRatio;
    
    private SamplePlaybackEvent(SoundTexture tex, long startTimeSamples,
		double[] pan, GLProgram soundProgram, Factory origin, SoundEvent parent) {
	    this(tex,startTimeSamples,pan,soundProgram,origin,parent,1);
	}//end constructor

    public SamplePlaybackEvent(SoundTexture tex, long startTimeSamples,
	    double[] pan, GLProgram soundProgram, Factory origin,
	    SoundEvent parent, double playbackRatio) {
	super(startTimeSamples, tex.getLengthInRealtimeSamples(), origin,
		parent);
	soundTexture = tex;
	this.pan = pan;
	this.soundProgram = soundProgram;
	this.playbackRatio = playbackRatio;
    }

    /**
     * @return the soundTexture
     */
    public SoundTexture getSoundTexture() {
        return soundTexture;
    }

    /**
     * @return the pan
     */
    public double[] getPan() {
        return pan;
    }

    @Override
    public void apply(GL3 gl, long bufferStartTimeFrames) {
	soundProgram.getUniform("pan").set((float)getPan()[0], (float)getPan()[1]);//Pan center
	final double startTimeInBuffers=((double)(getStartRealtimeSamples()-bufferStartTimeFrames)/(double)SoundSystem.BUFFER_SIZE_FRAMES)*2-1;
	soundProgram.getUniform("numRows").setui((int)getSoundTexture().getNumRows());
	soundProgram.getUniform("start").set((float)startTimeInBuffers);
	soundProgram.getUniform("lengthPerRow")
	 .set(((float)((double)SoundTexture.ROW_LENGTH_SAMPLES/(double)SoundSystem.BUFFER_SIZE_FRAMES))*2*(float)getSoundTexture().getResamplingScalar()/(float)playbackRatio);
	final int lengthInSegments = (int)(getSoundTexture().getNumRows()) * 2; //Times two because of the turn
	getSoundTexture().getGLTexture().bindToTextureUnit(0, gl);
	gl.glDrawArrays(GL3.GL_LINE_STRIP, 0, lengthInSegments+1);
    }
    
    public static class Factory extends AbstractSoundEvent.Factory{
	private GLVertexShader   soundVertexShader;
	private GLFragmentShader soundFragmentShader;//1 fragment = 1 frame
	private GLProgram soundProgram;
	
	public Factory(final TR tr) {
	    super(tr);
	    final GPU gpu = tr.gpu.get();
	    tr.getThreadManager().submitToGL(new Callable<Void>() {
		    @Override
		    public Void call() throws Exception {
			soundVertexShader = gpu.newVertexShader();
			    soundFragmentShader = gpu.newFragmentShader();
			    soundVertexShader
				    .setSourceFromResource("/shader/soundVertexShader.glsl");
			    soundFragmentShader
				    .setSourceFromResource("/shader/soundFragShader.glsl");
			    soundProgram = gpu.newProgram().attachShader(soundVertexShader)
				    .attachShader(soundFragmentShader).link().use();
			return null;
		    }// end call()
		}).get();
	}//end constructor

	@Override
	public void apply(GL3 gl, Collection<SoundEvent> events, long bufferStartTimeFrames) {
	    gl.glLineWidth(1);
	    gl.glDisable(GL3.GL_LINE_SMOOTH);
	    gl.glEnable(GL3.GL_BLEND);
	    gl.glDepthFunc(GL3.GL_ALWAYS);
	    gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	    gl.glDepthMask(false);
	    gl.glBlendFunc(GL3.GL_ONE, GL3.GL_ONE);
	    soundProgram.use();
	    soundProgram.getUniform("soundTexture").set((int)0);
	    for(SoundEvent ev:events){
		ev.apply(gl, bufferStartTimeFrames);
	    }
	    //Cleanup
	    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glDisable(GL3.GL_BLEND);
	}//end apply(...)
	
	public SamplePlaybackEvent create(SoundTexture tex, double [] pan){
	    final SoundSystem ss = getTR().soundSystem.get();
	    final double modSamples = (System.currentTimeMillis()*getTR().soundSystem.get().getSamplesPerMilli())%SoundSystem.BUFFER_SIZE_FRAMES;
	    return create(tex,(long)(ss.getCurrentBufferFrameCounter()+modSamples),pan);
	}
	
	public SamplePlaybackEvent create(SoundTexture tex, long startTimeSamples,
		double[] pan){
	    return new SamplePlaybackEvent(tex,startTimeSamples,pan,soundProgram,this,null);
	}//end create(...)
	public SamplePlaybackEvent create(SoundTexture tex, long startTimeSamples,
		double[] pan, SoundEvent parent){
	    return new SamplePlaybackEvent(tex,startTimeSamples,pan,soundProgram,this,parent);
	}//end create(...)
	public SamplePlaybackEvent create(SoundTexture tex, long startTimeSamples,
		double[] pan, SoundEvent parent, double playbackRatio){
	    return new SamplePlaybackEvent(tex,startTimeSamples,pan,soundProgram,this,parent,playbackRatio);
	}//end create(...)
    }//end Factory
}//end SamplePlaybackEvent
