/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.game;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.BriefingScreen;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.DisplayModeHandler;
import org.jtrfp.trcl.EarlyLoadingScreen;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.LevelLoadingScreen;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.SatelliteDashboard;
import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.gui.BriefingLayout;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.gui.F3BriefingLayout;
import org.jtrfp.trcl.gui.F3DashboardLayout;
import org.jtrfp.trcl.gui.TVBriefingLayout;
import org.jtrfp.trcl.gui.TVDashboardLayout;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.DebrisSystem;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionSystem;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PowerupSystem;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeSystem;
import org.jtrfp.trcl.prop.IntroScreen;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.snd.SoundSystem;

public class TVF3Game implements Game {
    //// PROPERTIES
    public static final String VOX              = "vox",
	                       GAME_VERSION     = "gameVersion",
	                       PLAYER_NAME      = "playerName",
	                       DIFFICULTY       = "difficulty",
	                       CURRENT_MISSION  = "currentMission";
    private final RenderableSpacePartitioningGrid partitioningGrid = new RenderableSpacePartitioningGrid();
    private GameVersion gameVersion;
    private GameShell gameShell;
    public enum Difficulty {
	EASY(1.5,.5,1), NORMAL(1,1,1), HARD(.5,1.5,1), FURIOUS(.1,2,1.08);
	
	private final double timeBetweenFiringScalar;
	private final double shieldScalar;
	private final double defSpeedScalar;
	
	Difficulty(double timeBetweenFiringScalar, double shieldScalar, double defSpeedScalar){
	    this.timeBetweenFiringScalar= timeBetweenFiringScalar;
	    this.shieldScalar    = shieldScalar;
	    this.defSpeedScalar  = defSpeedScalar;
	}
	
	@Override
	public String toString(){
	    String low = this.name().toLowerCase();
	    return Character.toUpperCase(low.charAt(0))+low.substring(1);
	}
	
	public double getFiringRateScalar(){
	    return timeBetweenFiringScalar;
	}

	public double getShieldScalar() {
	    return shieldScalar;
	}

	public double getDefSpeedScalar() {
	    return defSpeedScalar;
	}
    }//end Difficulty
    
	private TR              tr;
	private VOXFile 	vox;
	    private int 	levelIndex = 0;
	    private String 	playerName=null;
	    private Difficulty 	difficulty;
	    private Mission 	currentMission;
	    public HUDSystem 	        hudSystem;
	    public NAVSystem 	        navSystem;
	    private SatelliteDashboard satDashboard;
	    private Player 	player;
	    private GLFont	upfrontFont;
	    public UpfrontDisplay
	    			upfrontDisplay;
	    public LevelLoadingScreen
	    			levelLoadingScreen;
	    public BriefingScreen
	    			briefingScreen;
	    private IntroScreen
	    			introScreen;
	    private final DisplayModeHandler
	    			displayModes;
	    private Object[]	earlyLoadingMode,
	    			titleScreenMode,
	    			missionMode,
	    			emptyMode;
	    private final PropertyChangeSupport
	    			pcSupport = new PropertyChangeSupport(this);
	    private boolean paused=false;
	    private TRFutureTask<Void>[] startupTask = new TRFutureTask[]{null};
	    
	    private static final int UPFRONT_HEIGHT = 23;
	    private boolean inGameplay	=false;
	    private DashboardLayout dashboardLayout;
	    
	    public TVF3Game() {//TODO: These Features-dependent inits probably shouldn't be in the constructor
		this.tr = Features.get(Features.getSingleton(), TR.class);
		displayModes = new DisplayModeHandler(this.getPartitioningGrid());
		emptyMode = missionMode = new Object[]{};
	    }// end constructor

	    public void setupNameWithUser() throws CanceledException {
		GameSetupDialog gsd = new GameSetupDialog();
		gsd.setModal(true);
		gsd.setVisible(true);
		if(!gsd.isBeginMission())
		    throw new CanceledException();
		setPlayerName(gsd.getCallSign());
		setDifficulty(gsd.getDifficulty());
	    }// end setupNameWithUser()

