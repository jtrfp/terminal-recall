/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.DefaultKeyedExecutor;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.KeyedExecutor;
import org.jtrfp.trcl.core.PackedTreeNode;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.pool.ObjectFactory;
import org.jtrfp.trcl.snd.SoundSystemKernel.AddToActiveEvents;
import org.jtrfp.trcl.snd.SoundSystemKernel.AddToPendingEvents;
import org.jtrfp.trcl.snd.SoundSystemKernel.RemoveFromEvents;
import org.jtrfp.trcl.snd.SoundSystemKernel.SetAudioOutputConfig;
import org.jtrfp.trcl.snd.SoundSystemKernel.SetBufferLag;
import org.jtrfp.trcl.snd.SoundSystemKernel.SetBufferSizeFrames;
import org.jtrfp.trcl.tools.Util;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.ochafik.util.Adapter;

import lombok.Getter;
import me.xdrop.fuzzywuzzy.FuzzySearch;

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
           OUTPUT_CONFIG      = "outputConfig",
           PACKED_OUTPUT_CONFIG_NODE = "packedOutputConfigNode",
           BUFFER_SIZE_FRAMES_STRING = "bufferSizeFramesString";
    
    private TR tr;
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private SamplePlaybackEvent.Factory playbackFactory, musicPlaybackFactory;
    private MusicPlaybackEvent.Factory musicFactory;
    private LoopingSoundEvent.Factory loopFactory;
    private long soundRenderingFinishedSync;
    private AtomicBoolean paused = new AtomicBoolean(false);
    private int bufferSizeFrames = 4096;
    
    private static final AudioProcessor SILENCE = new Silence();
    private TRConfiguration trConfiguration;
    private boolean         initialized = false;
    private GPU             gpu;
    private boolean         bufferLag=true, linearFiltering=false;
    private double          modStereoWidth = .3;
    private final CollectionActionDispatcher<String> audioDriverNames = new CollectionActionDispatcher<String>(new HashSet<String>());
    private final SoundSystemKernel soundSystemKernel = new SoundSystemKernel();
    //private final Queue<Runnable> soundThreadExecutor = new ArrayDeque<>(512);
    private final KeyedExecutor<Object> soundThreadExecutor = new DefaultKeyedExecutor<Object>();
    private DefaultMutableTreeNode outputConfig;
    private long timeOfLastBarrier;
    private final ObjectFactory<Class<? extends AudioDriver>,AudioDriver> driverFactory = 
	    new ObjectFactory<>(new HashMap<Class<? extends AudioDriver>,AudioDriver>(), new Adapter<Class<? extends AudioDriver>,AudioDriver>(){

		@Override
		public AudioDriver adapt(Class<? extends AudioDriver> value) {
		    try{return value.getConstructor().newInstance();}
		    catch(Exception e){e.printStackTrace();return null;}
		}

		@Override
		public Class<? extends AudioDriver> reAdapt(AudioDriver value) {
		    return value.getClass();
		}});
    
    
    private double bufferTimeCounter;
    

    public static final double DEFAULT_SFX_VOLUME = .3;
    public static final double [] DEFAULT_SFX_VOLUME_STEREO = new double [] {DEFAULT_SFX_VOLUME, DEFAULT_SFX_VOLUME};
    
    //If no valid audio devices are available, provide a dummy driver
    private static final AudioDriver DUMMY_DRIVER = new DummyAudioDriver();
    
    @Getter(lazy=true)
    private final DefaultMutableTreeNode outputConfigTree = generateOutputConfigTree();
    
    public SoundSystem() {//TODO: Use Features to update this.
	audioDriverNames.add("org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput");
	audioDriverNames.add("org.jtrfp.trcl.snd.DummyAudioDriver");
	soundSystemKernel.setKeyedExecutor(soundThreadExecutor);
    }// end constructor
    
    public void initialize(){
	if(initialized)
	    throw new IllegalStateException("initialize() was already called. Can only be called once.");
	Util.assertPropertiesNotNull(this, "tr","gpu");
	final TR tr = getTr();
	System.out.println("Setting up sound system...");
	loadConfigAndAttachListeners();
	final GPU gpu = getGpu();
	
	soundSystemKernel.setGpu(gpu);
	soundSystemKernel.setThreadManager(tr.getThreadManager());
	
	try {gpu.getGlExecutor().submitToGL(new GLExecutable<Void, GL3>() {
	    @Override
	    public Void execute(GL3 gl) throws Exception {
		System.out.println("SoundSystem: setting up textures...");
		// TODO: Setup uniforms here.
		System.out.println("...Done.");
		gpu.defaultProgram();
		gpu.defaultTIU();
		gpu.defaultTexture();
		gpu.defaultFrameBuffers();
		return null;
	    }// end call()
	}).get(); }
	catch(Exception e) {e.printStackTrace();}
	
	musicPlaybackFactory = new SamplePlaybackEvent.Factory(tr);
	playbackFactory= new SamplePlaybackEvent.Factory(tr);
	musicFactory   = new MusicPlaybackEvent.Factory(tr, getModStereoWidth());
	loopFactory    = new LoopingSoundEvent.Factory(tr);
	
	new Thread() {
	    @Override
	    public void run() {
		try {
		    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		    Thread.currentThread().setName("SoundSystem");
		    
		    while (true) {
			//synchronized(paused){
			    while(paused.get()){
				final AudioDriver driver = getActiveDriver();
				if( driver == null )
				    break;
				try {
				driver.setSource(SILENCE);
				driver.flush();
				} catch(Exception e) {//Keep going
				    e.printStackTrace();
				    soundSystemKernel.setActiveDriver(SoundSystem.DUMMY_DRIVER);
				}
			    }//while(paused.get())
			    
				//paused.wait();
			//}//end sync()
			
			//while(getActiveFormat() == null || getActiveOutput() == null || getActiveDevice() == null)
			//    Thread.sleep(100);//Rolling loop waiting for valid state.
			
			final double currentBufferTimeCounter = bufferTimeCounter;
			bufferTimeCounter += getBufferSizeSeconds();
			soundSystemKernel.execute(currentBufferTimeCounter);
			
			timeLimitedBarrier(50);//Limit. If the state is invalid, this loop will roll rapidly.
			
		    }// end while(true)
		} catch (Exception e) {
		    tr.showStopper(e);
		}
	    }// end run()
	}.start();
	initialized = true;
    }// end initialize()
    
    private DefaultMutableTreeNode generateOutputConfigTree() {
	// Generate initial tree
	final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	getAudioDriverNames().stream().forEach(x->{
	    final DefaultMutableTreeNode driverNode = new DefaultMutableTreeNode();
	    driverNode.setUserObject(x);
	    root.add(driverNode);
	    try {
		final AudioDriver driver = driverFactory.get((Class<? extends AudioDriver>)Class.forName(x));
		for(AudioDevice dev : driver.getDevices()) {
		    final DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode();
		    deviceNode.setUserObject(dev.getUniqueName());
		    driverNode.add(deviceNode);
		    for(AudioOutput output : dev.getOutputs()) {
			final DefaultMutableTreeNode outputNode = new DefaultMutableTreeNode();
			outputNode.setUserObject(output.getUniqueName());
			deviceNode.add(outputNode);
			for( AudioFormat format : output.getFormats()) {
			    final DefaultMutableTreeNode formatNode = new DefaultMutableTreeNode();
			    formatNode.setUserObject(format.toString());
			    outputNode.add(formatNode);
			}//end for(format)
		    }//end for(output)
		}//end for(devices)
	    } catch (ClassNotFoundException e) {e.printStackTrace();}
	});
	
	// Prune leaves where path length != 5
	final Collection<DefaultMutableTreeNode> toRemove = new ArrayList<>(16);
	boolean done;
	do {
	    done = true;
	    for(DefaultMutableTreeNode node : Util.getLeaves(root)) {
		final int nodeDepth = node.getPath().length-1;
		System.out.print("Node `"+node+"` has path depth of "+nodeDepth+". ");
		if(nodeDepth != 4 && node.getParent() != null) {
		    System.out.println("Discarding...");
		    toRemove.add(node);
		    done = false;
		} else
		    System.out.println("Retaining...");
	    }//end for(leaves)
	    
	    toRemove.stream().forEach(x->x.removeFromParent());
	    toRemove.clear();
	} while (!done);
	return root;
    }//end generateOutputConfigTree()

    private void timeLimitedBarrier(int maxTimeInMillis) throws InterruptedException {
	final long currentTimeMillis = System.currentTimeMillis();
	final long timeRemaining = (timeOfLastBarrier-currentTimeMillis)+maxTimeInMillis;
	if(timeRemaining > 0 )
	    Thread.sleep(timeRemaining);
	timeOfLastBarrier = currentTimeMillis+timeRemaining;
    }//end timeLimitedBarrier
    
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
	//setDriverByName("org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput");
	//setDeviceByName(config.getActiveAudioDevice());
	//setOutputByName(config.getActiveAudioOutput());
	//setFormatByName(config.getActiveAudioFormat());
	setBufferSizeFrames(4096);
	getOutputConfigNode();//Sets up the default, at least until the specified one is loaded if there is one.
    }//end loadConfigAndAttachListeners

    /*
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
	new Throwable("setDeviceByName()").printStackTrace();
	System.out.println("setDeviceByName "+newDeviceByName);
	if(newDeviceByName!=null){
	    final AudioDevice audioDevice = getActiveDriver().getDeviceByName(newDeviceByName);
	    System.out.println("Resulting audio device: "+audioDevice);
	    if(audioDevice!=null)
		setActiveDevice(audioDevice);
	}else{setActiveDevice(null);}
	final String oldDeviceByName = this.deviceByName;
	this.deviceByName = newDeviceByName;
	pcs.firePropertyChange(DEVICE_BY_NAME, oldDeviceByName, newDeviceByName);
	//setOutputByName(null);
    }//end setDeviceByName(...)
    */
