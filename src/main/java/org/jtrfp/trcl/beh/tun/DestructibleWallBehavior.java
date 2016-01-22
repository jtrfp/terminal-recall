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
package org.jtrfp.trcl.beh.tun;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageListener;
import org.jtrfp.trcl.beh.DamageListener.CollisionDamage;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.WorldObject;

public class DestructibleWallBehavior extends Behavior implements CollisionBehavior {
    private static final double THICKNESS_X=4000;
    public static final int DAMAGE_ON_IMPACT=6554;
    @Override
    public void proposeCollision(WorldObject other){
	final double [] otherPos=other.getPosition();
	final WorldObject p = getParent();
	final double [] thisPos=p.getPosition();
	if(otherPos[0]>thisPos[0]&& otherPos[0]<thisPos[0]+THICKNESS_X){
    	    if(other instanceof Player){
    	        final Player player=(Player)other;
    	        player.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
		    @Override
		    public void submit(DamageableBehavior item) {
			final DamageListener.CollisionDamage dmg = 
				new DamageListener.CollisionDamage();
			dmg.setDamageAmount(DAMAGE_ON_IMPACT);
			item.proposeDamage(dmg);
		    }}, DamageableBehavior.class);
    	        }//end if(Player)
    	    else if(other instanceof Projectile)
    		other.probeForBehavior(ProjectileBehavior.class).forceCollision(p);
    	}//end if(in range)
    }//end _proposeCollision(...)
}//end DestructibleAllBehavior
