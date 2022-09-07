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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.DisplayModeHandler;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.beh.SpawnsRandomSmoke;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.lvl.LVLFileEnhancementsFactory.LVLFileEnhancements;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.START;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.miss.LoadingProgressReporter.UpdateHandler;
import org.jtrfp.trcl.miss.NAVObjective.Factory;
import org.jtrfp.trcl.miss.TunnelSystemFactory.TunnelSystem;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Player.PlayerSaveState;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.SpawnsRandomExplosionsAndDebris;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.snd.GPUResidentMOD;
import org.jtrfp.trcl.snd.MusicPlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.tools.Util;

public class Mission {
 // PROPERTIES
    public static final String LEVEL_NAME               = "levelName",
	                       //NAVS                     = "navs",
	                       NAV_SUB_OBJECTS          = "navSubObjects",
	                       GROUND_TARGETS_DESTROYED = "groundTargetsDestroyed",
	                       AIR_TARGETS_DESTROYED    = "airTargetsDestroyed",
	                       FOLIAGE_DESTROYED        = "foliageDestroyed",
	                       SHOW_INTRO               = "showIntro",
	                       CURRENT_NAV_TARGET       = "currentNavTarget",
	                       LVL_FILE_NAME            = "lvlFileName",
	                       SATELLITE_VIEW           = "satelliteView",
	                       PLAYER_START_POSITION    = "playerStartPosition",
	                       PLAYER_START_HEADING     = "playerStartHeading",
	                       PLAYER_START_TOP         = "playerStartTop",
	                       DEF_OBJECT_LIST          = "defObjectList",
	                       CURRENT_BOSS             = "currentBoss",
	                       PLAYER_SAVE_STATE        = "playerSaveState";
    //// INTROSPECTOR
    static {
	try{
	final Set<String> persistentProperties = new HashSet<String>();
	persistentProperties.addAll(Arrays.asList(
		LEVEL_NAME,
		//NAVS,
		NAV_SUB_OBJECTS,
		GROUND_TARGETS_DESTROYED,
		AIR_TARGETS_DESTROYED,
		FOLIAGE_DESTROYED,
		SHOW_INTRO,
		CURRENT_NAV_TARGET,
		LVL_FILE_NAME,
		PLAYER_START_POSITION,
		PLAYER_START_HEADING,
		PLAYER_START_TOP,
		DEF_OBJECT_LIST,
		PLAYER_SAVE_STATE
		));
	
	BeanInfo info = Introspector.getBeanInfo(Mission.class);
	PropertyDescriptor[] propertyDescriptors =
	                             info.getPropertyDescriptors();
	for (int i = 0; i < propertyDescriptors.length; ++i) {
	    PropertyDescriptor pd = propertyDescriptors[i];
	    if (!persistentProperties.contains(pd.getName())) {
	        pd.setValue("transient", Boolean.TRUE);
	    }
	}
	}catch(Exception e){e.printStackTrace();}
    }//end static{}
    
    private final TR 		tr;
    private final List<NAVObjective> 
    				navs	= new LinkedList<NAVObjective>();
    private LVLFile 	        lvl;
    private double[] 		playerStartPosition;
    private List<NAVSubObject> 	navSubObjects;
    //private double []           playerStartHeading,
    //                            playerStartTop;
    private Game 		game;
    private String       	levelName;
    private OverworldSystem 	overworldSystem;
    //private final Result[]	missionEnd = new Result[]{null};
    private int			groundTargetsDestroyed=0,
	    			airTargetsDestroyed=0,
	    			foliageDestroyed=0;
    private boolean	        showIntro = true;
    private volatile MusicPlaybackEvent
    				bgMusic;
    //private final Object	missionLock = new Object();
    private boolean 		satelliteView = false;
    private WorldObject         currentBoss = null;
    //private MissionMode		missionMode = new Mission.LoadingMode();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final DisplayModeHandler displayHandler;
    private Object [] levelLoadingMode, overworldMode, gameplayMode, briefingMode, summaryMode, emptyMode=  new Object[]{};
    private NAVObjective currentNavTarget;
    private final RenderableSpacePartitioningGrid partitioningGrid = new RenderableSpacePartitioningGrid();
    private GameShell gameShell;
    private final RunStateListener runStateListener = new RunStateListener();
    @SuppressWarnings("unused")
    private WeakPropertyChangeListener weakRunStateListener; // HARD REFERENCE; DO NOT REMOVE.
    private String lvlFileName;
    private List<DEFObject> defObjectList;
    private Map<NAVObjective,NAVSubObject> navMap = new HashMap<>();
    private PlayerSaveState playerSaveState;
    //private GLExecutor<?> glExecutor;
    //private Vector3D sunVector;

