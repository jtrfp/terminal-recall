package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Velocible
	{public void setVelocity(Vector3D vel);
	public Vector3D getVelocity();
	public void accellerate(Vector3D scalarMultiply);
	}
