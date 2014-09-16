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
package org.jtrfp.trcl.flow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.AbstractVector;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.LoadingProgressReporter.UpdateHandler;
import org.jtrfp.trcl.flow.NAVObjective.Factory;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Propelled;

public class Mission {
    private final TR 		tr;
    private final List<NAVObjective> 
    				navs	= new LinkedList<NAVObjective>();
    private final LVLFile 	lvl;
    private final HashMap<String, Tunnel> 
    				tunnels = new HashMap<String, Tunnel>();
    private double[] 		playerStartPosition 
    					= new double[3];
    private List<NAVSubObject> 	navSubObjects;
    private ObjectDirection 	playerStartDirection;
    private final Game 		game;
    private final String 	levelName;
    private OverworldSystem 	overworldSystem;
    private final Result[]	missionEnd = new Result[]{null};
    private int			groundTargetsDestroyed=0,
	    			airTargetsDestroyed=0,
	    			foliageDestroyed=0;
    private int			totalNumTunnels;
    private final LinkedList<Tunnel>
    				tunnelsRemaining = new LinkedList<Tunnel>();

    private enum LoadingStages {
	navs, tunnels, overworld
    }// end LoadingStages
    
    public Mission(TR tr, Game game, LVLFile lvl, String levelName) {
	this.tr 	= tr;
	this.lvl 	= lvl;
	this.game 	= game;
	this.levelName 	= levelName;
    }// end Mission

