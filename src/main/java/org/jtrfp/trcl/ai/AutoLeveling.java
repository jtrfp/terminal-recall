package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AutoLeveling extends ObjectBehavior {
    @Override
    public void _tick(long timeInMillis){
	final Vector3D old = getParent().getHeading();
	getParent().setHeading(new Vector3D(old.getX(),old.getY()*.99,old.getZ()).normalize());
	final Vector3D oTop = getParent().getTop();
	getParent().setTop(new Vector3D(oTop.getX(),oTop.getY()*1.01,oTop.getZ()).normalize());
    }
}
