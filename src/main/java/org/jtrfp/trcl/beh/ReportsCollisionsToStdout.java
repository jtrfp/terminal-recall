package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class ReportsCollisionsToStdout extends Behavior implements SurfaceImpactListener{

    @Override
    public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal) {
	if(isEnabled())System.out.println("Object "+getParent()+" collided with "+wo);
    }

}
