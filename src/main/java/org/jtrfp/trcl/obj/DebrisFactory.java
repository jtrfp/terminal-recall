package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;

public class DebrisFactory {
    private final int DEBRIS_POOL_SIZE=40;
    private final TR tr;
    private final Debris[] allDebris = new Debris[DEBRIS_POOL_SIZE];
    private int index=0;
    public DebrisFactory(TR tr){
	this.tr=tr;
	for(int i=0; i<DEBRIS_POOL_SIZE; i++){
	    allDebris[i]=new Debris(tr);
	    }//end for(debris)
    }//end constructor
    
    public Debris spawn(Vector3D startPosition, Vector3D velocity) {
	final Debris result = allDebris[index];
	result.destroy();
	result.reset(startPosition, velocity);
	tr.getWorld().add(result);
	index++;
	index%=DEBRIS_POOL_SIZE;
	return result;
    }//end fire(...)
}
