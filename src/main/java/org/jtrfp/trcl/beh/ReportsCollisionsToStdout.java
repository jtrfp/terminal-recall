package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;

public class ReportsCollisionsToStdout extends Behavior implements SurfaceImpactListener{

    @Override
    public void collidedWithSurface(WorldObject wo, double [] surfaceNormal) {
	if(isEnabled())System.out.println("Object "+getParent()+" collided with "+wo);
    }

}
