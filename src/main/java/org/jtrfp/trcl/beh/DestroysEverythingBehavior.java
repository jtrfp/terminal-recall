package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.WorldObject;

public class DestroysEverythingBehavior extends Behavior implements CollisionBehavior {
    int counter=2;
    boolean replenishingPlayerHealth=true;
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof DEFObject){
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(65536);//Really smash that sucker.
	}//end if(DEFObject)
    }//end proposeCollision()
    @Override
    public void _tick(long timeMillis){
	counter--;
	if(counter==1&&isReplenishingPlayerHealth()){
	    try{getParent().getTr().getPlayer().getBehavior().probeForBehavior(DamageableBehavior.class).unDamage();}
	    catch(SupplyNotNeededException e){}//Ok, whatever.
	}
	if(counter<=0){//We can't stick around for long. Not with all this destroying going on.
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
