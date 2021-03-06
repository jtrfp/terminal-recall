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

import javax.sound.sampled.AudioFormat;

public interface AudioOutput {
    public String getUniqueName();
    public AudioFormat []getFormats();
    public AudioFormat getFormatFromUniqueName(String name);
    public AudioDevice getDevice();
    public AudioFormat getDefaultFormat();
}
