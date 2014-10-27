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

package org.jtrfp.trcl.snd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GPU;

public final class SoundSystem {
    private final TR tr;
    private GLFrameBuffer playbackFrameBuffer;
    private GLTexture playbackTexture;
    private GLVertexShader soundVertexShader;
    private GLFragmentShader soundFragmentShader;//1 fragment = 1 frame
    private GLProgram soundProgram;
    private boolean firstRun=true;
    private final TreeSet<PlaybackEvent> pendingEvents = new TreeSet<PlaybackEvent>(new Comparator<PlaybackEvent>(){
	@Override
	public int compare(PlaybackEvent first, PlaybackEvent second) {
	    final long result = first.getStartTimeSamples()-second.getStartTimeSamples();
	    if(result==0L)return 0;
	    else if(result>0)return 1;
	    else return -1;
	}//end compare()
    });
    private final ArrayList<PlaybackEvent> activeEvents = new ArrayList<PlaybackEvent>();
    private long bufferStartTimeSamples;
    
    private static final int SAMPLE_RATE=44100;
    private static final int BUFFER_SIZE_FRAMES=4096;
    public static final int NUM_CHANNELS=2;
    public static final int BYTES_PER_SAMPLE=4;
    private static final int SAMPLE_SIZE_BITS = BYTES_PER_SAMPLE*8;
    public static final int BYTES_PER_FRAME=BYTES_PER_SAMPLE*NUM_CHANNELS;
    private static final int BUFFER_SIZE_BYTES=BUFFER_SIZE_FRAMES*BYTES_PER_FRAME;
    private static final int NUM_BUFFER_ROWS=8;
    //private static final double SAMPLES_PER_MILLI = (double)SAMPLE_RATE/1000.;
    
    private static final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    private static final AudioFormat audioFormat = new AudioFormat(
	    encoding,
	    (float)SAMPLE_RATE,
	    SAMPLE_SIZE_BITS,
	    NUM_CHANNELS,
	    BYTES_PER_FRAME,
	    SAMPLE_RATE,
	    ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN,
	    new HashMap<String,Object>());
    private static final DataLine.Info lineInfo = 
	    new DataLine.Info(SourceDataLine.class, audioFormat);
    
