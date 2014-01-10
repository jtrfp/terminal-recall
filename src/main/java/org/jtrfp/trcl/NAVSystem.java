package org.jtrfp.trcl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.NAVFile.XIT;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class NAVSystem extends RenderableSpacePartitioningGrid {
private final NavArrow arrow;
private final TR tr;

    public NAVSystem(SpacePartitioningGrid<PositionedRenderable> parent, 
	    TR tr) {
	super(parent);
	this.tr=tr;
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr,this);
	arrow.setPosition(new Vector3D(.825,.8,0));
	add(arrow);
	activate();
	
	System.out.println("...Done.");
    }//end constructor
    
    public void updateNAVState(){
	tr.getHudSystem().
	getObjective().
	setContent(tr.getCurrentMission().
		currentNAVTarget().
		getDescription());
    }
}//end NAVSystem