	    /**
	     * @return the vox
	     */
	    public synchronized VOXFile getVox() {
		return vox;
	    }

	    /**
	     * @param vox
	     *            the vox to set
	     */
	    public synchronized void setVox(VOXFile vox) {
		if(this.vox==vox)
		    return;//No change.
		final VOXFile oldVox = this.vox;
		this.vox = vox;
		pcSupport.firePropertyChange(VOX, oldVox, vox);
	    }

	    /**
	     * @return the levelIndex
	     */
	    public synchronized int getLevelIndex() {
		return levelIndex;
	    }

	    public boolean isInGameplay(){
		return inGameplay;
	    }

	    private boolean setInGameplay(boolean newValue){
		boolean old = inGameplay;
		inGameplay=newValue;
		pcSupport.firePropertyChange("inGameplay", old, newValue);
		return old;
	    }

	    /**
	     * @param levelIndex
	     *            the levelIndex to set
	     * @throws FileNotFoundException 
	     * @throws FileLoadException 
	     * @throws IOException 
	     * @throws IllegalAccessException 
	     */
	    public void setLevelIndex(int levelIndex) throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException {
		this.levelIndex = levelIndex;
		if (levelIndex != -1) {// -1 means 'abort'
		    MissionLevel lvl = vox.getLevels()[getLevelIndex()];
		    final String lvlFileName = lvl.getLvlFile();
		        setLevelDirect(lvlFileName);
		}//end if(levelIndex!=-1)
		else // Make sure the Mission is destroyed
		    setCurrentMission(null);
	    }// end setLevelIndex(...)
	    
	    public void setLevelDirect(String lvlFileName) throws FileNotFoundException, IllegalAccessException, IOException, FileLoadException{
		setCurrentMission(null);
		final Mission newMission = new Mission();
		newMission.setLvlFileName(lvlFileName);
		newMission.setLevelName  (prepareLevelName(lvlFileName));
		newMission.setShowIntro  (getLevelIndex() % 3 == 0);
		setCurrentMission(newMission);
	    }//end setLevelDirect()
	    
	    private String prepareLevelName(String rawName){
		String result;
		result = rawName.substring(0,
			 rawName.lastIndexOf('.'));
		return result.charAt(0)+result.toLowerCase().substring(1);
	    }//end prepareLevelName

	    /**
	     * @return the playerName
	     */
	    public String getPlayerName() {
		return playerName;
	    }

	    /**
	     * @param playerName
	     *            the playerName to set
	     */
	    public void setPlayerName(String playerName) {
		this.playerName = playerName;
	    }

	    

	    /**
	     * @return the difficulty
	     */
	    public Difficulty getDifficulty() {
		return difficulty;
	    }

