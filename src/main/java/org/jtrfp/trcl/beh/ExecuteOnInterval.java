/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

public class ExecuteOnInterval extends Behavior {
    
    @Override
    public void tick(long tickTimeInMillis){
	
    }//end tick(...)
    
    public static class RegularIntervalLogic implements IntervalLogic {
	private long nextCompletionMillis;
	private long intervalTimeMillis;

	@Override
	public boolean proposeTime(final long tickTimeInMillis) {
	    final long intervalTimeMillis = getIntervalTimeMillis();
	    if(tickTimeInMillis >= getNextCompletionMillis()){
		setNextCompletionMillis(tickTimeInMillis+intervalTimeMillis);
		return true;
	    }
	    return false;
	}//end proposeTime(...)
	
	public long getIntervalTimeMillis() {
	    return intervalTimeMillis;
	}

	public void setIntervalTimeMillis(long intervalTimeMillis) {
	    this.intervalTimeMillis = intervalTimeMillis;
	}

	public long getNextCompletionMillis() {
	    return nextCompletionMillis;
	}

	public void setNextCompletionMillis(long nextCompletionMillis) {
	    this.nextCompletionMillis = nextCompletionMillis;
	}
	
    }
 public static interface IntervalLogic {
     public boolean proposeTime(long tickTimeInMillis);
 }
}//end ExecuteOnInterval
