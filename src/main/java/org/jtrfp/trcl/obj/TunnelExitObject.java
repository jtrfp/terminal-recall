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
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.NormalMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.PortalTexture;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.flow.TVF3Game;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.shell.GameShell;

public class TunnelExitObject extends PortalEntrance {
    private 		Vector3D 	exitLocation, exitHeading, exitTop;
    private final 	Tunnel 		tun;
    private final 	TR 		tr;
    private 		NAVObjective 	navObjectiveToRemove;
    private 		boolean 	mirrorTerrain = false;
    private		boolean		onlyRemoveIfTargeted=false;
    private static final int            NUDGE = 5000;

    public TunnelExitObject(TR tr, Tunnel tun) {
	super(tr, tr.mainRenderer.get().getCamera());
	addBehavior(new TunnelExitBehavior());
	final DirectionVector v = tun.getSourceTunnel().getExit();
	final NormalMap map = 
		new NormalMap(
		tr.
		getGame().
		getCurrentMission().
		getOverworldSystem().
		getAltitudeMap());
	final double exitY = 
		map.heightAt(TR.legacy2Modern(v.getZ()), TR.legacy2Modern(v
		.getX()));
	this.exitLocation = new Vector3D(TR.legacy2Modern(v.getZ()),
		exitY, TR.legacy2Modern(v
			.getX()));
	
	this.tun = tun;
	exitHeading = map.
		normalAt(
		exitLocation.getX(),
		exitLocation.getZ());
	
	this.exitLocation = exitLocation.add(exitHeading.scalarMultiply(NUDGE));
	
	if(exitHeading.getY()<.99&&exitHeading.getNorm()>0)//If the ground is flat this doesn't work.
		 exitTop = (Vector3D.PLUS_J.crossProduct(exitHeading).crossProduct(exitHeading)).negate();
		else exitTop = (Vector3D.PLUS_I);// ... so we create a clause for that.
	this.tr = tr;
	PortalExit pExit = new PortalExit(tr, tr.secondaryRenderer.get().getCamera());
	pExit.setPosition(exitLocation.toArray());
	pExit.setHeading(exitHeading);
	pExit.setTop(exitTop);
	pExit.setRootGrid(((TVF3Game)tr.getGame()).getCurrentMission().getOverworldSystem());
	pExit.notifyPositionChange();
	this.setPortalExit(pExit);
	setVisible(true);
	Triangle [] tris = Triangle.quad2Triangles(new double[]{-50000,50000,50000,-50000}, new double[]{50000,50000,-50000,-50000}, new double[]{0,0,0,0}, new double[]{0,1,1,0}, new double[]{1,1,0,0}, new PortalTexture(0), RenderMode.STATIC, false, Vector3D.ZERO, "TunnelExitObject.portalModel");
	//Model m = Model.buildCube(100000, 100000, 200, new PortalTexture(0), new double[]{50000,50000,100},false,tr);
	Model m = new Model(false, tr);
	m.addTriangles(tris);
	setModel(m);
    }//end constructor