    public Result go() {
	System.out.println("Starting GampeplayLevel loading sequence...");
	final HUDSystem hud = game.getHUDSystem();
	final LoadingProgressReporter rootProgress = LoadingProgressReporter.Impl
		.createRoot(new UpdateHandler() {
		    @Override
		    public void update(double unitProgress) {
			game.getLevelLoadingScreen().setLoadingProgress(unitProgress);
		    }
		});
	final LoadingProgressReporter[] progressStages = rootProgress
		.generateSubReporters(LoadingStages.values().length);
	game.setDisplayMode(game.levelLoadingMode);
	game.getUpfrontDisplay().submitPersistentMessage(levelName);
	try {
	    final ResourceManager rm = tr.getResourceManager();
	    final Player player      = tr.getPlayer();
	    final World world 	     = tr.getWorld();
	    final TDFFile tdf 	     = rm.getTDFData(lvl.getTunnelDefinitionFile());
	    player.setActive(false);
	    overworldSystem = new OverworldSystem(world,
		    progressStages[LoadingStages.overworld.ordinal()]);
	    getOverworldSystem().loadLevel(lvl, tdf);
	    System.out.println("\t...Done.");
	    // Install NAVs
	    final NAVSystem navSystem = tr.getGame().getNavSystem();
	    navSubObjects = rm.getNAVData(lvl.getNavigationFile())
		    .getNavObjects();

	    START s = (START) navSubObjects.get(0);
	    Location3D l3d = s.getLocationOnMap();
	    playerStartPosition[0] = TR.legacy2Modern(l3d.getZ());
	    playerStartPosition[1] = TR.legacy2Modern(l3d.getY());
	    playerStartPosition[2] = TR.legacy2Modern(l3d.getX());
	    playerStartDirection = new ObjectDirection(s.getRoll(),
		    s.getPitch(), s.getYaw());
	    // ////// INITIAL HEADING
	    player.setPosition(getPlayerStartPosition());
	    player.setDirection(getPlayerStartDirection());
	    player.setHeading(player.getHeading().negate());// Kludge to fix
							    // incorrect heading
	    ///////// STATE
	    final Propelled propelled = player.probeForBehavior(Propelled.class); 
	    propelled.setPropulsion(propelled.getMinPropulsion());
	    
	    installTunnels(tdf,progressStages[LoadingStages.tunnels.ordinal()]);
	    Factory f = new NAVObjective.Factory(tr);

	    final LoadingProgressReporter[] navProgress = progressStages[LoadingStages.navs
		    .ordinal()].generateSubReporters(navSubObjects.size());
	    for (int i = 0; i < navSubObjects.size(); i++) {
		final NAVSubObject obj = navSubObjects.get(i);
		f.create(tr, obj, navs);
		navProgress[i].complete();
	    }// end for(navSubObjects)
	    navSystem.updateNAVState();
	    final String startX = System.getProperty("org.jtrfp.trcl.startX");
	    final String startY = System.getProperty("org.jtrfp.trcl.startY");
	    final String startZ = System.getProperty("org.jtrfp.trcl.startZ");
	    final double[] playerPos = player.getPosition();
	    if (startX != null && startY != null && startZ != null) {
		System.out.println("Using user-specified start point");
		final int sX = Integer.parseInt(startX);
		final int sY = Integer.parseInt(startY);
		final int sZ = Integer.parseInt(startZ);
		playerPos[0] = sX;
		playerPos[1] = sY;
		playerPos[2] = sZ;
		player.notifyPositionChange();
	    }// end if(user start point)
	    System.out.println("Start position set to " + player.getPosition()[0]+" "+player.getPosition()[1]+" "+player.getPosition()[2]);
	    System.out.println("Setting sun vector");
	    final AbstractVector sunVector = lvl.getSunlightDirectionVector();
	    tr.getThreadManager().submitToGL(new Callable<Void>() {
		@Override
		public Void call() throws Exception {
		    tr.renderer.get().setSunVector(
			    new Vector3D(sunVector.getX(), -sunVector.getY(),
				    sunVector.getZ()).normalize());
		    return null;
		}
	    }).get();
	    System.out.println("\t...Done.");

	    System.out.println("\t...Done.");
	    System.out.println("Invoking JVM's garbage collector...");
	    System.gc();
	    System.out.println("\t...Ahh, that felt good.");
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (System.getProperties().containsKey(
		"org.jtrfp.trcl.flow.Mission.skipNavs")) {
	    try {
		final int skips = Integer.parseInt(System
			.getProperty("org.jtrfp.trcl.flow.Mission.skipNavs"));
		System.out.println("Skipping " + skips + " navs.");
		for (int i = 0; i < skips; i++) {
		    removeNAVObjective(currentNAVObjective());
		}// end for(skips)
	    } catch (NumberFormatException e) {
		System.err
			.println("Invalid format for property \"org.jtrfp.trcl.flow.Mission.skipNavs\". Must be integer.");
	    }
	}// end if(containsKey)
	System.out.println("Mission.go() complete.");
	// Transition to gameplay mode.
	game.getUpfrontDisplay().removePersistentMessage();
	game.getBackdropSystem().overworldMode();
	game.getBackdropSystem().activate();
	game.getBriefingScreen().briefingSequence(lvl);
	tr.getWorld().setFogColor(overworldSystem.getFogColor());
	game.getNavSystem()	.activate();
	game.setDisplayMode(game.gameplayMode);
	game.getPlayer()	.setActive(true);
	//Wait for mission end
	synchronized(missionEnd){
	while(missionEnd[0]==null){try{missionEnd.wait();}
		catch(InterruptedException e){break;}}}
	//Completion summary
	if(missionEnd[0]!=null)
	    game.getBriefingScreen().missionCompleteSummary(lvl,missionEnd[0]);
	return missionEnd[0];
    }// end go()

    public NAVObjective currentNAVObjective() {
	if (navs.isEmpty())
	    return null;
	return navs.get(0);
    }//end currentNAVObjective()

    public void removeNAVObjective(NAVObjective o) {
	navs.remove(o);
	if (navs.size() == 0) {
	    missionCompleteSequence();
	} else
	    tr.getGame().getNavSystem().updateNAVState();
    }// end removeNAVObjective(...)

    public static class Result {
	private final int airTargetsDestroyed, groundTargetsDestroyed,foliageDestroyed;
	private final double tunnelsFoundPctNorm;

	public Result(int airTargetsDestroyed, int groundTargetsDestroyed, int foliageDestroyed, double tunnelsFoundPctNorm) {
	    this.airTargetsDestroyed	=airTargetsDestroyed;
	    this.groundTargetsDestroyed	=groundTargetsDestroyed;
	    this.foliageDestroyed	=foliageDestroyed;
	    this.tunnelsFoundPctNorm	=tunnelsFoundPctNorm;
	}//end constructor

	/**
	 * @return the airTargetsDestroyed
	 */
	public int getAirTargetsDestroyed() {
	    return airTargetsDestroyed;
	}

	/**
	 * @return the groundTargetsDestroyed
	 */
	public int getGroundTargetsDestroyed() {
	    return groundTargetsDestroyed;
	}

	/**
	 * @return the foliageDestroyed
	 */
	public int getFoliageDestroyed() {
	    return foliageDestroyed;
	}

	/**
	 * @return the tunnelsFoundPctNorm
	 */
	public double getTunnelsFoundPctNorm() {
	    return tunnelsFoundPctNorm;
	}
    }// end Result

    /**
     * @return the playerStartPosition
     */
    public double[] getPlayerStartPosition() {
	return playerStartPosition;
    }

    /**
     * @return the playerStartDirection
     */
    public ObjectDirection getPlayerStartDirection() {
	return playerStartDirection;
    }
    
    private void installTunnels(TDFFile tdf, LoadingProgressReporter reporter){
	TDFFile.Tunnel[] tuns = tdf.getTunnels();
	tuns = tuns == null?new TDFFile.Tunnel[0]:tuns;//Null means no tunnels.
	final LoadingProgressReporter[] reporters = reporter
		.generateSubReporters(tuns.length);
	if (tuns != null) {
	    int tIndex = 0;
	    // Build tunnels
	    for (TDFFile.Tunnel tun : tuns) {
		tr
		 .getReporter()
		  .report("org.jtrfp.trcl.TunnelInstaller.tunnel."
				+ tIndex + ".entrance", tun.getEntrance());
		tr
		 .getReporter()
		  .report("org.jtrfp.trcl.TunnelInstaller.tunnel."
				+ tIndex + ".exit", tun.getExit());
		newTunnel(tun,reporters[tIndex]);
		tIndex++;
	    }//end if(tuns!=null)
	}// end if(tuns!=null)
	totalNumTunnels = tunnelsRemaining.size();
    }//end installTunnels()

    private Tunnel newTunnel(org.jtrfp.trcl.file.TDFFile.Tunnel tun,
	    LoadingProgressReporter reporter) {
	final Tunnel result = new Tunnel(tr.getWorld(), tun, reporter);
	tunnelsRemaining.add(result);
	tunnels.put(tun.getTunnelLVLFile().toUpperCase(), result);
	return result;
    }

    public Tunnel getTunnelByFileName(String tunnelFileName) {
	return tunnels.get(tunnelFileName.toUpperCase());
    }

    public Tunnel getTunnelWhoseEntranceClosestTo(double xInLegacyUnits,
	    double yInLegacyUnits, double zInLegacyUnits) {
	Tunnel result = null;
	double closestDistance = Double.POSITIVE_INFINITY;
	for (Tunnel t : tunnels.values()) {
	    TDFFile.Tunnel src = t.getSourceTunnel();
	    final double distance = Math.sqrt(Math.pow((xInLegacyUnits - src
		    .getEntrance().getX()), 2)
		    + Math.pow((yInLegacyUnits - src.getEntrance().getY()), 2)
		    + Math.pow((zInLegacyUnits - src.getEntrance().getZ()), 2));
	    if (distance < closestDistance) {
		closestDistance = distance;
		result = t;
	    }
	}// end for(tunnels)
	return result;
    }// end getTunnelWhoseEntranceClosestTo(...)

    private void missionCompleteSequence() {
	new Thread() {
	    @Override
	    public void run() {
		// TODO: Behavior change: Camera XZ static, lag Y by ~16
		// squares, heading/top affix toward player
		// TODO: Turn off all player control behavior
		// TODO: Behavior change: Player turns upward, top rolls on
		// heading, speed at full throttle
		// TODO: Wait 3 seconds
		// TODO: Lightning shell on
		// TODO: Wait 1 second
		// TODO: Turbo forward
		// TODO: Wait 500ms
		// TODO: Jet thrust noise
		// TODO: Player invisible.
		System.out.println("MISSION COMPLETE.");
		notifyMissionEnd(
			new Result(
				airTargetsDestroyed,
				groundTargetsDestroyed,
				foliageDestroyed,
				1.-(double)tunnelsRemaining.size()/(double)totalNumTunnels));
	    }// end run()
	}.start();
    }//end missionCompleteSequence()

    public void playerDestroyed() {
	new Thread() {
	    @Override
	    public void run() {
		// TODO Behavior change: Camera XYZ static, heading/top affix
		// toward player
		// TODO: Turn off all player control behavior
		// TODO Player behavior change: Slow spin along heading axis,
		// slow downward drift of heading
		// TODO: Add behavior: explode and destroy on impact with ground
		System.out.println("MISSION FAILED.");
		notifyMissionEnd(null);
	    }// end run()
	}.start();
    }// end playerDestroyed()
    
    private void notifyMissionEnd(Result r){
	synchronized(missionEnd){
	 missionEnd[0]=r;
	 missionEnd.notifyAll();}
    }//end notifyMissionEnd()

    public List<NAVObjective> getRemainingNAVObjectives() {
	return navs;
    }

    /**
     * @return the navSubObjects
     */
    public List<NAVSubObject> getNavSubObjects() {
	return navSubObjects;
    }

    /**
     * @param navSubObjects
     *            the navSubObjects to set
     */
    public void setNavSubObjects(List<NAVSubObject> navSubObjects) {
	this.navSubObjects = navSubObjects;
    }

    public void missionComplete() {
	missionCompleteSequence();
    }

    public OverworldSystem getOverworldSystem() {
	return overworldSystem;
    }
    
    public Mission notifyAirTargetDestroyed(){
	airTargetsDestroyed++;
	return this;
    }
    
    public Mission notifyGroundTargetDestroyed(){
	groundTargetsDestroyed++;
	return this;
    }
    
    public Mission notifyTunnelFound(Tunnel tun){
	tunnelsRemaining.remove(tun);
	return this;
    }
    
    public Mission notifyFoliageDestroyed(){
	foliageDestroyed++;
	return this;
    }
}// end Mission
