package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.objects.WorldObject;

public interface SurfaceImpactListener {
    public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal);
}
