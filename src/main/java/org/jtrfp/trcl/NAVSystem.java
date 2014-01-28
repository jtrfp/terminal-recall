package org.jtrfp.trcl;

import java.util.List;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.obj.NavArrow;
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
	final double [] aPos = arrow.getPosition();
	aPos[0]=.825;
	aPos[1]=.8;
	aPos[2]=0;
	arrow.notifyPositionChange();
	//arrow.setPosition(new Vector3D(.825,.8,0));
	add(arrow);
	activate();
	
	System.out.println("...Done.");
    }//end constructor
    
    public void updateNAVState(){
	tr.getHudSystem().
	getObjective().
	setContent(tr.getCurrentMission().
		currentNAVObjective().
		getDescription());
    }
}//end NAVSystem
