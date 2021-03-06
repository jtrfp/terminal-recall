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

import org.jtrfp.trcl.obj.WorldObject;

public class LimitedLifeSpan extends Behavior {
    private long timeRemainingMillis=Long.MAX_VALUE;
    @Override
    public void tick(long tickTimeInMillis){
	final WorldObject p = getParent();
	timeRemainingMillis-=p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick();
	if(timeRemainingMillis<=0){
	    p.probeForBehavior(DeathBehavior.class).die();
	}//end if(remainign)
    }
    
    public LimitedLifeSpan reset(long millisUntilDestroyed){
	timeRemainingMillis=millisUntilDestroyed;
	return this;
    }
}//end LimitedLifeSpan
