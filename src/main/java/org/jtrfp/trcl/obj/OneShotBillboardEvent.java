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

package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;

public abstract class OneShotBillboardEvent extends BillboardSprite {
    public static final int PROXIMITY_TEST_DIST = (int)TR.mapSquareSize/3;
    public static final int MAX_PROXIMAL_EVENTS       = 3;
    private final Sequencer sequencer;
    private long timeOfLastReset = 0L;
    private final OneShotBillboardEventBehavior eventBehavior;
    private final int numFrames, millisPerFrame;

    public OneShotBillboardEvent(TR tr, int millisPerFrame, int numFrames) {
	super(tr);
	this.numFrames     =numFrames;
	this.millisPerFrame=millisPerFrame;
	addBehavior(eventBehavior = new OneShotBillboardEventBehavior());
	sequencer=new Sequencer(millisPerFrame, numFrames, false,false);
    }
    

    /**
     * @return the timeOfLastReset
     */
    public long getTimeOfLastReset() {
        return timeOfLastReset;
    }
    
    public int getNumFrames(){return numFrames;}
    public int getMillisPerFrame(){return millisPerFrame;}
    
    public void reset() {
	destroy();
	eventBehavior.reset();
	setVisible(true);
	setActive(true);
	sequencer.reset();
	timeOfLastReset=System.currentTimeMillis();
    }//end reset()
    
    private class OneShotBillboardEventBehavior extends Behavior{
	private long timeoutTimeInMillis=0;
	@Override
	public void tick(long tickTimeMillis){
	    if(tickTimeMillis>=timeoutTimeInMillis){
		destroy();
	    }//end if(timeout)
	}//end _tick(...)
	public void reset(){
	    timeoutTimeInMillis=System.currentTimeMillis()+getMillisPerFrame()*(getNumFrames()-2);//-10 is padding to avoid stray frame looping
	}//end reset()
    }//end ExplosionBehavior

    /**
     * @return the sequencer
     */
    public Sequencer getSequencer() {
        return sequencer;
    }

}//end OneShotBillboardEvent
