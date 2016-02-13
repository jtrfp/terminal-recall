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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.DisplayModeHandler;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.SkySystem;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.beh.SpawnsRandomSmoke;
import org.jtrfp.trcl.beh.SpinCrashDeathBehavior;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.AbstractTriplet;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.miss.LoadingProgressReporter.UpdateHandler;
import org.jtrfp.trcl.miss.NAVObjective.Factory;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PortalEntrance;
import org.jtrfp.trcl.obj.PortalExit;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.SpawnsRandomExplosionsAndDebris;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShell;
import org.jtrfp.trcl.snd.GPUResidentMOD;
import org.jtrfp.trcl.snd.MusicPlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;

public class Mission {
    // PROPERTIES
    //public static final String MISSION_MODE = "missionMode";
    public static final String SATELLITE_VIEW = "satelliteView";
    public static final String CURRENT_NAV_TARGET = "currentNavTarget";
    
    private final TR 		tr;
    private final List<NAVObjective> 
    				navs	= new LinkedList<NAVObjective>();
    private final LVLFile 	lvl;
    private final HashMap<String, Tunnel> 
    				tunnels = new HashMap<String, Tunnel>();
    private final HashMap<Integer, PortalEntrance>
    				tunnelPortals = new HashMap<Integer, PortalEntrance>();
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
    //private MissionMode		missionMode = new Mission.LoadingMode();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Tunnel		currentTunnel;
    private final DisplayModeHandler displayHandler;
    public Object [] levelLoadingMode, tunnelMode, overworldMode, gameplayMode, briefingMode, summaryMode, emptyMode=  new Object[]{};
    private NAVObjective currentNavTarget;
    private final RenderableSpacePartitioningGrid tunnelGrid       = new RenderableSpacePartitioningGrid();
    private final RenderableSpacePartitioningGrid partitioningGrid = new RenderableSpacePartitioningGrid();

    private enum LoadingStages {
	navs, tunnels, overworld
    }// end LoadingStages
    
    //ROOT STATES
    public interface MissionState       extends Game.GameRunningMode{}
    public interface ConstructingState  extends MissionState{}
    public interface ConstructedState   extends MissionState{}
    public interface ActiveMissionState extends ConstructedState{}
    
     public interface LoadingState      extends ActiveMissionState{}
     public interface GameplayState     extends ActiveMissionState{}
      public interface Briefing        extends GameplayState{}
       public interface PlanetBrief     extends Briefing{}
       public interface EnemyBrief      extends Briefing{}
       public interface MissionSummary  extends Briefing{}
      public interface PlayerActivity  extends GameplayState{}
       public interface OverworldState  extends PlayerActivity{}
       public interface SatelliteState   extends OverworldState{}
       public interface ChamberState    extends OverworldState{}
       public interface TunnelState     extends PlayerActivity{}
    
    public Mission(TR tr, Game game, LVLFile lvl, String levelName, boolean showIntro) {
	this.tr 	= tr;
	this.lvl 	= lvl;
	this.game 	= game;
	this.levelName 	= levelName;
	this.showIntro	= showIntro;
	this.displayHandler = new DisplayModeHandler(this.getPartitioningGrid());
	Features.init(this);
	tr.setRunState(new ConstructingState(){});
	levelLoadingMode = new Object[]{
		 ((TVF3Game)game).levelLoadingScreen,
		 ((TVF3Game)game).upfrontDisplay
	    };
	tr.setRunState(new ConstructedState(){});
    }// end Mission
    
