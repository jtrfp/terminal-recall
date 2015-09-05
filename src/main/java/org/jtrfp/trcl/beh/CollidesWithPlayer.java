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

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithPlayer extends Behavior implements CollisionBehavior {
    private final double boundingRadius;
    private Player player;
    public CollidesWithPlayer(double boundingRadius){
	this.boundingRadius=boundingRadius;
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof Player){
	    final double distance=TR.twosComplimentDistance(other.getPosition(), getParent().getPosition());
	    player=(Player)other;
	    if(distance<(boundingRadius+2048)){
		getParent().probeForBehaviors(sub, PlayerCollisionListener.class);
	    }//end if(close enough)
	}//end if(player)
    }//end _proposeCollision()
    
    private final Submitter<PlayerCollisionListener> sub = new AbstractSubmitter<PlayerCollisionListener>(){
	@Override
	public void submit(PlayerCollisionListener l){
	    l.collidedWithPlayer(player);
	}//end submit(...)
    };//end Submitter
}
