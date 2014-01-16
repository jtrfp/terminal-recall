package org.jtrfp.trcl.flow;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.beh.RemovesNAVObjectiveOnDeath;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.BOS;
import org.jtrfp.trcl.file.NAVFile.CHK;
import org.jtrfp.trcl.file.NAVFile.DUN;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.TGT;
import org.jtrfp.trcl.file.NAVFile.TUN;
import org.jtrfp.trcl.file.NAVFile.XIT;
import org.jtrfp.trcl.obj.Checkpoint;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.TunnelExitObject;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class NAVObjective {
    private static TR tr;//for debug
    private static int counter=0;//for debug
    private static final double CHECKPOINT_HEIGHT_PADDING=70000;
    public static void create(TR tr, NAVSubObject obj, final List<DEFObject>defs, List<NAVObjective>dest, OverworldSystem overworld){
	NAVObjective.tr=tr;
	if(obj instanceof TGT){///////////////////////////////////////////
	    TGT tgt = (TGT)obj;
	    int [] targs =  tgt.getTargets();
	    for(int i=0; i<targs.length;i++){
		final WorldObject targ = defs.get(targs[i]);
		final NAVObjective objective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Destroy Target";
		    }
		    @Override
		    public WorldObject getTarget() {
			return targ;
		    }
		};//end new NAVObjective
		dest.add(objective);
		targ.addBehavior(new RemovesNAVObjectiveOnDeath(objective,tr.getCurrentMission()));
	    }//end for(targs)
	} else if(obj instanceof TUN){///////////////////////////////////////////
	    TUN tun = (TUN)obj;
	    final Location3D loc3d = tun.getLocationOnMap();
	    final Vector3D loc = new Vector3D(
		        TR.legacy2Modern(loc3d.getZ()),
		        TR.legacy2Modern(loc3d.getY()),
			TR.legacy2Modern(loc3d.getX()));
	    final TunnelEntranceObject tunnelEntrance = tr.getCurrentMission().
		    getTunnelByFileName(tun.getTunnelFileName()).
		    getEntranceObject();
	   
	    final NAVObjective objective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Enter Tunnel";
		    }
		    @Override
		    public WorldObject getTarget() {
			return tunnelEntrance;
		    }
	    };//end new NAVObjective
	    tunnelEntrance.setNavObjectiveToRemove(objective);
	    dest.add(objective);
	    final TunnelExitObject tunnelExit = tr.getCurrentMission().getTunnelByFileName(tun.getTunnelFileName()).getExitObject();
	    final NAVObjective exitObjective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Exit Tunnel";
		    }
		    @Override
		    public WorldObject getTarget() {
			return tunnelExit;
		    }
	    };//end new NAVObjective
	    dest.add(exitObjective);
	    tunnelExit.setNavObjectiveToRemove(exitObjective);
	} else if(obj instanceof BOS){///////////////////////////////////////////
	    final BOS bos = (BOS)obj;
	    final WorldObject bossObject = defs.get(bos.getBossIndex());
	    final NAVObjective objective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Mission Goal";
		    }
		    @Override
		    public WorldObject getTarget() {
			return bossObject;
		    }
		};//end new NAVObjective
		dest.add(objective);
		bossObject.addBehavior(new RemovesNAVObjectiveOnDeath(objective,tr.getCurrentMission()));
	} else if(obj instanceof CHK){///////////////////////////////////////////
	    final CHK cp = (CHK)obj;
	    final Location3D loc3d = cp.getLocationOnMap();
	    final Vector3D loc = new Vector3D(
		        TR.legacy2Modern(loc3d.getZ()),
		        TR.legacy2Modern(loc3d.getY()),
			TR.legacy2Modern(loc3d.getX()));
	    final Checkpoint chk = new Checkpoint(tr);
	    chk.setPosition(loc.add(new Vector3D(0,CHECKPOINT_HEIGHT_PADDING,0)));
	    chk.setIncludeYAxisInCollision(false);
	    final NAVObjective objective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Checkpoint";
		    }
		    @Override
		    public WorldObject getTarget() {
			return chk;
		    }
	    };//end new NAVObjective
	    chk.setObjectiveToRemove(objective,tr.getCurrentMission());
	    overworld.add(chk);
	    dest.add(objective);
	} else if(obj instanceof XIT){///////////////////////////////////////////
	    //Not used (TUN automatically sets this up)
	} else if(obj instanceof DUN){///////////////////////////////////////////
	    final DUN xit = (DUN)obj;
	    final Location3D loc3d = xit.getLocationOnMap();
	    final Vector3D loc = new Vector3D(
		        TR.legacy2Modern(loc3d.getZ()),
		        TR.legacy2Modern(loc3d.getY()),
			TR.legacy2Modern(loc3d.getX()));
	    final Checkpoint chk = new Checkpoint(tr);
	    chk.setPosition(loc);
	    chk.setVisible(false);
	    try{
	    WorldObject jumpZone = new WorldObject(tr,tr.getResourceManager().getBINModel("JUMP-PNT.BIN", tr.getGlobalPalette(), tr.getGPU().getGl()));
	    jumpZone.setPosition(chk.getPosition());
	    jumpZone.setVisible(true);
	    overworld.add(jumpZone);
	    final NAVObjective objective = new NAVObjective(){
		    @Override
		    public String getDescription() {
			return "Fly To Jump Zone";
		    }
		    @Override
		    public WorldObject getTarget() {
			return chk;
		    }
	    };//end new NAVObjective
	    chk.setObjectiveToRemove(objective,tr.getCurrentMission());
	    chk.setIncludeYAxisInCollision(false);
	    overworld.add(chk);
	    dest.add(objective);
	    }catch(Exception e){e.printStackTrace();}
	}else{System.err.println("Unrecognized NAV objective: "+obj);}
    }
    public abstract String getDescription();
    public abstract WorldObject getTarget();
    protected NAVObjective(){
	tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+counter+".desc", getDescription());
	if(getTarget()!=null)tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+counter+".loc", getTarget().getPosition());
	counter++;
    }
    
}
