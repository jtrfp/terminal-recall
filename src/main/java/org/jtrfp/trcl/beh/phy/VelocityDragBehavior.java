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
import org.jtrfp.trcl.math.Vect3D;

public class VelocityDragBehavior extends Behavior {
	private double dragCoeff=1;
	private Velocible velocible;
	private ThreadManager threadManager;
	public VelocityDragBehavior setDragCoefficient(double dragCoefficientPerSecond)
		{dragCoeff=dragCoefficientPerSecond; return this;}

	public double getVelocityDrag()
		{return dragCoeff;}
	
	@Override
	public void tick(long tickTimeMillis){
	    	final double timeProgressedInFrames=((double)getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/(1000./ThreadManager.GAMEPLAY_FPS));
	    	if(timeProgressedInFrames<=0)return;
	    	final double finalCoeff=Math.pow(dragCoeff,timeProgressedInFrames);
	    	Velocible v=getVelocible();
	    	final double [] velocity = v.getVelocity();
	    	Vect3D.scalarMultiply(velocity, finalCoeff, velocity);
	    	v.setVelocity(velocity);//Just to make sure
		}

	public Velocible getVelocible() {
	    if( velocible == null )
		velocible = getParent().probeForBehavior(Velocible.class);
	    return velocible;
	}

	public void setVelocible(Velocible velocible) {
	    this.velocible = velocible;
	}
	
	public ThreadManager getThreadManager() {
	    if( threadManager == null )
		threadManager = getParent().getTr().getThreadManager();
	    return threadManager;
	}

	public void setThreadManager(ThreadManager threadManager) {
	    this.threadManager = threadManager;
	}

	}//end VelocityDragBehavior
