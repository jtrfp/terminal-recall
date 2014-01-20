package org.jtrfp.trcl.flow;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
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
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TDFFile.ExitMode;
import org.jtrfp.trcl.file.TDFFile.TunnelLogic;
import org.jtrfp.trcl.obj.Checkpoint;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.TunnelExitObject;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class NAVObjective {
    
    private static final double CHECKPOINT_HEIGHT_PADDING=70000;
    
    public abstract String getDescription();
    public abstract WorldObject getTarget();
    private final Factory factory;
    private int counter;
    protected NAVObjective(Factory f){
	factory=f;
	f.tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+counter+".desc", getDescription());
	if(getTarget()!=null)f.tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+counter+".loc", getTarget().getPosition());
	counter++;
    }
    
    
    public static class Factory{
	private TR tr;//for debug
	private int counter=0;//for debug
	private Tunnel currentTunnel;
	public Factory(TR tr){
	    this.tr=tr;
	}//end constructor
	
	public void create(TR tr, NAVSubObject obj, List<NAVObjective>dest){
		final OverworldSystem overworld=tr.getOverworldSystem();
		final List<DEFObject> defs = overworld.getDefList();
		if(obj instanceof TGT){///////////////////////////////////////////
		    TGT tgt = (TGT)obj;
		    int [] targs =  tgt.getTargets();
		    for(int i=0; i<targs.length;i++){
			final WorldObject targ = defs.get(targs[i]);
			final NAVObjective objective = new NAVObjective(this){
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
		    
		    /*final Vector3D loc = new Vector3D(
			        TR.legacy2Modern(loc3d.getZ()),
			        TR.legacy2Modern(loc3d.getY()),
				TR.legacy2Modern(loc3d.getX()));*/
		    //Entrance and exit locations are already set up.
		    final Location3D loc3d = tun.getLocationOnMap();
		    final Tunnel tunnel = tr.getCurrentMission().getTunnelWhoseEntranceClosestTo(loc3d.getX(),loc3d.getY(),loc3d.getZ());
		    /*final Tunnel tunnel = tr.getCurrentMission().
			    getTunnelByFileName(tun.getTunnelFileName());*/
		    currentTunnel = tunnel;
		    final TunnelEntranceObject tunnelEntrance = tunnel.
			    getEntranceObject();
		    final double [] entPos=tunnelEntrance.getPosition();
		    entPos[0]=TR.legacy2Modern(loc3d.getZ());
		    entPos[1]=TR.legacy2Modern(loc3d.getY());
		    entPos[2]=TR.legacy2Modern(loc3d.getX());
		    tunnelEntrance.notifyPositionChange();
		    
		    final NAVObjective enterObjective = new NAVObjective(this){
			    @Override
			    public String getDescription() {
				return "Enter Tunnel";
			    }
			    @Override
			    public WorldObject getTarget() {
				return tunnelEntrance;
			    }
		    };//end new NAVObjective tunnelEnrance
		    tunnelEntrance.setNavObjectiveToRemove(enterObjective);
		    dest.add(enterObjective);
		    final TunnelExitObject tunnelExit = tunnel.getExitObject();
		    final NAVObjective exitObjective = new NAVObjective(this){
			    @Override
			    public String getDescription() {
				return "Exit Tunnel";
			    }
			    @Override
			    public WorldObject getTarget() {
				return tunnelExit;
			    }
		    };//end new NAVObjective tunnelExit
		    dest.add(exitObjective);
		    tunnelExit.setNavObjectiveToRemove(exitObjective);
		    tunnelExit.setMirrorTerrain(tunnel.getSourceTunnel().getExitMode()==ExitMode.exitToChamber);
		} else if(obj instanceof BOS){///////////////////////////////////////////
		    final BOS bos = (BOS)obj;
		    final WorldObject bossObject = defs.get(bos.getBossIndex());
		    final NAVObjective objective = new NAVObjective(this){
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
		    final Checkpoint chk = new Checkpoint(tr);
		    final double [] chkPos = chk.getPosition();
		    chkPos[0]=TR.legacy2Modern(loc3d.getZ());
		    chkPos[1]=TR.legacy2Modern(loc3d.getY()+CHECKPOINT_HEIGHT_PADDING);
		    chkPos[2]=TR.legacy2Modern(loc3d.getX());
		    chk.notifyPositionChange();
		    chk.setIncludeYAxisInCollision(false);
		    final NAVObjective objective = new NAVObjective(this){
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
		    XIT xit = (XIT)obj;
		    Location3D loc3d = xit.getLocationOnMap();
		    currentTunnel.getExitObject().setExitLocation(
			    new Vector3D(TR.legacy2Modern(loc3d.getZ()),TR.legacy2Modern(loc3d.getY()),TR.legacy2Modern(loc3d.getX())));
		    //currentTunnel.getExitObject().setExitDirection(exitDirection);
		} else if(obj instanceof DUN){///////////////////////////////////////////
		    final DUN xit = (DUN)obj;
		    final Location3D loc3d = xit.getLocationOnMap();
		    final Checkpoint chk = new Checkpoint(tr);
		    final double [] chkPos = chk.getPosition();
		    chkPos[0]=TR.legacy2Modern(loc3d.getZ());
		    chkPos[1]=TR.legacy2Modern(loc3d.getY());
		    chkPos[2]=TR.legacy2Modern(loc3d.getX());
		    chk.notifyPositionChange();
		    chk.setVisible(false);
		    try{
		    WorldObject jumpZone = new WorldObject(tr,tr.getResourceManager().getBINModel("JUMP-PNT.BIN", tr.getGlobalPalette(), tr.getGPU().getGl()));
		    jumpZone.setPosition(chk.getPosition());
		    jumpZone.setVisible(true);
		    overworld.add(jumpZone);
		    final NAVObjective objective = new NAVObjective(this){
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
	    }//end create()
    }//end Factory
    
}//end NAVObjective
