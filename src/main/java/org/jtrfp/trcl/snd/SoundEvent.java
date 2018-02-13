/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.snd;

import java.util.Collection;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TRFactory.TR;

public interface SoundEvent {
    public double getStartRealtimeSeconds();
    public double getEndRealtimeSeconds();
    public double getDurationRealtimeSeconds();
    public void apply(GL3 gl, double bufferStartTimeSeconds);
    public Factory getOrigin();
    public boolean isActive();
    public void activate();
    public void deactivate();
    public SoundEvent getParent();
    public void destroy();
    public boolean isDestroyed();
    
    public interface Factory{
	public void apply(GL3 gl, Collection<SoundEvent> events, double bufferStartTimeSeconds);
	public TR getTR();
    }//end Factory
}//end PlaybackEvent
