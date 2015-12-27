/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.miss;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.CustomDeathBehavior;
import org.jtrfp.trcl.beh.CustomNAVTargetableBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.HorizAimAtPlayerBehavior;
import org.jtrfp.trcl.beh.RemovesNAVObjectiveOnDeath;
import org.jtrfp.trcl.beh.tun.TunnelEntryListener;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.BOS;
import org.jtrfp.trcl.file.NAVFile.CHK;
import org.jtrfp.trcl.file.NAVFile.DUN;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.TGT;
import org.jtrfp.trcl.file.NAVFile.TUN;
import org.jtrfp.trcl.file.NAVFile.XIT;
import org.jtrfp.trcl.file.TDFFile.ExitMode;
import org.jtrfp.trcl.file.TDFFile.TunnelLogic;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.obj.Checkpoint;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Jumpzone;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.TunnelExitObject;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class NAVObjective {
    private static final double CHECKPOINT_HEIGHT_PADDING=70000;
    public abstract String getDescription();
    public abstract WorldObject getTarget();
    protected NAVObjective(Factory f){
	if(f!=null)
	 f.tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+f.counter+".desc", getDescription());
	
	if(getTarget()!=null && f!=null){
	    final double [] loc = getTarget().getPosition();
	    f.tr.getReporter().report("org.jtrfp.trcl.flow.NAVObjective."+f.counter+".loc", "X="+loc[0]+" Y="+loc[1]+" Z="+loc[2]);
	    f.counter++;}
    }
    public static class Factory{
	private final TR tr;//for debug
	private Tunnel currentTunnel;
	int counter;
	private WorldObject worldBossObject,bossChamberExitShutoffTrigger;
	private final String debugName;
	public Factory(TR tr, String debugName){
	    this.tr=tr;
	    this.debugName = debugName;
	}//end constructor
	
	public void create(final TR tr, NAVSubObject navSubObject, List<NAVObjective>indexedNAVObjectiveList){
		final OverworldSystem overworld=((TVF3Game)tr.getGame()).getCurrentMission().getOverworldSystem();
		final List<DEFObject> defs = overworld.getDefList();
		if(navSubObject instanceof TGT){///////////////////////////////////////////
		    TGT tgt = (TGT)navSubObject;
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
			indexedNAVObjectiveList.add(objective);
			targ.addBehavior(new RemovesNAVObjectiveOnDeath(objective,((TVF3Game)tr.getGame()).getCurrentMission()));
			targ.addBehavior(new CustomDeathBehavior(new Runnable(){
			    @Override
			    public void run(){
				((TVF3Game)((TVF3Game)tr.getGame())).getUpfrontDisplay()
					.submitMomentaryUpfrontMessage("Target Destroyed");
			    }//end run()
			}));
		    }//end for(targs)
		} else if(navSubObject instanceof TUN){///////////////////////////////////////////
		    TUN tun = (TUN)navSubObject;
		    //Entrance and exit locations are already set up.
		    final Location3D 	loc3d 	= tun.getLocationOnMap();
		    final Vector3D modernLoc = new Vector3D(
				    TR.legacy2Modern(loc3d.getX()),
				    TR.legacy2Modern(loc3d.getY()),
				    TR.legacy2Modern(loc3d.getZ()));
		    /*final TunnelEntranceObject teo = ((TVF3Game)tr.getGame()).getCurrentMission().getTunnelEntranceObject(
			    new Point((int)(modernLoc.getX()/TR.mapSquareSize),(int)(modernLoc.getZ()/TR.mapSquareSize)));
		    */
		    final Mission mission = ((TVF3Game)tr.getGame()).getCurrentMission();
		    final TunnelEntranceObject teo = mission.getNearestTunnelEntrance(loc3d.getX(),loc3d.getY(),loc3d.getZ());
		    currentTunnel=teo.getSourceTunnel();
		    		/*final TunnelEntranceObject tunnelEntrance 
		    				= currentTunnel.getEntranceObject();
		    final double [] entPos=tunnelEntrance.getPosition();
		    entPos[0]=TR.legacy2Modern(loc3d.getZ());
		    entPos[1]=TR.legacy2Modern(loc3d.getY());
		    entPos[2]=TR.legacy2Modern(loc3d.getX());
		    entPos[1]=((TVF3Game)tr.getGame()).
			    getCurrentMission().
			    getOverworldSystem().
			    getAltitudeMap().
			    heightAt(
				TR.legacy2MapSquare(loc3d.getZ()), 
				TR.legacy2MapSquare(loc3d.getX()))*(tr.getWorld().sizeY/2)+TunnelEntranceObject.GROUND_HEIGHT_PAD;
		    tunnelEntrance.notifyPositionChange();
		    */
		    final NAVObjective enterObjective = new NAVObjective(this){
			    @Override
			    public String getDescription() {
				return "Enter Tunnel";
			    }
			    @Override
			    public WorldObject getTarget() {
				return teo;
			    }
		    };//end new NAVObjective tunnelEntrance
		   //tunnelEntrance.setNavObjectiveToRemove(enterObjective,true);
		    final WorldObject tunnelEntranceObject = teo;
		    currentTunnel.addTunnelEntryListener(new TunnelEntryListener(){
			@Override
			public void notifyTunnelEntered(Tunnel tunnel) {
			    if(((TVF3Game)tr.getGame()).getCurrentMission().getRemainingNAVObjectives().get(0).getTarget()==tunnelEntranceObject){
				((TVF3Game)tr.getGame()).getCurrentMission().removeNAVObjective(enterObjective);
				tunnel.removeTunnelEntryListener(this);
			    }
			}});
		    indexedNAVObjectiveList.add(enterObjective);
		    final TunnelExitObject tunnelExit = currentTunnel.getExitObject();
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
		    final Point tunnelPoint = new Point((int)TR.legacy2MapSquare(loc3d.getZ()),(int)TR.legacy2MapSquare(loc3d.getX()));
		    /*
		    //if(mission.getTunnelEntrancePortal(new Point((int)TR.legacy2MapSquare(loc3d.getZ()),(int)TR.legacy2MapSquare(loc3d.getX())))==null){
			//TODO
			final Camera tunnelCam = tr.secondaryRenderer.get().getCamera();
			final PortalExit portalExit = tunnelExit.getPortalExit();
			//if(tunnelExit.isMirrorTerrain())
			// portalExit.setRootGrid(((TVF3Game)tr.getGame()).getCurrentMission().getOverworldSystem().getMirroredTerrainGrid());
			//else
			 //portalExit.setRootGrid(((TVF3Game)tr.getGame()).getCurrentMission().getOverworldSystem());
			portalExit.setRootGrid(currentTunnel);//DEBUG
			final Vector3D exitLocation = tunnelExit.getExitLocation();
			//portalExit.setPosition(exitLocation.toArray());
			System.out.println("NAVObjective setPosition="+exitLocation);
			//portalExit.setPosition(Tunnel.TUNNEL_START_POS.toArray());//DEBUG
			Vector3D heading = new NormalMap(((TVF3Game)tr.getGame()).getCurrentMission().getOverworldSystem().getAltitudeMap()).normalAt(exitLocation.getX(), exitLocation.getZ());
			//Vector3D heading = Tunnel.TUNNEL_START_DIRECTION.getHeading();//DEBUG
			//portalExit.setHeading(heading);
			
			if(heading.getY()<.99&heading.getNorm()>0)//If the ground is flat this doesn't work.
				 portalExit.setTop(Vector3D.PLUS_J.crossProduct(heading).crossProduct(heading).negate());
			portalExit.notifyPositionChange();
			//tunnelExit.setPortalExit(portalExit);
			//final PortalEntrance entrance = new PortalEntrance(tr,portalModel,exit,tr.mainRenderer.get().getCamera());
			//mission.registerTunnelEntrancePortal(tunnelPoint, exit);
		    //}
			*/
		    indexedNAVObjectiveList.add(exitObjective);
		    tunnelExit.setNavObjectiveToRemove(exitObjective,true);
		    
		    tunnelExit.setMirrorTerrain(currentTunnel.getSourceTunnel().getExitMode()==ExitMode.exitToChamber);
		    
		    if(currentTunnel.getSourceTunnel().getEntranceLogic()==TunnelLogic.visibleUnlessBoss){
			teo.setVisible(true);
			bossChamberExitShutoffTrigger.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
			    @Override
			    public void run() {
				//tunnelEntrance.getBehavior().probeForBehavior(TunnelEntranceBehavior.class).setEnable(false);
				teo.setVisible(false);}
			}));
			//Avoid memory leaks
			final WeakReference<Mission> weakMission = new WeakReference<Mission>(mission);
			worldBossObject.addBehavior(new CustomDeathBehavior(new Runnable(){
			    @Override
			    public void run(){
				final Mission mission = weakMission.get();
				if(mission!=null)
				 mission.setBossFight(false);
				teo.setActive(true);
				teo.setVisible(true);
			    }
			}));
		    }else if(currentTunnel.getSourceTunnel().getEntranceLogic()==TunnelLogic.visible){//end if(visibleUnlessBoss)
			teo.setActive(true);
			teo.setVisible(true);
		    }
		} else if(navSubObject instanceof BOS){///////////////////////////////////////////
		    final Mission mission = ((TVF3Game)tr.getGame()).getCurrentMission();
		    final WeakReference<Mission> wMission = new WeakReference<Mission>(mission);
		    final BOS bos = (BOS)navSubObject;
		    boolean first=true;
		    final int [] bossTargs = bos.getTargets();
		    final DEFObject bossObject = defs.get(bos.getBossIndex());
		    if(bossTargs!=null){
		     for(final int target:bos.getTargets()){
			final WorldObject shieldGen = defs.get(target);
			final NAVObjective objective = new NAVObjective(this){
			    @Override
			    public String getDescription() {
				return "Destroy Shield";
			    }
			    @Override
			    public WorldObject getTarget() {
				return shieldGen;
			    }
			};//end new NAVObjective
			((DEFObject)shieldGen).setShieldGen(true);
			if(first){
			    bossChamberExitShutoffTrigger=shieldGen;
			    shieldGen.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
				@Override
				public void run(){
				    wMission.get().enterBossMode(bos.getMusicFile());
				    ((TVF3Game)tr.getGame()).getUpfrontDisplay()
					.submitMomentaryUpfrontMessage("Mission Objective");
				}//end run()
			    }));
			    first=false;
			 }//end if(first)
			shieldGen.addBehavior(new RemovesNAVObjectiveOnDeath(objective,mission));
			bossChamberExitShutoffTrigger.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
			    @Override
			    public void run() {
				shieldGen.probeForBehavior(DamageableBehavior.class).setEnable(true);
				shieldGen.setActive(true);
			    }
			}));
			indexedNAVObjectiveList.add(objective);
		     }//end for(targets)
		    }//end if(bos.targets() !=null))
		    else {// No shield gens, just mark the boss.
			bossChamberExitShutoffTrigger=bossObject;
			    bossObject.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
				@Override
				public void run(){
				    wMission.get().enterBossMode(bos.getMusicFile());
				    ((TVF3Game)tr.getGame()).getUpfrontDisplay()
					.submitMomentaryUpfrontMessage("Mission Objective");
				}//end run()
			    }));
		    }
		    bossObject.addBehavior(new HorizAimAtPlayerBehavior(((TVF3Game)tr.getGame()).getPlayer()));
		    bossObject.setIgnoringProjectiles(true);
		    final NAVObjective objective = new NAVObjective(this){
			    @Override
			    public String getDescription() {
				return "Destroy Boss";
			    }
			    @Override
			    public WorldObject getTarget() {
				return bossObject;
			    }
			};//end new NAVObjective
			indexedNAVObjectiveList.add(objective);
			bossObject.addBehavior(new RemovesNAVObjectiveOnDeath(objective,mission));
			//bossObject.addBehavior(new ChangesBehaviorWhenTargeted(true,DamageableBehavior.class));
			bossObject.addBehavior(new CustomDeathBehavior(new Runnable(){
			    @Override
			    public void run(){
				wMission.get().exitBossMode();
			    }//end run()
			}));
			bossObject.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
			    @Override
			    public void run() {
				bossObject.probeForBehavior(DamageableBehavior.class).setEnable(true);
				bossObject.setIgnoringProjectiles(false);}
				}));
			
			if(bossTargs!=null){
			 if(bossTargs.length==0){
			    bossChamberExitShutoffTrigger=bossObject;}}
			else bossChamberExitShutoffTrigger=bossObject;
			worldBossObject = bossObject;
			bossChamberExitShutoffTrigger.addBehavior(new CustomNAVTargetableBehavior(new Runnable(){
			    @Override
			    public void run() {
				bossObject.setActive(true);}
			}));
		} else if(navSubObject instanceof CHK){///////////////////////////////////////////
		    final CHK cp = (CHK)navSubObject;
		    final Location3D loc3d = cp.getLocationOnMap();
		    final Checkpoint chk = new Checkpoint(tr,"NAVObjective."+debugName);
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
		    chk.setObjectiveToRemove(objective,((TVF3Game)tr.getGame()).getCurrentMission());
		    overworld.add(chk);
		    indexedNAVObjectiveList.add(objective);
		} else if(navSubObject instanceof XIT){///////////////////////////////////////////
		    XIT xit = (XIT)navSubObject;
		    Location3D loc3d = xit.getLocationOnMap();
		    currentTunnel.getExitObject().setExitLocation(
			    new Vector3D(TR.legacy2Modern(loc3d.getZ()),TR.legacy2Modern(loc3d.getY()),TR.legacy2Modern(loc3d.getX())));
		} else if(navSubObject instanceof DUN){///////////////////////////////////////////
		    final DUN xit = (DUN)navSubObject;
		    final Location3D loc3d = xit.getLocationOnMap();
		    final Jumpzone chk = new Jumpzone(tr);
		    final double [] chkPos = chk.getPosition();
		    chkPos[0]=TR.legacy2Modern(loc3d.getZ());
		    chkPos[1]=TR.legacy2Modern(loc3d.getY());
		    chkPos[2]=TR.legacy2Modern(loc3d.getX());
		    chk.notifyPositionChange();
		    chk.setVisible(false);
		    try{//Start placing the jump zone.
		    //WorldObject jumpZone = new WorldObject(tr,tr.getResourceManager().getBINModel("JUMP-PNT.BIN", tr.getGlobalPaletteVL(), tr.gpu.get().getGl()));
		    //jumpZone.setPosition(chk.getPosition());
		    //jumpZone.setVisible(true);
		    //overworld.add(jumpZone);
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
		    chk.setObjectiveToRemove(objective,((TVF3Game)tr.getGame()).getCurrentMission());
		    chk.setIncludeYAxisInCollision(false);
		    overworld.add(chk);
		    indexedNAVObjectiveList.add(objective);
		    }catch(Exception e){e.printStackTrace();}
		}else{System.err.println("Unrecognized NAV objective: "+navSubObject);}
	    }//end create()
    }//end Factory
}//end NAVObjective