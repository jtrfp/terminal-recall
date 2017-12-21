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

import java.util.concurrent.Executor;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.NAVObjective;
import org.jtrfp.trcl.miss.WarpEscapeFactory.WarpEscape;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class Jumpzone extends WorldObject {

private NAVObjective objective;
private boolean includeYAxisInCollision=true;
private GameShell gameShell;
private WarpEscape warpEscape;
private Mission mission;

public interface FinishingRunState extends Mission.GameplayState{}

    public Jumpzone() {
	super();
	final TR tr = getTr();
	try{setModel(tr.getResourceManager().getBINModel("JUMP-PNT.BIN", tr.getGlobalPaletteVL(),null, Features.get(tr, GPUFeature.class).getGl()));}
	catch(Exception e){tr.showStopper(e);}
	setVisible(false);
    }//end constructor
    
    public void setObjectiveToRemove(NAVObjective objective) {
	this.objective=objective;
	addBehavior(new JumpzoneBehavior());
	addBehavior(new TerrainLocked());
    }//end setObjectiveToRemove(...)
    
    private class JumpzoneBehavior extends Behavior implements CollisionBehavior, NAVTargetableBehavior{
	boolean navTargeted;
	@Override
	public void proposeCollision(WorldObject other){
	    if(other instanceof Player){
		final Player player = (Player)other;
		final WorldObject parent = getParent();
		double [] playerPos = includeYAxisInCollision?player.getPosition():new double []{player.getPosition()[0],0,player.getPosition()[2]};
		double [] parentPos = includeYAxisInCollision?parent.getPosition():new double []{parent.getPosition()[0],0,parent.getPosition()[2]};
		if(TRFactory.twosComplementDistance(playerPos,parentPos)<CollisionManager.SHIP_COLLISION_DISTANCE*4&&navTargeted){
		    handlePlayerCollision();
		    destroy();
		    getGameShell().getGame().getCurrentMission().removeNAVObjective(objective);
		}//end if(collided)
	    }//end if(Player)
	}//end proposeCollision()
	@Override
	public void notifyBecomingCurrentTarget() {
	    navTargeted=true;
	    setVisible(true);
	}
    }//end CheckpointBehavior
    
    private void handlePlayerCollision(){
	final Executor executor = TransientExecutor.getSingleton();
	final WarpEscape warpEscape = getWarpEscape();
	if(warpEscape != null)
	 synchronized(executor) {
	    executor.execute(new Runnable(){
		@Override
		public void run() {
		    warpEscape.
		        missionComplete(Jumpzone.this);
		}});
	}//end sync(executor)
	else
	    getMission().notifyMissionComplete();
    }//end handlePlayerCollision

    /**
     * @return the includeYAxisInCollision
     */
    public boolean isIncludeYAxisInCollision() {
        return includeYAxisInCollision;
    }

    /**
     * @param includeYAxisInCollision the includeYAxisInCollision to set
     */
    public Jumpzone setIncludeYAxisInCollision(boolean includeYAxisInCollision) {
        this.includeYAxisInCollision = includeYAxisInCollision;
        return this;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }

    public WarpEscape getWarpEscape() {
        return warpEscape;
    }

    public void setWarpEscape(WarpEscape warpEscape) {
        this.warpEscape = warpEscape;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
        if(getWarpEscape() == null)
            setWarpEscape(Features.get(mission, WarpEscape.class));
    }

}//end Jumpzone
