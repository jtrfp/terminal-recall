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
package org.jtrfp.trcl.beh;

public abstract class DamageTrigger extends Behavior {
private boolean triggered=false;
private int threshold=2048;

	@Override
	public void _tick(long timeInMillis){
	    if(!triggered){
		if(getParent().getBehavior().probeForBehavior(DamageableBehavior.class).getHealth()<threshold){
		    triggered=true;//Just in case disabling has latency
		    setEnable(false);///zzz...
		    healthBelowThreshold();
		}//end if(damaged)
	    }//end if(!triggered)
	}//end _tick(...)
	
	public abstract void healthBelowThreshold();

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
	    return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public DamageTrigger setThreshold(int threshold) {
	    this.threshold = threshold;
	    return this;
	}
}//end DamageTrigger
