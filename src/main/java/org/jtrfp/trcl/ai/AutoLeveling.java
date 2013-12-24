package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.objects.WorldObject;

public class AutoLeveling extends Behavior {
    @Override
    public void _tick(long timeInMillis){
	final WorldObject parent = getParent();
	final Vector3D oldHeading = parent.getHeading();
	final Vector3D newHeading =new Vector3D(oldHeading.getX(),oldHeading.getY(),oldHeading.getZ()).normalize();
	final Vector3D oldTop = parent.getTop();
	final Vector3D newTop=new Vector3D(oldTop.getX()*.993,oldTop.getY(),oldTop.getZ()*.993).normalize();
	final Rotation topDelta=new Rotation(oldTop,newTop);
	final Rotation headingDelta=new Rotation(oldHeading,newHeading);
	parent.setHeading(headingDelta.applyTo(topDelta.applyTo(oldHeading)));
	parent.setTop(headingDelta.applyTo(topDelta.applyTo(oldTop)));
    }
}
