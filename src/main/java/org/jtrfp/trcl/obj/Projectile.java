package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Projectile {

    void destroy();

    void reset(Vector3D firingPosition, Vector3D scalarMultiply);

}
