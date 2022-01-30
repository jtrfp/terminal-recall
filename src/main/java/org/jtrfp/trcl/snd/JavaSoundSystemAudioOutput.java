/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class JavaSoundSystemAudioOutput implements AudioDriver {
    private static final DummyAudioDriver DUMMY_DRIVER = new DummyAudioDriver();
    private static final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    private AudioProcessor source;
    private AudioFormat format;
    private ByteBuffer     buffer;
    private int            bufferSizeFrames;
    private SourceDataLine sourceDataLine;
    private JavaSoundOutput        output;
    private Collection<AudioDevice>devices;
    private SampleSubmitter		   activePutter;
    
    public JavaSoundSystemAudioOutput() {
	setFormat(new AudioFormat(
		    encoding,
		    (float)44100,
		    16,
		    2,
		    4,
		    44100,
		    ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN,
		    new HashMap<String,Object>()));//Default
    }//end constructor
    
    @Override
    public synchronized void setFormat(AudioFormat format) {
	if(format == null)
	    throw new IllegalArgumentException();
	if(this.format!=format){
		this.format=format;
		if(format!=null){
		    final boolean signed = format.getEncoding() == Encoding.PCM_SIGNED;
		    if(format.getSampleSizeInBits()==8)
			activePutter=signed?new BytePutter():new UnsignedBytePutter();
		    else if(format.getSampleSizeInBits()==16)
			activePutter=signed?new ShortPutter():new UnsignedShortPutter();
		    else if(format.getSampleSizeInBits()==32)
			activePutter=signed?new IntPutter():new UnsignedIntPutter();
		    else activePutter=null;
		}
		ensureSourceDataLineIsReleased();
	}//Reset dependencies
	setBuffer(null);
    }

    @Override
    public synchronized void setSource(AudioProcessor source) {
	this.source=source;
    }

    @Override
    public synchronized void flush() {
	if(format==null||source==null)
	    return;
	
	final SampleSubmitter putter = getPutter();
	if(putter==null)
	    return;
	ByteBuffer scratch = getBuffer();
	
	final int bufferSizeFrames = getBufferSizeFrames();
	final int numChannels = format.getChannels();
	final int numIterations = bufferSizeFrames * numChannels;
	try{for (int i = numIterations; i > 0; i--)
	    putter.submit(scratch,source.get());
	}catch(BufferUnderflowException e){System.out.println("UNDERFLOW: javaSoundSystem bufferSizeFrames="+bufferSizeFrames+" numChannels="+numChannels); throw e;}
	scratch.clear();
	try{
	    final SourceDataLine sourceDataLine = getSourceDataLine();
	    final AudioFormat fmt = sourceDataLine.getFormat();
	    sourceDataLine.write(scratch.array(), 0, scratch.remaining());}
	catch(LineUnavailableException e){throw new RuntimeException(e);}//TODO: Manage this better.
    }//end flush()
    
    private ByteBuffer getBuffer(){
	if(buffer==null){
	    final ByteBuffer bb = ByteBuffer.allocate(getBufferSizeFrames()*format.getChannels()*(format.getSampleSizeInBits()/8));
	    bb.order(format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN);
	    setBuffer(bb);
	}//end if(buffer)
	return buffer;
    }//end getBuffer()
    
    private SampleSubmitter getPutter(){
	return activePutter;
    }
    
    private interface SampleSubmitter{
	public void submit(ByteBuffer bb, float source);
    }
    
    private final class BytePutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.put((byte)(source * (float) Byte.MAX_VALUE));
	}
    }//end BytePutter
    
    private final class UnsignedBytePutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.put((byte)((source * (float) Byte.MAX_VALUE) - Byte.MIN_VALUE));
	}
    }//end UnsignedBytePutter
    
    private final class ShortPutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.putShort((short) (source * (float) Short.MAX_VALUE));
	}
    }//end ShortPutter
    
    private final class UnsignedShortPutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.putShort((short) ((source * (float) Short.MAX_VALUE) - Short.MIN_VALUE));
	}
    }//end ShortPutter
    
    private final class IntPutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.putInt((int) (source * (float) Integer.MAX_VALUE));
	}
    }//end IntPutter
    
    private final class UnsignedIntPutter implements SampleSubmitter{
	@Override
	public synchronized void submit(ByteBuffer bb, float source) {
	    bb.putInt((int) ((source * (float) Integer.MAX_VALUE) - Integer.MIN_VALUE));
	}
    }//end UnsignedIntPutter

    /**
     * @return the sourceDataLine
     */
    public synchronized SourceDataLine getSourceDataLine() throws LineUnavailableException{
	if(output==null)
	    throw new LineUnavailableException();
	if(sourceDataLine==null){
	        setSourceDataLine((SourceDataLine) AudioSystem.getLine(output.info));
	        sourceDataLine.open(format);
	        sourceDataLine.start();
	}//end if(sourceDataLine==null)
        return sourceDataLine;
    }//end getSourceDataLine()

    /**
     * @return the bufferSizeFrames
     */
    public synchronized int getBufferSizeFrames() {
        return bufferSizeFrames;
    }

    /**
     * @param bufferSizeFrames the bufferSizeFrames to set
     */
    public synchronized void setBufferSizeFrames(int bufferSizeFrames) {
	if( this.bufferSizeFrames == bufferSizeFrames )
	    return;//No change.
        this.bufferSizeFrames = bufferSizeFrames;
        staleBuffer();
    }

    /**
     * @return the format
     */
    public synchronized AudioFormat getFormat() {
        return format;
    }
    
    private void staleBuffer(){
	this.buffer=null;
    }

    /**
     * @param buffer the buffer to set
     */
    private void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        if(buffer!=null)
         this.buffer.order((getFormat().isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * @param sourceDataLine the sourceDataLine to set
     */
    public synchronized void setSourceDataLine(SourceDataLine sourceDataLine) {
	ensureSourceDataLineIsReleased();
        this.sourceDataLine = sourceDataLine;
    }
    
    @Override
    public synchronized String toString(){
	return "JavaSound Driver";
    }

    @Override
    public synchronized Collection<AudioDevice> getDevices() {
	if(devices==null){
	    devices = new ArrayList<AudioDevice>();
	    for(Mixer.Info i:AudioSystem.getMixerInfo())
		 if(!i.toString().toLowerCase().contains("pulseaudio"))//XXX Blacklist the Pulseaudio device
	             devices.add(new JavaSoundDevice(i,this));
	}
	return devices;
    }//end getDevices()
    
    private class JavaSoundOutput implements AudioOutput{
	final SourceDataLine.Info info;
	final ArrayList<AudioFormat> formats = new ArrayList<AudioFormat>();
	final AudioDevice dev;
	public JavaSoundOutput(SourceDataLine.Info info, AudioDevice dev){
	    this.info=info;this.dev=dev;
	    for(AudioFormat f:info.getFormats()){
		if(isFormatValid(f))
		    formats.add(sanitizeFormat(f));
	    }//end for(format)
	}
	
	AudioFormat sanitizeFormat(AudioFormat format) {
	    AudioFormat result = format;
	    /*With Java sound PCM, frame rate and sample rates are identical.
	     * In TRCL, the frame rate is the traditional "sample rate" whereas a sampling rate is frame rate * channel count
	     * for use in calculating buffer sizes. 
	     * */
	    if(result.getFrameRate() <= 0)
		result = new AudioFormat(
			result.getEncoding(), 
			44100, 
			result.getSampleSizeInBits(), 
			result.getChannels(), 
			result.getFrameSize(), 
			44100, 
			result.isBigEndian());
	    return result;
	}
	
	private boolean isFormatValid(AudioFormat format) {
	    final int sampleSizeBits = format.getSampleSizeInBits();
	    final int numChannels = format.getChannels();
	    if(numChannels != 2)
		return false;
	    if(sampleSizeBits != 8 && sampleSizeBits != 16 && sampleSizeBits != 32)
		return false;
	    //if(format.getFrameRate() <= 0)
		//return false;
	    final Encoding encoding = format.getEncoding();
	    if(encoding == Encoding.ALAW || encoding == Encoding.ULAW)
		return false;
	    return true;
	}
	
	@Override
	public synchronized String getUniqueName() {
	    return info.toString();
	}
	@Override
	public synchronized String toString(){
	    return getUniqueName();
	}
	@Override
	public synchronized AudioFormat[] getFormats() {
	    return formats.toArray(new AudioFormat[formats.size()]);
	}
	@Override
	public synchronized AudioFormat getFormatFromUniqueName(String name) {
	    for (AudioFormat fmt : getFormats())
		if (fmt.toString().contentEquals(name))
		    return fmt;
	    System.err.println("Failed to find matching format for "+name);
	    return null;
	}
	@Override
	public AudioDevice getDevice() {
	    return dev;
	}
	@Override
	public AudioFormat getDefaultFormat() {
	    final AudioFormat []formats = getFormats();
	    if(formats.length==0)
		return null;
	    for( AudioFormat fmt : formats )
		if(SoundSystem.isAcceptableFormat(fmt))
		    return fmt;
	    return null;//Give up
	}
    }//end JavaSoundOutput
    
    private class JavaSoundDevice implements AudioDevice{
	Mixer.Info info;
	private final AudioDriver driver;
	public JavaSoundDevice(Mixer.Info info, AudioDriver driver){
	    this.info=info;this.driver=driver;
	}
	@Override
	public synchronized Collection<? extends AudioOutput> getOutputs() {
	    final Line.Info [] lines = AudioSystem.getMixer(info).getSourceLineInfo();
	    final Collection<AudioOutput> result = new ArrayList<AudioOutput>();
	    for(Line.Info info:lines){
		if(info instanceof SourceDataLine.Info && info.getLineClass() != Clip.class){
		    final SourceDataLine.Info sdlInfo = (SourceDataLine.Info)info;
		    result.add(new JavaSoundOutput(sdlInfo, this));
		}
	    }//end for(lines)
	    return result;
	}
	@Override
	public synchronized String toString(){
	    return getUniqueName();
	}
	@Override
	public synchronized String getUniqueName() {
	    return info.toString();
	}
	@Override
	public synchronized AudioOutput getOutputByName(String uniqueName) {
	    Collection<? extends AudioOutput> outputs = getOutputs();
	    for(AudioOutput ao:outputs){
		if(ao.getUniqueName().contentEquals(uniqueName))
		    return ao;
	    }
	    System.err.println("Failed to find matching output for "+uniqueName);
	    new Throwable().printStackTrace();;
	    return null;
	}
	@Override
	public AudioDriver getDriver() {
	    return driver;
	}
	@Override
	public AudioOutput getDefaultOutput() {
	    final Collection<? extends AudioOutput> outputs = getOutputs();
	    if(outputs.size()<=0)
		return null;
	    return getOutputs().iterator().next();
	}
    }//end JavaSoundDevice

    @Override
    public synchronized AudioDevice getDeviceByName(String name) {
	for(AudioDevice dev:getDevices())
	    if(dev.getUniqueName().contentEquals(name))
		return dev;
	System.err.println("Failed to find matching device for "+name);
	return getDefaultDevice();
    }

    @Override
    public synchronized void setOutput(AudioOutput o) {
	if(this.output!=o)
	    ensureSourceDataLineIsReleased();
	
	if(o==null){
	    this.output=null;
	    return;}
	
	if(o instanceof JavaSoundOutput){
	    JavaSoundOutput jso = (JavaSoundOutput)o;
	    this.output = jso;
	}else throw new RuntimeException("Unsupported AudioOutput type: "+o.getClass().getName()+" expecting JavaSoundOutput created by this driver.");
    }
    
    private void ensureSourceDataLineIsReleased(){
	if(sourceDataLine!=null)
	    if(sourceDataLine.isOpen()){
		if(sourceDataLine.isRunning() || sourceDataLine.isActive())
		    sourceDataLine.stop();
		sourceDataLine.close();
		}
	sourceDataLine=null;
    }//end ensureSourceDataLineIsReleased()

    @Override
    public synchronized void release() {
	ensureSourceDataLineIsReleased();
    }
    
    @Override
    public synchronized void finalize() throws Throwable{
	release();
	super.finalize();
    }

    @Override
    public AudioDevice getDefaultDevice() {
	final Collection<? extends AudioDevice> devices = getDevices();
	if(devices.size()<=0)
	    return DUMMY_DRIVER.getDefaultDevice();
	return devices.iterator().next();
    }
}//end JavaSoundSystemAudioOutput
