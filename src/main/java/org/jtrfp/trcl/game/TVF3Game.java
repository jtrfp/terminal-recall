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
package org.jtrfp.trcl.game;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.BriefingScreen;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.DisplayModeHandler;
import org.jtrfp.trcl.EarlyLoadingScreen;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.LevelLoadingScreen;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.SatelliteDashboard;
import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.game.Game.CanceledException;
import org.jtrfp.trcl.game.Game.GameConstructedMode;
import org.jtrfp.trcl.game.Game.GameConstructingMode;
import org.jtrfp.trcl.game.Game.GameDestructedMode;
import org.jtrfp.trcl.game.Game.GameDestructingMode;
import org.jtrfp.trcl.game.Game.GameLoadedMode;
import org.jtrfp.trcl.game.Game.GameRunningMode;
import org.jtrfp.trcl.gui.BriefingLayout;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.gui.F3BriefingLayout;
import org.jtrfp.trcl.gui.F3DashboardLayout;
import org.jtrfp.trcl.gui.TVBriefingLayout;
import org.jtrfp.trcl.gui.TVDashboardLayout;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.Mission.Result;
import org.jtrfp.trcl.obj.DebrisSystem;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionSystem;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PowerupSystem;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeSystem;
import org.jtrfp.trcl.prop.IntroScreen;
import org.jtrfp.trcl.snd.SoundSystem;

public class TVF3Game implements Game {
    enum Difficulty {
	EASY, NORMAL, HARD, FURIOUS;
	
	@Override
	public String toString(){
	    String low = this.name().toLowerCase();
	    return Character.toUpperCase(low.charAt(0))+low.substring(1);
	}
    }//end Difficulty
    
	private TR              tr;
	private VOXFile 	vox;
	    private int 	levelIndex = 0;
	    private String 	playerName="DEBUG";
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
	    private volatile boolean aborting=false;
	    private TRFutureTask<Void>[] startupTask = new TRFutureTask[]{null};
	    
	    private static final int UPFRONT_HEIGHT = 23;
	    private final double 	FONT_SIZE=.07;
	    private boolean inGameplay	=false;
	    private DashboardLayout dashboardLayout;
	    
	    public TVF3Game(TR tr) {
		this.tr = tr;
		Features.init(this);
		tr.setRunState(new GameConstructingMode(){});
		displayModes = new DisplayModeHandler(tr.getDefaultGrid());
		//if (!tr.config.isDebugMode())
		//    setupNameWithUser();
		emptyMode = missionMode = new Object[]{};
		tr.setRunState(new GameConstructedMode(){});
	    }// end constructor

	    public void setupNameWithUser() throws CanceledException {
		GameSetupDialog gsd = new GameSetupDialog();
		gsd.setModal(true);
		gsd.setVisible(true);
		/*
		setPlayerName((String) JOptionPane.showInputDialog(tr.getRootWindow(),
			"Callsign:", "Pilot Registration", JOptionPane.PLAIN_MESSAGE,
			null, null, "Councilor"));
		String difficulty = (String) JOptionPane.showInputDialog(
			tr.getRootWindow(), "Difficulty:", "Pilot Registration",
			JOptionPane.PLAIN_MESSAGE, null, new String[] { "Easy",
				"Normal", "Hard", "Furious" }, "Normal");
		
		if (difficulty.contentEquals("Easy")) {
		    setDifficulty(Difficulty.EASY);
		}
		if (difficulty.contentEquals("Normal")) {
		    setDifficulty(Difficulty.NORMAL);
		}
		if (difficulty.contentEquals("Hard")) {
		    setDifficulty(Difficulty.HARD);
		}
		if (difficulty.contentEquals("Furious")) {
		    setDifficulty(Difficulty.FURIOUS);
		}
		*/
		if(!gsd.isBeginMission())
		    throw new CanceledException();
		setPlayerName(gsd.getCallSign());
		setDifficulty(gsd.getDifficulty());
	    }// end setupNameWithUser()
	    
