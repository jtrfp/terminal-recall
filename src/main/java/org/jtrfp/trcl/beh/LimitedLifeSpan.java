package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;

public class LimitedLifeSpan extends Behavior {
    private long timeRemainingMillis=Long.MAX_VALUE;
    @Override
    public void _tick(long tickTimeInMillis){
	final WorldObject p = getParent();
	timeRemainingMillis-=p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick();
	if(timeRemainingMillis<=0){
	    p.getBehavior().probeForBehavior(DeathBehavior.class).die();
	}//end if(remainign)
    }
    
    public LimitedLifeSpan reset(long millisUntilDestroyed){
	timeRemainingMillis=millisUntilDestroyed;
	return this;
    }
}//end LimitedLifeSpan
