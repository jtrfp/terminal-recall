/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 */

package org.jtrfp.trcl.snd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import com.jogamp.opengl.GL3;
import javax.sound.sampled.AudioFormat;

import org.jtrfp.trcl.core.KeyedExecutor;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadDataType;
import org.jtrfp.trcl.gpu.GLTexture.PixelReadOrder;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.snd.SoundEvent.Factory;

public class SoundSystemKernel {
    private GPU gpu;
    private ThreadManager threadManager;
    private final DynamicCompressor compressor = new DynamicCompressor();
    private ByteBuffer renderFloatBytes;
    private GLFrameBuffer playbackFrameBuffer;
    private GLTexture playbackTexture;
    private int bufferSizeFrames = -1;
    private boolean bufferLag = false;
    private AudioFormat format = null;
    private AudioDriver activeDriver = null;
    private AudioDevice activeDevice = null;
    private AudioOutput activeOutput;
    
    private final HashMap<SoundEvent.Factory,ArrayList<SoundEvent>> eventMap 
        = new HashMap<SoundEvent.Factory,ArrayList<SoundEvent>>();
    private KeyedExecutor<Object> keyedExecutor;
    private final ArrayList<SoundEvent> activeEvents = new ArrayList<SoundEvent>();
    private final TreeSet<SoundEvent> pendingEvents = new TreeSet<SoundEvent>(new Comparator<SoundEvent>(){
	@Override
	public int compare(SoundEvent first, SoundEvent second) {
	    final double result = 88200. * (first.getStartRealtimeSeconds()-second.getStartRealtimeSeconds());
	    if(result==0)return first.hashCode()-second.hashCode();
	    else if(result>0)return 1;
	    else return -1;
	}//end compare()
    });
    
    public static class AddToActiveEvents implements Runnable {
	private final Collection<SoundEvent> events;
	private final SoundSystemKernel target;

	public AddToActiveEvents(Collection<SoundEvent> event, SoundSystemKernel kernel){
	    this.events  = event;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.activeEvents.addAll(events);
	}
    }//end AddToActiveEvent
    
    public static class AddToPendingEvents implements Runnable {
	private final Collection<SoundEvent> events;
	private final SoundSystemKernel      target;

	public AddToPendingEvents(Collection<SoundEvent> event, SoundSystemKernel kernel){
	    this.events  = event;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.pendingEvents.addAll(events);
	}
    }//end AddToPendingEvents
    
    public static class RemoveFromEvents implements Runnable {
	private final Collection<SoundEvent> events;
	private final SoundSystemKernel      target;

	public RemoveFromEvents(Collection<SoundEvent> event, SoundSystemKernel kernel){
	    this.events  = event;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.activeEvents .removeAll(events);
	    target.pendingEvents.removeAll(events);
	}
    }//end AddToActiveEvent
    
    public static class SetBufferSizeFrames implements Runnable {
	private final int newValue;
	private final SoundSystemKernel      target;

	public SetBufferSizeFrames(int newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setBufferSizeFrames(newValue);
	    final AudioDriver driver = target.getActiveDriver();
	    if( driver != null )
	      target.getActiveDriver().setBufferSizeFrames(newValue);
	}
    }//end SetBufferSizeFrames
    
    public static class SetFormat implements Runnable {
	private final AudioFormat newValue;
	private final SoundSystemKernel      target;

	public SetFormat(AudioFormat newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setFormat(newValue);
	}
    }//end SetFormat
    
    public static class SetBufferLag implements Runnable {
	private final boolean newValue;
	private final SoundSystemKernel target;

	public SetBufferLag(boolean newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setBufferLag(newValue);
	}
    }//end SetBufferLag
    
    public static class SetActiveDriver implements Runnable {
	private final AudioDriver newValue;
	private final SoundSystemKernel target;

	public SetActiveDriver(AudioDriver newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target   = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setActiveDriver(newValue);
	}
    }//end SetActiveDriver
    
    public static class SetActiveDevice implements Runnable {
	private final AudioDevice newValue;
	private final SoundSystemKernel target;

	public SetActiveDevice(AudioDevice newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target   = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setActiveDevice(newValue);
	}
    }//end SetActiveDevice
    
    public static class SetActiveOutput implements Runnable {
	private final AudioOutput newValue;
	private final SoundSystemKernel target;

	public SetActiveOutput(AudioOutput newValue, SoundSystemKernel kernel){
	    this.newValue = newValue;
	    this.target   = kernel;
	}//end constructor(...)

	@Override
	public void run() {
	    target.setActiveOutput(newValue);
	}
    }//end SetActiveDevice
    
    public static class SetAudioOutputConfig implements Runnable {
	private final AudioDriver       driver;
	private final AudioDevice       device;
	private final AudioOutput       output;
	private final AudioFormat       format;
	private final SoundSystemKernel kernel;
	
	public SetAudioOutputConfig(AudioDriver driver,
		                    AudioDevice device,
		                    AudioOutput output,
		                    AudioFormat format,
		                    SoundSystemKernel kernel) {
	    this.driver = driver;
	    this.device = device;
	    this.output = output;
	    this.format = format;
	    this.kernel = kernel;
	}//end constructor
	
