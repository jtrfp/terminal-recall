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

import java.util.ArrayList;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.NAVRadarBlipFactory;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class UpdatesNAVRadar extends Behavior implements CollisionBehavior {
    private int counter=0;
    private boolean performRefresh=false;
    public static final int REFRESH_INTERVAL=5;
    private NAVRadarBlipFactory blips;
    private GameShell gameShell;
    private ArrayList<WorldObject> newBlipList = new ArrayList<WorldObject>(128);
    @Override
    public void tick(long timeInMillis){
	counter++;
	final int refreshSequence = counter % REFRESH_INTERVAL;
	if(refreshSequence == 0){
	    final Game game = getGameShell().getGame();
	    blips = ((TVF3Game)game).
		    getNavSystem().
		    getBlips();
	    performRefresh=!(getParent().getTr().getRunState() instanceof Mission.TunnelState);
	}else if(refreshSequence == 1){
	    final Game game = getGameShell().getGame();
	    blips = ((TVF3Game)game).
		    getNavSystem().
		    getBlips();
	    blips.refreshBlips(newBlipList);
	    newBlipList.clear();
	}else
	    performRefresh=false;
    }//end _tick(...)
    @Override
    public void proposeCollision(WorldObject other){
	if(performRefresh)
	    newBlipList.add(other);
    }//end _proposeCollision(...)
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end UpdatesNAVRadar
