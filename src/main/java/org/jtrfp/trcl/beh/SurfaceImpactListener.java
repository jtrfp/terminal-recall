package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public interface SurfaceImpactListener {
    public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal);
}
