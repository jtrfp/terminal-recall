package org.jtrfp.trcl;

import java.util.Collection;

import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

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
	final NAVObjective obj = tr.getCurrentMission().currentNAVObjective();
	tr.getHudSystem().
		getObjective().
		setContent(obj.getDescription());
	final WorldObject target = obj.getTarget();
	if(target!=null){
	    target.getBehavior().probeForBehaviors(ntbSubmitter, NAVTargetableBehavior.class);
	}//end if(target!=null)
    }//end updateNAVState()
    
    private final Submitter<NAVTargetableBehavior> ntbSubmitter = new Submitter<NAVTargetableBehavior>(){

	@Override
	public void submit(NAVTargetableBehavior item) {
	    item.notifyBecomingCurrentTarget();
	}

	@Override
	public void submit(Collection<NAVTargetableBehavior> items) {
	    for(NAVTargetableBehavior i:items){this.submit(i);}
	}
	
    };//end ntbSubmitter
}//end NAVSystem
