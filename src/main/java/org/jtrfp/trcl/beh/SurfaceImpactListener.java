package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;

public interface SurfaceImpactListener {

    public void collidedWithSurface(WorldObject seg, double[] surfaceNormal);
}
