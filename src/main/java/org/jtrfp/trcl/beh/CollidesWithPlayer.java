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
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithPlayer extends Behavior implements CollisionBehavior {
    private Player player;
    private Double collisionRadius = null;
    public CollidesWithPlayer(){
    }
    @Override
    public void proposeCollision(WorldObject other){
	//if(getParent() instanceof ProjectileObject3D && ((ProjectileObject3D)getParent()).getWeapon()==Weapon.ION)
	//    System.out.println("PO3D "+other);
	if(other instanceof Player){
	    final WorldObject parent = getParent();
	    final GL33Model parentModel  = parent.getModel();
	    if(other.getModel()==null)
		return;
	    if(parentModel==null)
		return;
	    final Vector3D pMax = parent.getModel().getMaximumVertexDims();
	    final Vector3D oMax = other .getModel().getMaximumVertexDims();
	    
	    double dXZ       = TRFactory.rolloverDistance(Vect3D.distanceXZ(parent.getPositionWithOffset(), other.getPositionWithOffset()));
	    final double dY  = Math.abs(parent.getPositionWithOffset()[1]-other.getPositionWithOffset()[1]);
	    //System.out.println("INSTANCE="+getParent().getClass().getName());
	    /*if(getParent() instanceof ProjectileObject3D){
		final ProjectileObject3D po3 = (ProjectileObject3D)getParent();
		//if(po3.getWeapon() == Weapon.ION)
		 System.out.println("instance="+getParent()+" dXZ="+dXZ+" dY="+dY+" Weapon="+po3.getWeapon());
		 }*/
	    if(collisionRadius==null){
		final boolean cXZ = dXZ < Math.sqrt(Math.pow(pMax.getX()+oMax.getX(),2) + Math.pow(pMax.getZ()+oMax.getZ(),2));
		final boolean cY = dY   < (pMax.getY() + oMax.getY());
		player=(Player)other;
		if(cXZ && cY){
		    reportCollision();
		}//end if(close enough)
	    }else if(Math.sqrt(dXZ*dXZ+dY*dY) < collisionRadius)
		    reportCollision();
	}//end if(player)
    }//end _proposeCollision()
    
    private void reportCollision(){
	getParent().probeForBehaviors(sub, PlayerCollisionListener.class);
    }
    
    private final Submitter<PlayerCollisionListener> sub = new AbstractSubmitter<PlayerCollisionListener>(){
	@Override
	public void submit(PlayerCollisionListener l){
	    l.collidedWithPlayer(player);
	}//end submit(...)
    };//end Submitter
    public Double getCollisionRadius() {
        return collisionRadius;
    }
    public void setCollisionRadius(Double collisionRadius) {
        this.collisionRadius = collisionRadius;
    }
}
