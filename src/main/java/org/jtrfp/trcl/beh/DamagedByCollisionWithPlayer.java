/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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
import org.jtrfp.trcl.obj.Player;

public class DamagedByCollisionWithPlayer extends Behavior implements PlayerCollisionListener {
    private final int damageAmount;
    private final int damageIntervalMillis;
    private long nextMinDamageTimeMillis;
    
    public DamagedByCollisionWithPlayer(int damageAmount, int damageIntervalMillis){
	this.damageAmount        =damageAmount;
	this.damageIntervalMillis=damageIntervalMillis;
    }

    @Override
    public void collidedWithPlayer(Player player) {
	if(System.currentTimeMillis() < nextMinDamageTimeMillis)
	    return;//Not yet
	getParent().probeForBehaviors(damageableSubmitter, DamageableBehavior.class);
	nextMinDamageTimeMillis = System.currentTimeMillis()+damageIntervalMillis;
    }
    
    private final AbstractSubmitter<DamageableBehavior> damageableSubmitter = new AbstractSubmitter<DamageableBehavior>(){
	@Override
	public void submit(DamageableBehavior item) {
	    item.proposeDamage(new DamageListener.Event(damageAmount){});
	}//end damageableSubmitter
    };

}//end DamagedByCollisionWithPlayer