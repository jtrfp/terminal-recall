/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2017 Chuck Ritola
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2ES2;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadDataType;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadOrder;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.snd.SoundEvent.Factory;
import org.jtrfp.trcl.tools.Util;

public class SoundSystem {
    //// PROPERTIES
    public static final String
           LINEAR_FILTERING   = "linearFiltering",
           BUFFER_LAG         = "bufferLag",
           MOD_STEREO_WIDTH   = "modStereoWidth",
           FORMAT_BY_NAME     = "formatByName",
           OUTPUT_BY_NAME     = "outputByName",
           DRIVER_BY_NAME     = "driverByName",
           DEVICE_BY_NAME     = "deviceByName",
           ACTIVE_DRIVER      = "activeDriver",
           ACTIVE_DEVICE      = "activeDevice",
           ACTIVE_OUTPUT      = "activeOutput",
           ACTIVE_FORMAT      = "activeFormat",
           MUSIC_VOLUME       = "musicVolume",
           SFX_VOLUME         = "sfxVolume",
           BUFFER_SIZE_FRAMES = "bufferSizeFrames",
           BUFFER_SIZE_FRAMES_STRING = "bufferSizeFramesString";
    
    private TR tr;
    private GLFrameBuffer playbackFrameBuffer;
    private GLTexture playbackTexture;
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final HashMap<SoundEvent.Factory,ArrayList<SoundEvent>> eventMap 
     = new HashMap<SoundEvent.Factory,ArrayList<SoundEvent>>();
    private SamplePlaybackEvent.Factory playbackFactory, musicPlaybackFactory;
    private MusicPlaybackEvent.Factory musicFactory;
    private LoopingSoundEvent.Factory loopFactory;
    private long soundRenderingFinishedSync;
    private AtomicBoolean paused = new AtomicBoolean(false);
    private int bufferSizeFrames = 4096;
    private ByteBuffer gpuFloatBytes;
    private static final AudioProcessor SILENCE = new Silence();
    private TRConfiguration trConfiguration;
    private boolean         initialized = false;
    private GPU             gpu;
    private boolean         bufferLag=true, linearFiltering=false;
    private double          modStereoWidth = .3;
    private final CollectionActionDispatcher<String> audioDriverNames = new CollectionActionDispatcher<String>(new HashSet<String>());
    private final ReentrantLock lock = new ReentrantLock();
    
   ////VARS
   private AudioDriver          activeDriver;
   private AudioDevice		activeDevice;
   private AudioOutput		activeOutput;
   private AudioFormat		activeFormat;
   
   private String               driverByName,
                                deviceByName,
                                outputByName,
                                formatByName;
    
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
    
    public SoundSystem() {
	audioDriverNames.add("org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput");//TODO: Implement a registry.
    }// end constructor
    
