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

import org.jtrfp.trcl.obj.NAVRadarBlipFactory;
import org.jtrfp.trcl.obj.WorldObject;

public class UpdatesNAVRadar extends Behavior implements CollisionBehavior {
    private int counter=0;
    private boolean performRefresh=false;
    public static final int REFRESH_INTERVAL=5;
    private NAVRadarBlipFactory blips;
    @Override
    public void _tick(long timeInMillis){
	counter++;
	if(counter%REFRESH_INTERVAL==0){
	    blips = getParent().getTr().getGame().getNavSystem().getBlips();
	    blips.clearRadarBlips();
	    performRefresh=true;
	}else if(counter%REFRESH_INTERVAL==1){
	    performRefresh=false;
	}
    }//end _tick(...)
    @Override
    public void proposeCollision(WorldObject other){
	if(performRefresh){
	    blips.submitRadarBlip(other);
	}
    }//end _proposeCollision(...)
}//end UpdatesNAVRadar
