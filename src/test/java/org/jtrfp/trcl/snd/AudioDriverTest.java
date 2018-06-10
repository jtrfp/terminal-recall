/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2018 Chuck Ritola
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

import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.junit.Assert;
import org.junit.Test;

public abstract class AudioDriverTest {
    
    protected abstract AudioDriver getSubject();
    
    private static final AudioProcessor AUDIO_PROCESSOR = new AudioProcessor(){
	@Override
	public float get() {
	    return 0;
	}};

    @Test
    public void testSetFormat() {
	final AudioDriver subject = getSubject();
	subject.setFormat(new AudioFormat(Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true));
    }
    
    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testSetFormatNull() {
	final AudioDriver subject = getSubject();
	subject.setFormat(null);
    }

    @Test
    public void testSetSource() {
	final AudioDriver subject = getSubject();
	subject.setSource(AUDIO_PROCESSOR);
    }
    
    @Test
    public void testSetSourceNull() {
	final AudioDriver subject = getSubject();
	subject.setSource(null);
    }

    @Test
    public void testFlush() {
	final AudioDriver subject = getSubject();
	subject.flush();
    }

    @Test
    public void testGetDevices() {
	final AudioDriver subject = getSubject();
	final Collection<AudioDevice> devs = subject.getDevices();
	Assert.assertNotNull(devs);
    }

    @Test
    public void testRelease() {
	final AudioDriver subject = getSubject();
	subject.release();
    }

    @Test
    public void testSetBufferSizeFrames() {
	final AudioDriver subject = getSubject();
	subject.setBufferSizeFrames(4096);
    }

    @Test
    public void testGetDefaultDevice() {
	final AudioDriver subject = getSubject();
	final AudioDevice dev = subject.getDefaultDevice();
	Assert.assertNotNull(dev);
    }

}//end AudioDriverTest
