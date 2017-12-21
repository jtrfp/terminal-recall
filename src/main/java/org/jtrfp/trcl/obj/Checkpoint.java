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
package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.NAVObjective;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;


public class Checkpoint extends BillboardSprite {
private NAVObjective objective;
private boolean includeYAxisInCollision=true;
private GameShell gameShell;
    public Checkpoint() {
	super();
	super.setModelOffset(0, 80000, 0);
	addBehavior(new CheckpointBehavior());
	addBehavior(new TerrainLocked());
	setBillboardSize(new Dimension(80000,80000));
	setVisible(true);
	final TR tr = getTr();
	try{setTexture(
		    tr.getResourceManager().getRAWAsTexture("CHECK1.RAW", 
			    tr.getGlobalPaletteVL(),null,false,true),true);
	}catch(Exception e){e.printStackTrace();}
    }//end constructor

    public void setObjectiveToRemove(NAVObjective objective, Mission m) {
	this.objective=objective;
    }//end setObjectiveToRemove(...)
    
    private class CheckpointBehavior extends Behavior implements CollisionBehavior{
	@Override
	public void proposeCollision(WorldObject other){
	    if(other instanceof Player){
		final Player player = (Player)other;
		final WorldObject parent = getParent();
		double [] playerPos = includeYAxisInCollision?player.getPosition():new double []{player.getPosition()[0],0,player.getPosition()[2]};
		double [] parentPos = includeYAxisInCollision?parent.getPosition():new double []{parent.getPosition()[0],0,parent.getPosition()[2]};
		if(TRFactory.twosComplementDistance(playerPos,parentPos)<CollisionManager.SHIP_COLLISION_DISTANCE*4){
		    destroy();
		    getGameShell().getGame().getCurrentMission().removeNAVObjective(objective);
		}//end if(collided)
	    }//end if(Player)
	}//end _proposeCollision()
    }//end CheckpointBehavior

    /**
     * @return the includeYAxisInCollision
     */
    public boolean isIncludeYAxisInCollision() {
        return includeYAxisInCollision;
    }

    /**
     * @param includeYAxisInCollision the includeYAxisInCollision to set
     */
    public Checkpoint setIncludeYAxisInCollision(boolean includeYAxisInCollision) {
        this.includeYAxisInCollision = includeYAxisInCollision;
        return this;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    final TR tr = Features.get(Features.getSingleton(),TR.class);
	    gameShell = Features.get(tr, GameShell.class);}
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end Checkpoint
