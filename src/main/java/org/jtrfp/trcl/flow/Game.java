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

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.BackdropSystem;
import org.jtrfp.trcl.BriefingScreen;
import org.jtrfp.trcl.DisplayModeHandler;
import org.jtrfp.trcl.EarlyLoadingScreen;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.LevelLoadingScreen;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.DebrisFactory;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeFactory;

public class Game {
    private TR 		tr;
    private VOXFile 	vox;
    private int 	levelIndex = 0;
    private String 	playerName;
    private Difficulty 	difficulty;
    private Mission 	currentMission;
    private HUDSystem 	hudSystem;
    private NAVSystem 	navSystem;
    private Player 	player;
    private GLFont	greenFont,upfrontFont;
    private UpfrontDisplay
    			upfrontDisplay;
    private EarlyLoadingScreen
    			earlyLoadingScreen;
    private LevelLoadingScreen
    			levelLoadingScreen;
    private BriefingScreen
    			briefingScreen;
    private final DisplayModeHandler
    			displayModes =
    			new DisplayModeHandler();
    public Object[]	earlyLoadingMode,
    			levelLoadingMode,
    			briefingMode,
    			gameplayMode,
    			performanceReportMode;
    
    private static final int UPFRONT_HEIGHT = 23;
    private final double 	FONT_SIZE=.07;

    public Game(TR tr, VOXFile vox) {
	setTr(tr);
	setVox(vox);
	if (!tr.getTrConfig().isDebugMode())
	    setupNameWithUser();
    }// end constructor

    private void setupNameWithUser() {
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
    }// end setupNameWithUser()

    public void save(File fileToSaveTo) {
	// TODO
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
    public void setTr(TR tr) {
	this.tr = tr;
    }

    /**
     * @return the vox
     */
    public VOXFile getVox() {
	return vox;
    }

    /**
     * @param vox
     *            the vox to set
     */
    public void setVox(VOXFile vox) {
	this.vox = vox;
    }

    /**
     * @return the levelIndex
     */
    public int getLevelIndex() {
	return levelIndex;
    }

    /**
     * @param levelIndex
     *            the levelIndex to set
     */
    public void setLevelIndex(int levelIndex) {
	this.levelIndex = levelIndex;
    }

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

    enum Difficulty {
	EASY, NORMAL, HARD, FURIOUS
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
	return navSystem;
    }
    
    public void setLevel(String skipToLevel) {
	final MissionLevel[] levs = vox.getLevels();
	for (int index = 0; index < levs.length; index++) {
	    if (levs[index].getLvlFile().toUpperCase()
		    .contentEquals(skipToLevel.toUpperCase()))
		setLevelIndex(index);
	}// end for(levs)
    }// end setLevel()
    
    public HUDSystem getHUDSystem(){
	return hudSystem;
    }

    public void go() {
	// Set up player, HUD, fonts...
	System.out.println("Game.go()...");
	System.out.println("Initializing general resources...");
	System.out.println("Activating renderer...");
	tr.renderer.get().activate();
	greenFont = new GLFont(tr.getResourceManager().getFont("OCRA.zip", "OCRA.ttf"),tr);
	NDXFile ndx = tr.getResourceManager().getNDXFile("STARTUP\\FONT.NDX");
	upfrontFont = new GLFont(tr.getResourceManager().getFontBIN("STARTUP\\FONT.BIN", ndx),
		    UPFRONT_HEIGHT, ndx.getWidths(), 32,tr);
	earlyLoadingScreen = new EarlyLoadingScreen(tr.getWorld(), tr, greenFont);
	earlyLoadingScreen.setStatusText("Reticulating Splines...");
	earlyLoadingMode = new Object []{
		earlyLoadingScreen
	};
	displayModes.setDisplayMode(earlyLoadingMode);
	
	upfrontDisplay = new UpfrontDisplay(tr.getWorld(),tr);
	
	hudSystem = new HUDSystem(tr.getWorld(),greenFont);
	hudSystem.deactivate();
	navSystem = new NAVSystem(tr.getWorld(), tr);
	navSystem.deactivate();
	try {
	    // Make color zero translucent.
	    final ResourceManager rm = tr.getResourceManager();
	    final Color[] pal = tr.getGlobalPalette();
	    pal[0] = new Color(0, 0, 0, 0);
	    tr.setGlobalPalette(pal);
	    final BackdropSystem backdrop = new BackdropSystem(tr.getWorld());
	    backdrop.loadingMode();
	    tr.setBackdropSystem(backdrop);
	    // POWERUPS
	    earlyLoadingScreen.setStatusText("Loading powerup assets...");
	    rm.setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
	    // EXPLOSIONS
	    earlyLoadingScreen.setStatusText("Loading explosion assets...");
	    rm.setExplosionFactory(new ExplosionFactory(tr));
	    // SMOKE
	    earlyLoadingScreen.setStatusText("Loading smoke assets...");
	    rm.setSmokeFactory(new SmokeFactory(tr));
	    // DEBRIS
	    earlyLoadingScreen.setStatusText("Loading debris assets...");
	    rm.setDebrisFactory(new DebrisFactory(tr));

	    // SETUP PROJECTILE FACTORIES
	    earlyLoadingScreen.setStatusText("Setting up projectile factories...");
	    Weapon[] w = Weapon.values();
	    ProjectileFactory[] pf = new ProjectileFactory[w.length];
	    for (int i = 0; i < w.length; i++) {
		pf[i] = new ProjectileFactory(tr, w[i], ExplosionType.Blast);
	    }// end for(weapons)
	    rm.setProjectileFactories(pf);
	    player = new Player(tr, tr.getResourceManager().getBINModel(
		    "SHIP.BIN", tr.getGlobalPaletteVL(), tr.gpu.get().getGl()));
	    final Camera camera = tr.renderer.get().getCamera();
	    camera.probeForBehavior(MatchPosition.class).setTarget(player);
	    camera.probeForBehavior(MatchDirection.class).setTarget(player);
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
	    tr.setPlayer(player);
	    tr.getWorld().add(player);
	    System.out.println("\t...Done.");
	    levelLoadingScreen	= new LevelLoadingScreen(tr.getWorld(),tr);
	    briefingScreen	= new BriefingScreen(tr.getWorld(),tr,greenFont);
	    earlyLoadingScreen.setStatusText("Starting game...");
	    levelLoadingMode = new Object[]{
		 levelLoadingScreen,
		 upfrontDisplay
	    };
	    gameplayMode = new Object[]{
		 navSystem,
		 hudSystem
	    };
	    briefingMode = new Object[]{
		 briefingScreen
	    };
	    MissionLevel [] levels = vox.getLevels();
	    while(getLevelIndex()<levels.length){
		try {
		    MissionLevel lvl = levels[getLevelIndex()];
		    final String lvlFileName = lvl.getLvlFile();
		    currentMission = new Mission(tr, this, tr.getResourceManager()
			    .getLVL(lvlFileName),lvlFileName.substring(0, lvlFileName.lastIndexOf('.')));
		    Mission.Result result=null;
		    while(result==null) 
			result = currentMission.go();
		} catch (IllegalAccessException e) {
		    tr.showStopper(e);
		} catch (FileLoadException e) {
		    tr.showStopper(e);
		} catch (IOException e) {
		    tr.showStopper(e);
		}
		//Rube Goldberg style increment
		setLevelIndex(getLevelIndex()+1);
	    }//end while(getLevelIndex<length)
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }// end go()

    public Mission getCurrentMission() {
	return currentMission;
    }
    
    public Player getPlayer(){
	return player;
    }

    public GLFont getGreenFont() {
	return greenFont;
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
}// end Game
