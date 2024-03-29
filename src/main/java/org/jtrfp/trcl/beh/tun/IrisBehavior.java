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
import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageListener;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class IrisBehavior extends Behavior implements CollisionBehavior {
public static final int     DAMAGE_ON_IMPACT=6554;
private static final double X_FLUFF         =10000;
private static final double RADIUS_SKEW     =1.5;//1 = fair, 1+ =wider, 0-1 =narrower,  0 =wall (how could you?!)
private final Controller controller;
private final double     maxRadius;

    public IrisBehavior(Controller controller, double maxRadius) {
	if(controller==null)throw new NullPointerException("Controller is intolerably null.");
	this.controller=controller;
	this.maxRadius =maxRadius;
    }//end constructor
    @Override
    public void proposeCollision(WorldObject wo){// 0=closed, 1=open 2=closed
	if(wo instanceof Player){
	    final WorldObject p = getParent();
	    final double [] thisPos = p.getPosition();
	    final double [] playerPos = wo.getPosition();
	    
	    if(playerPos[0]>thisPos[0]&&playerPos[0]<thisPos[0]+X_FLUFF){
		final double dY=thisPos[1]-playerPos[1];
		final double dZ=thisPos[2]-playerPos[2];
		final double dist=Math.sqrt(dY*dY+dZ*dZ);
		final double currentRadius=maxRadius*(1.-Math.abs(1.-controller.getCurrentFrame()))*RADIUS_SKEW;
		if(dist>currentRadius){
		    wo.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
			    @Override
			    public void submit(DamageableBehavior item) {
				final DamageListener.ShearDamage dmg = 
					new DamageListener.ShearDamage();
				dmg.setDamageAmount(DAMAGE_ON_IMPACT);
				item.proposeDamage(dmg);
			    }}, DamageableBehavior.class);
		}//end if(crushed)
	    }//end if(in range)
	}//end if(Player)
    }//end proposeCollision(...)
}//end IrisBehavior
