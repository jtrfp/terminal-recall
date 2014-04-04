package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;

public interface ObjectPlacer {
    public void placeObjects(RenderableSpacePartitioningGrid target,
	    Vector3D positionOffset);
}
