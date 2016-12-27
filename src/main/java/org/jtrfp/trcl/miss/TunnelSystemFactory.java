/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.miss.Mission.TunnelState;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PortalEntrance;
import org.jtrfp.trcl.obj.PortalExit;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class TunnelSystemFactory implements FeatureFactory<Mission> {
    private GameShell gameShell;

    @Override
    public Feature<Mission> newInstance(Mission target) {
	return new TunnelSystem();
    }

    @Override
    public Class<Mission> getTargetClass() {
	return Mission.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TunnelSystem.class;
    }

    public class TunnelSystem implements Feature<Mission>{
	private final HashMap<String, Tunnel> 
	tunnels = new HashMap<String, Tunnel>();
	private final HashMap<Integer, PortalEntrance>
	tunnelPortals = new HashMap<Integer, PortalEntrance>();
	private final LinkedList<Tunnel>
	tunnelsRemaining = new LinkedList<Tunnel>();
	private final RenderableSpacePartitioningGrid tunnelGrid       = new RenderableSpacePartitioningGrid();

	private int			totalNumTunnels;
	private final Map<Integer,TunnelEntranceObject>
	tunnelMap = new HashMap<Integer,TunnelEntranceObject>();
	private Tunnel		currentTunnel;
	private Mission target;
	private Object [] tunnelMode;

	@Override
	public void apply(Mission target) {
	    setTarget(target);
	}//end apply()
	
	private Object [] getTunnelMode(){
	    if(tunnelMode == null){
		final ResourceManager rm = getTr().getResourceManager();
		final Game game = getTarget().getGame();
		tunnelMode = new Object[]{
			((TVF3Game)game).upfrontDisplay,
			rm.getDebrisSystem(),
			rm.getPowerupSystem(),
			rm.getProjectileFactories(),
			rm.getExplosionFactory(),
			rm.getSmokeSystem(),
			tunnelGrid
		};
	    }//end if(null)
		return tunnelMode;
	}//end getTunnelMode()

	@Override
	public void destruct(Mission target) {
	}

	public void installTunnels(TDFFile tdf, LoadingProgressReporter reporter){
	    TDFFile.Tunnel[] tuns = tdf.getTunnels();
	    tuns = tuns == null?new TDFFile.Tunnel[0]:tuns;//Null means no tunnels.
	    final LoadingProgressReporter[] reporters = reporter
		    .generateSubReporters(tuns.length);
	    if (tuns != null) {
		int tIndex = 0;
		final Reporter r = Features.get(getTr(), Reporter.class);
		// Build tunnels
		for (TDFFile.Tunnel tun : tuns) {
		    r.report("org.jtrfp.trcl.TunnelInstaller.tunnel."
			    + tIndex + ".entrance", tun.getEntrance().toString());
		    r.report("org.jtrfp.trcl.TunnelInstaller.tunnel."
			    + tIndex + ".exit", tun.getExit().toString());
		    newTunnel(tun,reporters[tIndex]);
		    tIndex++;
		}//end if(tuns!=null)
	    }// end if(tuns!=null)
	    totalNumTunnels = tunnelsRemaining.size();
	}//end installTunnels()

	private Tunnel newTunnel(org.jtrfp.trcl.file.TDFFile.Tunnel tdfTun,
		LoadingProgressReporter reporter) {
	    final Tunnel tunnel = new Tunnel(getTr(), tdfTun, reporter, tdfTun.getTunnelLVLFile());
	    tunnelsRemaining.add(tunnel);
	    DirectionVector tunnelEntranceLegacyPos = tdfTun.getEntrance();
	    final Point tunnelEntranceMapSquarePos = new Point(
		    (int)(TRFactory.legacy2MapSquare(tunnelEntranceLegacyPos.getZ())),
		    (int)(TRFactory.legacy2MapSquare(tunnelEntranceLegacyPos.getX())));
	    final PortalEntrance portalEntrance = getTunnelEntrancePortal(tunnelEntranceMapSquarePos);
	    final PortalExit portalExit = portalEntrance.getPortalExit();
	    addTunnelEntrance(tunnelEntranceMapSquarePos,tunnel,portalEntrance);
	    if(portalExit!=null){
		portalExit.setHeading(Tunnel.TUNNEL_START_DIRECTION.getHeading());
		portalExit.setTop(Tunnel.TUNNEL_START_DIRECTION.getTop());
		portalExit.setPosition(Tunnel.TUNNEL_START_POS.toArray());
		portalExit.notifyPositionChange();
		portalExit.setRootGrid(tunnel);
	    }else throw new NullPointerException("Null portal exit! "+tunnelEntranceMapSquarePos);
	    DirectionVector tunnelExitLegacyPos = tdfTun.getExit();
	    final Point tunnelExitMapSquarePos = new Point(
		    (int)(TRFactory.legacy2MapSquare(tunnelExitLegacyPos.getZ())),
		    (int)(TRFactory.legacy2MapSquare(tunnelExitLegacyPos.getX())));
	    assert tunnel.getExitObject().getPosition()[0]>0;//TODO: Remove
	    tunnels.put(tdfTun.getTunnelLVLFile().toUpperCase(), tunnel);
	    return tunnel;
	}

	public Tunnel getTunnelByFileName(String tunnelFileName) {
	    return tunnels.get(tunnelFileName.toUpperCase());
	}

	public TunnelEntranceObject getNearestTunnelEntrance(double xInLegacyUnits,
		double yInLegacyUnits, double zInLegacyUnits) {
	    TunnelEntranceObject result = null;
	    double closestDistance = Double.POSITIVE_INFINITY;
	    final Vector3D entPos = new Vector3D(
		    TRFactory.legacy2Modern(zInLegacyUnits),//Intentionally backwards
		    TRFactory.legacy2Modern(yInLegacyUnits),
		    TRFactory.legacy2Modern(xInLegacyUnits)
		    );
	    for (TunnelEntranceObject teo : tunnelMap.values()) {
		final Vector3D pos = new Vector3D(teo.getPosition());
		final double distance = pos.distance(entPos);
		if (distance < closestDistance) {
		    closestDistance = distance;
		    result = teo;
		}
	    }// end for(tunnels)
	    return result;
	}// end getTunnelWhoseEntranceClosestTo(...)

	public void notifyTunnelFound(Tunnel tun){
	    tunnelsRemaining.remove(tun);
	}


	public void registerTunnelEntrancePortal(Point mapSquareXZ, PortalEntrance entrance){
	    synchronized(tunnelPortals){
		tunnelPortals.put(pointToHash(mapSquareXZ),entrance);}
	}

	PortalEntrance getTunnelEntrancePortal(Point mapSquareXZ){
	    synchronized(tunnelPortals){
		return tunnelPortals.get(pointToHash(mapSquareXZ));}
	}

	private int pointToHash(Point point){
	    final int key =(int)point.getX()+(int)point.getY()*65536;
	    return key;
	}

	public void addTunnelEntrance(Point mapSquareXZ, Tunnel tunnel, PortalEntrance entrance){
	    TunnelEntranceObject teo;
	    getTarget().getOverworldSystem().add(teo = new TunnelEntranceObject(tunnel,entrance));
	    final int key = pointToHash(mapSquareXZ);
	    tunnelMap.put(key,teo);
	}

	public synchronized void enterTunnel(final TunnelEntranceObject teo) {
	    final Tunnel tunnelToEnter = teo.getSourceTunnel();
	    final Game game = ((TVF3Game)getGameShell().getGame());
	    final OverworldSystem overworldSystem = ((TVF3Game)game).getCurrentMission().getOverworldSystem();

	    assert tunnelToEnter.getExitObject().getPosition()[0]>0:""+tunnelToEnter.getExitObject().getPosition()[0];//TODO: Remove

	    notifyTunnelFound(tunnelToEnter);

	    //Move player to tunnel
	    getTr().mainRenderer.getSkyCube().setSkyCubeGen(Tunnel.TUNNEL_SKYCUBE_GEN);
	    //Ensure chamber mode is off
	    overworldSystem.setChamberMode(false);
	    overworldSystem.setTunnelMode(true);
	    //Update debug data
	    final Reporter r = Features.get(getTr(), Reporter.class);
	    r.report("org.jtrfp.Tunnel.isInTunnel?", "true");

	    final ProjectileFactory [] pfs = getTr().getResourceManager().getProjectileFactories();
	    for(ProjectileFactory pf:pfs){
		Projectile [] projectiles = pf.getProjectiles();
		for(Projectile proj:projectiles){
		    ((WorldObject)proj).
		    probeForBehavior(LoopingPositionBehavior.class).
		    setEnable(false);
		}//end for(projectiles)
	    }//end for(projectileFactories)
	    final Player player = ((TVF3Game)getGameShell().getGame()).getPlayer();
	    player.setActive(false);
	    player.resetVelocityRotMomentum();
	    player.probeForBehavior(CollidesWithTunnelWalls.class).setEnable(true);
	    player.probeForBehavior(MovesByVelocity.class)        .setVelocity(new double[3]);
	    player.probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
	    player.probeForBehavior(CollidesWithTerrain.class)    .setEnable(false);
	    tunnelToEnter.dispatchTunnelEntryNotifications();
	    final PortalExit portalExit = teo.getPortalEntrance().getPortalExit();
	    //final Camera secondaryCam = portalRenderer.getCamera();
	    player.setPosition    (portalExit.getControlledPosition());
	    player.setHeadingArray(portalExit.getControlledHeading());
	    player.setTopArray    (portalExit.getControlledTop());
	    //player.setPosition(secondaryPos.toArray());
	    //player.setHeading (secondaryHdg);
	    //player.setTop     (secondaryTop);
	    player.notifyPositionChange();
	    //Move the secondary cam to the overworld.
	    overworldSystem.setChamberMode(tunnelToEnter.getExitObject().isMirrorTerrain());
	    /*
	    //Set the skycube appropriately
	    portalRenderer.getSkyCube().setSkyCubeGen(((TVF3Game)getGameShell().getGame()).
		    getCurrentMission().
		    getOverworldSystem().
		    getSkySystem().
		    getBelowCloudsSkyCubeGen());
*/
	    getTr().setRunState(new TunnelState(){});
	    setCurrentTunnel(tunnelToEnter);
	    getTr().setRunState(new TunnelState(){});
	    getTarget().setDisplayMode(getTunnelMode());
	    player.setActive(true);
	}//end enterTunnel()


	/**
	 * 
	 * @param newTunnel
	 * @return The old tunnel, or null if none.
	 * @since Jan 23, 2016
	 */
	public Tunnel setCurrentTunnel(final Tunnel newTunnel){
	    final Tunnel oldTunnel = getCurrentTunnel();
	    this.currentTunnel = newTunnel;
	    tunnelGrid.removeAll();
	    tunnelGrid.addBranch(newTunnel);
	    return oldTunnel;
	}//end setCurrentTunnel

	public Collection<Tunnel> getTunnelsRemaining() {
	    return Collections.unmodifiableCollection(tunnelsRemaining);
	}

	public int getTotalNumTunnels() {
	    return totalNumTunnels;
	}

	public void setTotalNumTunnels(int totalNumTunnels) {
	    this.totalNumTunnels = totalNumTunnels;
	}

	public Tunnel getCurrentTunnel() {
	    if(!(getTr().getRunState() instanceof TunnelState))return null;
	    return currentTunnel;
	}

	/**
	 * Find a tunnel at the given map square, if any.
	 * @param mapSquareXZ Position in cells, not world coords.
	 * @return The Tunnel at this map square, or null if none here.
	 * @since Jan 13, 2015
	 */
	public TunnelEntranceObject getTunnelEntranceObject(Point mapSquareXZ){
	    final int key = pointToHash(mapSquareXZ);
	    return tunnelMap.get(key);
	}

	public Mission getTarget() {
	    return target;
	}

	public void setTarget(Mission target) {
	    this.target = target;
	}
	
	public TR getTr(){
	    return target.getTr();
	}
    }//end TunnelSystem

    public GameShell getGameShell() {
	if(gameShell == null){
	    final TR tr = Features.get(Features.getSingleton(),TR.class);
	    gameShell = Features.get(tr, GameShell.class);}
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }
}//end TunnelSystemFactory
