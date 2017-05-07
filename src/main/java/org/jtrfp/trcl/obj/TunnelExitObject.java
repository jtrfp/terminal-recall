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
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.PortalTexture;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.NAVObjective;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class TunnelExitObject extends PortalEntrance {
    private 		Vector3D 	exitLocation, exitHeading, exitTop;
    private final 	Tunnel 		tun;
    private 		NAVObjective 	navObjectiveToRemove;
    private 		boolean 	mirrorTerrain = false;
    private		boolean		onlyRemoveIfTargeted=false;
    private static final int            NUDGE = 5000;
    private GameShell                   gameShell;
    
    public TunnelExitObject(Tunnel tun, String debugName, WorldObject approachingObject) {
	super(new PortalExit(),approachingObject);
	addBehavior(new TunnelExitBehavior());
	final DirectionVector v = tun.getSourceTunnel().getExit();
	final NormalMap map = 
		new NormalMap(
		getGameShell().
		getGame().
		getCurrentMission().
		getOverworldSystem().
		getAltitudeMap());
	final double exitY = 
		map.heightAt(TRFactory.legacy2Modern(v.getZ()), TRFactory.legacy2Modern(v
		.getX()));
	this.exitLocation = new Vector3D(TRFactory.legacy2Modern(v.getZ()),
		exitY, TRFactory.legacy2Modern(v
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
	final PortalExit pExit = getPortalExit();
	pExit.setPosition(exitLocation.toArray());
	pExit.setHeading(exitHeading);
	pExit.setTop(exitTop);
	pExit.setRootGrid(((TVF3Game)getGameShell().getGame()).getCurrentMission().getOverworldSystem());
	pExit.notifyPositionChange();
	this.setPortalExit(pExit);
	setPortalTexture(new PortalTexture());
	setVisible(true);
	Triangle [] tris = Triangle.quad2Triangles(new double[]{-50000,50000,50000,-50000}, new double[]{50000,50000,-50000,-50000}, new double[]{0,0,0,0}, new double[]{0,1,1,0}, new double[]{1,1,0,0}, getPortalTexture(), RenderMode.STATIC, false, Vector3D.ZERO, "TunnelExitObject.portalModel");
	//Model m = Model.buildCube(100000, 100000, 200, new PortalTexture(0), new double[]{50000,50000,100},false,tr);
	GL33Model m = new GL33Model(false, getTr(),"TunnelExitObject."+debugName);
	m.addTriangles(tris);
	setModel(m);
    }//end constructor

    private class TunnelExitBehavior extends Behavior implements
	    CollisionBehavior, NAVTargetableBehavior {
	private boolean navTargeted=false;
	@Override
	public void proposeCollision(WorldObject other) {
	    final TR tr = getTr();
	    if (other instanceof Player) {
		if(getParent().getPosition()[0]<0)
		    throw new RuntimeException("Negative X coord! "+getParent().getPosition()[0]);
		//System.out.println("TunnelExitObject relevance tally="+tr.gpu.get().rendererFactory.get().getRelevanceTallyOf(getParent())+" within range? "+TunnelExitObject.this.isWithinRange());
		//We want to track the camera's crossing in deciding when to move the player.
		final Camera camera = tr.mainRenderer.getCamera();
		//System.out.println("hash: "+super.hashCode()+" Cam pos = "+camera.getPosition()[0]+" thisPos="+TunnelExitObject.this.getPosition()[0]);
		if (camera.getPosition()[0] > TunnelExitObject.this
			.getPosition()[0]) {
		    System.out.println("Escaping tunnel at exit.X="+getPosition()[0]+" camera.X="+camera.getPosition()[0]);
		    final Game game = ((TVF3Game)getGameShell().getGame());
		    final Mission mission = game.getCurrentMission();
		    final OverworldSystem overworldSystem = mission.getOverworldSystem();
		    System.out.println("TunnelExitObject leaving tunnel "+tun);
		    //tr.getDefaultGrid().nonBlockingAddBranch(overworldSystem);
		    //tr.getDefaultGrid().nonBlockingRemoveBranch(branchToRemove)
		    
		    tr.mainRenderer.getSkyCube().setSkyCubeGen(overworldSystem.getSkySystem().getBelowCloudsSkyCubeGen());
		    final Renderer portalRenderer = TunnelExitObject.this.getPortalRenderer();
		    //if(portalRenderer == null)
			//throw new IllegalStateException("PortalRenderer intolerably null.");
		    //portalRenderer.getSkyCube().setSkyCubeGen(GameShellFactory.DEFAULT_GRADIENT);
		    // Teleport
		    //final Camera secondaryCam = portalRenderer.getCamera();
		    final TunnelExitObject teo = (TunnelExitObject)this.getParent();
		    
			other.setPosition(teo.getPortalExit().getControlledPosition());
			other.setHeadingArray(teo.getPortalExit().getControlledHeading());
			other.setTopArray(teo.getPortalExit().getControlledTop());
			other.notifyPositionChange();
		    World.relevanceExecutor.submit(new Runnable(){
			@Override
			public void run() {
			    //final SpacePartitioningGrid grid = tr.getDefaultGrid();
			    // Tunnel off
			    //grid.removeBranch(tun);
			    // World on
			    //grid.addBranch(overworldSystem);
			    //Switch to overworld mode
			    //try{mission.setDisplayMode(mission.overworldMode);}
			    //catch(Exception e){e.printStackTrace();}
			    // Nav
			    //grid.addBranch(game.getNavSystem());
			}});
		    // Reset player behavior
		    final Player player = ((TVF3Game)getGameShell().getGame()).getPlayer();
		    player.setActive(false);
		    player.resetVelocityRotMomentum();
		    player.probeForBehavior(CollidesWithTunnelWalls.class)
		    	    .setEnable(false);
		    player.probeForBehavior(DamageableBehavior.class)
			    .addInvincibility(250);// XXX Safety kludge when near
						   // walls.
		    player.probeForBehavior(CollidesWithTerrain.class)
			    .setEnable(true);
		    player.probeForBehavior(LoopingPositionBehavior.class)
			    .setEnable(true);
		   /* player.probeForBehavior(
				    HeadingXAlwaysPositiveBehavior.class)
			    .setEnable(false);*/
		    // Update debug data
		    Features.get(tr, Reporter.class).report("org.jtrfp.Tunnel.isInTunnel?",
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
			((TVF3Game)getGameShell().getGame()).getCurrentMission().removeNAVObjective(navObjective);
		    }// end if(have NAV to remove
		    
		    if(mirrorTerrain){
			tr.setRunState(new Mission.ChamberState(){});
		    }else tr.setRunState(new Mission.OverworldState(){});
		    overworldSystem.setChamberMode(mirrorTerrain);//TODO: Use PCL to set this automatically in Mission
		   /*
		    if(mirrorTerrain)
		     tr.setRunState(new Mission.ChamberState(){});
		    else
		     tr.setRunState(new Mission.PlayerActivity(){});
		    */
		    ((TVF3Game)getGameShell().getGame()).getNavSystem().updateNAVState();
		    mission.setDisplayMode(mission.getOverworldMode());
		    overworldSystem.setTunnelMode(false);
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
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }

}// end TunnelExitObject
