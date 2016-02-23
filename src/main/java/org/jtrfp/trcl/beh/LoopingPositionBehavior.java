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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Player;

public class LoopingPositionBehavior extends Behavior {
    private static final double THRESHOLD = TR.mapCartOffset;
    @Override
    public void tick(long timeInMillis){
	// Loop correction
	double [] oldPos = getParent().getPosition();
	boolean _transient=false;
	if (getParent().supportsLoop()){
		if (oldPos[0] > THRESHOLD)
			{oldPos[0]-=TR.mapWidth;_transient=true;}
		if (oldPos[2] > THRESHOLD)
		    	{oldPos[2]-=TR.mapWidth;_transient=true;}
		if (oldPos[0] < -THRESHOLD)
			{oldPos[0]+=TR.mapWidth;_transient=true;}
		if (oldPos[2] < -THRESHOLD)
		    	{oldPos[2]+=TR.mapWidth;_transient=true;}
	if(_transient)getParent().notifyPositionChange();
	}//end if(LOOP)
    }//end _tick(...)
}//end LoopingPositionBehavior
