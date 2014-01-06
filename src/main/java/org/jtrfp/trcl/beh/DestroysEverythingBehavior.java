package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.WorldObject;

public class DestroysEverythingBehavior extends Behavior {
    int counter=2;
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof DEFObject){
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(65536);//Really smash that sucker.
	}//end if(DEFObject)
    }//end proposeCollision()
    @Override
    public void _tick(long timeMillis){
	counter--;
	if(counter<=0){//We can't stick around for long. Not with all this destroying going on.
	    getParent().destroy();counter=2;
	}//end if(counter<=0)
    }//end _tick(...)
}//end DestroyesEverythinBehavior
