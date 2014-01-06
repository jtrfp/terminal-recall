package org.jtrfp.trcl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class NAVSystem extends RenderableSpacePartitioningGrid {
private final NavArrow arrow;
    public NAVSystem(SpacePartitioningGrid<PositionedRenderable> parent, TR tr) {
	super(parent);
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr);
	arrow.setPosition(new Vector3D(.825,.8,0));
	addAlwaysVisible(arrow);
	activate();
	System.out.println("...Done.");
    }//end constructor
}//end NAVSystem
