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

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.SkySystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.AbstractTriplet;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.LoadingProgressReporter.UpdateHandler;
import org.jtrfp.trcl.flow.NAVObjective.Factory;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PortalExit;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.GPUResidentMOD;
import org.jtrfp.trcl.snd.MusicPlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;

public class Mission {
    // PROPERTIES
    public static final String MISSION_MODE = "missionMode";
    public static final String SATELLITE_VIEW = "satelliteView";
    
    private final TR 		tr;
    private final List<NAVObjective> 
    				navs	= new LinkedList<NAVObjective>();
    private final LVLFile 	lvl;
    private final HashMap<String, Tunnel> 
    				tunnels = new HashMap<String, Tunnel>();
    private final HashMap<Integer, PortalExit>
    				tunnelPortals = new HashMap<Integer, PortalExit>();
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
    private final boolean	showIntro;
    private volatile MusicPlaybackEvent
    				bgMusic;
    private final Object	missionLock = new Object();
    private final Map<Integer,TunnelEntranceObject>
    				tunnelMap = new HashMap<Integer,TunnelEntranceObject>();
    private boolean 		bossFight = false, satelliteView = false;
    private MissionMode		missionMode = new Mission.LoadingMode();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Tunnel		currentTunnel;

    private enum LoadingStages {
	navs, tunnels, overworld
    }// end LoadingStages
    
