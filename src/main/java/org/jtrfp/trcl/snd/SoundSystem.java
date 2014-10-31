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

import java.nio.BufferUnderflowException;
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
    private GLVertexShader   soundVertexShader;
    private GLFragmentShader soundFragmentShader;//1 fragment = 1 frame
    private GLProgram soundProgram;
    private boolean firstRun=true;
    private final TreeSet<PlaybackEvent> pendingEvents = new TreeSet<PlaybackEvent>(new Comparator<PlaybackEvent>(){
	@Override
	public int compare(PlaybackEvent first, PlaybackEvent second) {
	    final long result = first.getStartTimeSamples()-second.getStartTimeSamples();
	    if(result==0L)return first.hashCode()-second.hashCode();
	    else if(result>0)return 1;
	    else return -1;
	}//end compare()
    });
    private final ArrayList<PlaybackEvent> activeEvents = new ArrayList<PlaybackEvent>();
    private long bufferStartTimeSamples;
    
    private static final int SAMPLE_RATE=44100;
    private static final int BUFFER_SIZE_FRAMES=4096*2;
    public static final int NUM_CHANNELS=2;
    public static final int BYTES_PER_SAMPLE=4;
    private static final int SAMPLE_SIZE_BITS = BYTES_PER_SAMPLE*8;
    public static final int BYTES_PER_FRAME=BYTES_PER_SAMPLE*NUM_CHANNELS;
    private static final int BUFFER_SIZE_BYTES=BUFFER_SIZE_FRAMES*BYTES_PER_FRAME;
    private static final int NUM_BUFFER_ROWS=1;
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

    public SoundSystem(final TR tr) {
	this.tr = tr;
	System.out.println("Setting up sound system...");
	tr.getThreadManager().submitToGL(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		System.out.println("SoundSystem: setting up textures...");
		final GPU gpu = tr.gpu.get();
		playbackTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG32F, BUFFER_SIZE_FRAMES,
				NUM_BUFFER_ROWS, GL3.GL_RGBA, GL3.GL_FLOAT,
				null);
		playbackFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(playbackTexture,
				GL3.GL_COLOR_ATTACHMENT0);
		soundVertexShader = gpu.newVertexShader();
		soundFragmentShader = gpu.newFragmentShader();
		soundVertexShader
			.setSourceFromResource("/shader/soundVertexShader.glsl");
		soundFragmentShader
			.setSourceFromResource("/shader/soundFragShader.glsl");
		soundProgram = gpu.newProgram().attachShader(soundVertexShader)
			.attachShader(soundFragmentShader).link().use();
		// TODO: Setup uniforms here.
		System.out.println("...Done.");
		return null;
	    }// end call()
	}).get();
	try {
	    sourceDataLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
	    sourceDataLine.open();
	} catch (LineUnavailableException e) {
	    tr.showStopper(e);
	}

	// Startup test beep.
	final IntBuffer fb = IntBuffer.allocate(4096 * 256);
	final int period=1024*64;
	for (int i = 0; i < fb.capacity(); i++) {
	    if(i%period < 128)
		fb.put((int) (Math.sin(i * .1)*(double)Integer.MAX_VALUE));
	    else
		fb.put((int) ((((i%period)-period/2.) / (period/2.))*(double)Integer.MAX_VALUE));
	}// end for(capacity)
	fb.clear();
	
	// /// DEBUG
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		 //enqueuePlaybackEvent(newSoundTexture(fb,44100),44100);
		 //new MusicPlayer(tr);
		return null;
	    }
	    
	});
	new Thread() {
	    @Override
	    public void run() {
		try {
		    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		    byte[] intBytes = new byte[BUFFER_SIZE_BYTES];
		    final ByteBuffer floatBytes = ByteBuffer.allocateDirect(
			    BUFFER_SIZE_BYTES).order(ByteOrder.nativeOrder());
		    IntBuffer iBuf = ByteBuffer.wrap(intBytes)
			    .order(ByteOrder.nativeOrder()).asIntBuffer();
		    FloatBuffer fBuf = floatBytes.asFloatBuffer();
		    sourceDataLine.start();
		    while (true) {
			tr.getThreadManager().submitToGL(new Callable<Void>() {
			    @Override
			    public Void call() throws Exception {
				floatBytes.clear();
				render(tr.gpu.get().getGl(), floatBytes);
				return null;
			    }
			}).get();
			iBuf.clear();
			fBuf.clear();
			for (int i = 0; i < BUFFER_SIZE_FRAMES * NUM_CHANNELS; i++) {
			    iBuf.put((int) (fBuf.get() * (double) Integer.MAX_VALUE));
			}
			sourceDataLine.write(intBytes, 0, BUFFER_SIZE_BYTES);
		    }// end while(true)
		} catch (Exception e) {
		    tr.showStopper(e);
		}
	    }// end run()
	}.start();
    }// end constructor
    
    /**
     * 
     * @param samples 
     * @param localSampleRate
     * @return
     * @since Oct 28, 2014
     */
    public SoundTexture newSoundTexture(final IntBuffer samples, final int localSampleRate){
	final FloatBuffer fb = FloatBuffer.allocate(samples.remaining());
	fb.clear();
	while(samples.hasRemaining()){
	    fb.put((float)((double)samples.get()/(double)Integer.MAX_VALUE));
	}
	fb.clear();
	return newSoundTexture(fb,localSampleRate);
    }//end newSoundTexture(...)
    
    /**
     * 
     * @param samples 
     * @param localSampleRate
     * @return
     * @since Oct 28, 2014
     */
    public SoundTexture newSoundTexture(FloatBuffer samples, final int localSampleRate){
	final GLTexture texture = tr.gpu.get().newTexture();
	final int lengthInSamples = samples.remaining();
	final double resamplingRatio = (double)SAMPLE_RATE / (double)localSampleRate;
	final int numRows=(int)(Math.ceil((double)lengthInSamples / (double)SoundTexture.ROW_LENGTH_SAMPLES));
	final int quantizedSize = numRows*SoundTexture.ROW_LENGTH_SAMPLES;
	    final FloatBuffer fb = FloatBuffer.allocate(quantizedSize);
	   try{
	    for(int row=0; row<numRows; row++){
		int oldPos=fb.position();
		int newPos=fb.position()+SoundTexture.ROW_LENGTH_SAMPLES;
		boolean reverse = row%2==1;
		if(!reverse){// Forward
		    for(int i=oldPos; i<newPos;i++){
			fb.put(i, samples.get());
		    }//end for(i)
		    fb.position(newPos);//TODO: Optimize
		}else{       // Reverse
		    for(int i=newPos-1; i>=oldPos;i--){
			fb.put(i, samples.get());
		    }//end for(i)
		    fb.position(newPos);//TODO: Optimize
		}//end (reverse)
	    }//end for(row)
	   }catch(BufferUnderflowException e){}
	    fb.clear();
	
	final FloatBuffer finalSamples = fb;
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		texture
		 .bind()
		 .setMagFilter(GL3.GL_NEAREST)
		 .setMinFilter(GL3.GL_NEAREST)
		 .setWrapS(GL3.GL_CLAMP_TO_EDGE)
		 .setWrapT(GL3.GL_CLAMP_TO_EDGE)
		 .setImage(
			 GL3.GL_R32F, 
			 SoundTexture.ROW_LENGTH_SAMPLES, 
			 numRows, 
			 GL3.GL_RED, 
			 GL3.GL_FLOAT, 
			 finalSamples);
		return null;
	    }}).get();
	return new SoundTexture(){
	    @Override
	    public int getLengthInRealtimeSamples() {
		return (int)((double)lengthInSamples *(double)resamplingRatio);
	    }

	    @Override
	    public GLTexture getGLTexture() {
		return texture;
	    }

	    @Override
	    public int getNumRows() {
		return numRows;
	    }

	    @Override
	    public double getResamplingScalar() {
		return resamplingRatio;
	    }
	};//end new SoundTexture()
    }//end newSoundTexture
    
    public void enqueuePlaybackEvent(SoundTexture tex, long startTimeSamples){
	pendingEvents.add(new PlaybackEvent(tex,startTimeSamples));
    }
    
    private void firstRun(){
	firstRun=false;
    }
    
    public void render(GL3 gl, ByteBuffer audioByteBuffer){
	if(firstRun)firstRun();
	    cleanActiveEvents();
	    pickupActiveEvents(BUFFER_SIZE_FRAMES);
	    playbackFrameBuffer.bindToDraw();
	    gl.glViewport(0, 0, BUFFER_SIZE_FRAMES, 1);
	    gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	    gl.glLineWidth(1);
	    gl.glDisable(GL3.GL_LINE_SMOOTH);
	    gl.glEnable(GL3.GL_BLEND);
	    gl.glDepthFunc(GL3.GL_ALWAYS);
	    gl.glDepthMask(false);
	    gl.glBlendFunc(GL3.GL_ONE, GL3.GL_ONE);
	    soundProgram.use();
	    soundProgram.getUniform("soundTexture").set((int)0);
	    
	    //Render
	    for(PlaybackEvent ev:activeEvents){
		soundProgram.getUniform("pan").set(.5f, .5f);//Pan center
		final double startTime=((ev.getStartTimeSamples()-bufferStartTimeSamples)/BUFFER_SIZE_FRAMES)*2-1;
		//final double len = ev.getDurationSamples()/framesToWrite;
		soundProgram.getUniform("numRows").setui((int)ev.getSoundTexture().getNumRows());
		soundProgram.getUniform("start").set((float)startTime);
		//soundProgram.getUniform("resamplingScalar").set((float)ev.getSoundTexture().getResamplingScalar());
		soundProgram.getUniform("lengthPerRow")
		 .set(((float)((double)SoundTexture.ROW_LENGTH_SAMPLES/(double)BUFFER_SIZE_FRAMES))*2*(float)ev.getSoundTexture().getResamplingScalar());
		final int lengthInSegments = (int)(ev.getSoundTexture().getNumRows()) * 2; //Times two because of the turn
		ev.getSoundTexture().getGLTexture().bindToTextureUnit(0, gl);
		gl.glDrawArrays(GL3.GL_LINE_STRIP, 0, lengthInSegments+1);
	    }//end for(events)
	    //Read and export to sound card.
	    gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);//Unbind so we can read off the output
	    playbackTexture.bind().readPixels(GL3.GL_RG, GL3.GL_FLOAT, audioByteBuffer);// RG_INTEGER throws INVALID_OPERATION!?
	bufferStartTimeSamples+=BUFFER_SIZE_FRAMES;
	//Cleanup
	gl.glViewport(0, 0, tr.getRootWindow().getCanvas().getWidth(), tr.getRootWindow().getCanvas().getHeight());
	gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	gl.glDisable(GL3.GL_BLEND);
    }//end process()
    
    class PlaybackEvent{
	private final long startTimeSamples;
	private final long endTimeSamples;
	private final long durationSamples;
	private final SoundTexture soundTexture;
	public PlaybackEvent(SoundTexture tex, long startTimeSamples){
	    durationSamples = tex.getLengthInRealtimeSamples();
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