    public Result go() {
	tr.setRunState(new LoadingState(){});
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
			((TVF3Game)game).getLevelLoadingScreen().setLoadingProgress(unitProgress);
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
	((TVF3Game)game).levelLoadingMode();
	displayHandler.setDisplayMode(levelLoadingMode);
	((TVF3Game)game).getUpfrontDisplay().submitPersistentMessage(levelName);
	try {
	    final ResourceManager rm = tr.getResourceManager();
	    final Player player      = ((TVF3Game)tr.getGame()).getPlayer();
	    final TDFFile tdf 	     = rm.getTDFData(lvl.getTunnelDefinitionFile());
	    player.setActive(false);
	    // Abort check
	    synchronized(missionEnd){
		if(missionEnd[0]!=null)
		return missionEnd[0]; 
	    }
	    
	    overworldSystem = new OverworldSystem(tr,
		    progressStages[LoadingStages.overworld.ordinal()]);
	    briefingMode = new Object[]{
			 ((TVF3Game)game).briefingScreen,
			 overworldSystem
		    };
	    gameplayMode = new Object[]{
			 ((TVF3Game)game).upfrontDisplay,
			 rm.getDebrisSystem(),
			 rm.getPowerupSystem(),
			 rm.getProjectileFactories(),
			 rm.getExplosionFactory(),
			 rm.getSmokeSystem()
		    };
	    overworldMode = new Object[]{
		    gameplayMode,
		    overworldSystem
	    };
	    tunnelMode = new Object[]{
			 ((TVF3Game)game).upfrontDisplay,
			 rm.getDebrisSystem(),
			 rm.getPowerupSystem(),
			 rm.getProjectileFactories(),
			 rm.getExplosionFactory(),
			 rm.getSmokeSystem(),
			 tunnelGrid
		};
	    summaryMode = new Object[]{
		    ((TVF3Game)game).getBriefingScreen(),
		    overworldSystem
	    };
	    getOverworldSystem().loadLevel(lvl, tdf);
	    System.out.println("\t...Done.");
	    // Install NAVs
	    final NAVSystem navSystem = ((TVF3Game)tr.getGame()).getNavSystem();
	    navSubObjects = rm.getNAVData(lvl.getNavigationFile())
		    .getNavObjects();

	    START s = (START) navSubObjects.get(0);
	    Location3D l3d = s.getLocationOnMap();
	    playerStartPosition[0] = TR.legacy2Modern(l3d.getZ());
	    playerStartPosition[2] = TR.legacy2Modern(l3d.getX());
	    final double HEIGHT_PADDING = 10000;
	    playerStartPosition[1] = Math.max(HEIGHT_PADDING + getOverworldSystem().getAltitudeMap().heightAt(
		    TR.legacy2Modern(l3d.getZ()),
		    TR.legacy2Modern(l3d.getX())),TR.legacy2Modern(l3d.getY()));
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
	    player.probeForBehavior(CollidesWithTerrain.class)   .setEnable(true);
	    if(player.hasBehavior(SpawnsRandomSmoke.class))
	     player.probeForBehavior(SpawnsRandomSmoke.class)    .setEnable(false);
	    if(player.hasBehavior(SpawnsRandomExplosionsAndDebris.class))
	     player.probeForBehavior(SpawnsRandomExplosionsAndDebris.class).setEnable(false);
	    player.probeForBehavior(SpinCrashDeathBehavior.class).setEnable(false);
	    
	    installTunnels(tdf,progressStages[LoadingStages.tunnels.ordinal()]);
	    Factory f = new NAVObjective.Factory(tr, getLevelName());

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
	// Transition to gameplay mode.
	// Abort check
	synchronized (missionEnd) {
	    if (missionEnd[0] != null)
		return missionEnd[0];
	}//end sync(missionEnd)
	
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
		 if(bgMusic==null){
		  bgMusic=evt;
		  bgMusic.play();   
		 }
		 
		 }//end sync(Mission.this)
	((TVF3Game)game).getUpfrontDisplay().removePersistentMessage();
	tr.soundSystem.get().setPaused(false);
	tr.getThreadManager().setPaused(false);
	if(showIntro){
	    tr.setRunState(new EnemyBrief(){});
	    //setMissionMode(new Mission.IntroMode());
	    displayHandler.setDisplayMode(briefingMode);
	    ((TVF3Game)game).getBriefingScreen().briefingSequence(lvl);//TODO: Convert to feature
	}
	tr.setRunState(new OverworldState(){});
	final SkySystem skySystem = getOverworldSystem().getSkySystem();
	tr.mainRenderer.get().getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	renderer.getSkyCube().setSkyCubeGen(skySystem.getBelowCloudsSkyCubeGen());
	renderer.setAmbientLight(skySystem.getSuggestedAmbientLight());
	renderer.setSunColor(skySystem.getSuggestedSunColor());
	((TVF3Game)game).getNavSystem() .activate();
	displayHandler.setDisplayMode(overworldMode);
	
	((TVF3Game)game).getPlayer()	.setActive(true);
	((TVF3Game)tr.getGame()).setPaused(false);
	tr.setRunState(new PlayerActivity(){});
	//Wait for mission end
	synchronized(missionEnd){
	 while(missionEnd[0]==null){try{missionEnd.wait();}
		catch(InterruptedException e){break;}}}
	//Completion summary
	if(missionEnd[0]!=null)
	    if(!missionEnd[0].isAbort()){
		displayHandler.setDisplayMode(summaryMode);
		tr.setRunState(new MissionSummary(){});
		((TVF3Game)game).getBriefingScreen().missionCompleteSummary(lvl,missionEnd[0]);
	    }//end if(proper ending)
	bgMusic.stop();
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
	updateNavState();
	((TVF3Game)tr.getGame()).getNavSystem().updateNAVState();
    }// end removeNAVObjective(...)
    
