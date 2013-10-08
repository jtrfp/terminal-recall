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
package com.ritolaaudio.trcl;

public final class Sequencer implements Controller
	{
	private final int frameDelayMsec;
	private final boolean interpolate;
	private final int numFrames;
	
	public Sequencer(int frameDelayMsec, int numFrames, boolean interpolate)
		{
		this.numFrames=numFrames;
		this.frameDelayMsec=frameDelayMsec;
		this.interpolate=interpolate;
		}//end constructor(...)
	
	public double getCurrentFrame()
		{
		double result = (((double)System.currentTimeMillis()/(double)frameDelayMsec))%(double)numFrames;
		return interpolate?result:(int)result;
		}//end getCurentFrame()
	}//end Sequencer