    private class TunnelExitBehavior extends Behavior implements
	    CollisionBehavior, NAVTargetableBehavior {
	private boolean navTargeted=false;
	@Override
	public void proposeCollision(WorldObject other) {
	    if (other instanceof Player) {
		//We want to track the camera's crossing in deciding when to move the player.
		final Camera camera = tr.mainRenderer.get().getCamera();
		if (camera.getPosition()[0] > TunnelExitObject.this
			.getPosition()[0]) {
		    final Game game = ((TVF3Game)tr.getGame());
		    final Mission mission = game.getCurrentMission();
		    final OverworldSystem overworldSystem = mission.getOverworldSystem();
		    System.out.println("TunnelExitObject leaving tunnel "+tun);
		    if(mirrorTerrain){
			mission.setMissionMode(new Mission.ChamberMode());
		    }else mission.setMissionMode(new Mission.AboveGroundMode());
		    overworldSystem.setChamberMode(mirrorTerrain);//TODO: Use PCL to set this automatically in Mission
		    if(mirrorTerrain)
		     tr.setRunState(new Mission.ChamberState(){});
		    else
		     tr.setRunState(new Mission.PlayerActivity(){});
		    
		    //tr.getDefaultGrid().nonBlockingAddBranch(overworldSystem);
		    //tr.getDefaultGrid().nonBlockingRemoveBranch(branchToRemove)
		    
		    tr.mainRenderer     .get().getSkyCube().setSkyCubeGen(overworldSystem.getSkySystem().getBelowCloudsSkyCubeGen());
		    tr.secondaryRenderer.get().getSkyCube().setSkyCubeGen(GameShell.DEFAULT_GRADIENT);
		    // Teleport
		    final Camera secondaryCam = tr.secondaryRenderer.get().getCamera();
			other.setPosition(secondaryCam.getPosition());
			other.setHeading (secondaryCam.getHeading());
			other.setTop     (secondaryCam.getTop());
			other.notifyPositionChange();
		    World.relevanceExecutor.submit(new Runnable(){
			@Override
			public void run() {
			    final SpacePartitioningGrid grid = tr.getDefaultGrid();
			    // Tunnel off
			    grid.removeBranch(tun);
			    // World on
			    grid.addBranch(overworldSystem);
			    // Nav
			    //grid.addBranch(game.getNavSystem());
			}});
		    overworldSystem.setTunnelMode(false);
		    // Reset player behavior
		    final Player player = ((TVF3Game)tr.getGame()).getPlayer();
		    player.setActive(false);
		    player.resetVelocityRotMomentum();
		    player.probeForBehavior(CollidesWithTunnelWalls.class)
		    	    .setEnable(false);
		    player.probeForBehavior(DamageableBehavior.class)
			    .addInvincibility(250);// Safety kludge when near
						   // walls.
		    player.probeForBehavior(CollidesWithTerrain.class)
			    .setEnable(true);
		    player.probeForBehavior(LoopingPositionBehavior.class)
			    .setEnable(true);
		    player.probeForBehavior(
				    HeadingXAlwaysPositiveBehavior.class)
			    .setEnable(false);
		    // Update debug data
		    tr.getReporter().report("org.jtrfp.Tunnel.isInTunnel?",
			    "false");
		    // Reset projectile behavior
		    final ProjectileFactory[] pfs = tr.getResourceManager()
			    .getProjectileFactories();
		    for (ProjectileFactory pf : pfs) {
			Projectile[] projectiles = pf.getProjectiles();
			for (Projectile proj : projectiles) {
			    ((WorldObject) proj)
				    .probeForBehavior(
					    LoopingPositionBehavior.class)
				    .setEnable(true);
			}// end for(projectiles)
		    }// end for(projectileFactories)
		    final NAVObjective navObjective = getNavObjectiveToRemove();
		    if (navObjective != null && (navTargeted|!onlyRemoveIfTargeted)) {
			((TVF3Game)tr.getGame()).getCurrentMission().removeNAVObjective(navObjective);
		    }// end if(have NAV to remove
		    ((TVF3Game)tr.getGame()).getNavSystem().updateNAVState();
		    player.setActive(true);
		}// end if(x past threshold)
	    }// end if(Player)
	}// end proposeCollision()

	@Override
	public void notifyBecomingCurrentTarget() {
	    navTargeted=true;
	}
    }// end TunnelExitBehavior

    /**
     * @return the navObjectiveToRemove
     */
    public NAVObjective getNavObjectiveToRemove() {
	return navObjectiveToRemove;
    }

    /**
     * @param navObjectiveToRemove
     *            the navObjectiveToRemove to set
     * @param onlyRemoveIfTargeted 
     */
    public void setNavObjectiveToRemove(NAVObjective navObjectiveToRemove, boolean onlyRemoveIfTargeted) {
	this.onlyRemoveIfTargeted = onlyRemoveIfTargeted;
	this.navObjectiveToRemove = navObjectiveToRemove;
    }

    public void setMirrorTerrain(boolean b) {
	mirrorTerrain = b;
    }

    /**
     * @return the exitLocation
     */
    public Vector3D getExitLocation() {
	return exitLocation;
    }

    /**
     * @param exitLocation
     *            the exitLocation to set
     */
    public void setExitLocation(Vector3D exitLocation) {
	this.exitLocation = exitLocation;
    }

    /**
     * @return the mirrorTerrain
     */
    public boolean isMirrorTerrain() {
	return mirrorTerrain;
    }

}// end TunnelExitObject
