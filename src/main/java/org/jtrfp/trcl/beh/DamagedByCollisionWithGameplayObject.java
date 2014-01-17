/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class DamagedByCollisionWithGameplayObject extends Behavior{
    private final double MAX_SPEED=70000;
    private final int MIN_FRAGS=6;
	protected void _proposeCollision(WorldObject other){
	    	final WorldObject p = getParent();
	    	final double distance = Vect3D.distance(other.getPosition(),getParent().getPosition());
		if(distance<CollisionManager.SHIP_COLLISION_DISTANCE)
			{if(other instanceof Player && getParent() instanceof DEFObject)
				{getParent().getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(65535/30);
				other.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(65535/10);
				getParent().getTr().getResourceManager().getDebrisFactory().spawn(p.getPosition(), new Vector3D(0,1000,0));}
			/*for(int i=0; i<MIN_FRAGS+p.getModel().getTriangleList().getMaximumVertexValue()/6000; i++){
			    p.getTr().getResourceManager().getDebrisFactory().spawn(p.getPosition(), 
			    new Vector3D(
				Math.random()*MAX_SPEED-MAX_SPEED/2.,
				Math.random()*MAX_SPEED+30000,
				Math.random()*MAX_SPEED-MAX_SPEED/2.));}*/
			}//end if(nearby)
		}//end proposeCollision
	}//end DamagedByCollisionWithGameplayObject