    enum LoadingStages {
	navs, tunnels, overworld
    }// end LoadingStages
    
    //ROOT STATES
    public interface MissionState       extends Game.GameRunningMode{}
    public interface ConstructingState  extends MissionState{}
    public interface ConstructedState   extends MissionState{}
    public interface ActiveMissionState extends ConstructedState{}
    
     public interface LoadingState      extends ActiveMissionState{}
      public interface LoadingComplete   extends LoadingState{}
     public interface GameplayState     extends ActiveMissionState{}
      public interface Briefing        extends GameplayState{}
      // public interface MissionSummary  extends Briefing{}// Declared below
      public interface PlayerActivity  extends GameplayState{}
       public interface OverworldState  extends PlayerActivity{}
       public interface SatelliteState   extends OverworldState{}
       public interface ChamberState    extends PlayerActivity{}
       public interface TunnelState     extends PlayerActivity{}
      public interface MissionEnd extends GameplayState{}
       public interface MissionAbort extends MissionEnd{}
       public interface MissionComplete extends MissionEnd{}
       public interface PlayerDied extends MissionEnd{}
    
    public Mission() {
	this.tr 	    = Features.get(Features.getSingleton(), TR.class);
	this.displayHandler = new DisplayModeHandler(this.getPartitioningGrid());
	
	tr.addPropertyChangeListener(
		TRFactory.RUN_STATE, 
		weakRunStateListener = new WeakPropertyChangeListener(runStateListener, tr));
	//Features.init(this); //Cannot do this because it triggers when beans are (de)serialized!
    }// end Mission
    
