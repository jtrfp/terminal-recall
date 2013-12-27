package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.WorldObject;

public class DeathBehavior extends Behavior {
    private boolean dead=false;
    public void die(){
	if(dead)return;
	dead=true;//Only die once until reset
	WorldObject wo = getParent();
	wo.getBehavior().probeForBehaviors(sub,DeathListener.class);
	wo.destroy();
    }
    private final Submitter<DeathListener> sub = new Submitter<DeathListener>(){

	@Override
	public void submit(DeathListener item) {
	    item.notifyDeath();
	    
	}

	@Override
	public void submit(Collection<DeathListener> items) {
	   for(DeathListener l:items){submit(l);}
	    
	}
    };//end submitter

    public void reset(){
	 dead=false;
	}
}
