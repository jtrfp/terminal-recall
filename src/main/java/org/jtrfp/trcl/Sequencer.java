/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
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
package org.jtrfp.trcl;

public final class Sequencer implements Controller{
	private final int frameDelayMsec;
	private final boolean interpolate;
	private final int numFrames;
	private final boolean loop;
	private long timeOffset=0;
	
	public Sequencer(int frameDelayMsec, int numFrames, boolean interpolate, boolean loop){
	    this.numFrames=numFrames;
	    this.frameDelayMsec=frameDelayMsec;
	    this.interpolate=interpolate;
	    this.loop=loop;
	}//end constructor(...)
	
	public Sequencer(int frameDelayMsec, int numFrames, boolean interpolate){
		this(frameDelayMsec, numFrames, interpolate, true);
	}//end constructor(...)
	
	public double getCurrentFrame(){
	    	if(!loop&&System.currentTimeMillis()-timeOffset>numFrames*(frameDelayMsec-1))return numFrames-1;//Freeze on last frame/
		double result = (((double)(System.currentTimeMillis()-timeOffset)/(double)frameDelayMsec))%(double)numFrames;
		return interpolate?result:(int)result;
	}//end getCurentFrame()

	public void reset() {
	    timeOffset=System.currentTimeMillis();
	}
}//end Sequencer