    private class RunStateListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object newState = evt.getNewValue();
	    final Object oldState = evt.getOldValue();
	    //TODO: Add listener removal code here like with MissionStartRunStateListener
	    if(newState instanceof Game.GameDestructingMode && !(oldState instanceof Game.GameDestructingMode))
		abort();
	    if(! (newState instanceof MissionState) && oldState instanceof MissionState)//Cleanup
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, this);
	}//end propertyChange(...)
    }//end RunStateListener
	
	public void notifyMissionComplete(){//TODO: nonblocking
	    MissionSummary summary;
	    tr.setRunState(summary = new Mission.MissionSummary(
		    getAirTargetsDestroyed(),
		    getGroundTargetsDestroyed(),
		    getFoliageDestroyed()/*,
		    1.-(double)mission.getTunnelsRemaining().size()/(double)mission.getTotalNumTunnels()*/
		    ));
		displayHandler.setDisplayMode(summaryMode);
		final boolean showEndBriefing = !lvl.getBriefingTextFile().toLowerCase().contentEquals("null.txt");
		if( showEndBriefing )
		    ((TVF3Game)game).getBriefingScreen().missionCompleteSummary(lvl,summary);
		else missionComplete();
	}//End missionSummaryMode()
	
	public void missionComplete() {
	    bgMusic.stop();
	    cleanup();
	    ((TVF3Game)game).notifyMissionComplete();
	    //TODO: Level end video
	}//end missionComplete()

	private void startGameplay(){
	    final SoundSystem ss = Features.get(tr,SoundSystemFeature.class);
	    MusicPlaybackEvent mpe;

	    Features.get(tr,SoundSystemFeature.class).enqueuePlaybackEvent(
		    mpe =ss
		    .getMusicFactory()
		    .create(new GPUResidentMOD(tr, tr
			    .getResourceManager().getMOD(
				    lvl.getBackgroundMusicFile())),
			    true));

	    //synchronized(Mission.this){
	    if(bgMusic==null){
		bgMusic=mpe;
		bgMusic.play();
	    }

	    //}//end sync(Mission.this)
	    ((TVF3Game)game).getUpfrontDisplay().removePersistentMessage();
	    Features.get(tr,SoundSystemFeature.class).setPaused(false);
	    tr.getThreadManager().setPaused(false);
	    
	    //Make sure there is actually something to show, skip if not.
	    if(lvl.getBriefingTextFile().toLowerCase().contentEquals("null.txt"))
		setShowIntro(false);
	    
	    if(isShowIntro()){
		//tr.setRunState(new EnemyBrief(){});
		displayHandler.setDisplayMode(briefingMode);
		((TVF3Game)game).getBriefingScreen().briefingSequence(lvl);//TODO: Convert to feature
		setShowIntro(false);
	    }//end if(isShowIntro)
	    else
		switchToOverworldState();
	}//end startGameplay()
	
	
	
	public void switchToOverworldState() {
	    
	    final Renderer renderer = tr.mainRenderer;
	    renderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	    
	    //New LVL override stuff
	    
	    System.out.println("Mission searching for override of `"+lvlFileName+"` ...");
	    overworldSystem.applyToRenderer(renderer);
	    
	    ((TVF3Game)game).getNavSystem() .activate();
	    displayHandler.setDisplayMode(overworldMode);
	    
	    ((TVF3Game)game).getPlayer()	.setActive(true);
	    ((TVF3Game)getGameShell().getGame()).setPaused(false);
	    tr.setRunState(new OverworldState(){});
	    //tr.setRunState(new PlayerActivity(){});
	}

    public void go() {
	//XXX Kludge to fulfill previous dependencies
	tr.setRunState(new ConstructingState(){});
	//TODO Install runstate loader hooks
	//tr.addPropertyChangeListener(TRFactory.RUN_STATE, missionStartListener);
	tr.setRunState(new ConstructedState(){});
	
	Util.assertPropertiesNotNull(this,
		"tr",
		"game");
	final TR tr = getTr();
	tr.setRunState(new LoadingState(){});
	final LVLFile lvlData = getLvl();
	
	//synchronized(missionLock){
	//synchronized(missionEnd){
	//    if(missionEnd[0]!=null)
	//	return missionEnd[0]; 
	//}
	tr.getThreadManager().setPaused(true);
	for(ProjectileFactory pf:tr.getResourceManager().getProjectileFactories())
	    for(Projectile proj:pf.getProjectiles())
		proj.destroy();
	System.out.println("Starting GampeplayLevel loading sequence...");
	final LoadingProgressReporter rootProgress = LoadingProgressReporter.Impl
		.createRoot(new UpdateHandler() {
		    @Override
		    public void update(double unitProgress) {
			((TVF3Game)getGame()).getLevelLoadingScreen().setLoadingProgress(unitProgress);
		    }
		});
	final LoadingProgressReporter[] progressStages = rootProgress
		.generateSubReporters(LoadingStages.values().length);
	final Renderer renderer = tr.mainRenderer;
	final Camera camera = renderer.getCamera();
	final Player player      = ((TVF3Game)getGameShell().getGame()).getPlayer();
	//Set up the gray gradient
	camera.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	player.setHeading(Vector3D.PLUS_I);
	player.setTop(Vector3D.PLUS_J);
	player.notifyPositionChange();
	renderer.getSkyCube().setSkyCubeGen(GameShellFactory.DEFAULT_GRADIENT);
	
	((TVF3Game)game).levelLoadingMode();
	displayHandler.setDisplayMode(getLevelLoadingMode());
	((TVF3Game)game).getUpfrontDisplay().submitPersistentMessage(levelName);
	try {
	    final ResourceManager rm = tr.getResourceManager();
	    final TDFFile tdf 	     = rm.getTDFData(lvlData.getTunnelDefinitionFile());
	    player.setActive(false);
	    // Abort check
	    //synchronized(missionEnd){
		//if(missionEnd[0]!=null)
		//return missionEnd[0]; 
	    //}
	    
	    setOverworldSystem(new OverworldSystem(tr,
		    progressStages[LoadingStages.overworld.ordinal()]));
	    
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
	    summaryMode = new Object[]{
		    ((TVF3Game)game).getBriefingScreen(),
		    overworldSystem
	    };
	    
	  //New LVL override stuff
	    //boolean enhanced = false;
	    LVLFileEnhancements enhancements = Features.get(tr, LVLFileEnhancements.class);
	    
	    overworldSystem.loadLevel(lvlData, tdf, enhancements!=null?enhancements.findByHook(levelName):null);
	    
	    final List<DEFObject> defObjectList = getDefObjectList();
	    final ObjectSystem objectSystem = overworldSystem.getObjectSystem();
	    if(defObjectList != null)
	        objectSystem.setDefList(defObjectList);
	    else{
		objectSystem.setPositionOffset(new Vector3D(0,1024 * 16, 0));
		objectSystem.populateFromLVL(lvlData);
		setDefObjectList(objectSystem.getDefList());
		}
	    
	    assert getDefObjectList() != null;
	    
	    //overworldSystem.loadLevel(lvlData, tdf);
	    System.out.println("\t...Done.");
	    
	    //TODO: TunnelSystem should be isolated from Mission
	    final TunnelSystem ts = Features.get(this, TunnelSystem.class);
	    ts.installTunnels(tdf,progressStages[LoadingStages.tunnels.ordinal()]);
	    
	    // Install NAVs if not already set
	    if( getNavSubObjects() == null ){
		setNavSubObjects(rm.getNAVData(lvlData.getNavigationFile())
			.getNavObjects());
		}
	    if( navs.isEmpty() ){
		Factory f = new NAVObjective.Factory(tr, getLevelName());

		//final LoadingProgressReporter[] navProgress = progressStages[LoadingStages.navs
		//                                                             .ordinal()].generateSubReporters(navSubObjects.size());
		final Reporter reporter = Features.get(getTr(), Reporter.class);
		for (int i = 0; i < navSubObjects.size(); i++) {
		    final NAVSubObject obj = navSubObjects.get(i);
		    System.out.println("Parsing NAV Sub-Object "+obj);
		    f.create(reporter, obj, navs, navMap);
		    //navProgress[i].complete();
		}// end for(navSubObjects)
	    }//end if( empty )
	    
	    // ////// INITIAL HEADING
	    PlayerSaveState playerSaveState = getStoredPlayerSaveState();
	    //final double [] playerStartPos = getStoredPlayerStartPosition();
	    //final double [] playerStartHdg = getStoredPlayerStartHeading();
	    //final double [] playerStartTop = getStoredPlayerStartTop();
	    
	    if(playerSaveState == null){//No savestate data. Use the map
		playerSaveState = new PlayerSaveState();
		setPlayerSaveState(playerSaveState);
		playerSaveState.readFrom(player);
		
		START startNav = (START) getNavSubObjects().get(0);
		final ObjectDirection od = new ObjectDirection(startNav.getRoll(),
			    startNav.getPitch(), startNav.getYaw());
		Location3D l3d = startNav.getLocationOnMap();
		final double HEIGHT_PADDING = 10000;
		playerSaveState.setHeading(od.getHeading().negate().toArray());
		//setPlayerStartHeading (od.getHeading().negate().toArray());
		playerSaveState.setTop(od.getTop().toArray());
		//setPlayerStartTop     (od.getTop().toArray());
		playerSaveState.setPosition(new Vector3D(
			TRFactory.legacy2Modern(l3d.getZ()), //X (Z)
			Math.max(HEIGHT_PADDING + getOverworldSystem().getAltitudeMap().heightAt(// Y
				    TRFactory.legacy2Modern(l3d.getZ()),
				    TRFactory.legacy2Modern(l3d.getX())),TRFactory.legacy2Modern(l3d.getY())),
			TRFactory.legacy2Modern(l3d.getX()) //Z (X)
			).toArray());
	    }//end if(nulls)
	    //APPLY STATE
	    playerSaveState.writeTo(player);
	    //////// PLAYER POSITIONS
	    //player.setPosition(getStoredPlayerStartPosition()                     );
	    //player.setHeading(new Vector3D(getStoredPlayerStartHeading())         );// XXX Kludge to fix incorrect hdg
	    //player.setTop    (new Vector3D(getStoredPlayerStartTop())             );
	    ///////// STATE
	    final Propelled propelled = player.probeForBehavior(Propelled.class); 
	    propelled.setPropulsion(propelled.getMinPropulsion());
	    player.probeForBehavior(CollidesWithTerrain.class)   .setEnable(true);
	    if(player.hasBehavior(SpawnsRandomSmoke.class))
	     player.probeForBehavior(SpawnsRandomSmoke.class)    .setEnable(false);
	    if(player.hasBehavior(SpawnsRandomExplosionsAndDebris.class))
	     player.probeForBehavior(SpawnsRandomExplosionsAndDebris.class).setEnable(false);
	    //player.probeForBehavior(SpinCrashDeathBehavior.class).setEnable(false);
	    
	    final TVF3Game game = (TVF3Game)getGame();
	    game.getNavSystem().updateNAVState();
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
	    //final AbstractTriplet sunVectorTriplet = lvlData.getSunlightDirectionVector();
	    /*sunVector = new Vector3D(sunVectorTriplet.getX(), sunVectorTriplet.getY(),
		    sunVectorTriplet.getZ()).normalize();*/
	    //final GLExecutor<?> glExecutor = getGlExecutor();
	    
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
	tr.setRunState(new LoadingComplete(){});
	startGameplay();
	// Abort check
	//synchronized (missionEnd) {
	//    if (missionEnd[0] != null)
	//	return missionEnd[0];
	//}//end sync(missionEnd)
	
	//Wait for mission end
	//TODO: Make this non-blocking, use listener to complete.
	/*synchronized(missionEnd){
	 while(missionEnd[0]==null){try{missionEnd.wait();}
		catch(InterruptedException e){break;}}}
		
	}//end sync
	*/
    }// end go()
    /*
    private GLExecutor<?> getGlExecutor() {
	if( glExecutor == null )
	    glExecutor = Features.get(Features.get(getTr(), GLAutoDrawableProvider.class),GLExecutor.class);
	return glExecutor;
    }
*/
    private LVLFile getLvl() {
	if(lvl == null)
	    try{lvl = getTr().getResourceManager().getLVL(getLvlFileName());}
	catch(Exception e){e.printStackTrace();}
	return lvl;
    }

    private Object [] getLevelLoadingMode(){
	if(levelLoadingMode == null){
	    final Game game = getGame();
	    if(game == null)
		throw new IllegalStateException("game property intolerably null.");
	    levelLoadingMode = new Object[]{
		 ((TVF3Game)game).getLevelLoadingScreen(),
		 ((TVF3Game)game).getUpfrontDisplay()
	    };
	    }
	return levelLoadingMode;
    }//end getLevelLoadingMode

    public NAVObjective currentNAVObjective() {
	if (navs.isEmpty())
	    return null;
	return navs.get(0);
    }//end currentNAVObjective()

    public void removeNAVObjective(NAVObjective o) {
	navs.remove(o);
	getNavSubObjects().remove(navMap.get(o));
	updateNavState();
	((TVF3Game)getGameShell().getGame()).getNavSystem().updateNAVState();
    }// end removeNAVObjective(...)
    
    private void updateNavState(){
	try{this.setCurrentNavTarget(navs.get(0));}
	catch(IndexOutOfBoundsException e){setCurrentNavTarget(null);}
    }

    public static class MissionSummary implements Briefing {
	private final int airTargetsDestroyed, groundTargetsDestroyed,foliageDestroyed;
	private boolean abort=false;

	public MissionSummary(int airTargetsDestroyed, int groundTargetsDestroyed, int foliageDestroyed) {
	    this.airTargetsDestroyed	=airTargetsDestroyed;
	    this.groundTargetsDestroyed	=groundTargetsDestroyed;
	    this.foliageDestroyed	=foliageDestroyed;
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
     * NOTE: This returns the current Game's Player position and does not directly reflect setter changes.
     * @return
     * @since Jul 15, 2016
     */
    public double[] getPlayerStartPosition() {
	final Game game = getGame();
	if(game == null)
	    return null;
	final Player player = game.getPlayer();
	if(player == null)
	    return null;
	return player.getPosition();
    }
    
    public double[] getStoredPlayerStartPosition(){
	return playerStartPosition;
    }

    public void notifyPlayerDestroyed() {
	//new Thread() {
	//    @Override
	//    public void run() {
		System.out.println("MISSION FAILED.");
		//notifyMissionEnd(null);
		tr.setRunState(new PlayerDied(){});
		((TVF3Game)game).notifyPlayerDied();
	   // }// end run()
	//}.start();
    }// end playerDestroyed()
    /*
    public void notifyMissionEnd(Result r){
	synchronized(missionEnd){
	 missionEnd[0]=r;
	 missionEnd.notifyAll();}
    }//end notifyMissionEnd()
*/
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
	if( navSubObjects != null)
	    this.navSubObjects = new ArrayList<NAVSubObject>(navSubObjects);
	else
	    navSubObjects = null;
	navs.clear();//Ensure navs get repopulated.
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
    
    public Mission notifyFoliageDestroyed(){
	foliageDestroyed++;
	return this;
    }
    
    public void enterBossMode(final String bossMusicFile, WorldObject boss){
	setCurrentBoss(boss);
	//tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	//    @Override
	//    public Void call() throws Exception {
		MusicPlaybackEvent evt;
		final SoundSystem ss = Features.get(tr,SoundSystemFeature.class);
		Features.get(tr,SoundSystemFeature.class).enqueuePlaybackEvent(
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
		//return null;
	   // }// end call()
	//});
    }//end enterBossMode()
    
    public void exitBossMode(){
	setCurrentBoss(null);
	tr.getThreadManager().submitToThreadPool(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		MusicPlaybackEvent evt;
		final SoundSystem ss = Features.get(tr,SoundSystemFeature.class);
		Features.get(tr,SoundSystemFeature.class).enqueuePlaybackEvent(
			evt =ss
				.getMusicFactory()
				.create(tr.getResourceManager().gpuResidentMODs.get(getLvl().getBackgroundMusicFile()),
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
	tr.setRunState(new MissionAbort(){});
	if(bgMusic!=null)
	    bgMusic.stop();
	if(isSatelliteView())
	    setSatelliteView(false);
	cleanup();
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
    public WorldObject getCurrentBoss() {
        return currentBoss;
    }
    
    public void setCurrentBoss(WorldObject boss) {
	final WorldObject oldBoss = this.currentBoss;//Probably null.
        this.currentBoss = boss;
        pcs.firePropertyChange(CURRENT_BOSS, oldBoss, boss);
    }
    public void setSatelliteView(boolean satelliteView) {
	if(!(tr.getRunState() instanceof OverworldState)&&satelliteView)
	    throw new IllegalArgumentException("Cannot activate satellite view while runState is "+tr.getRunState().getClass().getSimpleName());
	if(satelliteView && ((TVF3Game)getGameShell().getGame()).isPaused())
	    throw new IllegalArgumentException("Cannot activate satellite view while paused.");
	final boolean oldValue = this.satelliteView;
	
	if(satelliteView!=oldValue){
	    final Game game =  ((TVF3Game)getGameShell().getGame());
	    final Camera cam = tr.mainRenderer.getCamera();
	    this.satelliteView=satelliteView;
	    pcs.firePropertyChange(SATELLITE_VIEW, oldValue, satelliteView);
	    tr.setRunState(satelliteView?new SatelliteState(){}:new OverworldState(){});
	    if(satelliteView){//Switched on
		tr.getThreadManager().setPaused(true);
		cam.setFogEnabled(false);
		cam.probeForBehavior(MatchPosition.class).setEnable(false);
		cam.probeForBehavior(MatchDirection.class).setEnable(false);
		final Vector3D pPos = new Vector3D(((TVF3Game)game).getPlayer().getPosition());
		final Vector3D pHeading = ((TVF3Game)getGameShell().getGame()).getPlayer().getHeading();
		cam.setPosition(new Vector3D(pPos.getX(),TRFactory.visibilityDiameterInMapSquares*TRFactory.mapSquareSize*.65,pPos.getZ()));
		cam.setHeading(Vector3D.MINUS_J);
		cam.setTop(new Vector3D(pHeading.getX(),.0000000001,pHeading.getZ()).normalize());
		((TVF3Game)getGameShell().getGame()).getSatDashboard().setVisible(true);
	    }else{//Switched off
		tr.getThreadManager().setPaused(false);
		World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			((TVF3Game)getGameShell().getGame()).getNavSystem().activate();
		    }});
		cam.setFogEnabled(true);
		cam.probeForBehavior(MatchPosition.class).setEnable(true);
		cam.probeForBehavior(MatchDirection.class).setEnable(true);
		((TVF3Game)getGameShell().getGame()).getSatDashboard().setVisible(false);
	    }//end !satelliteView
	}//end if(change)
    }
    /**
     * @return the satelliteView
     */
    public boolean isSatelliteView() {
        return satelliteView;
    }
    
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

    public TR getTr() {
        return tr;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public boolean isShowIntro() {
        return showIntro;
    }

    public void setShowIntro(boolean showIntro) {
        this.showIntro = showIntro;
    }

    public String getLvlFileName() {
        return lvlFileName;
    }

    public void setLvlFileName(String lvlFileName) {
        this.lvlFileName = lvlFileName;
    }

    public List<DEFObject> getDefObjectList() {
        return defObjectList;
    }

    public void setDefObjectList(List<DEFObject> defObjectList) {
        this.defObjectList = defObjectList;
    }//end setDefObjectList(...)

    void setOverworldSystem(OverworldSystem overworldSystem) {
        this.overworldSystem = overworldSystem;
    }
    
    public Object [] getOverworldMode(){
	return overworldMode;
    }
    
    
    public PlayerSaveState getPlayerSaveState(){
	final Game game = getGame();
	if(game == null)
	    return null;
	final Player player = game.getPlayer();
	if(player == null)
	    return null;
	return player.getPlayerSaveState();
    }
    
    protected PlayerSaveState getStoredPlayerSaveState(){
	return playerSaveState;
    }
    
    public void setPlayerSaveState(PlayerSaveState pss){
	playerSaveState = pss;
    }
}// end Mission
