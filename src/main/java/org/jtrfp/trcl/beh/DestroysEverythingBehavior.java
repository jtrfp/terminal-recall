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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.DamageListener.ProjectileDamage;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.WorldObject;

public class DestroysEverythingBehavior extends Behavior implements CollisionBehavior {
    private volatile int counter=2;
    private boolean replenishingPlayerHealth=true;
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof DEFObject){
	    other.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
		    @Override
		    public void submit(DamageableBehavior item) {
			item.proposeDamage(new ProjectileDamage(65536));
		    }}, DamageableBehavior.class);
	}//end if(DEFObject)
    }//end proposeCollision()
    @Override
    public void _tick(long timeMillis){
	counter--;
	if(counter==1&&isReplenishingPlayerHealth()){
	    try{getParent().getTr().getGame().getPlayer().getBehavior().probeForBehavior(DamageableBehavior.class).unDamage();}
	    catch(SupplyNotNeededException e){}//Ok, whatever.
	    //Destoy everything
	    final ArrayList<Positionable>[] positionables = new ArrayList[1];
	    try{World.relevanceExecutor.submit(new Runnable(){
		@Override
		public void run() {
		    positionables[0] = new ArrayList<Positionable>(getParent().getTr().mainRenderer.get().getCamera().getFlatRelevanceCollection());
		}}).get();}catch(Exception e){e.printStackTrace();}
	    for(Positionable pos:positionables[0]){
		if(pos instanceof DEFObject){
		    final DEFObject dObj = (DEFObject)pos;
		    dObj.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
			    @Override
			    public void submit(DamageableBehavior item) {
				item.proposeDamage(new ProjectileDamage(65536));
			    }}, DamageableBehavior.class);
		}//end for(positionables)
	    }//end if(counter==1)
	}
	if(counter==0){//We can't stick around for long. Not with all this destroying going on.
	    getParent().destroy();counter=2;
	}//end if(counter<=0)
    }//end _tick(...)
    /**
     * @return the replenishingPlayerHealth
     */
    public boolean isReplenishingPlayerHealth() {
        return replenishingPlayerHealth;
    }
    /**
     * @param replenishingPlayerHealth the replenishingPlayerHealth to set
     */
    public DestroysEverythingBehavior setReplenishingPlayerHealth(boolean replenishingPlayerHealth) {
        this.replenishingPlayerHealth = replenishingPlayerHealth;
        return this;
    }
}//end DestroyesEverythinBehavior
