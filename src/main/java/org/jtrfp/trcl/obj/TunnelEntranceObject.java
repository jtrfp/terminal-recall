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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.miss.Mission;

public class TunnelEntranceObject extends BillboardSprite {
    public static final double GROUND_HEIGHT_PAD=3500;
    private final Tunnel sourceTunnel;
    private final PortalEntrance portalEntrance;
    
    public TunnelEntranceObject(TR tr, Tunnel tunnel, final PortalEntrance portalEntrance) {
	super(tr,"TunnelEntranceObject."+tunnel.getDebugName());
	this.portalEntrance = portalEntrance;
	final Mission mission = tr.getGame().getCurrentMission();
	mission.addPropertyChangeListener(new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().contentEquals("BossFight"))
		    TunnelEntranceObject.this.setVisible(!(Boolean)evt.getNewValue());
	    }});
	addBehavior(new TunnelEntranceBehavior(){
	    @Override
	    public void proposeCollision(WorldObject other) {}
	    });
	this.sourceTunnel=tunnel;
	//setVisible(tunnel.getSourceTunnel().getEntranceLogic()!=TunnelLogic.invisible);
	setVisible(true);
	DirectionVector entrance = tunnel.getSourceTunnel().getEntrance();
	final double [] position = getPosition();
	position[0]=TR.legacy2Modern(entrance.getZ());
	position[1]=TR.legacy2Modern(entrance.getY()+GROUND_HEIGHT_PAD);
	position[2]=TR.legacy2Modern(entrance.getX());
	double height = tr.getGame().getCurrentMission().getOverworldSystem().getAltitudeMap().heightAt(
		position[0], position[2]);
	position[1]=height+GROUND_HEIGHT_PAD;
	notifyPositionChange();
	this.setBillboardSize(new Dimension(100,100));
	try {this.setTexture(
		    tr.getResourceManager().getRAWAsTexture("TARG1.RAW",
			    tr.getGlobalPaletteVL(),null,false), true);
	}	catch(Exception e){e.printStackTrace();}
    }//end constructor
    
    public class TunnelEntranceBehavior extends Behavior implements CollisionBehavior{
	@Override
	public void proposeCollision(WorldObject other) {}
	}//end TunnelEntranceBehavior
/*
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
		 tr.renderer.get().getSkyCube().setSkyCubeGen(Tunnel.TUNNEL_SKYCUBE_GEN);
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
    };*/
    /**
     * @return the sourceTunnel
     */
    public Tunnel getSourceTunnel() {
        return sourceTunnel;
    }
    public PortalEntrance getPortalEntrance() {
        return portalEntrance;
    }
}//end TunnelEntrance
