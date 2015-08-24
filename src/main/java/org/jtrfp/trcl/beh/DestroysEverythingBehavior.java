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
import java.util.concurrent.Future;

import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.DamageListener.ProjectileDamage;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.TerrainChunk;

public class DestroysEverythingBehavior extends Behavior {
    private volatile int           counter=3;
    private volatile boolean       replenishingPlayerHealth=true;
    private volatile Future<?>     future;
    private volatile int           destructionRadius = (int)TR.mapSquareSize*15;
    final ArrayList<Positionable>[]positionables = new ArrayList[1];
    
    @Override
    public void _tick(long timeMillis){
	counter--;
	if(counter==2){
	    //Populate the relevance collection
	    try{future = World.relevanceExecutor.submit(new Runnable(){
		@Override
		public void run() {
		    positionables[0] = new ArrayList<Positionable>(getParent().getTr().mainRenderer.get().getCamera().getFlatRelevanceCollection());
		}});}catch(Exception e){e.printStackTrace();}
	}else if(counter==1&&isReplenishingPlayerHealth()){
	    try{future.get();}catch(Exception e){throw new RuntimeException(e);}
	    try{getParent().getTr().getGame().getPlayer().getBehavior().probeForBehavior(DamageableBehavior.class).unDamage();}
	    catch(SupplyNotNeededException e){}//Ok, whatever.
	    final double [] parentPos = getParent().getPosition();
	    final int destructionRadius = getDestructionRadius();
	    //Destroy everything
	    for(Positionable pos:positionables[0]){
		final int distance = (int)Vect3D.distance(pos.getPosition(),parentPos);
		if(pos instanceof DEFObject && !(pos instanceof TerrainChunk) && distance < destructionRadius){
		 final DEFObject dObj = (DEFObject)pos;
		 final DamageableBehavior beh = dObj.probeForBehavior(DamageableBehavior.class);
		 beh.proposeDamage(new ProjectileDamage(beh.getHealth()+1));
		}//end if(DEFObject)
	    }//end for(positionables[0])
	}
	if(counter==0){//We can't stick around for long. Not with all this destroying going on.
	    getParent().destroy();counter=3;
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
    /**
     * @return the destructionRadius
     */
    public int getDestructionRadius() {
        return destructionRadius;
    }
    /**
     * @param destructionRadius the destructionRadius to set
     */
    public void setDestructionRadius(int destructionRadius) {
        this.destructionRadius = destructionRadius;
    }
}//end DestroyesEverythinBehavior
