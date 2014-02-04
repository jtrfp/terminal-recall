package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.obj.WorldObject;

public class DebrisOnDeathBehavior extends Behavior implements DeathListener {
    private final double MAX_SPEED=70000;
    private final int MIN_FRAGS=12;
    @Override
    public void notifyDeath() {
	WorldObject p = getParent();
	final double maxVertexValue;
	final Model model = p.getModel();
	if(model.getTriangleList()!=null)maxVertexValue=model.getTriangleList().getMaximumVertexValue();
	else if(model.getTransparentTriangleList()!=null)maxVertexValue=model.getTransparentTriangleList().getMaximumVertexValue();
	else if(model.getLineSegmentList()!=null)maxVertexValue=model.getLineSegmentList().getMaximumVertexValue();
	else return;//Give up
	for(int i=0; i<MIN_FRAGS+maxVertexValue/4000; i++){
	    p.getTr().getResourceManager().getDebrisFactory().spawn(p.getPosition(), 
	    new Vector3D(
		Math.random()*MAX_SPEED-MAX_SPEED/2.,
		Math.random()*MAX_SPEED+30000,
		Math.random()*MAX_SPEED-MAX_SPEED/2.));
	}//end for(NUM_FRAGS)
    }//end constructor

}
