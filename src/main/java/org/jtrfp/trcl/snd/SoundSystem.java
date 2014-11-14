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
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.VisibleEverywhere;
import org.jtrfp.trcl.snd.SoundEvent.Factory;

public final class SoundSystem {
    private final TR tr;
    private GLFrameBuffer playbackFrameBuffer;
    private GLTexture playbackTexture;
    private final HashMap<SoundEvent.Factory,ArrayList<SoundEvent>> eventMap 
     = new HashMap<SoundEvent.Factory,ArrayList<SoundEvent>>();
    private final SamplePlaybackEvent.Factory playbackFactory;
    private final MusicPlaybackEvent.Factory musicFactory;
    private long soundRenderingFinishedSync;
    
    private boolean firstRun=true;
    private final TreeSet<SoundEvent> pendingEvents = new TreeSet<SoundEvent>(new Comparator<SoundEvent>(){
	@Override
	public int compare(SoundEvent first, SoundEvent second) {
	    final long result = first.getStartRealtimeSamples()-second.getStartRealtimeSamples();
	    if(result==0L)return first.hashCode()-second.hashCode();
	    else if(result>0)return 1;
	    else return -1;
	}//end compare()
    });
    private final ArrayList<SoundEvent> activeEvents = new ArrayList<SoundEvent>();
    private long bufferFrameCounter;
    

    public static final double DEFAULT_SFX_VOLUME = .12;
    