	    /**
	     * @param difficulty
	     *            the difficulty to set
	     */
	    public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	    }

	    public NAVSystem getNavSystem(){
		if(navSystem == null)
		    navSystem = generateNavSystem();
		return navSystem;
	    }
	    
	    protected NAVSystem generateNavSystem(){
		return new NAVSystem(tr,getDashboardLayout());
	    }
	    
	    public synchronized void setLevel(String skipToLevel) throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException {
		final MissionLevel[] levs = vox.getLevels();
		for (int index = 0; index < levs.length; index++) {
		    if (levs[index].getLvlFile().toUpperCase()
			    .contentEquals(skipToLevel.toUpperCase()))
			setLevelIndex(index);
		}// end for(levs)
	    }// end setLevel()
	    
	    public HUDSystem getHUDSystem(){
		if(hudSystem==null)
		    try{hudSystem = new HUDSystem(tr,getGameShell().getGreenFont(),getDashboardLayout());}
		catch(Exception e){e.printStackTrace();return null;}
		return hudSystem;
	    }

	    public synchronized void boot() throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException {
			//Kludge to fulfill previous dependency
			tr.setRunState(new GameConstructingMode(){});
			tr.setRunState(new GameConstructedMode(){});
			// Set up player, HUD, fonts...
			System.out.println("Booting...");
			tr.getThreadManager().setPaused(true);
			NDXFile ndx = tr.getResourceManager().getNDXFile("STARTUP\\FONT.NDX");
			upfrontFont = new GLFont(tr.getResourceManager().getFontBIN("STARTUP\\FONT.BIN", ndx),
				    UPFRONT_HEIGHT, ndx.getWidths(), 32,tr);
			
			final EarlyLoadingScreen earlyLoadingScreen = getGameShell().getEarlyLoadingScreen();
			earlyLoadingScreen.setStatusText("Reticulating Splines...");
			earlyLoadingMode = new Object []{
				earlyLoadingScreen
			};
			displayModes.setDisplayMode(earlyLoadingMode);
			
			upfrontDisplay = new UpfrontDisplay(tr.getDefaultGrid(),tr);
			
			satDashboard = new SatelliteDashboard(tr);
			satDashboard.setVisible(false);
			tr.getDefaultGrid().add(satDashboard);
			
			final TRConfiguration trConfig = Features.get(tr,TRConfiguration.class);
			System.out.println("GameVersion="+trConfig._getGameVersion());
			    // Make color zero translucent.
			    final ResourceManager rm = tr.getResourceManager();
			    final Color[] pal 	     = tr.getGlobalPalette();
			    pal[0] 		     = new Color(0, 0, 0, 0);
			    tr.setGlobalPalette(pal);
			    // POWERUPS
			    earlyLoadingScreen.setStatusText("Loading powerup assets...");
			    rm.setPowerupSystem(new PowerupSystem(tr));
			    // EXPLOSIONS
			    earlyLoadingScreen.setStatusText("Loading explosion assets...");
			    rm.setExplosionFactory(new ExplosionSystem(tr, "Game"));
			    // SMOKE
			    earlyLoadingScreen.setStatusText("Loading smoke assets...");
			    rm.setSmokeSystem(new SmokeSystem("Game"));
			    // DEBRIS
			    earlyLoadingScreen.setStatusText("Loading debris assets...");
			    rm.setDebrisSystem(new DebrisSystem(tr));
			    // SETUP PROJECTILE FACTORIES
			    earlyLoadingScreen.setStatusText("Setting up projectile factories...");
			    Weapon[] w = Weapon.values();
			    ProjectileFactory[] pf = new ProjectileFactory[w.length];
			    for (int i = 0; i < w.length; i++) {
				pf[i] = new ProjectileFactory(tr, w[i], ExplosionType.Blast,"Game");
			    }// end for(weapons)
			    rm.setProjectileFactories(pf);
			    final Player player = new Player();
			    player.setModel(tr.getResourceManager().getBINModel(
				    "SHIP.BIN", tr.getGlobalPaletteVL(),null, Features.get(tr, GPUFeature.class).getGl()));
			    setPlayer(player);
			    final Camera camera = tr.mainRenderer.getCamera();
			    camera.probeForBehavior(MatchPosition.class) .setTarget(player);
			    camera.probeForBehavior(MatchDirection.class).setTarget(player);
			    tr.getDefaultGrid().add(player);
			    System.out.println("\t...Done.");
			    final BriefingLayout briefingLayout = trConfig._getGameVersion()==GameVersion.TV?new TVBriefingLayout():new F3BriefingLayout(); 
			    briefingScreen	= new BriefingScreen(tr,getGameShell().getGreenFont(), 
				    briefingLayout,"TVF3Game");
			    earlyLoadingScreen.setStatusText("Starting game...");
			    
			    introScreen = new IntroScreen(tr,"TITLE.RAW","SEX.MOD","Game");
			    
			    titleScreenMode = new Object[]{
				    introScreen
			    };
			    displayModes.setDisplayMode(titleScreenMode);
			    introScreen.startMusic();
			    setLevelIndex(0);
			    tr.setRunState(new Game.GameLoadedMode(){});
	    }// end boot()
	    
	    public void setPlayer(Player player) {
		final Player oldPlayer = this.player;
		this.player = player;
		pcSupport.firePropertyChange(PLAYER, oldPlayer, player);
	    }

	    public synchronized void doGameplay() throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException, CanceledException {
		final TRConfiguration trConfig = Features.get(tr,TRConfiguration.class);
		if (!trConfig.isDebugMode() && getPlayerName() == null)
		    setupNameWithUser();
		tr.setRunState(new Game.GameRunningMode(){});
		setInGameplay(true);
		try {
		    MissionLevel[] levels = vox.getLevels();
		    tr.getThreadManager().setPaused(false);
		    while (getLevelIndex() < levels.length && getLevelIndex() != -1) {
			Mission.Result result = null;
			final Mission mission = getCurrentMission();
			if (mission == null)
			    break;
			while (result == null){
			    displayModes.setDisplayMode(missionMode);
			    result = getCurrentMission().go();}
			if (result.isAbort())
			    break;
			final int prevLevelIndex = getLevelIndex();
			final int nextLevelIndex = prevLevelIndex+1;
			// Check if we won the game
			if(nextLevelIndex >= vox.getLevels().length)
			    wonGame();
			// Rube Goldberg style increment
			setLevelIndex(nextLevelIndex);
		    }// end while(getLevelIndex<length)
		    System.out.println("Escaping game loop.");
		    tr.getThreadManager().setPaused(true);
		    setInGameplay(false);
		} catch (IllegalAccessException e) {
		    throw e;
		} catch (FileNotFoundException e) {
		    throw e;
		} catch (IOException e) {
		    throw e;
		} catch (FileLoadException e) {
		    throw e;
		} finally {
		    tr.getThreadManager().setPaused(true);
		    tr.setRunState(new Game.GameLoadedMode() {});
		    setInGameplay(false);
		}//end finally{}
	    }// end beginGameplay()
	    
	    protected void wonGame(){//TODO
		System.out.println("!!!!!CONGRATULATIONS!!!!!!\n" +
				"You've won the game!\n" +
				"There's no code in place to handle this.\n" +
				"Therefore, the program will now crash.");
	    }

	    public void setCurrentMission(final Mission newMission) {
		final Mission oldMission = this.currentMission;
		if(newMission == oldMission)
		    return;
		if(newMission != null){
		    newMission.setGame(this);
		    newMission.setGameShell(getGameShell());
		}
		this.currentMission=newMission;
		World.relevanceExecutor.submit(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			final RenderableSpacePartitioningGrid grid = getPartitioningGrid();
			if(oldMission!=null)
			 grid.removeBranch(oldMission.getPartitioningGrid());
			if(newMission!=null)
			 grid.addBranch(newMission.getPartitioningGrid());
			return null;
		    }});
		if(oldMission!=null)
		 oldMission.destruct();
		if(newMission != null)
		    Features.init(newMission);
		pcSupport.firePropertyChange("currentMission", oldMission, newMission);
	    }//end setCurrentMission
	    
	    public void abort(){
		tr.setRunState(new GameDestructingMode(){});
		Features.destruct(this);
		try{setLevelIndex(-1);}
		catch(Exception e){tr.showStopper(e);}//Shouldn't happen.
		cleanup();
		displayModes.setDisplayMode(emptyMode);
		getGameShell().applyGFXState();
		tr.setRunState(new GameDestructedMode(){});
	    }
	    
	    public DashboardLayout getDashboardLayout(){
		if(dashboardLayout==null)
		    dashboardLayout = 
			    Features.get(tr,TRConfiguration.class)._getGameVersion()==GameVersion.TV?new TVDashboardLayout():new F3DashboardLayout();
		return dashboardLayout;
	    }//end getDashboardLayout()
	    
	    private void cleanup() {
		if(introScreen.isMusicPlaying())
		 introScreen.stopMusic();
		if(player!=null)
		 tr.getDefaultGrid().remove(player);
	    }

	    public void abortCurrentMission(){
		tr.getThreadManager().setPaused(true);
		synchronized(startupTask){
		    if(startupTask[0]!=null)
			startupTask[0].get();//Don't abort while setting up.
		}//end sync{}
		if(currentMission!=null)
		    currentMission.abort();
		setCurrentMission(null);
	    }//end abortCurrentMission()
	    
	    public Mission getCurrentMission() {
		return currentMission;
	    }//end getCurrentMission
	    
	    public Player getPlayer(){
		return player;
	    }
	    
	    public GLFont getUpfrontFont() {
		return upfrontFont;
	    }

	    /**
	     * @return the upfrontDisplay
	     */
	    public UpfrontDisplay getUpfrontDisplay() {
		if( upfrontDisplay == null )
		    upfrontDisplay = new UpfrontDisplay(tr.getDefaultGrid(),tr); 
	        return upfrontDisplay;
	    }

	    public LevelLoadingScreen getLevelLoadingScreen() {
		if( levelLoadingScreen == null )
		    try{levelLoadingScreen = new LevelLoadingScreen(tr.getDefaultGrid(),tr);}
		catch(IOException e){throw new RuntimeException(e);}
		return levelLoadingScreen;
	    }

	    public Game setDisplayMode(Object[] mode) {
		displayModes.setDisplayMode(mode);
		return this;
	    }
	    
	    public BriefingScreen getBriefingScreen(){
		return briefingScreen;
	    }

	    /**
	     * @return the paused
	     */
	    public boolean isPaused() {
	        return paused;
	    }

	    /**
	     * @param paused the paused to set
	     */
	    public Game setPaused(boolean paused) {//TODO: This feature should be in Mission
		if(paused==this.paused)
		    return this;//nothing to do.
		pcSupport.firePropertyChange(PAUSED, this.paused, paused);
	        this.paused = paused;
	        final SoundSystem ss = Features.get(getTr(),SoundSystemFeature.class);
		ss.setPaused(paused);
		getTr().getThreadManager().setPaused(paused);
		if(paused)
		 upfrontDisplay.submitPersistentMessage("Paused--F3 to Resume");
		else
		 upfrontDisplay.removePersistentMessage();
	        return this;
	    }//end setPaused(...)
	    
	    public Game addPropertyChangeListener(String propertyName, PropertyChangeListener l){
		pcSupport.addPropertyChangeListener(propertyName, l);
		return this;
	    }
	    
	    public Game removePropertyChangeListener(PropertyChangeListener l){
		pcSupport.removePropertyChangeListener(l);
		return this;
	    }

	    /**
	     * @return the mapDashboard
	     */
	    public SatelliteDashboard getSatDashboard() {
	        return satDashboard;
	    }

	    /**
	     * @param satDashboard the mapDashboard to set
	     */
	    public void setSatDashboard(SatelliteDashboard satDashboard) {
	        this.satDashboard = satDashboard;
	    }

	    public void levelLoadingMode() {
		introScreen.stopMusic();
	    }
    
    /**
     * @return the tr
     */
    public TR getTr() {
	return tr;
    }

    /**
     * @param tr
     *            the tr to set
     */
    public synchronized void setTr(TR tr) {
	this.tr = tr;
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcSupport.getPropertyChangeListeners();
    }

    public Game removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcSupport.removePropertyChangeListener(propertyName, listener);
	return this;
    }

    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcSupport.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
	return pcSupport.hasListeners(propertyName);
    }

    public RenderableSpacePartitioningGrid getPartitioningGrid() {
        return partitioningGrid;
    }

    public GameVersion getGameVersion() {
        return gameVersion;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getTr(), GameShell.class);
        return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }

    public void setGameVersion(GameVersion gameVersion) {
        this.gameVersion = gameVersion;
    }
}// end Game
