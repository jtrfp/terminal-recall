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

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class SamplePlaybackEvent extends AbstractSoundEvent {
    private final SoundTexture soundTexture;
    private final double[] pan;
    private final double playbackRatio;
    private Double lengthInSeconds = null;
    
    private SamplePlaybackEvent(SoundTexture tex, double startTimeSeconds,
		double[] pan, Factory origin, SoundEvent parent) {
	    this(tex,startTimeSeconds,pan,origin,parent,1);
	}//end constructor

    public SamplePlaybackEvent(SoundTexture tex, double startTimeSeconds,
	    double[] pan, Factory origin,
	    SoundEvent parent, double playbackRatio) {
	this(tex,startTimeSeconds,pan,origin,parent,playbackRatio,tex.getLengthInRealtimeSeconds());
    }

    public SamplePlaybackEvent(SoundTexture tex, double startTimeSeconds,
	    double[] pan, Factory factory, SoundEvent parent,
	    double playbackRatio, double lengthInSeconds) {
	super(startTimeSeconds, lengthInSeconds, factory,
		parent);
	soundTexture = tex;
	this.pan = pan;
	this.playbackRatio = playbackRatio;
	this.lengthInSeconds = lengthInSeconds;
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
    public void apply(GL3 gl, double bufferStartTimeSeconds) {
	SamplePlaybackEvent.Factory origin = (SamplePlaybackEvent.Factory)getOrigin();
	origin.getPanU().set((float)getPan()[0], (float)getPan()[1]);//Pan center
	final SoundSystem ss           = this.getOrigin().getTR().soundSystem.get();
	final double bufferSizeSeconds = ss.getBufferSizeSeconds(),
	             startTimeInBuffers=((getStartRealtimeSeconds()-bufferStartTimeSeconds)/(double)bufferSizeSeconds)*2-1,
	             lengthPerRow      = getSoundTexture().getLengthPerRowSeconds();
	final int    lengthInSegments  = (int)(getSoundTexture().getNumRows()) * 2; //Times two because of the turn
	origin.getNumRowsU().setui(getSoundTexture().getNumRows());
	origin.getStartU().set((float)startTimeInBuffers);
	origin.getLengthPerRowU()
	 .set((float)((2/playbackRatio)*(lengthPerRow/bufferSizeSeconds)));
	getSoundTexture().getGLTexture().bindToTextureUnit(0, gl);
	gl.glDrawArrays(GL3.GL_LINE_STRIP, 0, lengthInSegments+1);
    }//end apply(...)
    
    public static class Factory extends AbstractSoundEvent.Factory{
	private GLVertexShader   soundVertexShader;
	private GLFragmentShader soundFragmentShader;//1 fragment = 1 frame
	private GLProgram soundProgram;
	private GLUniform panU,numRowsU,startU,lengthPerRowU,soundTextureU;
	
	public Factory(final TR tr) {
	    super(tr);
	    final GPU gpu = tr.gpu.get();
	    tr.getThreadManager().submitToGL(new Callable<Void>() {
		    @Override
		    public Void call() throws Exception {
			soundVertexShader      = gpu.newVertexShader();
			    soundFragmentShader= gpu.newFragmentShader();
			    soundVertexShader
				    .setSourceFromResource("/shader/soundVertexShader.glsl");
			    soundFragmentShader
				    .setSourceFromResource("/shader/soundFragShader.glsl");
			    soundProgram = gpu.newProgram().attachShader(soundVertexShader)
				    .attachShader(soundFragmentShader).link().use();
			    panU         = soundProgram.getUniform("pan");
			    numRowsU     = soundProgram.getUniform("numRows");
			    startU       = soundProgram.getUniform("start");
			    lengthPerRowU= soundProgram.getUniform("lengthPerRow");
			    soundTextureU= soundProgram.getUniform("soundTexture");
			return null;
		    }// end call()
		}).get();
	}//end constructor

	@Override
	public void apply(GL3 gl, Collection<SoundEvent> events, double bufferStartTimeSeconds) {
	    gl.glLineWidth(1);
	    gl.glDisable(GL3.GL_LINE_SMOOTH);
	    gl.glDisable(GL3.GL_CULL_FACE);
	    gl.glEnable(GL3.GL_BLEND);
	    gl.glDepthFunc(GL3.GL_ALWAYS);
	    gl.glProvokingVertex(GL3.GL_FIRST_VERTEX_CONVENTION);
	    gl.glDepthMask(false);
	    gl.glBlendFunc(GL3.GL_ONE, GL3.GL_ONE);
	    soundProgram.use();
	    soundTextureU.set((int)0);
	    for(SoundEvent ev:events)
		ev.apply(gl, bufferStartTimeSeconds);
	    //Cleanup
	    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	    gl.glDisable(GL3.GL_BLEND);
	}//end apply(...)
	
	public SamplePlaybackEvent create(SoundTexture tex, double [] source, Camera dest, double volumeScalar){
	    return create(tex, source, dest, volumeScalar, 1);
	}
	
	public SamplePlaybackEvent create(SoundTexture tex, double [] source, Camera dest, double volumeScalar, double samplePlaybackRatio){
	    final double UNIT_FACTOR = TR.mapSquareSize*8;
	    final double dist = TR.rolloverDistance(Vect3D.distance(source, dest.getPosition()));
	    final double unitDist    = dist/UNIT_FACTOR;
	    final double vol = Misc.clamp(1./Math.pow(unitDist, 2),
		    0,1)*volumeScalar;
	    final double [] work     = new double[3];
	    final double [] destPos  = dest.getPosition();
	    TR.twosComplimentSubtract(source, destPos, work);
	    Rotation rot;
	    try{rot = new Rotation(dest.getHeading(), dest.getTop(),Vector3D.PLUS_K, Vector3D.PLUS_J);}
	    catch(MathArithmeticException e){rot = new Rotation(Vector3D.PLUS_K, 0);}//Default if given weird top/heading.
	    if((work[0]==0)&&(work[1]==0)&&(work[2]==0))
		work[0]=1;
	    final Vector3D localDir = rot.applyTo(new Vector3D(work)).normalize();
	    final double   pFactor  = (localDir.getX()+1)/2;
	    assert !Vect3D.isAnyNaN(source);
	    final double [] pan     = new double[]{vol*pFactor,vol*(1-pFactor)};
	    final SoundSystem ss    = getTR().soundSystem.get();
	    // Temporal dither to avoid phasiness
	    final double delay      = dist*.000001+Math.random()*.005;
	    final double startTime  = ss.getCurrentFrameBufferTimeCounter()+delay;
	    return create(tex,startTime,pan, null, samplePlaybackRatio);
	}//end create(...)
	
	public SamplePlaybackEvent create(SoundTexture tex, WorldObject source, Camera dest, double volumeScalar){
	    return create(tex,source.getPosition(),dest,volumeScalar);
	}
	
	public SamplePlaybackEvent create(SoundTexture tex, double [] pan){
	    final SoundSystem ss = getTR().soundSystem.get();
	    return create(tex,(ss.getCurrentFrameBufferTimeCounter()),pan);
	}
	
	public SamplePlaybackEvent create(SoundTexture tex, double startTimeSeconds,
		double[] pan){
	    return new SamplePlaybackEvent(tex,startTimeSeconds,pan,this,null);
	}//end create(...)
	public SamplePlaybackEvent create(SoundTexture tex, double startTimeSeconds,
		double[] pan, SoundEvent parent){
	    return new SamplePlaybackEvent(tex,startTimeSeconds,pan,this,parent);
	}//end create(...)
	public SamplePlaybackEvent create(SoundTexture tex, double startTimeSeconds,
		double[] pan, SoundEvent parent, double playbackRatio){
	    return new SamplePlaybackEvent(tex,startTimeSeconds,pan,this,parent,playbackRatio);
	}//end create(...)
	
	public SamplePlaybackEvent create(SoundTexture tex, double startTimeSeconds,
		double[] pan, SoundEvent parent, double playbackRatio, double lengthSeconds){
	    return new SamplePlaybackEvent(tex,startTimeSeconds,pan,this,parent,playbackRatio,lengthSeconds);
	}//end create(...)

	/**
	 * @return the panU
	 */
	GLUniform getPanU() {
	    return panU;
	}

	/**
	 * @return the startU
	 */
	GLUniform getStartU() {
	    return startU;
	}

	/**
	 * @return the lengthPerRowU
	 */
	GLUniform getLengthPerRowU() {
	    return lengthPerRowU;
	}

	/**
	 * @return the numRowsU
	 */
	GLUniform getNumRowsU() {
	    return numRowsU;
	}

	/**
	 * @return the soundProgram
	 */
	GLProgram getSoundProgram() {
	    return soundProgram;
	}
    }//end Factory

    /**
     * @return the playbackRatio
     */
    public double getPlaybackRatio() {
        return playbackRatio;
    }
}//end SamplePlaybackEvent