	    public void save(File fileToSaveTo) {
		// TODO
	    }

	   

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
		pcSupport.firePropertyChange("vox", this.vox, vox);
		this.vox = vox;
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
		        setCurrentMission(null);
			setCurrentMission(new Mission(tr, this, tr.getResourceManager()
				.getLVL(lvlFileName), lvlFileName.substring(0,
				lvlFileName.lastIndexOf('.')), getLevelIndex() % 3 == 0));
		}//end if(levelIndex!=-1)
		else // Make sure the Mission is destroyed
		    setCurrentMission(null);
	    }// end setLevelIndex(...)

	    /**
	     * @return the playerName
	     */
	    public synchronized String getPlayerName() {
		return playerName;
	    }

	    /**
	     * @param playerName
	     *            the playerName to set
	     */
	    public synchronized void setPlayerName(String playerName) {
		this.playerName = playerName;
	    }

	    

	    /**
	     * @return the difficulty
	     */
	    public synchronized Difficulty getDifficulty() {
		return difficulty;
	    }

	    /**
	     * @param difficulty
	     *            the difficulty to set
	     */
	    public synchronized void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	    }

	    public NAVSystem getNavSystem(){
		return navSystem;
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
		    try{hudSystem = new HUDSystem(tr,tr.getGameShell().getGreenFont(),getDashboardLayout());}
		catch(Exception e){e.printStackTrace();return null;}
		return hudSystem;
	    }

	    public synchronized void boot() throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException {
			// Set up player, HUD, fonts...
			System.out.println("Booting...");
			NDXFile ndx = tr.getResourceManager().getNDXFile("STARTUP\\FONT.NDX");
			upfrontFont = new GLFont(tr.getResourceManager().getFontBIN("STARTUP\\FONT.BIN", ndx),
				    UPFRONT_HEIGHT, ndx.getWidths(), 32,tr);
			
			final EarlyLoadingScreen earlyLoadingScreen = tr.getGameShell().getEarlyLoadingScreen();
			earlyLoadingScreen.setStatusText("Reticulating Splines...");
			earlyLoadingMode = new Object []{
				earlyLoadingScreen
			};
			displayModes.setDisplayMode(earlyLoadingMode);
			
			upfrontDisplay = new UpfrontDisplay(tr.getDefaultGrid(),tr);
			
			satDashboard = new SatelliteDashboard(tr);
			satDashboard.setVisible(false);
			tr.getDefaultGrid().add(satDashboard);
			
			//hudSystem = new HUDSystem(tr,tr.getGameShell().getGreenFont(),layout);
			System.out.println("GameVersion="+tr.config.getGameVersion());
			navSystem = new NAVSystem(tr.getDefaultGrid(), tr,getDashboardLayout());
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
			    rm.setExplosionFactory(new ExplosionSystem(tr));
			    // SMOKE
			    earlyLoadingScreen.setStatusText("Loading smoke assets...");
			    rm.setSmokeSystem(new SmokeSystem(tr));
			    // DEBRIS
			    earlyLoadingScreen.setStatusText("Loading debris assets...");
			    rm.setDebrisSystem(new DebrisSystem(tr));
			    // SETUP PROJECTILE FACTORIES
			    earlyLoadingScreen.setStatusText("Setting up projectile factories...");
			    Weapon[] w = Weapon.values();
			    ProjectileFactory[] pf = new ProjectileFactory[w.length];
			    for (int i = 0; i < w.length; i++) {
				pf[i] = new ProjectileFactory(tr, w[i], ExplosionType.Blast);
			    }// end for(weapons)
			    rm.setProjectileFactories(pf);
			    setPlayer(new Player(tr, tr.getResourceManager().getBINModel(
				    "SHIP.BIN", tr.getGlobalPaletteVL(),null, tr.gpu.get().getGl())));
			    final Camera camera = tr.mainRenderer.get().getCamera();
			    camera.probeForBehavior(MatchPosition.class).setTarget(player);
			    camera.probeForBehavior(MatchDirection.class).setTarget(player);
			    tr.getDefaultGrid().add(player);
			    System.out.println("\t...Done.");
			    levelLoadingScreen	= new LevelLoadingScreen(tr.getDefaultGrid(),tr);
			    final BriefingLayout briefingLayout = tr.config.getGameVersion()==GameVersion.TV?new TVBriefingLayout():new F3BriefingLayout(); 
			    briefingScreen	= new BriefingScreen(tr,tr.getGameShell().getGreenFont(), 
				    briefingLayout);
			    earlyLoadingScreen.setStatusText("Starting game...");
			    
			    introScreen = new IntroScreen(tr,"TITLE.RAW","SEX.MOD");
			    
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
		if (!tr.config.isDebugMode())
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
			// Rube Goldberg style increment
			setLevelIndex(getLevelIndex() + 1);
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

	    public void setCurrentMission(Mission mission) {
		final Mission oldMission = this.currentMission;
		if(oldMission!=null)
		 oldMission.destruct();
		this.currentMission=mission;
		pcSupport.firePropertyChange("currentMission", oldMission, mission);
		
	    }
	    
	    public void abort(){
		tr.setRunState(new GameDestructingMode(){});
		abortCurrentMission();
		Features.destruct(this);
		try{setLevelIndex(-1);}
		catch(Exception e){tr.showStopper(e);}//Shouldn't happen.
		cleanup();
		displayModes.setDisplayMode(emptyMode);
		tr.getGameShell().applyGFXState();
		tr.setRunState(new GameDestructedMode(){});
	    }
	    
	    public DashboardLayout getDashboardLayout(){
		if(dashboardLayout==null)
		    dashboardLayout = 
			tr.config.getGameVersion()==GameVersion.TV?new TVDashboardLayout():new F3DashboardLayout();
		return dashboardLayout;
	    }//end getDashboardLayout()
	    
	    private void cleanup() {
		if(introScreen.isMusicPlaying())
		 introScreen.stopMusic();
		if(player!=null)
		 tr.getDefaultGrid().remove(player);
	    }

	    public void abortCurrentMission(){//TODO: Replace with RunMode PCL
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
	        return upfrontDisplay;
	    }

	    public LevelLoadingScreen getLevelLoadingScreen() {
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
	        final SoundSystem ss = getTr().soundSystem.get();
		ss.setPaused(paused);
		getTr().getThreadManager().setPaused(paused);
		if(paused)
		 upfrontDisplay.submitPersistentMessage("Paused--F3 to Resume");
		else
		 upfrontDisplay.removePersistentMessage();
	        return this;
	    }
	    
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
}// end Game
