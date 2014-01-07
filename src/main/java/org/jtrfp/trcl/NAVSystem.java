package org.jtrfp.trcl;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class NAVSystem extends RenderableSpacePartitioningGrid {
private final NavArrow arrow;
private final List<NAVSubObject> navs;
private int navIndex=0;
    public NAVSystem(SpacePartitioningGrid<PositionedRenderable> parent, List<NAVSubObject> navs, TR tr) {
	super(parent);
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr);
	arrow.setPosition(new Vector3D(.825,.8,0));
	addAlwaysVisible(arrow);
	arrow.setTop(new Vector3D(.5,.5,0).normalize());
	this.navs=navs;
	activate();
	START s = (START)navs.get(navIndex++);
	Location3D l3d = s.getLocationOnMap();
	tr.getPlayer().setPosition(new Vector3D(TR.legacy2Modern(l3d.getX()),TR.legacy2Modern(l3d.getY()),TR.legacy2Modern(l3d.getZ())));
	System.out.println("Start position set to "+tr.getPlayer().getPosition());
	System.out.println("...Done.");
    }//end constructor
}//end NAVSystem
