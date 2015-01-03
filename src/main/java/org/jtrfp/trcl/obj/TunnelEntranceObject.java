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

import java.awt.Color;
import java.awt.Dimension;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.tun.TunnelEntryListener;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.TDFFile.TunnelLogic;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.math.Vect3D;

public class TunnelEntranceObject extends BillboardSprite {
    private final Tunnel tunnel;
    private NAVObjective navObjectiveToRemove;
    private boolean onlyRemoveIfCurrent=false;
    public static final double GROUND_HEIGHT_PAD=2500;
    public TunnelEntranceObject(TR tr, Tunnel tunnel) {
	super(tr);
	this.tunnel=tunnel;
	boolean debugMode=false;
	if(System.getProperties().containsKey("org.jtrfp.trcl.TunnelEntranceObject.debug")){
	    debugMode=System.getProperty("org.jtrfp.trcl.TunnelEntranceObject.debug").toUpperCase().contains("TRUE");
	}if(debugMode)System.out.println("TunnelEntranceObject.debug enabled.");
	addBehavior(new TunnelEntranceBehavior());
	setVisible(tunnel.getSourceTunnel().getEntranceLogic()!=TunnelLogic.invisible||debugMode);
	DirectionVector entrance = tunnel.getSourceTunnel().getEntrance();
	final double [] position = getPosition();
	position[0]=TR.legacy2Modern(entrance.getZ());
	position[1]=TR.legacy2Modern(entrance.getY()+GROUND_HEIGHT_PAD);
	position[2]=TR.legacy2Modern(entrance.getX());
	notifyPositionChange();
	this.setBillboardSize(new Dimension(10000,10000));
	try {
	    this.setTexture(
		    tr.getResourceManager().getRAWAsTexture("TARG1.RAW",
			    tr.getGlobalPaletteVL(),false), true);
	}	catch(Exception e){e.printStackTrace();}
    }//end constructor

    public class TunnelEntranceBehavior extends Behavior implements CollisionBehavior, NAVTargetableBehavior{
	private boolean navTargeted=false;
	@Override
	public void proposeCollision(WorldObject other){
	      if(other instanceof Player){
		 WorldObject entranceObject = getParent();
		final TR tr	  = entranceObject.getTr();
		final World world = tr.getWorld();
		final Game game   = tr.getGame();
		if(game==null)return;
		final Mission mission = game.getCurrentMission();
		if(mission.getOverworldSystem()==null) return;
		final InterpolatingAltitudeMap map = 
			game.
			getCurrentMission().
			getOverworldSystem().
			getAltitudeMap();
		if(map==null)return;
		double [] playerPos = other.getPosition();
		double [] thisPos   = entranceObject.getPosition();
		final double groundHeightNorm = map.heightAt((thisPos[0]/TR.mapSquareSize), 
			    (thisPos[2]/TR.mapSquareSize));
		final double groundHeight = groundHeightNorm*(world.sizeY/2);
		//Ignore ground height with chambers because entrances don't behave themselves with this.
		final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
		if(!overworldSystem.isChamberMode()&&playerPos[1]>groundHeight+GROUND_HEIGHT_PAD*4)return;
	        if(Vect3D.distanceXZ(entranceObject.getPosition(),other.getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE*2){
	         game.getCurrentMission().notifyTunnelFound(tunnel);
	         //Turn off overworld
		 overworldSystem.deactivate();
		 //Turn on tunnel
		 tunnel.activate();
		 //Move player to tunnel
		 world.setFogColor(new Color(10,30,15));
		 //Ensure chamber mode is off
		 overworldSystem.setChamberMode(false);
		 overworldSystem.setTunnelMode(true);
		 //Update debug data
		 tr.getReporter().report("org.jtrfp.Tunnel.isInTunnel?", "true");
		 
		 final ProjectileFactory [] pfs = tr.getResourceManager().getProjectileFactories();
		 for(ProjectileFactory pf:pfs){
		     Projectile [] projectiles = pf.getProjectiles();
		     for(Projectile proj:projectiles){
			 ((WorldObject)proj).
			  getBehavior().
			  probeForBehavior(LoopingPositionBehavior.class).
			  setEnable(false);
		     }//end for(projectiles)
		 }//end for(projectileFactories)
		 final Player player = tr.getGame().getPlayer();
		 final Behavior playerBehavior = player.getBehavior();
		 playerBehavior.probeForBehavior(MovesByVelocity.class).setVelocity(Vector3D.ZERO);
		 playerBehavior.probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		 playerBehavior.probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
		 playerBehavior.probeForBehavior(CollidesWithTerrain.class).setEnable(false);
		 entranceObject.getBehavior().probeForBehaviors(TELsubmitter, TunnelEntryListener.class);
		 player.setActive(false);
		 player.setPosition(Tunnel.TUNNEL_START_POS.toArray());
		 player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
		 player.notifyPositionChange();
		 
		 final NAVObjective navObjective = getNavObjectiveToRemove();
	         if(navObjective!=null && navTargeted){
	             final Mission m = game.getCurrentMission();
	             if(!(onlyRemoveIfCurrent&&navObjective!=m.currentNAVObjective()))m.removeNAVObjective(navObjective);
	         }//end if(have NAV to remove
	         player.setActive(true);
	        }//end if(close to Player)
	    }//end if(Player)
	}//end _proposeCollision
	@Override
	public void notifyBecomingCurrentTarget() {
	    navTargeted=true;
	}
    }//end TunnelEntranceBehavior
    private final AbstractSubmitter<TunnelEntryListener> TELsubmitter = new AbstractSubmitter<TunnelEntryListener>(){
	@Override
	public void submit(TunnelEntryListener tel){tel.notifyTunnelEntered();} 
    };
    /**
     * @return the navObjectiveToRemove
     */
    public NAVObjective getNavObjectiveToRemove() {
        return navObjectiveToRemove;
    }
    /**
     * @param navObjectiveToRemove the navObjectiveToRemove to set
     */
    public void setNavObjectiveToRemove(NAVObjective navObjectiveToRemove) {
        setNavObjectiveToRemove(navObjectiveToRemove,false);
    }
    public void setNavObjectiveToRemove(NAVObjective navObjectiveToRemove, boolean onlyRemoveIfCurrent) {
	this.onlyRemoveIfCurrent=onlyRemoveIfCurrent;
	this.navObjectiveToRemove=navObjectiveToRemove;
    }
    /**
     * @return the onlyRemoveIfCurrent
     */
    public boolean isOnlyRemoveIfCurrent() {
        return onlyRemoveIfCurrent;
    }
    /**
     * @param onlyRemoveIfCurrent the onlyRemoveIfCurrent to set
     */
    public void setOnlyRemoveIfCurrent(boolean onlyRemoveIfCurrent) {
        this.onlyRemoveIfCurrent = onlyRemoveIfCurrent;
    }
}//end TunnelEntrance