    public void initialize(){
	if(initialized)
	    throw new IllegalStateException("initialize() was already called. Can only be called once.");
	Util.assertPropertiesNotNull(this, "tr","gpu");
	final TR tr = getTr();
	System.out.println("Setting up sound system...");
	loadConfigAndAttachListeners();
	final GPU gpu = getGpu();
	tr.getThreadManager().submitToGL(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		System.out.println("SoundSystem: setting up textures...");
		// TODO: Setup uniforms here.
		System.out.println("...Done.");
		gpu.defaultProgram();
		gpu.defaultTIU();
		gpu.defaultTexture();
		gpu.defaultFrameBuffers();
		return null;
	    }// end call()
	}).get();
	
	musicPlaybackFactory = new SamplePlaybackEvent.Factory(tr);
	playbackFactory= new SamplePlaybackEvent.Factory(tr);
	musicFactory   = new MusicPlaybackEvent.Factory(tr, getModStereoWidth());
	loopFactory    = new LoopingSoundEvent.Factory(tr);
	
	new Thread() {//TODO: This is not thread-safe with sound config changes!
	    private final DynamicCompressor compressor = new DynamicCompressor();
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
				    render(getGpu().getGl(), floatBytes);
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
	initialized = true;
    }// end initialize()
    
    private void loadConfigAndAttachListeners() {
	final TRConfiguration config = getTrConfiguration();
	/*
	config.addPropertyChangeListener(TRConfigurationFactory.ACTIVE_AUDIO_DRIVER,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String driver = (String)evt.getNewValue();
		if(driver!=null)
		    setDriverByName(driver);
	    }});
	config.addPropertyChangeListener(TRConfigurationFactory.ACTIVE_AUDIO_DEVICE,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String device = (String)evt.getNewValue();
		if(device!=null)
		    setDeviceByName(device);
	    }});
	config.addPropertyChangeListener(TRConfigurationFactory.ACTIVE_AUDIO_OUTPUT,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String output = (String)evt.getNewValue();
		System.out.println("SoundSystem property change ACTIVE_AUDIO_OUTPUT /// "+evt);
		if(output!=null)
		    setOutputByName(output);
	    }});
	config.addPropertyChangeListener(TRConfigurationFactory.ACTIVE_AUDIO_FORMAT,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final String format = (String)evt.getNewValue();
		if(format!=null)
		    setFormatByName(format);
	    }});
	config.addPropertyChangeListener(TRConfigurationFactory.AUDIO_BUFFER_SIZE, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue()!=null)
		 setBufferSizeFrames((Integer)evt.getNewValue());
	    }});
	*/
	//Set the defaults in case they aren't written by a configurator
	setDriverByName("org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput");
	//setDeviceByName(config.getActiveAudioDevice());
	//setOutputByName(config.getActiveAudioOutput());
	//setFormatByName(config.getActiveAudioFormat());
	setBufferSizeFrames(4096);
    }//end loadConfigAndAttachListeners

    public void setFormatByName(String format) {
	final AudioOutput activeOutput = getActiveOutput();
	if(activeOutput==null)
	    return;
	if(format!=null){
	    final AudioFormat fmt = getActiveOutput().getFormatFromUniqueName(format);
	    if(fmt!=null){
		setActiveFormat(fmt);
	    }
	}else{setActiveFormat(null);}
	final String oldValue = this.formatByName;
	this.formatByName = format;
	pcs.firePropertyChange(FORMAT_BY_NAME, oldValue, format);
    }//end setFormatByName(...)

    public void setOutputByName(String output) {
	System.out.println("setOutputByName "+output);
	if(output!=null){
	    final AudioOutput ao = getActiveDevice().getOutputByName(output);
	    System.out.println("ao="+ao);
	    if(ao!=null)
		setActiveOutput(ao);
	}else{setActiveOutput(null);}
	final String oldValue = this.outputByName;
	this.outputByName = output;
	pcs.firePropertyChange(OUTPUT_BY_NAME, oldValue, output);
    }//end setOutputByName(...)

    public void setDeviceByName(String newDeviceByName) {
	if(newDeviceByName!=null){
	    final AudioDevice audioDevice = getActiveDriver().getDeviceByName(newDeviceByName);
	    if(audioDevice!=null)
		setActiveDevice(audioDevice);
	}else{setActiveDevice(null);}
	final String oldDeviceByName = this.deviceByName;
	this.deviceByName = newDeviceByName;
	pcs.firePropertyChange(DEVICE_BY_NAME, oldDeviceByName, newDeviceByName);
	//setOutputByName(null);
    }//end setDeviceByName(...)

    public void setDriverByName(String driver) {
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
	final String oldValue = this.driverByName;
	this.driverByName = driver;
	pcs.firePropertyChange(DRIVER_BY_NAME, oldValue, driver);
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
	final GLTexture texture      = getGpu().newTexture();
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
				getGpu().defaultTIU();
				texture
				 .bind()
				 .setMagFilter(getFilteringParm())
				 .setMinFilter(getFilteringParm())
				 .setWrapS(GL2ES2.GL_CLAMP_TO_EDGE)
				 .setWrapT(GL2ES2.GL_CLAMP_TO_EDGE)
				 .setImage(
					 GL2ES2.GL_R32F, 
					 SoundTexture.ROW_LENGTH_SAMPLES, 
					 numRows, 
					 GL2ES2.GL_RED, 
					 GL2ES2.GL_FLOAT, 
					 finalSamples);
				getGpu().defaultTexture();
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
	return isLinearFiltering()?GL2ES2.GL_LINEAR:GL2ES2.GL_NEAREST;
    }
    
    public void enqueuePlaybackEvent(SoundEvent evt){
	lock.lock();try{
	    if(evt instanceof RelevantEverywhere)
		activeEvents.add(evt);
	    else pendingEvents.add(evt);
	}finally{lock.unlock();}
    }
    
    private void firstRun(){
	firstRun=false;
    }

    private void renderPrep(){
	lock.lock();
	try{
	    cleanActiveEvents();
	    pickupActiveEvents(getBufferSizeFrames()/getActiveFormat().getFrameRate());
	}finally{lock.unlock();}
    }
    
    private void render(GL2ES2 gl, ByteBuffer audioByteBuffer) {
	if( lock.tryLock() )try{
	    if (firstRun)
		firstRun();
	    final GPU gpu = getGpu();

	    if(isBufferLag())
		readGLAudioBuffer(gpu,audioByteBuffer);

	    // Render
	    getPlaybackFrameBuffer().bindToDraw();
	    gl.glViewport(0, 0, getBufferSizeFrames(), 1);
	    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
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

	    if(!isBufferLag())
		readGLAudioBuffer(gpu,audioByteBuffer);

	    bufferTimeCounter += getBufferSizeSeconds();
	    // Cleanup
	    gpu.defaultFrameBuffers();
	    gpu.defaultProgram();
	    gpu.defaultTIU();
	    gpu.defaultTexture();
	    gpu.defaultViewport();
	}finally{lock.unlock();}
    }// end render()
    
    public double getBufferSizeSeconds() {
	final AudioFormat activeFormat = getActiveFormat();
	if( activeFormat != null )
	    return (double)getBufferSizeFrames() / (double)getActiveFormat().getFrameRate();
	return 0;
    }

    private void readGLAudioBuffer(GPU gpu, ByteBuffer audioByteBuffer){
	// Read and export previous results to sound card.
	final GL2ES2 gl = gpu.getGl();
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

    public void dequeueSoundEvent(SoundEvent event) {
	lock.lock();try{
	    pendingEvents.remove(event);
	    activeEvents.remove(event);
	}finally{lock.unlock();}
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
	final AudioDriver oldActiveDriver = this.activeDriver;
        this.activeDriver = activeDriver;
        activeDriver.setBufferSizeFrames(getBufferSizeFrames());
        setActiveDevice(activeDriver.getDefaultDevice());
        System.out.println("SoundSystem: Active Driver Set To "+activeDriver);
        pcs.firePropertyChange(ACTIVE_DRIVER, oldActiveDriver, activeDriver);
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
	final AudioDevice oldDevice = this.activeDevice;
        this.activeDevice = activeDevice;
        pcs.firePropertyChange(ACTIVE_DEVICE, oldDevice, activeDevice);
        setActiveOutput(activeDevice.getDefaultOutput());
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
     * @param newActiveOutput the activeOutput to set
     */
    private void setActiveOutput(AudioOutput newActiveOutput) {
	final AudioOutput oldOutput = this.activeOutput;
        this.activeOutput = newActiveOutput;
        System.out.println("SoundSystem: Active Output Set To "+newActiveOutput);
	getActiveDriver().setOutput(newActiveOutput);
	if( newActiveOutput != null )
	    setActiveFormat(newActiveOutput.getDefaultFormat());
	else
	    setActiveFormat(null);
	pcs.firePropertyChange(ACTIVE_OUTPUT, oldOutput, newActiveOutput);
    }

    /**
     * @return the activeFormat
     */
    public AudioFormat getActiveFormat() {
	if(activeFormat==null){
	    final AudioOutput activeOutput = getActiveOutput();
	    if(activeOutput != null)
	        return activeOutput.getDefaultFormat();
	    }
        return activeFormat;
    }

    /**
     * @param newFormat the activeFormat to set
     */
    private void setActiveFormat(AudioFormat newFormat) {
	final AudioFormat oldFormat = this.activeFormat;
	//If unspecified, default to 44100
	if(newFormat!=null)
	 if(newFormat.getSampleRate()==AudioSystem.NOT_SPECIFIED)
	    newFormat = new AudioFormat(
		    newFormat.getEncoding(), 
		    44100, 
		    newFormat.getSampleSizeInBits(), 
		    newFormat.getChannels(), 
		    newFormat.getFrameSize(), 
		    44100, 
		    newFormat.isBigEndian());
	
        this.activeFormat = newFormat;
        System.out.println("SoundSystem: Active Format Set To "+newFormat+" for driver "+activeDriver);
        if(activeDriver!=null)
	    activeDriver.setFormat(newFormat);
        pcs.firePropertyChange(ACTIVE_FORMAT, oldFormat, newFormat);
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
    public void setBufferSizeFrames(int bufferSizeFrames) {
	lock.lock();try{
	    System.out.println("setBufferSizeFrames "+bufferSizeFrames);
	    if(bufferSizeFrames<=0)
		throw new RuntimeException("Invalid buffer size: "+bufferSizeFrames+". Must be greater than zero.");
	    this.bufferSizeFrames = bufferSizeFrames;
	    getActiveDriver().setBufferSizeFrames(bufferSizeFrames);
	    stalePlaybackTexture();
	    staleFloatBytes();
	}finally{lock.unlock();}
    }
    
    public void setBufferSizeFramesString(String newValue){
	setBufferSizeFrames(Integer.parseInt(newValue));
    }
    
    public String getBufferSizeFramesString(){
	return ""+getBufferSizeFrames();
    }
    
    private GLFrameBuffer getPlaybackFrameBuffer(){
	lock.lock(); try{
	    if(playbackFrameBuffer==null){
		playbackFrameBuffer = tr.getThreadManager().submitToGL(new Callable<GLFrameBuffer>(){
		    @Override
		    public GLFrameBuffer call() throws Exception {
			final GPU gpu = getGpu();
			gpu.defaultTexture();
			gpu.defaultTIU();
			final GLTexture texture = getPlaybackTexture();
			return getGpu()
				.newFrameBuffer()
				.bindToDraw()
				.attachDrawTexture(texture,
					GL2ES2.GL_COLOR_ATTACHMENT0);
		    }}).get();
	    }//end if(playbackFrameBuffer==null)
	    return playbackFrameBuffer;
	}finally{lock.unlock();}
    }//end getPlaybackFrameBuffer()

    private void stalePlaybackFrameBuffer(){
	lock.lock();try{
	    if(playbackFrameBuffer!=null){
		final GLFrameBuffer toDelete = playbackFrameBuffer;
		tr.getThreadManager().submitToGL(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			toDelete.destroy();
			return null;
		    }});
	    }//end playbackFrameBuffer!=null
	    playbackFrameBuffer=null;
	}finally{lock.unlock();}
    }//end stalePlaybackFrameBuffer()
    
    private GLTexture getPlaybackTexture(){
	lock.lock();try{
	    if(playbackTexture==null){
		tr.getThreadManager().submitToGL(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			final GPU gpu = getGpu();
			gpu.defaultProgram();
			gpu.defaultTIU();
			gpu.defaultTexture();
			gpu.defaultFrameBuffers();
			playbackTexture = gpu
				.newTexture()
				.bind()
				.setMagFilter(GL2ES2.GL_NEAREST)
				.setMinFilter(GL2ES2.GL_NEAREST)
				.setWrapS(GL2ES2.GL_CLAMP_TO_EDGE)
				.setWrapT(GL2ES2.GL_CLAMP_TO_EDGE)
				.setDebugName("playbackTexture")
				.setExpectedMinValue(-1, -1, -1, -1)
				.setExpectedMaxValue(1, 1, 1, 1)
				.setPreferredUpdateIntervalMillis(100)
				.setImage(GL2ES2.GL_RG32F, getBufferSizeFrames(),
					NUM_BUFFER_ROWS, GL2ES2.GL_RGBA, GL2ES2.GL_FLOAT,
					null);
			return null;
		    }}).get();
	    }
	    return playbackTexture;
	}finally{lock.unlock();}
    }//end getPlaybackTexture()
    
    private void stalePlaybackTexture(){
	lock.lock();try{
	if(playbackTexture!=null){
	    final GLTexture toDelete = playbackTexture;
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    toDelete.delete();//This fails with no-GL exception?!
		    return null;
		}});
	}//end playbackTexture!=null
        stalePlaybackFrameBuffer();
	playbackTexture=null;
	}finally{lock.unlock();}
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

    public LoopingSoundEvent.Factory getLoopFactory() {
        return loopFactory;
    }

    public TRConfiguration getTrConfiguration() {
	if(trConfiguration == null)
	    trConfiguration = Features.get(tr, TRConfiguration.class);
        return trConfiguration;
    }

    public void setTrConfiguration(TRConfiguration trConfiguration) {
        this.trConfiguration = trConfiguration;
    }

    public TR getTr() {
        return tr;
    }

    public void setTr(TR tr) {
        this.tr = tr;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
	pcs.addPropertyChangeListener(arg0);
    }

    public void addPropertyChangeListener(String arg0,
	    PropertyChangeListener arg1) {
	pcs.addPropertyChangeListener(arg0, arg1);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String arg0) {
	return pcs.getPropertyChangeListeners(arg0);
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
	pcs.removePropertyChangeListener(arg0);
    }

    public void removePropertyChangeListener(String arg0,
	    PropertyChangeListener arg1) {
	pcs.removePropertyChangeListener(arg0, arg1);
    }

    public Boolean isBufferLag() {
        return bufferLag;
    }

    public void setBufferLag(Boolean bufferLag) {
	System.out.println("setBufferLag "+bufferLag);
	final Boolean oldValue = this.bufferLag;
        this.bufferLag = bufferLag;
        pcs.firePropertyChange(BUFFER_LAG, oldValue, bufferLag);
    }

    public Boolean isLinearFiltering() {
        return linearFiltering;
    }

    public void setLinearFiltering(Boolean linearFiltering) {
	final Boolean oldValue = this.linearFiltering;
        this.linearFiltering = linearFiltering;
        pcs.firePropertyChange(LINEAR_FILTERING, oldValue, linearFiltering);
    }

    public Double getModStereoWidth() {
        return modStereoWidth;
    }

    public void setModStereoWidth(Double modStereoWidth) {
	final double oldValue = this.modStereoWidth;
        this.modStereoWidth = modStereoWidth;
        getMusicFactory().setModStereoWidth(modStereoWidth);
        pcs.firePropertyChange(MOD_STEREO_WIDTH, oldValue, modStereoWidth);
    }

    public String getDriverByName() {
        return driverByName;
    }

    public String getDeviceByName() {
        return deviceByName;
    }

    public String getOutputByName() {
        return outputByName;
    }

    public String getFormatByName() {
        return formatByName;
    }

    public CollectionActionDispatcher<String> getAudioDriverNames() {
        return audioDriverNames;
    }
    
    public static boolean isAcceptableFormat(AudioFormat f){
	return  f.getChannels()==2 &&
		f.getEncoding()==Encoding.PCM_SIGNED &&
		f.getSampleSizeInBits() % 8 == 0;
    }
    
    public SamplePlaybackEvent.Factory getMusicPlaybackFactory() {
        return musicPlaybackFactory;
    }

    public Double getMusicVolume() {
	final SamplePlaybackEvent.Factory factory = getMusicPlaybackFactory();
	if( factory != null )
            return factory.getVolume();
	else
	    return 1.;
    }

    public void setMusicVolume(Double musicVolume) {
	final double oldValue = getMusicVolume();
        getMusicPlaybackFactory().setVolume(musicVolume);
        pcs.firePropertyChange(MUSIC_VOLUME, oldValue, musicVolume);
    }

    public Double getSfxVolume() {
	final SamplePlaybackEvent.Factory factory = getPlaybackFactory();
	if( factory != null )
            return factory.getVolume();
	else
	    return 1.;
    }

    public void setSfxVolume(Double sfxVolume) {
	final double oldValue = getSfxVolume();
        getPlaybackFactory().setVolume(sfxVolume);
        pcs.firePropertyChange(SFX_VOLUME, oldValue, sfxVolume);
    }
    
}//end SoundSystem
