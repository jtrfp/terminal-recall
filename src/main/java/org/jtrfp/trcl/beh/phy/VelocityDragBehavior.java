/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh.phy;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.Velocible;

public class VelocityDragBehavior extends Behavior
	{
	private double dragCoeff=1;
	public VelocityDragBehavior setDragCoefficient(double dragCoefficientPerSecond)
		{dragCoeff=dragCoefficientPerSecond; return this;}

	public double getVelocityDrag()
		{return dragCoeff;}
	
	@Override
	public void tick(long tickTimeMillis){
	    	final double timeProgressedInFrames=((double)getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/(1000./ThreadManager.GAMEPLAY_FPS));
	    	if(timeProgressedInFrames<=0)return;
	    	final double finalCoeff=Math.pow(dragCoeff,timeProgressedInFrames);
	    	Velocible v=getParent().probeForBehavior(Velocible.class);
	    	v.setVelocity(v.getVelocity().scalarMultiply(finalCoeff));
		}

	}//end VelocityDragBehavior
