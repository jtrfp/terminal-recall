package org.jtrfp.trcl.beh;

public class LimitedLifeSpan extends Behavior {
    private long timeRemainingMillis=Long.MAX_VALUE;
    @Override
    public void _tick(long tickTimeInMillis){
	timeRemainingMillis-=getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick();
	if(timeRemainingMillis<-0){getParent().destroy();System.out.println("LimiteDLifeSpan timed out.");}
    }
    
    public void reset(long millisUntilDestroyed){
	timeRemainingMillis=millisUntilDestroyed+System.currentTimeMillis();
    }
}
