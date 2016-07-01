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
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class TerrainLocked extends Behavior {
    private double  pad            =0;
    private boolean lockedToCeiling=false;
    private GameShell gameShell;
    @Override
    public void tick(long tickTimeMillis){
	final WorldObject p = getParent();
	final double[] thisPos=p.getPosition();
	final Game game = getGameShell().getGame();
	if(game==null)
	    return;
	final Mission currentMission = game.getCurrentMission();
	if(currentMission==null)
	    return;
	if(getGameShell().getGame().getCurrentMission().getOverworldSystem()==null)
	    return;
	if(getGameShell().getGame().getCurrentMission().getOverworldSystem().getAltitudeMap()==null)
	    return;
	final double height = 
		(lockedToCeiling?TerrainSystem.Y_NUDGE:0)+
		(lockedToCeiling?p.getTr().getWorld().sizeY:0)+(lockedToCeiling?-1:1)*
		getGameShell().
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
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end TerrainLocked
