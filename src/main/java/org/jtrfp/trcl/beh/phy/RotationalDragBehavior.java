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
import org.jtrfp.trcl.obj.WorldObject;

public class RotationalDragBehavior extends Behavior {
    private double dragCoeff=.86;
    private RotationalMomentumBehavior rotationalMomentumBehavior;
    private ThreadManager threadManager;
    @Override
    public void tick(long tickTimeInMillis){
	final double timeProgressedInFrames=((double)getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/(1000./ThreadManager.GAMEPLAY_FPS));
    	if(timeProgressedInFrames<=0)return;
    	final double finalCoeff=Math.pow(dragCoeff,timeProgressedInFrames);
	final RotationalMomentumBehavior rmb = getRotationalMomentumBehavior();
	rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*finalCoeff);
	rmb.setLateralMomentum(rmb.getLateralMomentum()*finalCoeff);
	rmb.setPolarMomentum(rmb.getPolarMomentum()*finalCoeff);
    }//end _tick()
    
    public RotationalDragBehavior setDragCoefficient(double drag){dragCoeff=drag;return this;}
    public double getDragCoefficient(){return dragCoeff;}

    public RotationalMomentumBehavior getRotationalMomentumBehavior() {
	if(rotationalMomentumBehavior == null)
	    rotationalMomentumBehavior = getParent().probeForBehavior(RotationalMomentumBehavior.class);
        return rotationalMomentumBehavior;
    }

    public void setRotationalMomentumBehavior(
    	RotationalMomentumBehavior rotationalMomentumBehavior) {
        this.rotationalMomentumBehavior = rotationalMomentumBehavior;
    }

    public ThreadManager getThreadManager() {
	if( threadManager == null )
	    threadManager = getParent().getTr().getThreadManager();
        return threadManager;
    }

    public void setThreadManager(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }
}//end RotationalDragBehavior
