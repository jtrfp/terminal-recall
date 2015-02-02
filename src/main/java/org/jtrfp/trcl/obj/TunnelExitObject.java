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
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.gpu.Model;

public class TunnelExitObject extends WorldObject {
    private 		Vector3D 	exitLocation, exitHeading, exitTop;
    private final 	Tunnel 		tun;
    private final 	TR 		tr;
    private 		NAVObjective 	navObjectiveToRemove;
    private 		boolean 	mirrorTerrain = false;
    private		boolean		onlyRemoveIfTargeted=false;

    public TunnelExitObject(TR tr, Tunnel tun) {
	super(tr);
	addBehavior(new TunnelExitBehavior());
	final DirectionVector v = tun.getSourceTunnel().getExit();
	final double EXIT_Y_NUDGE = 0;
	final InterpolatingAltitudeMap map = tr.
		getGame().
		getCurrentMission().
		getOverworldSystem().
		getAltitudeMap();
	final double exitY = 
		map.heightAt(TR.legacy2Modern(v.getZ()), TR.legacy2Modern(v
		.getX()))+EXIT_Y_NUDGE;
	this.exitLocation = new Vector3D(TR.legacy2Modern(v.getZ()),
		exitY, TR.legacy2Modern(v
			.getX()));
	
	this.tun = tun;
	exitHeading = map.
		normalAt(
		exitLocation.getZ() / TR.mapSquareSize,
		exitLocation.getX() / TR.mapSquareSize);
	Vector3D horiz = exitHeading.crossProduct(Vector3D.MINUS_J);
	if (horiz.getNorm() == 0) {
	    horiz = Vector3D.PLUS_I;
	} else
	    horiz = horiz.normalize();
	exitTop = exitHeading.crossProduct(horiz.negate()).normalize().negate();
	exitLocation = exitLocation.add(exitHeading.scalarMultiply(10000));
	this.tr = tr;
	setVisible(false);
	try {
	    Model m = tr.getResourceManager().getBINModel("SHIP.BIN",
		    tr.getGlobalPaletteVL(), tr.gpu.get().getGl());
	    setModel(m);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private class TunnelExitBehavior extends Behavior implements
	    CollisionBehavior, NAVTargetableBehavior {
	private boolean navTargeted=false;
	@Override
	public void proposeCollision(WorldObject other) {
	    if (other instanceof Player) {
		if (other.getPosition()[0] > TunnelExitObject.this
			.getPosition()[0]) {
		    final Game game = tr.getGame();
		    final Mission mission = game.getCurrentMission();
		    final OverworldSystem overworldSystem = mission.getOverworldSystem();
		    if(mirrorTerrain){
			mission.setMissionMode(new Mission.ChamberMode());
		    }else mission.setMissionMode(new Mission.AboveGroundMode());
		    overworldSystem.setChamberMode(mirrorTerrain);//TODO: Use PCL to set this automatically in Mission
		    
		    tr.renderer.get().getSkyCube().setSkyCubeGen(overworldSystem.getSkySystem().getBelowCloudsSkyCubeGen());
		    // Teleport
		    other.setPosition(exitLocation.toArray());
		    // Heading
		    other.setHeading(exitHeading);
		    other.setTop(exitTop);
		    // Tunnel off
		    tun.deactivate();
		    // World on
		    overworldSystem.activate();
		    overworldSystem.setTunnelMode(false);
		    // Reset player behavior
		    final Player player = tr.getGame().getPlayer();
		    player.setActive(false);
		    player.resetVelocityRotMomentum();
		    player.probeForBehavior(CollidesWithTunnelWalls.class)
		    	    .setEnable(false);
		    player.getBehavior()
			    .probeForBehavior(DamageableBehavior.class)
			    .addInvincibility(250);// Safety kludge when near
						   // walls.
		    player.getBehavior()
			    .probeForBehavior(CollidesWithTerrain.class)
			    .setEnable(true);
		    player.getBehavior()
			    .probeForBehavior(LoopingPositionBehavior.class)
			    .setEnable(true);
		    player.getBehavior()
			    .probeForBehavior(
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
				    .getBehavior()
				    .probeForBehavior(
					    LoopingPositionBehavior.class)
				    .setEnable(true);
			}// end for(projectiles)
		    }// end for(projectileFactories)
		    final NAVObjective navObjective = getNavObjectiveToRemove();
		    if (navObjective != null && (navTargeted|!onlyRemoveIfTargeted)) {
			tr.getGame().getCurrentMission().removeNAVObjective(navObjective);
		    }// end if(have NAV to remove
		    tr.getGame().getNavSystem().updateNAVState();
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
