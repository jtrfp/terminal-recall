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

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;

public class Jumpzone extends WorldObject {

private NAVObjective objective;
private boolean includeYAxisInCollision=true;

    public Jumpzone(TR tr) {
	super(tr);
	try{setModel(tr.getResourceManager().getBINModel("JUMP-PNT.BIN", tr.getGlobalPaletteVL(), tr.gpu.get().getGl()));}
	catch(Exception e){tr.showStopper(e);}
	setVisible(false);
    }//end constructor
    
    public void setObjectiveToRemove(NAVObjective objective, Mission m) {
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
		if(TR.twosComplimentDistance(playerPos,parentPos)<CollisionManager.SHIP_COLLISION_DISTANCE*4&&navTargeted){
		    destroy();
		    getTr().getGame().getCurrentMission().removeNAVObjective(objective);
		}//end if(collided)
	    }//end if(Player)
	}//end proposeCollision()
	@Override
	public void notifyBecomingCurrentTarget() {
	    navTargeted=true;
	    setVisible(true);
	}
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
    public Jumpzone setIncludeYAxisInCollision(boolean includeYAxisInCollision) {
        this.includeYAxisInCollision = includeYAxisInCollision;
        return this;
    }

}//end Jumpzone
