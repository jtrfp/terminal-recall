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

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public abstract class CustomPlayerWithinRangeBehavior extends Behavior {
    private double range = TRFactory.mapSquareSize*10;
    public abstract void withinRange();
    private GameShell gameShell;
    
    @Override
    public void tick(long timeInMillis){
	    final WorldObject thisObject=getParent();
	    final WorldObject other=getGameShell().getGame().getPlayer();
	    if(Vect3D.distance(thisObject.getPosition(), other.getPosition())<=range){
		withinRange();
	    }//end if(close)
    }//end _proposeCollision

    /**
     * @return the range
     */
    public double getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(double range) {
        this.range = range;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end CustomPlayerWithinRangeBehavior
