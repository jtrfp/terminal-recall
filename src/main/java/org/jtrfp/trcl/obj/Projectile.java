package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Projectile {

    void destroy();

    void reset(double[] ds, Vector3D scalarMultiply);

}