    public static final int SAMPLE_RATE=44100;
    static final int BUFFER_SIZE_FRAMES=4096*2;
    public static final int NUM_CHANNELS=2;
    public static final int BYTES_PER_SAMPLE=2;
    private static final int SAMPLE_SIZE_BITS = BYTES_PER_SAMPLE*8;
    public static final int BYTES_PER_FRAME=BYTES_PER_SAMPLE*NUM_CHANNELS;
    private static final int BUFFER_SIZE_BYTES=BUFFER_SIZE_FRAMES*BYTES_PER_FRAME;
    private static final int NUM_BUFFER_ROWS=1;
    
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
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
		 	.setWrapS(GL3.GL_CLAMP_TO_EDGE)
		 	.setWrapT(GL3.GL_CLAMP_TO_EDGE)
		 	.setDebugName("playbackTexture")
		 	.setExpectedMinValue(-1, -1, -1, -1)
		 	.setExpectedMaxValue(1, 1, 1, 1)
		 	.setPreferredUpdateIntervalMillis(100)
			.setImage(GL3.GL_RG32F, BUFFER_SIZE_FRAMES,
				NUM_BUFFER_ROWS, GL3.GL_RGBA, GL3.GL_FLOAT,
				null);
		playbackFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(playbackTexture,
				GL3.GL_COLOR_ATTACHMENT0);
		// TODO: Setup uniforms here.
		System.out.println("...Done.");
		return null;
	    }// end call()
	}).get();
	
	playbackFactory = new SamplePlaybackEvent.Factory(tr);
	musicFactory = new MusicPlaybackEvent.Factory(tr);
	
	try {
	    sourceDataLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
	    sourceDataLine.open();
	} catch (LineUnavailableException e) {
	    tr.showStopper(e);
	}
	
	new Thread() {
	    @Override
	    public void run() {
		try {
		    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		    byte[] shortBytes = new byte[BUFFER_SIZE_BYTES];
		    final ByteBuffer floatBytes = ByteBuffer.allocateDirect(
			    BUFFER_SIZE_FRAMES*4*NUM_CHANNELS).order(ByteOrder.nativeOrder());
		    ShortBuffer sBuf = ByteBuffer.wrap(shortBytes)
			    .order(ByteOrder.nativeOrder()).asShortBuffer();
		    FloatBuffer fBuf = floatBytes.asFloatBuffer();
		    sourceDataLine.start();
		    while (true) {
			renderPrep();
			tr.getThreadManager().submitToGL(new Callable<Void>() {
			    @Override
			    public Void call() throws Exception {
				floatBytes.clear();
				render(tr.gpu.get().getGl(), floatBytes);
				return null;
			    }
			}).get();
			sBuf.clear();
			fBuf.clear();
			for (int i = 0; i < BUFFER_SIZE_FRAMES * NUM_CHANNELS; i++) {
			    sBuf.put((short) (fBuf.get() * (double) Short.MAX_VALUE));
			}
			sourceDataLine.write(shortBytes, 0, BUFFER_SIZE_BYTES);
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
		 .setMagFilter(getFilteringParm())
		 .setMinFilter(getFilteringParm())
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
    
    private int getFilteringParm(){
	return tr.getTrConfig()[0].isAudioLinearFiltering()?GL3.GL_LINEAR:GL3.GL_NEAREST;
    }
    
    public synchronized void enqueuePlaybackEvent(SoundEvent evt){
	if(evt instanceof VisibleEverywhere)
	    activeEvents.add(evt);
	else pendingEvents.add(evt);
    }
    
    private void firstRun(){
	firstRun=false;
    }
    
    private synchronized void renderPrep(){
	cleanActiveEvents();
	pickupActiveEvents(BUFFER_SIZE_FRAMES);
    }
    
    private void render(GL3 gl, ByteBuffer audioByteBuffer) {
	if (firstRun)
	    firstRun();
	
	 // Read and export previous results to sound card.
	gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);// Unbind so we can read off
							  // the output
	playbackTexture.bind().readPixels(GL3.GL_RG, GL3.GL_FLOAT,
		audioByteBuffer);// RG_INTEGER throws INVALID_OPERATION!?
	playbackFrameBuffer.bindToDraw();
	gl.glViewport(0, 0, BUFFER_SIZE_FRAMES, 1);
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	
	// Render
	for (SoundEvent ev : activeEvents) {// TODO: Replace with Factory calls
	    if (ev.isActive()) {
		final SoundEvent.Factory factory = ev.getOrigin();
		if (!eventMap.containsKey(factory))
		    eventMap.put(factory, new ArrayList<SoundEvent>());
		eventMap.get(factory).add(ev);
	    }// end if(active)
	}// end for(events)
	for(Factory factory:eventMap.keySet()){
	    final ArrayList<SoundEvent> events = eventMap.get(factory);
	    factory.apply(gl, events, bufferFrameCounter);
	    events.clear();
	}//end for(keySet)
	bufferFrameCounter += BUFFER_SIZE_FRAMES;
	// Cleanup
	gl.glViewport(0, 0, tr.getRootWindow().getCanvas().getWidth(), tr
		.getRootWindow().getCanvas().getHeight());
    }// end process()
    
    private void pickupActiveEvents(long windowSizeInSamples){
	final long currentTimeSamples = bufferFrameCounter;
	final Iterator<SoundEvent> eI = pendingEvents.iterator();
	while(eI.hasNext()){
	    final SoundEvent event = eI.next();
	    if(event.isDestroyed())
		eI.remove();
	    else if(event.getStartRealtimeSamples()<currentTimeSamples+windowSizeInSamples && event.getEndRealtimeSamples()>currentTimeSamples){
		activeEvents.add(event);
		eI.remove();
	    }//end if(in range)
	    if(event.getStartRealtimeSamples()>currentTimeSamples+windowSizeInSamples)
		return;//Everything after this point is out of range.
	}//end hashNext(...)
    }//end pickupActiveEvents()
    
    private void cleanActiveEvents(){
	final long currentTimeSamples = bufferFrameCounter;
	final Iterator<SoundEvent> eI = activeEvents.iterator();
	while(eI.hasNext()){
	    final SoundEvent event = eI.next();
	    if(event.isDestroyed())
		eI.remove();
	    else if(event.getEndRealtimeSamples()<currentTimeSamples && 
		    !(event instanceof VisibleEverywhere))
		eI.remove();
	}//end while(hasNext)
    }//end cleanActiveEvents()

    /**
     * @return the playbackFactory
     */
    public SamplePlaybackEvent.Factory getPlaybackFactory() {
        return playbackFactory;
    }

    /**
     * @return the musicFactory
     */
    public MusicPlaybackEvent.Factory getMusicFactory() {
        return musicFactory;
    }

    public long getCurrentBufferFrameCounter() {
	return bufferFrameCounter;
    }

    public synchronized void dequeueSoundEvent(SoundEvent event) {
	pendingEvents.remove(event);
	activeEvents.remove(event);
    }//end dequeneSoundEvent(...)

    public GLFrameBuffer getSoundOutputFrameBuffer() {
	return playbackFrameBuffer;
    }
}//end SoundSystem