	@Override
	public void run() { 
	    kernel.setActiveDriver(driver);
	    kernel.setActiveDevice(device);
	    kernel.setActiveOutput(output);
	    kernel.setFormat(format);
	    
	    kernel.regenerateRenderFloatBytes();
	}//end run()
    }//end SetAudioOutputConfig
    
    private static final int NUM_BUFFER_ROWS=1;
    
    public double getBufferSizeSeconds() {
	final AudioFormat activeFormat = getFormat();
	if( activeFormat != null )
	    return (double)getBufferSizeFrames() / (double)activeFormat.getFrameRate();
	return 0;
    }
    
    public double execute(final double bufferTimeCounter) {
	synchronized(keyedExecutor){
	    keyedExecutor.executeAllFromThisThread();
	}
	//Get state
	final AudioDriver driver = getActiveDriver();
	final AudioFormat format = getFormat();
	final AudioOutput output = getActiveOutput();
	final AudioDevice device = getActiveDevice();
	//If invalid state, abort
	if( driver == null || format == null || output == null || device == null )
	    return 0;
	
	renderPrep(bufferTimeCounter);
	final ByteBuffer renderFloatBytes     = getRenderFloatBytes();
	final GLFrameBuffer renderFrameBuffer = getPlaybackFrameBuffer();
	final GLTexture renderTexture         = getPlaybackTexture();
	
	getThreadManager().submitToGL(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		renderFloatBytes.clear();
		render(getGpu(), getGpu().getGl(), renderFloatBytes, renderFrameBuffer, renderTexture, bufferTimeCounter);
		return null;
	    }
	}).get();
	//sBuf.clear();
	final FloatBuffer fBuf = renderFloatBytes.asFloatBuffer();
	fBuf.clear();
	compressor.setSource(fBuf);
	double bufferTimePassedSeconds = 0;
	if(driver!=null){
	    driver.setSource(compressor);
	    driver.flush();
	}//end driver!=null
	
	return bufferTimePassedSeconds;
    }//end execute

    private void renderPrep(double bufferTimeCounter){
	cleanActiveEvents(bufferTimeCounter);
	pickupActiveEvents(bufferTimeCounter, getBufferSizeFrames()/getFormat().getFrameRate());
    }//end renderPrep()
    
    private void render(GPU gpu, GL3 gl, ByteBuffer audioByteBuffer, GLFrameBuffer renderFrameBuffer, GLTexture renderTexture, double bufferTimeCounter) {
	    //final GPU gpu = getGpu();
	
	    ensureRenderFloatBytesAreValid();//TODO: Remove after debugged
	    ensureRenderTargetIsValid();//TODO: Remove after debugged

	    if(isBufferLag())
		readGLAudioBuffer(gpu,audioByteBuffer,renderTexture);

	    // Render
	    renderFrameBuffer.bindToDraw();
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

	    if(!isBufferLag())
		readGLAudioBuffer(gpu,audioByteBuffer,renderTexture);

	    // Cleanup
	    gpu.defaultFrameBuffers();
	    gpu.defaultProgram();
	    gpu.defaultTIU();
	    gpu.defaultTexture();
	    gpu.defaultViewport();
    }// end render()
    
    private void readGLAudioBuffer(GPU gpu, ByteBuffer audioByteBuffer, GLTexture renderTexture){
	// Read and export previous results to sound card.
	final GL3 gl = gpu.getGl();
	gpu.defaultFrameBuffers();
	renderTexture.bind().readPixels(PixelReadOrder.RG, PixelReadDataType.FLOAT,
		audioByteBuffer).unbind();// RG_INTEGER throws INVALID_OPERATION!?
    }//end readGLAudioBuffer(...)
    

    private void pickupActiveEvents(double bufferTimeCounter, double windowSizeInSeconds){
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
    
    private void cleanActiveEvents(double bufferTimeCounter){
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

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }
    /*
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
					GL3.GL_COLOR_ATTACHMENT0);
		    }}).get();
	    }//end if(playbackFrameBuffer==null)
	    return playbackFrameBuffer;
	}finally{lock.unlock();}
    }//end getPlaybackFrameBuffer()
    */
    private boolean areRenderFloatBytesValid( ) {
	if( renderFloatBytes == null )
	    return false;
	final AudioFormat format         = getFormat();
	final int expectedNumberOfFrames = getBufferSizeFrames();
	final int sampleSizeInBytes      = 4; //FLOAT32 in GPU
	final int numChannels            = format.getChannels();
	final int bytesPerFrame          = sampleSizeInBytes * numChannels;
	final int expectedNumberOfBytes  = expectedNumberOfFrames * bytesPerFrame;
	return renderFloatBytes.capacity() >= expectedNumberOfBytes;
    }//end areRenderFloatBytesValid(...)
    
    private boolean isPlaybackRenderTargetValid( ) {
	if( playbackFrameBuffer == null || playbackTexture == null )
	    return false;
	final int numTexels         = playbackTexture.getWidth() * playbackTexture.getHeight();
	final int expectedNumTexels = getBufferSizeFrames();
	return ( numTexels == expectedNumTexels );
    }//end isPlaybackRenderTargetValid()
    
    private void regenerateRenderFloatBytes(){
	releaseRenderFloatBytes();
	generateRenderFloatBytes();
    }
    
    private void releaseRenderFloatBytes() {
	renderFloatBytes = null;
    }
    
    private void generateRenderFloatBytes( ) {
	final AudioFormat format         = getFormat();
	if( format == null)
	    return;
	final int expectedNumberOfFrames = getBufferSizeFrames();
	final int sampleSizeInBytes      = 4;//Float32 for GPU
	final int numChannels            = format.getChannels();//XXX: Only supports 2 channels!!!
	final int bytesPerFrame          = sampleSizeInBytes * numChannels;
	final int expectedNumberOfBytes  = expectedNumberOfFrames * bytesPerFrame;
	renderFloatBytes = ByteBuffer.allocateDirect(expectedNumberOfBytes).order(ByteOrder.nativeOrder());
    }//end generateRenderFloatBytes(...)
    
    private void generateRenderTarget( ) {
	//Playback texture
	getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		final GPU gpu = getGpu();
		//// Generate Texture
		gpu.defaultProgram();
		gpu.defaultTIU();
		gpu.defaultTexture();
		gpu.defaultFrameBuffers();
		final GLTexture newTexture = playbackTexture = gpu
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
		//// Generate Framebuffer
		gpu.defaultTexture();
		gpu.defaultTIU();
		playbackFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(newTexture,
				GL3.GL_COLOR_ATTACHMENT0);
		return null;//FIXME: Should this reset to default texture/TIU afterward to improve performance?
	    }}).get();
    }//end generateRenderTarget(...)
    
    private void releaseRenderTarget( GLFrameBuffer playbackFrameBuffer, GLTexture playbackTexture ) {
	if( playbackFrameBuffer != null || playbackTexture != null ) {
	    final GLFrameBuffer frameBufferToDelete = playbackFrameBuffer;
	    final GLTexture     textureToDelete     = playbackTexture;
	    getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    if( frameBufferToDelete != null )
		        frameBufferToDelete.destroy();
		    if( textureToDelete != null )
			textureToDelete.delete();
		    return null;
		}});
	}//end if( !null )
	playbackFrameBuffer = null;
	playbackTexture     = null;
    }//end releaseRenderTarget()
    
    private void regenerateRenderTarget(){
	final GLFrameBuffer previousPlaybackFrameBuffer = playbackFrameBuffer;
	final GLTexture     previousPlaybackTexture     = playbackTexture;
	generateRenderTarget();
	releaseRenderTarget(previousPlaybackFrameBuffer, previousPlaybackTexture);
    }
    
    private void ensureRenderTargetIsValid() {
	if(! isPlaybackRenderTargetValid()) //TODO: Find out why this isn't catching the mismatch.
	    regenerateRenderTarget();
    }
    
    private void ensureRenderFloatBytesAreValid() {
	if(! areRenderFloatBytesValid() )
	    regenerateRenderFloatBytes();
    }
    
    private GLTexture getPlaybackTexture( ){
	ensureRenderTargetIsValid();
	return playbackTexture;
    }//end getPlaybackTexture()
    
    private GLFrameBuffer getPlaybackFrameBuffer( ){
	ensureRenderTargetIsValid();
	return playbackFrameBuffer;
    }
    
    private ByteBuffer getRenderFloatBytes( ){
	ensureRenderFloatBytesAreValid();
	return renderFloatBytes;
    }

    protected int getBufferSizeFrames() {
        return bufferSizeFrames;
    }

    protected void setBufferSizeFrames(int bufferSizeFrames) {
        this.bufferSizeFrames = bufferSizeFrames;
        releaseRenderFloatBytes();
        //regenerateRenderFloatBytes();
    }

    protected boolean isBufferLag() {
        return bufferLag;
    }

    protected void setBufferLag(boolean bufferLag) {
        this.bufferLag = bufferLag;
    }

    protected AudioFormat getFormat() {
        return format;
    }

    protected void setFormat(AudioFormat format) {
        this.format = format;
        if( activeDriver != null && format != null )
            activeDriver.setFormat(format);
    }

    protected AudioDriver getActiveDriver() {
        return activeDriver;
    }

    protected void setActiveDriver(AudioDriver activeDriver) {
	if(this.activeDriver!=null)
	    activeDriver.release();
        this.activeDriver = activeDriver;
        activeDriver.setBufferSizeFrames(getBufferSizeFrames());
    }

    void setKeyedExecutor(KeyedExecutor<Object> runnableQueue) {
	keyedExecutor = runnableQueue;
    }

    protected AudioDevice getActiveDevice() {
        return activeDevice;
    }

    protected void setActiveDevice(AudioDevice activeDevice) {
        this.activeDevice = activeDevice;
    }

    protected AudioOutput getActiveOutput() {
        return activeOutput;
    }

    protected void setActiveOutput(AudioOutput activeOutput) {
	getActiveDriver().setOutput(activeOutput);
        this.activeOutput = activeOutput;
    }
}//end SoundSystemKernel
