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
private final List<NAVObjective> navs= new LinkedList<NAVObjective>();
    public NAVSystem(SpacePartitioningGrid<PositionedRenderable> parent, List<NAVSubObject> navSubObjects, 
	    TR tr) {
	super(parent);
	this.tr=tr;
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr,this);
	arrow.setPosition(new Vector3D(.825,.8,0));
	add(arrow);
	activate();
	START s = (START)navSubObjects.get(0);
	navSubObjects.remove(0);
	Location3D l3d = s.getLocationOnMap();
	//////// INITIAL HEADING
	tr.getPlayer().setPosition(new Vector3D(TR.legacy2Modern(l3d.getZ()),TR.legacy2Modern(l3d.getY()),TR.legacy2Modern(l3d.getX())));
	tr.getPlayer().setDirection(new ObjectDirection(s.getRoll(),s.getPitch(),s.getYaw()));
	tr.getPlayer().setHeading(tr.getPlayer().getHeading().negate());//Kludge to fix incorrect heading
	System.out.println("Start position set to "+tr.getPlayer().getPosition());
	//Install NAVs
	for(NAVSubObject obj:navSubObjects){
	    NAVObjective.create(tr, obj, tr.getOverworldSystem().getDefList(), navs, tr.getOverworldSystem(), this);
	}//end for(navSubObjects)
	updateNAVState();
	System.out.println("...Done.");
    }//end constructor
    
    public NAVObjective currentNAVTarget(){
	if(navs.isEmpty())return null;
	return navs.get(0);
    }
    public void removeNAVObjective(NAVObjective o){
	navs.remove(o);
	if(navs.size()==0){tr.getGame().missionComplete();}
	else updateNAVState();
    }
    protected void updateNAVState(){
	tr.getHudSystem().getObjective().setContent(currentNAVTarget().getDescription());
    }
}//end NAVSystem
