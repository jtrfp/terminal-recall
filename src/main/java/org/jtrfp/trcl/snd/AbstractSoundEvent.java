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

import org.jtrfp.trcl.core.TRFactory.TR;

public abstract class AbstractSoundEvent implements SoundEvent {
    private final double startRealtimeSeconds;
    private final double endRealtimeSeconds;
    private final double durationRealtimeSeconds;
    private final Factory origin;
    private boolean active    = true;
    private final SoundEvent parent;
    private boolean destroyed = false;
    
    public AbstractSoundEvent(double startTimeSeconds,
		double durationRealtimeSeconds, Factory origin, SoundEvent parent) {
	    this.durationRealtimeSeconds = durationRealtimeSeconds;
	    this.startRealtimeSeconds    = startTimeSeconds;
	    endRealtimeSeconds           = startTimeSeconds + durationRealtimeSeconds;
	    this.origin=origin;
	    this.parent=parent;
	}//end constructor

    /**
     * @return the startRealtimeSeconds
     */
    public double getStartRealtimeSeconds() {
        return startRealtimeSeconds;
    }

    /**
     * @return the endRealtimeSeconds
     */
    public double getEndRealtimeSeconds() {
        return endRealtimeSeconds;
    }

    /**
     * @return the durationRealtimeSeconds
     */
    public double getDurationRealtimeSeconds() {
        return durationRealtimeSeconds;
    }

    /**
     * @return the origin
     */
    public Factory getOrigin() {
        return origin;
    }
    
    public static abstract class Factory implements SoundEvent.Factory{
	private final TR tr;
	protected Factory(TR tr){
	    super();
	    this.tr=tr;
	}//end constructor
	/**
	 * @return the tr
	 */
	public TR getTR() {
	    return tr;
	}
    }//end Factory

    public boolean isActive(){
	if(isDestroyed())return false;
	if(parent!=null)
	    return active&&parent.isActive();
	return active;
    }
    
    public void activate(){
	active=true;
    }
    
    public void deactivate(){
	active=false;
    }
    
    public SoundEvent getParent(){
	return parent;
    }
    
    public void destroy(){
	destroyed=true;
    }
    
    public boolean isDestroyed(){
	if(parent!=null)
	    return destroyed&&parent.isDestroyed();
	return destroyed;
    }
    
}//end AbstractPlaybackEvent
