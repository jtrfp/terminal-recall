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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL3;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadDataType;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadOrder;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.RelevantEverywhere;
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
    private AtomicBoolean paused = new AtomicBoolean(false);
    private int bufferSizeFrames = 4096;
    private ByteBuffer gpuFloatBytes;
    private static final AudioProcessor SILENCE = new Silence();
    
   ////VARS
   private AudioDriver          activeDriver;
   private AudioDevice		activeDevice;
   private AudioOutput		activeOutput;
   private AudioFormat		activeFormat;
    
    private boolean firstRun=true;
    private final TreeSet<SoundEvent> pendingEvents = new TreeSet<SoundEvent>(new Comparator<SoundEvent>(){
	@Override
	public int compare(SoundEvent first, SoundEvent second) {
	    final double result = 88200. * (first.getStartRealtimeSeconds()-second.getStartRealtimeSeconds());
	    if(result==0)return first.hashCode()-second.hashCode();
	    else if(result>0)return 1;
	    else return -1;
	}//end compare()
    });
    private final ArrayList<SoundEvent> activeEvents = new ArrayList<SoundEvent>();
    private double bufferTimeCounter;
    

    public static final double DEFAULT_SFX_VOLUME = .3;
    private static final int NUM_BUFFER_ROWS=1;
    
    public SoundSystem(final TR tr) {
	this.tr = tr;
	System.out.println("Setting up sound system...");
	loadConfigAndAttachListeners();
	tr.getThreadManager().submitToGL(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		System.out.println("SoundSystem: setting up textures...");
		final GPU gpu = tr.gpu.get();
		
		// TODO: Setup uniforms here.
		System.out.println("...Done.");
		gpu.defaultProgram();
		gpu.defaultTIU();
		gpu.defaultTexture();
		gpu.defaultFrameBuffers();
		return null;
	    }// end call()
	}).get();
	
	playbackFactory= new SamplePlaybackEvent.Factory(tr);
	musicFactory   = new MusicPlaybackEvent.Factory(tr);
	
	new Thread() {
	    private final AudioCompressor compressor = new AudioCompressor();
	    @Override
	    public void run() {
		try {
		    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		    Thread.currentThread().setName("SoundSystem");
		    
		    while (true) {
			synchronized(paused){
			    if(paused.get()){
				final AudioDriver driver = getActiveDriver();
				driver.setSource(SILENCE);
				driver.flush();
			    }while(paused.get())
				paused.wait();
			}//end sync()
			
			while(getActiveFormat() == null || getActiveOutput() == null || getActiveDevice() == null)
			    Thread.sleep(100);//Rolling loop waiting for valid state.
			
			renderPrep();
			tr.getThreadManager().submitToGL(new Callable<Void>() {
			    @Override
			    public Void call() throws Exception {
				final ByteBuffer floatBytes = getGPUFloatBytes();
				floatBytes.clear();
				render(tr.gpu.get().getGl(), floatBytes);
				return null;
			    }
			}).get();
			//sBuf.clear();
			final FloatBuffer fBuf = getGPUFloatBytes().asFloatBuffer();
			fBuf.clear();
			compressor.setSource(fBuf);
			final AudioDriver driver = getActiveDriver();
			if(driver!=null){
			    driver.setSource(compressor);
			    driver.flush();
			}//end driver!=null
		    }// end while(true)
		} catch (Exception e) {
		    tr.showStopper(e);
		}
	    }// end run()
	}.start();
    }// end constructor
    
    private void loadConfigAndAttachListeners() {
	final TRConfiguration config = tr.config;
	config.addPropertyChangeListener(TRConfiguration.ACTIVE_AUDIO_DRIVER,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String driver = (String)evt.getNewValue();
		if(driver!=null)
		    setDriverByName(driver);
	    }});
	config.addPropertyChangeListener(TRConfiguration.ACTIVE_AUDIO_DEVICE,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String device = (String)evt.getNewValue();
		if(device!=null)
		    setDeviceByName(device);
	    }});
	config.addPropertyChangeListener(TRConfiguration.ACTIVE_AUDIO_OUTPUT,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String output = (String)evt.getNewValue();
		System.out.println("SoundSystem property change ACTIVE_AUDIO_OUTPUT /// "+evt);
		if(output!=null)
		    setOutputByName(output);
	    }});
	config.addPropertyChangeListener(TRConfiguration.ACTIVE_AUDIO_FORMAT,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String format = (String)evt.getNewValue();
		if(format!=null)
		    setFormatByName(format);
	    }});
	config.addPropertyChangeListener(TRConfiguration.AUDIO_BUFFER_SIZE, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue()!=null)
		 setBufferSizeFrames((Integer)evt.getNewValue());
	    }});
	setDriverByName(config.getActiveAudioDriver());
	setDeviceByName(config.getActiveAudioDevice());
	setOutputByName(config.getActiveAudioOutput());
	setFormatByName(config.getActiveAudioFormat());
	setBufferSizeFrames(config.getAudioBufferSize());
    }//end loadConfigAndAttachListeners

    protected void setFormatByName(String format) {
	final AudioOutput activeOutput = getActiveOutput();
	if(activeOutput==null)
	    return;
	if(format!=null){
	    final AudioFormat fmt = getActiveOutput().getFormatFromUniqueName(format);
	    if(fmt!=null){
		setActiveFormat(fmt);
	    }
	}else{setActiveFormat(null);}
    }//end setFormatByName(...)

    protected void setOutputByName(String output) {
	System.out.println("setOutputByName "+output);
	if(output!=null){
	    final AudioOutput ao = getActiveDevice().getOutputByName(output);
	    System.out.println("ao="+ao);
	    if(ao!=null)
		setActiveOutput(ao);
	}else{setActiveOutput(null);}
    }//end setOutputByName(...)

    protected void setDeviceByName(String device) {
	if(device!=null){
	    final AudioDevice audioDevice = getActiveDriver().getDeviceByName(device);
	    if(audioDevice!=null)
		setActiveDevice(audioDevice);
	}else{setActiveDevice(null);}
	//setOutputByName(null);
    }//end setDeviceByName(...)

    protected void setDriverByName(String driver) {
	if(driver!=null){
	    if(activeDriver!=null)
		activeDriver.release();
	    Class driverClass;
	    try{driverClass = Class.forName(driver);}
		catch(ClassNotFoundException e){
	    driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;}
	    if(!AudioDriver.class.isAssignableFrom(driverClass))
	      driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;
	    try{setActiveDriver((AudioDriver)driverClass.newInstance());}
	     catch(Exception e){e.printStackTrace();return;}
	}else{}
	//setDeviceByName(null);
    }//end setDriverByName(...)

    public SoundSystem setPaused(boolean state){
	synchronized(paused){
	    if(paused.get()==state)//No change
		return this;
	    paused.set(state);
	    paused.notifyAll();
	}//end sync()
	return this;
    }//end setPaused()
    
    public AtomicBoolean isPaused() {
	return paused;
    }//end isPaused()
    
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
    public SoundTexture newSoundTexture(final FloatBuffer samples, final int localSampleRate){
	final GLTexture texture      = tr.gpu.get().newTexture();
	final int lengthInSamples    = samples.remaining();
	final double lengthInSeconds = (double)lengthInSamples/(double)localSampleRate;
	final int numRows            = (int)(Math.ceil((double)lengthInSamples / (double)SoundTexture.ROW_LENGTH_SAMPLES));
	final int quantizedSize      = numRows*SoundTexture.ROW_LENGTH_SAMPLES;
	final double quantizedSizeSeconds = (double)quantizedSize / (double)localSampleRate;
	final double lengthPerRowSeconds
	     = quantizedSizeSeconds / (double)numRows;
	tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
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
				tr.gpu.get().defaultTIU();
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
				tr.gpu.get().defaultTexture();
				return null;
			    }}).get();
		return null;
	    }});
	return new SoundTexture(){
	    @Override
	    public double getLengthInRealtimeSeconds() {
		return lengthInSeconds;
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
	    public double getLengthPerRowSeconds() {
		return lengthPerRowSeconds;
	    }
	};//end new SoundTexture()
    }//end newSoundTexture
    
    private int getFilteringParm(){
	return tr.config.isAudioLinearFiltering()?GL3.GL_LINEAR:GL3.GL_NEAREST;
    }
    
    public synchronized void enqueuePlaybackEvent(SoundEvent evt){
	if(evt instanceof RelevantEverywhere)
	    activeEvents.add(evt);
	else pendingEvents.add(evt);
    }
    
    private void firstRun(){
	firstRun=false;
    }
    
    private synchronized void renderPrep(){
	cleanActiveEvents();
	pickupActiveEvents(getBufferSizeFrames()/getActiveFormat().getFrameRate());
    }
    
    private synchronized void render(GL3 gl, ByteBuffer audioByteBuffer) {
	if (firstRun)
	    firstRun();
	final GPU gpu = tr.gpu.get();
	
	if(tr.config.isAudioBufferLag())
	    readGLAudioBuffer(gpu,audioByteBuffer);
	
	// Render
	getPlaybackFrameBuffer().bindToDraw();
	gl.glViewport(0, 0, getBufferSizeFrames(), 1);
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
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
	    factory.apply(gl, events, bufferTimeCounter);
	    events.clear();
	}//end for(keySet)
	
	if(!tr.config.isAudioBufferLag())
	    readGLAudioBuffer(gpu,audioByteBuffer);
	
	bufferTimeCounter += getBufferSizeSeconds();
	// Cleanup
	gpu.defaultFrameBuffers();
	gpu.defaultProgram();
	gpu.defaultTIU();
	gpu.defaultTexture();
	gpu.defaultViewport();
    }// end process()
    
    public double getBufferSizeSeconds() {
	return (double)getBufferSizeFrames() / (double)getActiveFormat().getFrameRate();
    }

    private void readGLAudioBuffer(GPU gpu, ByteBuffer audioByteBuffer){
	// Read and export previous results to sound card.
	final GL3 gl = gpu.getGl();
	gpu.defaultFrameBuffers();
	getPlaybackTexture().bind().readPixels(PixelReadOrder.RG, PixelReadDataType.FLOAT,
		audioByteBuffer).unbind();// RG_INTEGER throws INVALID_OPERATION!?
    }//end readGLAudioBuffer(...)
    
    private void pickupActiveEvents(double windowSizeInSeconds){
	final double playbackTimeSeconds = bufferTimeCounter;
	final Iterator<SoundEvent> eI = pendingEvents.iterator();
	while(eI.hasNext()){
	    final SoundEvent event = eI.next();
	    if(event.isDestroyed())
		eI.remove();
	    else if(event.getStartRealtimeSeconds()<playbackTimeSeconds+windowSizeInSeconds && event.getEndRealtimeSeconds()>playbackTimeSeconds){
		activeEvents.add(event);
		eI.remove();
	    }//end if(in range)
	    if(event.getStartRealtimeSeconds()>playbackTimeSeconds+windowSizeInSeconds)
		return;//Everything after this point is out of range.
	}//end hashNext(...)
    }//end pickupActiveEvents()
    
    private void cleanActiveEvents(){
	final double currentTimeSeconds = bufferTimeCounter;
	final Iterator<SoundEvent> eI = activeEvents.iterator();
	while(eI.hasNext()){
	    final SoundEvent event = eI.next();
	    if(event.isDestroyed())
		eI.remove();
	    else if(event.getEndRealtimeSeconds()<currentTimeSeconds && 
		    !(event instanceof RelevantEverywhere))
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

    public double getCurrentFrameBufferTimeCounter() {
	return bufferTimeCounter;
    }

    public synchronized void dequeueSoundEvent(SoundEvent event) {
	pendingEvents.remove(event);
	activeEvents.remove(event);
    }//end dequeneSoundEvent(...)

    public GLFrameBuffer getSoundOutputFrameBuffer() {
	return playbackFrameBuffer;
    }

    public double getSamplesPerMilli() {
	return ((double)getActiveFormat().getFrameRate())/1000.;
    }

    /**
     * @return the activeDriver
     */
    private AudioDriver getActiveDriver() {
	if(activeDriver==null){
            System.out.println("Overriding null driver to default...");
            setActiveDriver(new JavaSoundSystemAudioOutput());
        }//end if(null)
        return activeDriver;
    }

    /**
     * @param activeDriver the activeDriver to set
     */
    private void setActiveDriver(AudioDriver activeDriver) {
	if(this.activeDriver!=null)
	    activeDriver.release();
        this.activeDriver = activeDriver;
        System.out.println("SoundSystem: Active Driver Set To "+activeDriver);
    }

    /**
     * @return the activeDevice
     */
    private AudioDevice getActiveDevice() {
	if(activeDevice==null){
            System.out.println("Overriding null device to default...");
            setActiveDevice(getActiveDriver().getDefaultDevice());
        }//end if(null)
        return activeDevice;
    }

    /**
     * @param activeDevice the activeDevice to set
     */
    private void setActiveDevice(AudioDevice activeDevice) {
        this.activeDevice = activeDevice;
        System.out.println("SoundSystem: Active Device Set To "+activeDevice);
    }

    /**
     * @return the activeOutput
     */
    private AudioOutput getActiveOutput() {
	if(activeOutput==null)
	    activeOutput = getActiveDevice().getDefaultOutput();
        return activeOutput;
    }

    /**
     * @param activeOutput the activeOutput to set
     */
    private void setActiveOutput(AudioOutput activeOutput) {
        this.activeOutput = activeOutput;
        System.out.println("SoundSystem: Active Output Set To "+activeOutput);
	getActiveDriver().setOutput(activeOutput);
    }

    /**
     * @return the activeFormat
     */
    public AudioFormat getActiveFormat() {
	if(activeFormat==null)
	    getActiveOutput().getDefaultFormat();
        return activeFormat;
    }

    /**
     * @param activeFormat the activeFormat to set
     */
    private void setActiveFormat(AudioFormat activeFormat) {
	//If unspecified, default to 44100
	if(activeFormat!=null)
	 if(activeFormat.getSampleRate()==AudioSystem.NOT_SPECIFIED)
	    activeFormat = new AudioFormat(
		    activeFormat.getEncoding(), 
		    44100, 
		    activeFormat.getSampleSizeInBits(), 
		    activeFormat.getChannels(), 
		    activeFormat.getFrameSize(), 
		    44100, 
		    activeFormat.isBigEndian());
	
        this.activeFormat = activeFormat;
        System.out.println("SoundSystem: Active Format Set To "+activeFormat);
        if(activeDriver!=null)
	    activeDriver.setFormat(activeFormat);
    }

    /**
     * @return the bufferSizeFrames
     */
    int getBufferSizeFrames() {
        return bufferSizeFrames;
    }

    /**
     * @param bufferSizeFrames the bufferSizeFrames to set
     */
    private synchronized void setBufferSizeFrames(int bufferSizeFrames) {
	if(bufferSizeFrames<=0)
	    throw new RuntimeException("Invalid buffer size: "+bufferSizeFrames+". Must be greater than zero.");
        this.bufferSizeFrames = bufferSizeFrames;
        getActiveDriver().setBufferSizeFrames(bufferSizeFrames);
        stalePlaybackTexture();
        staleFloatBytes();
    }
    
    private synchronized GLFrameBuffer getPlaybackFrameBuffer(){
	if(playbackFrameBuffer==null){
	    playbackFrameBuffer = tr.getThreadManager().submitToGL(new Callable<GLFrameBuffer>(){
		@Override
		public GLFrameBuffer call() throws Exception {
		    final GPU gpu = tr.gpu.get();
		    gpu.defaultTexture();
		    gpu.defaultTIU();
		    final GLTexture texture = getPlaybackTexture();
		    return tr.gpu.get()
				.newFrameBuffer()
				.bindToDraw()
				.attachDrawTexture(texture,
					GL3.GL_COLOR_ATTACHMENT0);
		}}).get();
	}//end if(playbackFrameBuffer==null)
	return playbackFrameBuffer;
    }//end getPlaybackFrameBuffer()
    
    private synchronized void stalePlaybackFrameBuffer(){
	if(playbackFrameBuffer!=null){
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    playbackFrameBuffer.destroy();
		    return null;
		}}).get();
	}//end playbackFrameBuffer!=null
	playbackFrameBuffer=null;
    }//end stalePlaybackFrameBuffer()
    
    private synchronized GLTexture getPlaybackTexture(){
	if(playbackTexture==null){
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    final GPU gpu = tr.gpu.get();
		    gpu.defaultProgram();
		    gpu.defaultTIU();
		    gpu.defaultTexture();
		    gpu.defaultFrameBuffers();
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
			    .setImage(GL3.GL_RG32F, getBufferSizeFrames(),
				    NUM_BUFFER_ROWS, GL3.GL_RGBA, GL3.GL_FLOAT,
				    null);
		    return null;
		}}).get();
	}
	return playbackTexture;
    }//end getPlaybackTexture()
    
    private synchronized void stalePlaybackTexture(){
	if(playbackTexture!=null){
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    playbackTexture.delete();//This fails with no-GL exception?!
		    return null;
		}}).get();
	}//end playbackTexture!=null
        stalePlaybackFrameBuffer();
	playbackTexture=null;
    }//end stalePlaybackTexture()

    private ByteBuffer getGPUFloatBytes() {
	if(gpuFloatBytes==null)
	    setGPUFloatBytes(ByteBuffer.allocateDirect(
		     getBufferSizeFrames()*4
		     *getActiveFormat().getChannels())
		    .order(ByteOrder.nativeOrder()));
	return gpuFloatBytes;
    }

    private void setGPUFloatBytes(ByteBuffer gpuFloatBytes) {
	this.gpuFloatBytes = gpuFloatBytes;
    }
    
    private void staleFloatBytes(){
	gpuFloatBytes=null;
    }
}//end SoundSystem
