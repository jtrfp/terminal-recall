package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class DebrisOnDeathBehavior extends Behavior implements DeathListener {
    private final double MAX_SPEED=70000;
    private final int MIN_FRAGS=6;
    @Override
    public void notifyDeath() {
	WorldObject p = getParent();
	for(int i=0; i<MIN_FRAGS+p.getModel().getTriangleList().getMaximumVertexValue()/6000; i++){
	    p.getTr().getResourceManager().getDebrisFactory().spawn(p.getPosition(), 
	    new Vector3D(
		Math.random()*MAX_SPEED-MAX_SPEED/2.,
		Math.random()*MAX_SPEED+30000,
		Math.random()*MAX_SPEED-MAX_SPEED/2.));
	}//end for(NUM_FRAGS)
    }//end constructor

}