    private SourceDataLine sourceDataLine;
    private final byte [] outputBuffer = new byte[BUFFER_SIZE_BYTES*NUM_BUFFER_ROWS];
    private final ByteBuffer outputByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE_BYTES*NUM_BUFFER_ROWS);
    private final FloatBuffer outputFloatBuffer = outputByteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
    private final IntBuffer outputIntBuffer = ByteBuffer.wrap(outputBuffer).order(ByteOrder.nativeOrder()).asIntBuffer();
    
    public SoundSystem(final TR tr){
	this.tr=tr;
	System.out.println("Setting up sound system...");
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		System.out.println("SoundSystem: setting up textures...");
		final GPU gpu = tr.gpu.get();
		playbackTexture = 
			gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG32F, BUFFER_SIZE_FRAMES, NUM_BUFFER_ROWS, GL3.GL_RGBA, GL3.GL_FLOAT, null);
		playbackFrameBuffer = 
			gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(playbackTexture, GL3.GL_COLOR_ATTACHMENT0);
		soundVertexShader =
			gpu.newVertexShader();
		soundFragmentShader =
			gpu.newFragmentShader();
		soundVertexShader.setSourceFromResource("/shader/soundVertexShader.glsl");
		soundFragmentShader.setSourceFromResource("/shader/soundFragShader.glsl");
		soundProgram = gpu.newProgram()
			.attachShader(soundVertexShader)
			.attachShader(soundFragmentShader)
			.link()
			.use();
		//TODO: Setup uniforms here.
		System.out.println("...Done.");
		return null;
	    }//end call()
	}).get();
	try{sourceDataLine = (SourceDataLine)AudioSystem.getLine(lineInfo); sourceDataLine.open();}
	catch(LineUnavailableException e){tr.showStopper(e);}
	
	//Startup test beep.
	FloatBuffer fb = FloatBuffer.allocate(4096);
	for(int i=0; i<4096; i++){
	    fb.put((float)Math.sin(i*.01*2*Math.PI));
	}//end for(4096)
	fb.clear();
	enqueuePlaybackEvent(newSoundTexture(fb,44100),0);
    }//end constructor
    
    public SoundTexture newSoundTexture(final FloatBuffer samples, final int localSampleRate){
	final GLTexture texture = tr.gpu.get().newTexture();
	final int lengthInSamples = samples.remaining();
	final double resamplingRatio = (double)SAMPLE_RATE / (double)localSampleRate;
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		texture
		 .setBindingTarget(GL3.GL_TEXTURE_1D)
		 .bind()
		 .setMagFilter(GL3.GL_NEAREST)
		 .setMinFilter(GL3.GL_NEAREST)
		 .setImage1D(GL3.GL_R32F, samples.remaining(), GL3.GL_RED, GL3.GL_FLOAT, samples);
		return null;
	    }}).get();
	return new SoundTexture(){
	    @Override
	    public int getLengthInSamples() {
		return (int)((double)lengthInSamples *(double)resamplingRatio);
	    }

	    @Override
	    public GLTexture getGLTexture() {
		return texture;
	    }
	};//end new SoundTexture()
    }//end newSoundTexture
    
    public void enqueuePlaybackEvent(SoundTexture tex, long startTimeSamples){
	pendingEvents.add(new PlaybackEvent(tex,startTimeSamples));
    }
    
    private void firstRun(){
	sourceDataLine.start();
	firstRun=false;
    }
    
    public void render(GL3 gl){
	if(firstRun)firstRun();
	    final int dataLineAvailable = sourceDataLine.available();
	    final int framesAvailable = dataLineAvailable/BYTES_PER_FRAME;
	    final int framesToWrite = framesAvailable<=BUFFER_SIZE_FRAMES?framesAvailable:BUFFER_SIZE_FRAMES;
	    cleanActiveEvents();
	    pickupActiveEvents(framesAvailable);
	    playbackFrameBuffer.bindToDraw();
	    gl.glViewport(0, 0, framesToWrite, 1);
	    gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	    gl.glLineWidth(1);
	    gl.glDepthFunc(GL3.GL_ALWAYS);
	    gl.glDepthMask(false);
	    gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE);
	    soundProgram.use();
	    soundProgram.getUniform("soundTexture").set((int)0);
	    
	    //Render
	    for(PlaybackEvent ev:activeEvents){
		soundProgram.getUniform("pan").set(.5f, .5f);//Pan center
		final double startTime=((ev.getStartTimeSamples()-bufferStartTimeSamples)/framesToWrite)*2-1;
		final double len = ev.getDurationSamples()/framesToWrite;
		soundProgram.getUniform("start").set((float)startTime);
		soundProgram.getUniform("length").set((float)len);
		ev.getSoundTexture().getGLTexture().bindToTextureUnit(0, gl);
		gl.glDrawArrays(GL3.GL_LINES, 0, 2);
	    }//end for(events)
	    /*
	    /// DEBUG
	    gl.glViewport(0, 0, 4096, 1);//DEBUG
	    soundProgram.getUniform("pan").set(.5f, .5f);//Pan center
		final double startTime=-1;
		final double len = 2;
		soundProgram.getUniform("start").set((float)startTime);
		soundProgram.getUniform("length").set((float)len);
		//ev.getSoundTexture().getGLTexture().bindToTextureUnit(0, gl);
		gl.glDrawArrays(GL3.GL_LINES, 0, 2);
	    //////////
	    */
	    //Read and export to sound card.
	    gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);//Unbind so we can read off the output
	    outputByteBuffer.clear();
	    playbackTexture.bind().readPixels(GL3.GL_RG, GL3.GL_FLOAT, outputByteBuffer);// RG_INTEGER throws INVALID_OPERATION!?
	    outputByteBuffer.clear();
	    outputFloatBuffer.clear();
	    outputIntBuffer.clear();
	    for(int i=framesToWrite; i>0; i--){//Doing conversion manually until GL's conversion issue is figured out.
		outputIntBuffer.put((int)((double)outputFloatBuffer.get()*(double)Integer.MAX_VALUE));
	    }
	    sourceDataLine.write(outputBuffer, 0, dataLineAvailable);
	bufferStartTimeSamples+=framesToWrite;
	
	//Cleanup
	gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	
    }//end process()
    
    class PlaybackEvent{
	private final long startTimeSamples;
	private final long endTimeSamples;
	private final long durationSamples;
	private final SoundTexture soundTexture;
	public PlaybackEvent(SoundTexture tex, long startTimeSamples){
	    durationSamples = tex.getLengthInSamples();
	    this.startTimeSamples = startTimeSamples;
	    endTimeSamples = startTimeSamples+durationSamples;
	    soundTexture = tex;
	}
	public long getDurationSamples() {
	    return durationSamples;
	}
	/**
	 * @return the startTimeSamples
	 */
	public long getStartTimeSamples() {
	    return startTimeSamples;
	}
	/**
	 * @return the endTimeSamples
	 */
	public long getEndTimeSamples() {
	    return endTimeSamples;
	}
	
	public SoundTexture getSoundTexture(){
	    return soundTexture;
	}
    }//end PlaybackEvent
    
    private void pickupActiveEvents(long windowSizeInSamples){
	final long currentTimeSamples = bufferStartTimeSamples;
	final Iterator<PlaybackEvent> eI = pendingEvents.iterator();
	while(eI.hasNext()){
	    final PlaybackEvent event = eI.next();
	    if(event.getStartTimeSamples()<currentTimeSamples+windowSizeInSamples && event.getEndTimeSamples()>currentTimeSamples){
		activeEvents.add(event);
		eI.remove();
	    }//end if(in range)
	    if(event.getStartTimeSamples()>currentTimeSamples+windowSizeInSamples)
		return;//Everything after this point is out of range.
	}//end hashNext(...)
    }//end pickupActiveEvents()
    
    private void cleanActiveEvents(){
	final long currentTimeSamples = bufferStartTimeSamples;
	final Iterator<PlaybackEvent> eI = activeEvents.iterator();
	while(eI.hasNext()){
	    final PlaybackEvent event = eI.next();
	    if(event.getEndTimeSamples()<currentTimeSamples)
		eI.remove();
	}//end while(hasNext)
    }//end cleanActiveEvents()
}//end SoundSystem