    public Mission(TR tr, Game game, LVLFile lvl, String levelName, boolean showIntro) {
	this.tr 	= tr;
	this.lvl 	= lvl;
	this.game 	= game;
	this.levelName 	= levelName;
	this.showIntro	= showIntro;
    }// end Mission
    /*
    public TRFutureTask<Result> go(){
	synchronized(missionTask){
	 missionTask[0] = tr.getThreadManager().submitToThreadPool(new Callable<Result>(){
	    @Override
	    public Result call() throws Exception {
		return _go();
	    }});
	}//end sync{}
	return missionTask[0];
    }//end go()
*/
    public Result go() {
	setMissionMode(new Mission.LoadingMode());
	synchronized(missionLock){
	synchronized(missionEnd){
	    if(missionEnd[0]!=null)
		return missionEnd[0]; 
	}
	tr.getThreadManager().setPaused(true);
	for(ProjectileFactory pf:tr.getResourceManager().getProjectileFactories())
	    for(Projectile proj:pf.getProjectiles())
		proj.destroy();
	System.out.println("Starting GampeplayLevel loading sequence...");
	final LoadingProgressReporter rootProgress = LoadingProgressReporter.Impl
		.createRoot(new UpdateHandler() {
		    @Override
		    public void update(double unitProgress) {
			game.getLevelLoadingScreen().setLoadingProgress(unitProgress);
		    }
		});
	final LoadingProgressReporter[] progressStages = rootProgress
		.generateSubReporters(LoadingStages.values().length);
	final Renderer renderer = tr.mainRenderer.get();
	renderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	renderer.getSkyCube().setSkyCubeGen(GameShell.DEFAULT_GRADIENT);
	final Camera camera = renderer.getCamera();
	camera.setHeading(Vector3D.PLUS_I);
	camera.setTop(Vector3D.PLUS_J);
	game.setDisplayMode(game.levelLoadingMode);
	game.getUpfrontDisplay().submitPersistentMessage(levelName);
	try {
	    final ResourceManager rm = tr.getResourceManager();
	    final Player player      = tr.getGame().getPlayer();
	    final World world 	     = tr.getWorld();
	    final TDFFile tdf 	     = rm.getTDFData(lvl.getTunnelDefinitionFile());
	    player.setActive(false);
	    // Abort check
	    synchronized(missionEnd){
		if(missionEnd[0]!=null)
		return missionEnd[0]; 
	    }
	    
	    overworldSystem = new OverworldSystem(tr,
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
	    playerStartPosition[2] = TR.legacy2Modern(l3d.getX());
	    final double HEIGHT_PADDING = 10000;
	    playerStartPosition[1] = Math.max(HEIGHT_PADDING + (world.sizeY/2) * getOverworldSystem().getAltitudeMap().heightAt(
		    TR.legacy2MapSquare(l3d.getZ()),
		    TR.legacy2MapSquare(l3d.getX())),TR.legacy2Modern(l3d.getY()));
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
	    player.resetVelocityRotMomentum();
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
	    final AbstractTriplet sunVector = lvl.getSunlightDirectionVector();
	    tr.getThreadManager().submitToGL(new Callable<Void>() {
		@Override
		public Void call() throws Exception {
		    tr.mainRenderer.get().setSunVector(
			    new Vector3D(sunVector.getX(), sunVector.getY(),
				    sunVector.getZ()).normalize());
		    return null;
		}
	    }).get();
	    System.out.println("\t...Done.");
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
	//System.out.println("Invoking JVM's garbage collector...");
	//TR.nuclearGC();
	//System.out.println("Mission.go() complete.");
	// Transition to gameplay mode.
	// Abort check
	synchronized (missionEnd) {
	    if (missionEnd[0] != null)
		return missionEnd[0];
	}//end sync(missionEnd)
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		final SoundSystem ss = Mission.this.tr.soundSystem.get();
		MusicPlaybackEvent evt;
		Mission.this.tr.soundSystem.get().enqueuePlaybackEvent(
			evt =ss
				.getMusicFactory()
				.create(new GPUResidentMOD(tr, tr
					.getResourceManager().getMOD(
						lvl.getBackgroundMusicFile())),
					 true));
		synchronized(Mission.this){
		 if(bgMusic!=null)
		  return null;
		 bgMusic=evt;
		 bgMusic.play();
		 }//end sync(Mission.this)
		return null;
	    }// end call()
	});
	game.getUpfrontDisplay().removePersistentMessage();
	tr.getThreadManager().setPaused(false);
	if(showIntro){
	    setMissionMode(new Mission.IntroMode());
	    game.getBriefingScreen().briefingSequence(lvl);
	}
	setMissionMode(new Mission.AboveGroundMode());
	getOverworldSystem().activate();
	final SkySystem skySystem = getOverworldSystem().getSkySystem();
	tr.mainRenderer.get().getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	renderer.getSkyCube().setSkyCubeGen(skySystem.getBelowCloudsSkyCubeGen());
	renderer.setAmbientLight(skySystem.getSuggestedAmbientLight());
	renderer.setSunColor(skySystem.getSuggestedSunColor());
	game.getNavSystem()	.activate();
	game.setDisplayMode(game.gameplayMode);
	
	game.getPlayer()	.setActive(true);
	tr.getGame().setPaused(false);
	//Wait for mission end
	synchronized(missionEnd){
	 while(missionEnd[0]==null){try{missionEnd.wait();}
		catch(InterruptedException e){break;}}}
	//Completion summary
	if(missionEnd[0]!=null)
	    if(!missionEnd[0].isAbort()){
		setMissionMode(new Mission.MissionSummaryMode());
		game.getBriefingScreen().missionCompleteSummary(lvl,missionEnd[0]);
	    }//end if(proper ending)
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		bgMusic.stop();
		return null;
	    }// end call()
	});
	cleanup();
	return missionEnd[0];
	}//end sync
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
	private boolean abort=false;

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

	/**
	 * @return the abort
	 */
	public boolean isAbort() {
	    return abort;
	}

	/**
	 * @param abort the abort to set
	 */
	public void setAbort(boolean abort) {
	    this.abort = abort;
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
	final Tunnel result = new Tunnel(tr, tun, reporter);
	DirectionVector v = tun.getEntrance();
	tunnelsRemaining.add(result);
	final Point point = new Point(
		(int)(TR.legacy2MapSquare(v.getZ())),
		(int)(TR.legacy2MapSquare(v.getX())));
	addTunnelEntrance(point,result);
	final PortalExit portalExit = getTunnelEntrancePortal(point);//TODO: Returning null
	portalExit.setPosition(Tunnel.TUNNEL_START_POS.toArray());
	portalExit.getControlledCamera();//TODO: Add tunnel to camera
	tunnels.put(tun.getTunnelLVLFile().toUpperCase(), result);
	return result;
    }

    public Tunnel getTunnelByFileName(String tunnelFileName) {
	return tunnels.get(tunnelFileName.toUpperCase());
    }

    public TunnelEntranceObject getNearestTunnelEntrance(double xInLegacyUnits,
	    double yInLegacyUnits, double zInLegacyUnits) {
	TunnelEntranceObject result = null;
	double closestDistance = Double.POSITIVE_INFINITY;
	final Vector3D entPos = new Vector3D(
		    TR.legacy2Modern(zInLegacyUnits),//Intentionally backwards
		    TR.legacy2Modern(yInLegacyUnits),
		    TR.legacy2Modern(xInLegacyUnits)
		    );
	System.out.println("Requested entry pos="+entPos);
	for (TunnelEntranceObject teo : tunnelMap.values()) {
	    final Vector3D pos = new Vector3D(teo.getPosition());
	    System.out.println("Found tunnel at "+pos);
	    final double distance = pos.distance(entPos);
	    if (distance < closestDistance) {
		closestDistance = distance;
		result = teo;
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
    
    public void enterBossMode(final String bossMusicFile){
	setBossFight(true);
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		MusicPlaybackEvent evt;
		final SoundSystem ss = Mission.this.tr.soundSystem.get();
		Mission.this.tr.soundSystem.get().enqueuePlaybackEvent(
			evt =ss
				.getMusicFactory()
				.create(tr.getResourceManager().gpuResidentMODs.get(bossMusicFile),
					 true));
		synchronized(Mission.this){
		 evt.play();
		 if(bgMusic!=null)
		  bgMusic.stop();
		 bgMusic=evt;
		}
		return null;
	    }// end call()
	});
    }//end enterBossMode()
    
    public void exitBossMode(){
	setBossFight(false);
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		MusicPlaybackEvent evt;
		final SoundSystem ss = Mission.this.tr.soundSystem.get();
		Mission.this.tr.soundSystem.get().enqueuePlaybackEvent(
			evt =ss
				.getMusicFactory()
				.create(tr.getResourceManager().gpuResidentMODs.get(lvl.getBackgroundMusicFile()),
					 true));
		synchronized(Mission.this){
		 evt.play();
		 bgMusic.stop();
		 bgMusic=evt;}
		return null;
	    }// end call()
	});
    }//end exitBossMode()

    public void abort() {
	final Result result = new Result(
		airTargetsDestroyed,
		groundTargetsDestroyed,
		foliageDestroyed,
		1.-(double)tunnelsRemaining.size()/(double)totalNumTunnels);
	result.setAbort(true);
	notifyMissionEnd(result);
	//Wait for mission to end
	synchronized(missionLock){//Don't execute while mission is in progress.
	    cleanup();
	}//end sync{}
    }//end abort()

    private void cleanup() {
	if(overworldSystem!=null)
	    overworldSystem.deactivate();
    }
    /**
     * Find a tunnel at the given map square, if any.
     * @param mapSquareXZ Position in cells, not world coords.
     * @return The Tunnel at this map square, or null if none here.
     * @since Jan 13, 2015
     */
    public TunnelEntranceObject getTunnelEntranceObject(Point mapSquareXZ){
	final int key = pointToHash(mapSquareXZ);
	System.out.println("getTunnelEntranceObject "+mapSquareXZ);
	for(TunnelEntranceObject teo:tunnelMap.values())
	    System.out.print(" "+new Vector3D(teo.getPosition()).scalarMultiply(1/TR.mapSquareSize));
	System.out.println();
	return tunnelMap.get(key);
    }
    
    public void registerTunnelEntrancePortal(Point mapSquareXZ, PortalExit exit){
	tunnelPortals.put(pointToHash(mapSquareXZ),exit);
    }
    
    PortalExit getTunnelEntrancePortal(Point mapSquareXZ){
	return tunnelPortals.get(pointToHash(mapSquareXZ));
    }
    
    public void addTunnelEntrance(Point mapSquareXZ, Tunnel tunnel){
	TunnelEntranceObject teo;
	overworldSystem.add(teo = new TunnelEntranceObject(tr,tunnel));
	tunnelMap.put(pointToHash(mapSquareXZ),teo);
    }
    
    private int pointToHash(Point point){
	final int key =(int)point.getX()+(int)point.getY()*65536;
	return key;
    }
    
    public void enterTunnel(final Tunnel tunnel) {
	final Game game = tr.getGame();
	final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
	currentTunnel = tunnel;
	game.getCurrentMission().notifyTunnelFound(tunnel);
	setMissionMode(new TunnelMode());
	World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		//Turn on tunnel
		tunnel.activate();
		//Turn off overworld
		overworldSystem.deactivate();
	    }});
	//Move player to tunnel
	tr.mainRenderer.get().getSkyCube().setSkyCubeGen(Tunnel.TUNNEL_SKYCUBE_GEN);
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
	player.setActive(false);
	player.resetVelocityRotMomentum();
	final Behavior playerBehavior = player.getBehavior();
	playerBehavior.probeForBehavior(CollidesWithTunnelWalls.class).setEnable(true);
	playerBehavior.probeForBehavior(MovesByVelocity.class).setVelocity(Vector3D.ZERO);
	playerBehavior.probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
	playerBehavior.probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
	playerBehavior.probeForBehavior(CollidesWithTerrain.class).setEnable(false);
	//entranceObject.getBehavior().probeForBehaviors(TELsubmitter, TunnelEntryListener.class);
	tunnel.dispatchTunnelEntryNotifications();
	player.setPosition(Tunnel.TUNNEL_START_POS.toArray());
	player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
	player.notifyPositionChange();
	/*
	final NAVObjective navObjective = getNavObjectiveToRemove();
	if(navObjective!=null && navTargeted){
	    final Mission m = game.getCurrentMission();
	    if(!(onlyRemoveIfCurrent&&navObjective!=m.currentNAVObjective()))m.removeNAVObjective(navObjective);
	}//end if(have NAV to remove
	*/
	player.setActive(true);
    }//end enterTunnel()
    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }
    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }
    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }
    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }
    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }
    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }
    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }
    
    public static interface MissionMode{}
    public static class LoadingMode implements MissionMode{}
    
    public static class BriefingMode implements MissionMode{}
    public static class IntroMode extends BriefingMode{}
    public static class EnemyIntroMode extends IntroMode{}
    public static class PlanetIntroMode extends IntroMode{}
    public static class MissionSummaryMode extends BriefingMode{}
    
    public static class GameplayMode implements MissionMode{}
    public static class TunnelMode extends GameplayMode{}
    public static class ChamberMode extends GameplayMode{}
    public static class AboveGroundMode extends GameplayMode{}

    /**
     * @return the missionMode
     */
    public MissionMode getMissionMode() {
        return missionMode;
    }
    /**
     * @param missionMode the missionMode to set
     */
    public void setMissionMode(MissionMode missionMode) {
	pcs.firePropertyChange(MISSION_MODE, this.missionMode, missionMode);
        this.missionMode = missionMode;
    }
    /**
     * @return the bossFight
     */
    public boolean isBossFight() {
        return bossFight;
    }
    /**
     * @param bossFight the bossFight to set
     */
    public void setBossFight(boolean bossFight) {
	pcs.firePropertyChange("bossFight", this.bossFight, bossFight);
        this.bossFight = bossFight;
    }
    public void setSatelliteView(boolean satelliteView) {
	if(!(getMissionMode() instanceof AboveGroundMode)&&satelliteView)
	    throw new IllegalArgumentException("Cannot activate satellite view while mission mode is "+getMissionMode().getClass().getSimpleName());
	if(satelliteView && tr.getGame().isPaused())
	    throw new IllegalArgumentException("Cannot activate satellite view while paused.");
	pcs.firePropertyChange(SATELLITE_VIEW, this.satelliteView, satelliteView);
	if(satelliteView!=this.satelliteView){
	    final Game game =  tr.getGame();
	    final Camera cam = tr.mainRenderer.get().getCamera();
	    if(satelliteView){//Switched on
		tr.getThreadManager().setPaused(true);
		game.getNavSystem().deactivate();
		game.getHUDSystem().deactivate();
		cam.setFogEnabled(false);
		cam.probeForBehavior(MatchPosition.class).setEnable(false);
		cam.probeForBehavior(MatchDirection.class).setEnable(false);
		final Vector3D pPos = new Vector3D(game.getPlayer().getPosition());
		final Vector3D pHeading = tr.getGame().getPlayer().getHeading();
		cam.setPosition(new Vector3D(pPos.getX(),TR.mapSquareSize*25,pPos.getZ()));
		cam.setHeading(Vector3D.MINUS_J);
		cam.setTop(new Vector3D(pHeading.getX(),.0000000001,pHeading.getZ()).normalize());
		tr.getGame().getSatDashboard().setVisible(true);
	    }else{//Switched off
		tr.getThreadManager().setPaused(false);
		tr.getGame().getNavSystem().activate();
		game.getHUDSystem().activate();
		cam.setFogEnabled(true);
		cam.probeForBehavior(MatchPosition.class).setEnable(true);
		cam.probeForBehavior(MatchDirection.class).setEnable(true);
		tr.getGame().getSatDashboard().setVisible(false);
	    }//end !satelliteView
	}//end if(change)
	this.satelliteView=satelliteView;
    }
    /**
     * @return the satelliteView
     */
    public boolean isSatelliteView() {
	System.out.println("isSatelliteView="+satelliteView);
        return satelliteView;
    }
    public Tunnel getCurrentTunnel() {
	if(!(getMissionMode() instanceof TunnelMode))return null;
	return currentTunnel;
    }
}// end Mission
