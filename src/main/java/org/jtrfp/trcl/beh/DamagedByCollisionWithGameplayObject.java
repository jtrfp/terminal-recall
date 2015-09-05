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
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class DamagedByCollisionWithGameplayObject extends Behavior implements
	CollisionBehavior {
    @Override
    public void proposeCollision(WorldObject other) {
	final WorldObject p = getParent();
	final double distance = Vect3D.distance(other.getPosition(),
		p.getPosition());
	if (distance < CollisionManager.SHIP_COLLISION_DISTANCE) {
	    if (other instanceof Player && getParent() instanceof DEFObject) {
		p.probeForBehaviors(
			new AbstractSubmitter<DamageableBehavior>() {
			    @Override
			    public void submit(DamageableBehavior item) {
				item.proposeDamage(new DamageListener.ProjectileDamage(
					65535 / 30));
			    }
			}, DamageableBehavior.class);
		other.probeForBehaviors(
			new AbstractSubmitter<DamageableBehavior>() {
			    @Override
			    public void submit(DamageableBehavior item) {
				item.proposeDamage(new DamageListener.ProjectileDamage(
					65535 / 10));
			    }
			}, DamageableBehavior.class);
		p.getTr()
			.getResourceManager()
			.getDebrisSystem()
			.spawn(new Vector3D(p.getPosition()),
				new Vector3D(0, 1000, 0));
	    }//end if(Player & this is DEFObject)
	}// end if(nearby)
    }// end proposeCollision
}// end DamagedByCollisionWithGameplayObject
