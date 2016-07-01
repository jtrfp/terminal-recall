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

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageListener;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.RedFlashFactory.RedFlash;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class RedFlashOnDamage extends Behavior implements DamageListener {
    private GameShell gameShell;
    
    private void flash(){
	Features.get(((TVF3Game)getGameShell().getGame()), RedFlash.class).flash();
    }

    @Override
    public void damageEvent(Event ev) {
	flash();
    }

    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getParent().getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }
    
}//end RedFlashOnCollisions
