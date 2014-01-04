package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class HeadingXAlwaysPositiveBehavior extends Behavior {
    @Override
    public void _tick(long tickTime){
	final WorldObject p = getParent();
	final Vector3D heading = p.getHeading();
	if(heading.getX()<0){
	    //Rotate 180 degrees on top axis
	    p.setHeading(new Vector3D(0,heading.getY(),heading.getZ()).normalize());
	    final Vector3D horiz = p.getHeading().crossProduct(p.getTop()).normalize();
	    final Vector3D newTop = horiz.crossProduct(p.getHeading()).normalize();
	    p.setTop(newTop);
	}
    }//end _tick(...)
}//end HeadingXAlwaysPositiveBehavior
