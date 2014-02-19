package org.jtrfp.trcl.beh;

import java.util.Arrays;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Smoke.SmokeType;
import org.jtrfp.trcl.obj.SmokeFactory;

public class SpawnsRandomSmoke extends Behavior {
    private final SmokeFactory Smokes;
    public SpawnsRandomSmoke(TR tr){
	this.Smokes=tr.getResourceManager().getSmokeFactory();
    }
    @Override
    public void _tick(long timeMillis){
	if(Math.random()<.6){
	    final double [] pos = Arrays.copyOf(getParent().getPosition(), 3);
	    pos[0]+=Math.random()*2000;
	    pos[2]+=Math.random()*2000;
	    Smokes.triggerSmoke(pos, SmokeType.Puff);}
    }//end _tick(...)
}//end SpawnsRandomSmoke
