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

import java.util.Collection;

import javax.sound.sampled.AudioFormat;

public interface AudioDriver {
    public void setFormat(AudioFormat format);
    public void setSource(AudioProcessor compressor);
    public void flush();
    public Collection<AudioDevice> getDevices();
    public AudioDevice getDeviceByName(String outputName);
    public void setOutput(AudioOutput o);
    public AudioOutput getDefaultOutput();
}
