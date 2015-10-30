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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithPlayer extends Behavior implements CollisionBehavior {
    private Player player;
    public CollidesWithPlayer(){
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof Player){
	    if(other.getModel()==null)
		return;
	    if(other.getModel().getTriangleList()==null)
		return;
	    final WorldObject parent = getParent();
	    if(parent.getModel()==null)
		return;
	    if(parent.getModel().getTriangleList()==null)
		return;
	    final Vector3D pMax = parent.getModel().getTriangleList().getMaximumVertexDims();
	    final Vector3D oMax = other .getModel().getTriangleList().getMaximumVertexDims();
	    
	    double dXZ       = TR.rolloverDistance(Vect3D.distanceXZ(parent.getPositionWithOffset(), other.getPositionWithOffset()));
	    final double dY  = Math.abs(parent.getPositionWithOffset()[1]-other.getPositionWithOffset()[1]);
	    final boolean cXZ = dXZ < Math.sqrt(Math.pow(pMax.getX()+oMax.getX(),2) + Math.pow(pMax.getZ()+oMax.getZ(),2));
	    final boolean cY = dY   < (pMax.getY() + oMax.getY());
	    player=(Player)other;
	    if(cXZ && cY){
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
