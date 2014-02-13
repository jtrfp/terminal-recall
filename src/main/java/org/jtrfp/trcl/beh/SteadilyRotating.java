package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class SteadilyRotating extends Behavior {
    private double rotationPeriodMillis=10*1000;
    private double rotationPhase=0;
    @Override
    public void _tick(long tickTime){
	final WorldObject thisObject=getParent();
	final double theta=((double)tickTime/rotationPeriodMillis)*Math.PI*2+rotationPhase;
	thisObject.setHeading(new Vector3D(
		Math.sin(theta),
		0,
		Math.cos(theta)));
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
    public Behavior setRotationPhase(double phaseInRadians) {
	rotationPhase=phaseInRadians;
	return this;
    }
}//end SlowlyRotating
