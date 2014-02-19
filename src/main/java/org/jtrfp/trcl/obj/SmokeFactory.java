package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Smoke.SmokeType;

public class SmokeFactory {
    	private final TR tr;
    	private final int MAX_SMOKE_PER_POOL=80;
    	private final Smoke[][] allSmokes = new Smoke[SmokeType.values().length][];
    	private final int [] indices = new int[SmokeType.values().length];
	public SmokeFactory(TR tr){
	    this.tr=tr;
	    int i;
	    for(SmokeType t:SmokeType.values()){
		allSmokes[t.ordinal()]=new Smoke[MAX_SMOKE_PER_POOL];
		for(i=0; i<MAX_SMOKE_PER_POOL; i++){
			allSmokes[t.ordinal()][i]=new Smoke(tr,t);
		    }//end for(MAX_SMOKE_PER_POOL)
	    }//end for(SmokeType s)
	}//end constructor()
	public Smoke triggerSmoke(double [] position, SmokeType type) {
	    indices[type.ordinal()]++;indices[type.ordinal()]%=MAX_SMOKE_PER_POOL;
	    Smoke result = allSmokes[type.ordinal()][indices[type.ordinal()]];
	    result.destroy();
	    result.resetSmoke();
	    result.setPosition(position);
	    tr.getWorld().add(result);
	    return result;
	}//end triggerSmoke()
}//end SmokeFactory
