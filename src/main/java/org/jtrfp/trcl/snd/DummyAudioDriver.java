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

import java.util.Arrays;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class DummyAudioDriver implements AudioDriver{ 
    private final AudioDevice [] devices = new AudioDevice[]{
	    new DummyAudioDevice(this)
    };

    @Override
    public void setFormat(AudioFormat format) {}

    @Override
    public void setSource(AudioProcessor processor) {}

    @Override
    public void flush() {}

    @Override
    public Collection<AudioDevice> getDevices() {
	return Arrays.asList(devices);
    }

    @Override
    public AudioDevice getDeviceByName(String outputName) {
	return devices[0];
    }

    @Override
    public void setOutput(AudioOutput o) {}

    @Override
    public void release() {}

    @Override
    public void setBufferSizeFrames(int numFrames) {}

    @Override
    public AudioDevice getDefaultDevice() {
	return devices[0];
    }
    
    private static class DummyAudioDevice implements AudioDevice {
	private final AudioDriver driver;
	private final AudioOutput [] outputs = new AudioOutput [] {
		new DummyAudioOutput(this)
		};
	
	public DummyAudioDevice(AudioDriver driver){
	    this.driver = driver;
	}//end constructor

	@Override
	public Collection<? extends AudioOutput> getOutputs() {
	    return Arrays.asList(outputs);
	}

	@Override
	public AudioOutput getOutputByName(String uniqueName) {
	    return outputs[0];
	}

	@Override
	public String getUniqueName() {
	    return "Dummy Audio Device (no sound)";
	}

	@Override
	public AudioDriver getDriver() {
	    return driver;
	}

	@Override
	public AudioOutput getDefaultOutput() {
	    return outputs[0];
	}
    }//end DummyAudioDevice
    
    private static class DummyAudioOutput implements AudioOutput {
	private final AudioDevice device;
	private static final AudioFormat [] FORMATS 
	      = new AudioFormat [] {
		new AudioFormat(Encoding.PCM_SIGNED, 44100, 16, 2, 2, 44100, true)
	};

	public DummyAudioOutput(AudioDevice device) {
	    this.device = device;
	}

	@Override
	public String getUniqueName() {
	    return "Dummy Audio Driver (no sound)";
	}

	@Override
	public AudioFormat[] getFormats() {
	    return FORMATS;
	}

	@Override
	public AudioFormat getFormatFromUniqueName(String name) {
	    return FORMATS[0];
	}

	@Override
	public AudioDevice getDevice() {
	    return device;
	}

	@Override
	public AudioFormat getDefaultFormat() {
	    return FORMATS[0];
	}

    }//end DummyAudioOutput

}//end DummyAudioDriver
