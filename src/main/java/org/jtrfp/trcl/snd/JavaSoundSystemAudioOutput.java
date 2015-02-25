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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.collections.CollectionUtils;
import org.jtrfp.trcl.gui.SoundOutputSelector;

public class JavaSoundSystemAudioOutput implements AudioDriver {
    private static final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    
    static{SoundOutputSelector.outputDrivers.add(new JavaSoundSystemAudioOutput());
    }
    
    private AudioProcessor source;
    private AudioFormat format = new AudioFormat(
	    encoding,
	    (float)44100,
	    16,
	    2,
	    4,
	    44100,
	    ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN,
	    new HashMap<String,Object>());//Default
    private ByteBuffer     buffer;
    private int            bufferSizeFrames = 4096 * 2;
    private SourceDataLine sourceDataLine;
    private DataLine.Info  lineInfo;
    private Collection<AudioDevice>outputs;
    
    @Override
    public void setFormat(AudioFormat format) {
	this.format=format;
	//Reset dependencies
	setBuffer(null);
	setLineInfo(null);
	setSourceDataLine(null);
    }

    @Override
    public void setSource(AudioProcessor source) {
	this.source=source;
    }

    @Override
    public void flush() {
	if(format==null||source==null)
	    return;
	ByteBuffer scratch = getBuffer();
	ShortBuffer shortScratch = scratch.asShortBuffer();
	shortScratch.clear();
	for (int i = 0; i < getBufferSizeFrames() * format.getChannels(); i++) 
	    scratch.putShort((short) (source.get() * (double) Short.MAX_VALUE));
	scratch.clear();
	try{getSourceDataLine().write(scratch.array(), 0, scratch.remaining());}
	catch(LineUnavailableException e){throw new RuntimeException(e);}//TODO: Manage this better.
    }//end flush()
    
    private ByteBuffer getBuffer(){
	if(buffer==null)
	    setBuffer(ByteBuffer.allocate(getBufferSizeFrames()*format.getChannels()*(format.getSampleSizeInBits()/8)));
	return buffer;
    }

    /**
     * @return the sourceDataLine
     */
    public SourceDataLine getSourceDataLine() throws LineUnavailableException{
	if(sourceDataLine==null){
	        setSourceDataLine((SourceDataLine) AudioSystem.getLine(getLineInfo()));
	        sourceDataLine.open();
	        sourceDataLine.start();
	}//end if(sourceDataLine==null)
        return sourceDataLine;
    }//end getSourceDataLine()

    /**
     * @return the lineInfo
     */
    public DataLine.Info getLineInfo() {
        if(lineInfo==null)
            lineInfo = 
     	       new DataLine.Info(SourceDataLine.class, getFormat());
        return lineInfo;
    }//end getLineInfo

    /**
     * @return the bufferSizeFrames
     */
    public int getBufferSizeFrames() {
        return bufferSizeFrames;
    }

    /**
     * @param bufferSizeFrames the bufferSizeFrames to set
     */
    public void setBufferSizeFrames(int bufferSizeFrames) {
        this.bufferSizeFrames = bufferSizeFrames;
    }

    /**
     * @return the format
     */
    public AudioFormat getFormat() {
        return format;
    }

    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        if(buffer!=null)
         this.buffer.order((getFormat().isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * @param sourceDataLine the sourceDataLine to set
     */
    public void setSourceDataLine(SourceDataLine sourceDataLine) {
	if(this.sourceDataLine!=null)
	    this.sourceDataLine.close();
        this.sourceDataLine = sourceDataLine;
    }

    /**
     * @param lineInfo the lineInfo to set
     */
    public void setLineInfo(DataLine.Info lineInfo) {
        this.lineInfo = lineInfo;
    }
    
    @Override
    public String toString(){
	return "JavaSound Driver";
    }

    @Override
    public Collection<AudioDevice> getDevices() {
	if(outputs==null){
	    outputs = new ArrayList<AudioDevice>();
	    for(Mixer.Info i:AudioSystem.getMixerInfo())
	     outputs.add(new JavaSoundDevice(i));
	}
	return outputs;
    }//end getOutputs()
    
    private class JavaSoundOutput implements AudioOutput{
	final SourceDataLine.Info info;
	public JavaSoundOutput(SourceDataLine.Info info){
	    this.info=info;}
	@Override
	public String getUniqueName() {
	    return info.toString();
	}
	@Override
	public String toString(){
	    return getUniqueName();
	}
	@Override
	public AudioFormat[] getFormats() {
	    return info.getFormats();
	}
	@Override
	public AudioFormat getFormatFromUniqueName(String name) {
	    for (AudioFormat fmt : getFormats())
		if (fmt.toString().contentEquals(name))
		    return fmt;
	    return null;
	}
    }//end JavaSoundOutput
    
    private class JavaSoundDevice implements AudioDevice{
	Mixer.Info info;
	public JavaSoundDevice(Mixer.Info info){
	    this.info=info;
	}
	@Override
	public Collection<? extends AudioOutput> getOutputs() {
	    final Line.Info [] lines = AudioSystem.getMixer(info).getSourceLineInfo();
	    final Collection<AudioOutput> result = new ArrayList<AudioOutput>();
	    for(Line.Info info:lines)
		if(info instanceof SourceDataLine.Info){
		    final SourceDataLine.Info sdlInfo = (SourceDataLine.Info)info;
		    result.add(new JavaSoundOutput(sdlInfo));
		}
	    return result;
	}
	@Override
	public String toString(){
	    return getUniqueName();
	}
	@Override
	public String getUniqueName() {
	    return info.toString();
	}
	@Override
	public AudioOutput getOutputByName(String uniqueName) {
	    for(AudioOutput ao:getOutputs())
		if(ao.getUniqueName().contentEquals(uniqueName))
		    return ao;
	    return null;
	}
    }//end JavaSoundDevice

    @Override
    public AudioDevice getDeviceByName(String outputName) {
	for(AudioDevice dev:getDevices())
	    if(dev.getUniqueName().contentEquals(outputName))
		return dev;
	return null;
    }

    @Override
    public void setOutput(AudioOutput o) {
	if(o instanceof JavaSoundOutput){
	    try{setSourceDataLine((SourceDataLine) AudioSystem.getLine(((JavaSoundOutput) o).info));}
	    catch(Exception e){e.printStackTrace();}
	}else throw new RuntimeException("Unsupported AudioOutput type: "+o.getClass().getName()+" expecting JavaSoundOutput created by this driver.");
	
	//setLineInfo(info);
    }

    @Override
    public AudioOutput getDefaultOutput() {
	throw new RuntimeException("Not implemented");//TODO
    }

}//end JavaSoundSystemAudioOutput
