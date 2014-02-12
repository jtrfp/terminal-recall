package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class SteadilyRotating extends Behavior {
    private double rotationPeriodMillis=10*1000;
    @Override
    public void _tick(long tickTime){
	final WorldObject thisObject=getParent();
	thisObject.setHeading(new Vector3D(
		Math.sin(((double)tickTime/rotationPeriodMillis)*Math.PI*2),
		0,
		Math.cos(((double)tickTime/rotationPeriodMillis)*Math.PI*2)));
    }//end _tick(...)
    /**
     * @return the rotationPeriodMillis
     */
    public double getRotationPeriodMillis() {
        return rotationPeriodMillis;
    }
    /**
     * @param rotationPeriodMillis the rotationPeriodMillis to set
     */
    public void setRotationPeriodMillis(double rotationPeriodMillis) {
        this.rotationPeriodMillis = rotationPeriodMillis;
    }
}//end SlowlyRotating