/*
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
	    try{setActiveDriver((AudioDriver)driverFactory.get(driverClass));}
	     catch(Exception e){e.printStackTrace();return;}
	}else{}
	final String oldValue = this.driverByName;
	this.driverByName = driver;
	pcs.firePropertyChange(DRIVER_BY_NAME, oldValue, driver);
	//setDeviceByName(null);
    }//end setDriverByName(...)
*/
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
			gpu.getGlExecutor().submitToGL(new GLExecutable<Void,GL3>(){
			    @Override
			    public Void execute(GL3 gl) throws Exception {
				getGpu().defaultTIU();
				texture
				 .bind(gl)
				 .setMagFilter(getFilteringParm(), gl)
				 .setMinFilter(getFilteringParm(), gl)
				 .setWrapS(GL2ES2.GL_CLAMP_TO_EDGE, gl)
				 .setWrapT(GL2ES2.GL_CLAMP_TO_EDGE, gl)
				 .setImage(
					 GL2ES2.GL_R32F, 
					 SoundTexture.ROW_LENGTH_SAMPLES, 
					 numRows, 
					 GL2ES2.GL_RED, 
					 GL2ES2.GL_FLOAT, 
					 finalSamples, gl);
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
	    if(evt instanceof RelevantEverywhere)
		synchronized(soundThreadExecutor){
		    soundThreadExecutor.execute(new AddToActiveEvents(Collections.singleton(evt), soundSystemKernel));
		}//end sync(queue)
	    else synchronized(soundThreadExecutor){
		soundThreadExecutor.execute(new AddToPendingEvents(Collections.singleton(evt), soundSystemKernel));
		}//end sync(queue)
    }//end enqueuePlaybackEvent
    
    public double getBufferSizeSeconds() {
	final AudioFormat activeFormat = getActiveFormat();
	if( activeFormat != null )
	    return (double)getBufferSizeFrames() / (double)getActiveFormat().getFrameRate();
	return 0;
    }
    
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
	synchronized(soundThreadExecutor){
	    soundThreadExecutor.execute(new RemoveFromEvents(Collections.singleton(event), soundSystemKernel));
	}//end sync()
    }//end dequeueSoundEvent(...)

    public double getSamplesPerMilli() {
	return ((double)getActiveFormat().getFrameRate())/1000.;
    }

    /**
     * @return the activeDriver
     */
    AudioDriver getActiveDriver() {
	/*
	if(activeDriver==null){
            System.out.println("Overriding null driver to default...");
            setActiveDriver(new JavaSoundSystemAudioOutput());
        }//end if(null)
        return activeDriver;
        */
	return soundSystemKernel.getActiveDriver();
    }
/*
    private void setActiveDriver(AudioDriver activeDriver) {
	//If the default device is null then switch to the dummy driver.
	if( activeDriver.getDefaultDevice() == null )
	    activeDriver = DUMMY_DRIVER;
	
	//if(this.activeDriver!=null)
	//    activeDriver.release();
	final AudioDriver oldActiveDriver = this.activeDriver;
	this.activeDriver = activeDriver;
	
	synchronized(soundThreadExecutor){
	    soundThreadExecutor.execute(new SetActiveDriver(activeDriver, soundSystemKernel), SoundSystem.ACTIVE_DRIVER);
	    setBufferSizeFrames(getBufferSizeFrames());//Reset
	    setActiveDevice(activeDriver.getDefaultDevice());
	}
        System.out.println("SoundSystem: Active Driver Set To "+activeDriver);
        pcs.firePropertyChange(ACTIVE_DRIVER, oldActiveDriver, activeDriver);
    }//end setActiveDriver(...)
*/
    private AudioDevice getActiveDevice() {
	/*
	if(activeDevice==null){
            System.out.println("Overriding null device to default...");
            setActiveDevice(getActiveDriver().getDefaultDevice());
        }//end if(null)
        return activeDevice;
        */
	return soundSystemKernel.getActiveDevice();
    }

    /*
    private void setActiveDevice(AudioDevice activeDevice) {
	final AudioDevice oldDevice = this.activeDevice;
        this.activeDevice = activeDevice;
        
        synchronized( soundThreadExecutor ) {
            soundThreadExecutor.execute(new SetActiveDevice(activeDevice, soundSystemKernel), SoundSystem.ACTIVE_DEVICE);
            setActiveOutput(activeDevice.getDefaultOutput());
        }//end sync
        
        System.out.println("SoundSystem: Active Device Set To "+activeDevice);
        pcs.firePropertyChange(ACTIVE_DEVICE, oldDevice, activeDevice);
    }
*/
    private AudioOutput getActiveOutput() {
	/*
	if(activeOutput==null)
	    activeOutput = getActiveDevice().getDefaultOutput();
        return activeOutput;
        */
	return soundSystemKernel.getActiveOutput();
    }

    /*
    private void setActiveOutput(AudioOutput newActiveOutput) {
	final AudioOutput oldOutput = this.activeOutput;
        this.activeOutput = newActiveOutput;
        
        synchronized( soundThreadExecutor ){
            soundThreadExecutor.execute(new SetActiveOutput(newActiveOutput, soundSystemKernel), SoundSystem.ACTIVE_OUTPUT);
            if( newActiveOutput != null )
        	setActiveFormat(newActiveOutput.getDefaultFormat());
            else
        	setActiveFormat(null);
        }//end sync()
        
        System.out.println("SoundSystem: Active Output Set To "+newActiveOutput);
	//getActiveDriver().setOutput(newActiveOutput);
	pcs.firePropertyChange(ACTIVE_OUTPUT, oldOutput, newActiveOutput);
    }*/
    
    public AudioFormat getActiveFormat() {
	/*
	if(activeFormat==null){
	    final AudioOutput activeOutput = getActiveOutput();
	    if(activeOutput != null)
	        return activeOutput.getDefaultFormat();
	    }
        return activeFormat;
        */
	return soundSystemKernel.getFormat();
    }

    /*
    private void setActiveFormat(AudioFormat newFormat) {
	final AudioFormat oldFormat = this.activeFormat;
	//If unspecified, default to 44100
	if(newFormat!=null)
	 if(newFormat.getSampleRate()==AudioSystem.NOT_SPECIFIED){
	    System.out.println("New format sample rate was not specified. Defaulting to 44100.");
	    newFormat = new AudioFormat(
		    newFormat.getEncoding(), 
		    44100, 
		    newFormat.getSampleSizeInBits(), 
		    newFormat.getChannels(), 
		    newFormat.getFrameSize(), 
		    44100, 
		    newFormat.isBigEndian());
	    }
	
        this.activeFormat = newFormat;
        
        synchronized( soundThreadExecutor ) {
            if( activeDriver != null )
                soundThreadExecutor.execute(new SetFormat(newFormat, soundSystemKernel), SoundSystem.ACTIVE_FORMAT);
        }//end sync()
        
        System.out.println("SoundSystem: Active Format Set To "+newFormat+" for driver "+activeDriver);
        pcs.firePropertyChange(ACTIVE_FORMAT, oldFormat, newFormat);
    }
*/
    
    int getBufferSizeFrames() {
        return bufferSizeFrames;
    }

    /**
     * @param bufferSizeFrames the bufferSizeFrames to set
     */
    public void setBufferSizeFrames(int bufferSizeFrames) {
	    if(bufferSizeFrames<=0)
		throw new RuntimeException("Invalid buffer size: "+bufferSizeFrames+". Must be greater than zero.");
	    final int oldValue = this.bufferSizeFrames;
	    this.bufferSizeFrames = bufferSizeFrames;
	    synchronized( soundThreadExecutor ) {
		soundThreadExecutor.execute(new SetBufferSizeFrames(bufferSizeFrames, soundSystemKernel), SoundSystem.BUFFER_SIZE_FRAMES);
	    }//end sync()
	    pcs.firePropertyChange(BUFFER_SIZE_FRAMES, oldValue, bufferSizeFrames);
    }
    
    public void setBufferSizeFramesString(String newValue){//TODO: Catch parsing error, throw IllegalArgumentException
	setBufferSizeFrames(Integer.parseInt(newValue));
    }
    
    public String getBufferSizeFramesString(){
	return ""+getBufferSizeFrames();
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
	final Boolean oldValue = this.bufferLag;
        this.bufferLag = bufferLag;
        synchronized( soundThreadExecutor ) {
            soundThreadExecutor.execute(new SetBufferLag(bufferLag,soundSystemKernel), SoundSystem.BUFFER_LAG);
        }
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
/*
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
*/
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
    
    public PackedTreeNode getPackedOutputConfigNode() {
	return new PackedTreeNode(getOutputConfigNode());
    }
    
    public void setPackedOutputConfigNode(PackedTreeNode node) {
	setOutputConfigNode(node.getNode());
    }

    public DefaultMutableTreeNode getOutputConfigNode() {
	if(outputConfig == null)
	    setOutputConfigNode(getDefaultConfigNode());

	return outputConfig;
    }//end getOutputConfig()
    
    public SoundSystemOutputConfig getOutputConfig() {
	final Object [] oPath = getOutputConfigNode().getUserObjectPath();
	final SoundSystemOutputConfig result = new SoundSystemOutputConfig();
	result.setDriverByName(oPath[1].getClass().getName());
	result.setDeviceByName(oPath[2].toString());
	result.setPortByName(oPath[3].toString());
	result.setFormatByName(oPath[4].toString());
	return result;
    }//end getOutputConfig()
    
    public void setOutputConfig(SoundSystemOutputConfig config) {
	    final DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);
	    List<DefaultMutableTreeNode> path = Util.nodePathFromUserObjectPath(config.getDriverByName(), config.getDeviceByName(), config.getPortByName(), config.getFormatByName());
	    path.add(0,root);
	    root.add(path.get(1));
	    setOutputConfigNode(path.get(path.size()-1));
    }//end setOutputConfig()

    private DefaultMutableTreeNode getDefaultConfigNode() {
	final DefaultMutableTreeNode fakeRoot = new DefaultMutableTreeNode("");
	final DefaultMutableTreeNode fakeDriver = new DefaultMutableTreeNode("org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput");
	final DefaultMutableTreeNode fakeDevice = new DefaultMutableTreeNode("default");
	final DefaultMutableTreeNode fakeOutput = new DefaultMutableTreeNode("SourceDataLine");
	final DefaultMutableTreeNode fakeFormat = new DefaultMutableTreeNode("PCM_SIGNED 44100.0Hz, 16bit, stereo, 4 bytes/frame, big-endian");
	fakeRoot.add(fakeDriver);
	fakeDriver.add(fakeDevice);
	fakeDevice.add(fakeOutput);
	fakeOutput.add(fakeFormat);

	DefaultMutableTreeNode outputConfig = fakeFormat;
	
	return Util.getComparatorApproximation(outputConfig, getOutputConfigTree(), 
        	(x,y)->100-FuzzySearch.ratio(x.toString(),y.toString()));
    }//end getDefaultConfig()
    
    public void setOutputConfigNode(DefaultMutableTreeNode outputConfig) {
	if( outputConfig == null)
	    outputConfig = getDefaultConfigNode();
	else if( outputConfig.getPath().length != 5) {
	    System.err.println("Invalid audio output config node depth of "+(outputConfig.getPath().length-1)+". Expected depth 4. Path leaf was `"+outputConfig+"`. Using default instead.");
	    outputConfig = getDefaultConfigNode();
	}
	final Object oldValue = this.outputConfig;
	if( Objects.equals(outputConfig,oldValue) )
	    return;
        
        this.outputConfig = Util.getComparatorApproximation(outputConfig, getOutputConfigTree(), 
        	(x,y)->100-FuzzySearch.ratio(x.toString(),y.toString()));
        
        final TreeNode [] configPath = this.outputConfig.getPath();
        
        if(configPath.length != 5)
            System.err.println("config path must be of level 4. Got level "+(configPath.length-1)+". Contents were: `"+outputConfig+"`. Retrived config was `"+this.outputConfig+"`");
        
        //// DRIVER
        final String driverName = configPath[1].toString();
        AudioDriver driver = null;
        final AudioDriver oldDriver = getActiveDriver();
        if(driverName!=null){
	    if(oldDriver!=null)
		oldDriver.release();
	    Class driverClass;
	    try{driverClass = Class.forName(driverName);}
		catch(ClassNotFoundException e){
	    driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;}
	    if(!AudioDriver.class.isAssignableFrom(driverClass))
	      driverClass = org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput.class;
	    try{driver = (AudioDriver)driverFactory.get(driverClass);}
	     catch(Exception e){e.printStackTrace();return;}
	}//end if(!null)
        
        //// DEVICE
        final String newDeviceByName = configPath[2].toString();
        AudioDevice device = null;
        if(newDeviceByName!=null){
	    final AudioDevice audioDevice = driver.getDeviceByName(newDeviceByName);
	    if(audioDevice!=null)
		device = audioDevice;
	}//end if(!null)
        
        //// OUTPUT
        final String outputByName = configPath[3].toString();
        AudioOutput output = null;
        if(outputByName!=null){
	    final AudioOutput ao = device.getOutputByName(outputByName);
	    if(ao!=null)
		output = ao;
	}//end if(!null)
        
        //// FORMAT
        final String formatByName = configPath[4].toString();
        AudioFormat format = null;
	if(formatByName!=null){
	    //final AudioOutput ao = getActiveOutput();
	    if( output != null ){
		final AudioFormat fmt = output.getFormatFromUniqueName(formatByName);
		if( fmt != null )
		    format = fmt;
	    }//end if(null)
	}//end if(!null)
        
        synchronized( soundThreadExecutor ) {
            soundThreadExecutor.execute(new SetAudioOutputConfig(
        	    driver,
        	    device,
        	    output,
        	    format,
        	    soundSystemKernel));
        }
        pcs.firePropertyChange(OUTPUT_CONFIG, oldValue, outputConfig);
    }//end setOutputConfig
    
}//end SoundSystem