    private void updateNavState(){
	try{this.setCurrentNavTarget(navs.get(0));}
	catch(IndexOutOfBoundsException e){setCurrentNavTarget(null);}
    }

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
				+ tIndex + ".entrance", tun.getEntrance().toString());
		tr
		 .getReporter()
		  .report("org.jtrfp.trcl.TunnelInstaller.tunnel."
				+ tIndex + ".exit", tun.getExit().toString());
		newTunnel(tun,reporters[tIndex]);
		tIndex++;
	    }//end if(tuns!=null)
	}// end if(tuns!=null)
	totalNumTunnels = tunnelsRemaining.size();
    }//end installTunnels()

    private Tunnel newTunnel(org.jtrfp.trcl.file.TDFFile.Tunnel tdfTun,
	    LoadingProgressReporter reporter) {
	final Tunnel tunnel = new Tunnel(tr, tdfTun, reporter, tdfTun.getTunnelLVLFile());
	tunnelsRemaining.add(tunnel);
	DirectionVector tunnelEntranceLegacyPos = tdfTun.getEntrance();
	final Point tunnelEntranceMapSquarePos = new Point(
		(int)(TR.legacy2MapSquare(tunnelEntranceLegacyPos.getZ())),
		(int)(TR.legacy2MapSquare(tunnelEntranceLegacyPos.getX())));
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
		(int)(TR.legacy2MapSquare(tunnelExitLegacyPos.getZ())),
		(int)(TR.legacy2MapSquare(tunnelExitLegacyPos.getX())));
	System.out.println("Tunnel exit at sector "+tunnelExitMapSquarePos);
	//portalExit = getTunnelEntrancePortal(tunnelExitMapSquarePos);
	
	/*if(portalExit!=null){
	 portalExit.setHeading(tunnel.getExitObject().getHeading().negate());
	 portalExit.setTop(tunnel.getExitObject().getTop());
	 portalExit.setPosition(tunnel.getExitObject().getPosition());
	 portalExit.notifyPositionChange();
	 portalExit.setRootGrid(tunnel);
	}else System.err.println("Null exit.");*/
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

    public void playerDestroyed() {
	new Thread() {
	    @Override
	    public void run() {
		System.out.println("MISSION FAILED.");
		notifyMissionEnd(null);
	    }// end run()
	}.start();
    }// end playerDestroyed()
    
    public void notifyMissionEnd(Result r){
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
	displayHandler.setDisplayMode(emptyMode);
	tr.getResourceManager().getPowerupSystem().removeAll();
	// Remove projectile factories
	for(ProjectileFactory pf:tr.getResourceManager().getProjectileFactories())
	    for(Projectile projectile:pf.getProjectiles())
		projectile.destroy();
    }//end cleanup()
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
    
    public void registerTunnelEntrancePortal(Point mapSquareXZ, PortalEntrance entrance){
	synchronized(tunnelPortals){
	 tunnelPortals.put(pointToHash(mapSquareXZ),entrance);}
    }
    
    PortalEntrance getTunnelEntrancePortal(Point mapSquareXZ){
	synchronized(tunnelPortals){
	 return tunnelPortals.get(pointToHash(mapSquareXZ));}
    }
    
    public void addTunnelEntrance(Point mapSquareXZ, Tunnel tunnel, PortalEntrance entrance){
	TunnelEntranceObject teo;
	overworldSystem.add(teo = new TunnelEntranceObject(tr,tunnel,entrance));
	tunnelMap.put(pointToHash(mapSquareXZ),teo);
    }
    
    private int pointToHash(Point point){
	final int key =(int)point.getX()+(int)point.getY()*65536;
	return key;
    }
    
    public synchronized void enterTunnel(final TunnelEntranceObject teo) {
	final Tunnel tunnelToEnter = teo.getSourceTunnel();
	System.out.println("Entering tunnel "+tunnelToEnter);
	final Game game = ((TVF3Game)tr.getGame());
	final OverworldSystem overworldSystem = ((TVF3Game)game).getCurrentMission().getOverworldSystem();
	
	assert tunnelToEnter.getExitObject().getPosition()[0]>0:""+tunnelToEnter.getExitObject().getPosition()[0];//TODO: Remove
	
	((TVF3Game)game).getCurrentMission().notifyTunnelFound(tunnelToEnter);
	
	//tr.getDefaultGrid().nonBlockingAddBranch(tunnel);
	//tr.getDefaultGrid().blockingRemoveBranch(overworldSystem);
	
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
		probeForBehavior(LoopingPositionBehavior.class).
		setEnable(false);
	    }//end for(projectiles)
	}//end for(projectileFactories)
	final Player player = ((TVF3Game)tr.getGame()).getPlayer();
	player.setActive(false);
	player.resetVelocityRotMomentum();
	player.probeForBehavior(CollidesWithTunnelWalls.class).setEnable(true);
	player.probeForBehavior(MovesByVelocity.class)        .setVelocity(Vector3D.ZERO);
	player.probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
	//player.probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
	player.probeForBehavior(CollidesWithTerrain.class)    .setEnable(false);
	tunnelToEnter.dispatchTunnelEntryNotifications();
	final Renderer portalRenderer = teo.getPortalEntrance().getPortalRenderer();
	final Camera secondaryCam = /*tr.secondaryRenderer.get().getCamera()*/portalRenderer.getCamera();
	//player.setPosition(Tunnel.TUNNEL_START_POS.toArray());//TODO: remove debug code
	player.setPosition(secondaryCam.getPosition());
	player.setHeading (secondaryCam.getHeading());
	player.setTop     (secondaryCam.getTop());
	player.notifyPositionChange();
	//Move the secondary cam to the overworld.
	overworldSystem.setChamberMode(tunnelToEnter.getExitObject().isMirrorTerrain());
	//Set the skycube appropriately
	portalRenderer.getSkyCube().setSkyCubeGen(((TVF3Game)tr.getGame()).
		      getCurrentMission().
		      getOverworldSystem().
		      getSkySystem().
		      getBelowCloudsSkyCubeGen());
	
	tr.setRunState(new TunnelState(){});
	setCurrentTunnel(tunnelToEnter);
	tr.setRunState(new TunnelState(){});
	setDisplayMode(tunnelMode);
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
	if(!(tr.getRunState() instanceof OverworldState)&&satelliteView)
	    throw new IllegalArgumentException("Cannot activate satellite view while runState is "+tr.getRunState().getClass().getSimpleName());
	if(satelliteView && ((TVF3Game)tr.getGame()).isPaused())
	    throw new IllegalArgumentException("Cannot activate satellite view while paused.");
	final boolean oldValue = this.satelliteView;
	
	if(satelliteView!=oldValue){
	    final Game game =  ((TVF3Game)tr.getGame());
	    final Camera cam = tr.mainRenderer.get().getCamera();
	    this.satelliteView=satelliteView;
	    pcs.firePropertyChange(SATELLITE_VIEW, oldValue, satelliteView);
	    tr.setRunState(satelliteView?new SatelliteState(){}:new OverworldState(){});
	    if(satelliteView){//Switched on
		tr.getThreadManager().setPaused(true);
		World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			//getPartitioningGrid().removeBranch(((TVF3Game)game).getNavSystem());
			//getPartitioningGrid().removeBranch(((TVF3Game)game).getHUDSystem());
		    }});
		cam.setFogEnabled(false);
		cam.probeForBehavior(MatchPosition.class).setEnable(false);
		cam.probeForBehavior(MatchDirection.class).setEnable(false);
		final Vector3D pPos = new Vector3D(((TVF3Game)game).getPlayer().getPosition());
		final Vector3D pHeading = ((TVF3Game)tr.getGame()).getPlayer().getHeading();
		cam.setPosition(new Vector3D(pPos.getX(),TR.visibilityDiameterInMapSquares*TR.mapSquareSize*.65,pPos.getZ()));
		cam.setHeading(Vector3D.MINUS_J);
		cam.setTop(new Vector3D(pHeading.getX(),.0000000001,pHeading.getZ()).normalize());
		((TVF3Game)tr.getGame()).getSatDashboard().setVisible(true);
	    }else{//Switched off
		tr.getThreadManager().setPaused(false);
		World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			((TVF3Game)tr.getGame()).getNavSystem().activate();
			//getPartitioningGrid().addBranch(((TVF3Game)game).getNavSystem());
			//getPartitioningGrid().addBranch(((TVF3Game)game).getHUDSystem());
		    }});
		cam.setFogEnabled(true);
		cam.probeForBehavior(MatchPosition.class).setEnable(true);
		cam.probeForBehavior(MatchDirection.class).setEnable(true);
		((TVF3Game)tr.getGame()).getSatDashboard().setVisible(false);
	    }//end !satelliteView
	}//end if(change)
    }
    /**
     * @return the satelliteView
     */
    public boolean isSatelliteView() {
	System.out.println("isSatelliteView="+satelliteView);
        return satelliteView;
    }
    public Tunnel getCurrentTunnel() {
	if(!(tr.getRunState() instanceof TunnelState))return null;
	return currentTunnel;
    }
    
    /**
     * 
     * @param newTunnel
     * @return The old tunnel, or null if none.
     * @since Jan 23, 2016
     */
    public Tunnel setCurrentTunnel(final Tunnel newTunnel){
	final Tunnel oldTunnel = getCurrentTunnel();
	this.currentTunnel = newTunnel;
	World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		tunnelGrid.removeAll();
		//if(oldTunnel != null)
		//    tunnelGrid.removeBranch(oldTunnel);
		tunnelGrid.addBranch(newTunnel);
	    }});
	return oldTunnel;
    }//end setCurrentTunnel

    public Game getGame() {
	return game;
    }

    public void destruct() {
	Features.destruct(this);
    }

    public String getLevelName() {
        return levelName;
    }

    public int getGroundTargetsDestroyed() {
        return groundTargetsDestroyed;
    }

    public void setGroundTargetsDestroyed(int groundTargetsDestroyed) {
        this.groundTargetsDestroyed = groundTargetsDestroyed;
    }

    public int getAirTargetsDestroyed() {
        return airTargetsDestroyed;
    }

    public void setAirTargetsDestroyed(int airTargetsDestroyed) {
        this.airTargetsDestroyed = airTargetsDestroyed;
    }

    public int getFoliageDestroyed() {
        return foliageDestroyed;
    }

    public void setFoliageDestroyed(int foliageDestroyed) {
        this.foliageDestroyed = foliageDestroyed;
    }

    public Collection<Tunnel> getTunnelsRemaining() {
        return Collections.unmodifiableCollection(tunnelsRemaining);
    }

    public int getTotalNumTunnels() {
        return totalNumTunnels;
    }

    public void setTotalNumTunnels(int totalNumTunnels) {
        this.totalNumTunnels = totalNumTunnels;
    }

    public NAVObjective getCurrentNavTarget() {
        return currentNavTarget;
    }

    public Mission setCurrentNavTarget(NAVObjective newTarget) {
	final NAVObjective oldTarget = this.currentNavTarget;
	this.currentNavTarget = newTarget;
	pcs.firePropertyChange(CURRENT_NAV_TARGET, oldTarget, newTarget);
        return this;
    }

    public void setDisplayMode(Object [] newMode){//TODO: Refactor this to follow tr's run state instead
	displayHandler.setDisplayMode(newMode);
    }

    public RenderableSpacePartitioningGrid getPartitioningGrid() {
        return partitioningGrid;
    }
}// end Mission
