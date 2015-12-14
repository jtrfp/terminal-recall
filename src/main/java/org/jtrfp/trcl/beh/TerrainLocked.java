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

import org.jtrfp.trcl.TerrainSystem;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.WorldObject;

public class TerrainLocked extends Behavior {
    private double  pad            =0;
    private boolean lockedToCeiling=false;
    @Override
    public void tick(long tickTimeMillis){
	final WorldObject p = getParent();
	final double[] thisPos=p.getPosition();
	final Game game = p.getTr().getGame();
	if(game==null)
	    return;
	final Mission currentMission = game.getCurrentMission();
	if(currentMission==null)
	    return;
	if(p.getTr().getGame().getCurrentMission().getOverworldSystem()==null)
	    return;
	if(p.getTr().getGame().getCurrentMission().getOverworldSystem().getAltitudeMap()==null)
	    return;
	final double height = 
		(lockedToCeiling?TerrainSystem.Y_NUDGE:0)+
		(lockedToCeiling?p.getTr().getWorld().sizeY:0)+(lockedToCeiling?-1:1)*
		p.getTr().
		getGame().
		getCurrentMission().
		getOverworldSystem().
		getAltitudeMap().
		 heightAt(thisPos[0],thisPos[2]);
	final double [] pPos = p.getPosition();
	pPos[0]=thisPos[0];
	pPos[1]=height+pad;
	pPos[2]=thisPos[2];
	p.setPosition(pPos);
	p.notifyPositionChange();
    }//end _tick(...)
    /**
     * @return the lockedToCeiling
     */
    public boolean isLockedToCeiling() {
        return lockedToCeiling;
    }
    /**
     * @param lockedToCeiling the lockedToCeiling to set
     */
    public TerrainLocked setLockedToCeiling(boolean lockedToCeiling) {
        this.lockedToCeiling = lockedToCeiling;
        return this;
    }
}//end TerrainLocked
