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
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
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
    private TR tr;
    private VOXFile vox;
    private int levelIndex = 0;
    private String playerName;
    private Difficulty difficulty;
    private Mission currentMission;
    private HUDSystem hudSystem;
    private NAVSystem navSystem;
    private Player player;

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

    private void startMissionSequence(String lvlFileName) {
	recursiveMissionSequence(lvlFileName);
    }

    private void recursiveMissionSequence(String lvlFileName) {
	try {
	    currentMission = new Mission(tr, this, tr.getResourceManager()
		    .getLVL(lvlFileName),lvlFileName.substring(0, lvlFileName.lastIndexOf('.')));
	    Mission.Result result = currentMission.go();
	    final String nextLVL = result.getNextLVL();
	    if (nextLVL != null)
		recursiveMissionSequence(nextLVL);
	} catch (IllegalAccessException e) {
	    tr.showStopper(e);
	} catch (FileLoadException e) {
	    tr.showStopper(e);
	} catch (IOException e) {
	    tr.showStopper(e);
	}
    }// end recursiveMissionSequence(...)

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
	hudSystem = new HUDSystem(tr.getWorld());
	navSystem = new NAVSystem(tr.getWorld(),tr);
	hudSystem.deactivate();
	navSystem.deactivate();
	System.out.println("Activating renderer...");
	tr.renderer.get().activate();//TODO: Eventually move this to Game, pending primitives are given valid textureIDs.
	try{
	  //Make color zero translucent.
	    	final ResourceManager rm = tr.getResourceManager();
	    	final Color [] pal = tr.getGlobalPalette();
	    	pal[0]=new Color(0,0,0,0);
	    	tr.setGlobalPalette(pal);
	    	final BackdropSystem backdrop = new BackdropSystem(tr.getWorld());
	    	backdrop.loadingMode();
    		tr.setBackdropSystem(backdrop);
		// POWERUPS
		rm.setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
		/// EXPLOSIONS
		rm.setExplosionFactory(new ExplosionFactory(tr));
		// SMOKE
		rm.setSmokeFactory(new SmokeFactory(tr));
		// DEBRIS
		rm.setDebrisFactory(new DebrisFactory(tr));
		
		//SETUP PROJECTILE FACTORIES
			Weapon [] w = Weapon.values();
			ProjectileFactory [] pf = new ProjectileFactory[w.length];
			for(int i=0; i<w.length;i++){
			    pf[i]=new ProjectileFactory(tr, w[i], ExplosionType.Blast);
			}//end for(weapons)
			rm.setProjectileFactories(pf);
	player =new Player(tr,tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPaletteVL(), tr.gpu.get().getGl()));
	final String startX=System.getProperty("org.jtrfp.trcl.startX");
	final String startY=System.getProperty("org.jtrfp.trcl.startY");
	final String startZ=System.getProperty("org.jtrfp.trcl.startZ");
	final double [] playerPos = player.getPosition();
	if(startX!=null && startY!=null&&startZ!=null){
	    System.out.println("Using user-specified start point");
	    final int sX=Integer.parseInt(startX);
	    final int sY=Integer.parseInt(startY);
	    final int sZ=Integer.parseInt(startZ);
	    playerPos[0]=sX;
	    playerPos[1]=sY;
	    playerPos[2]=sZ;
	    player.notifyPositionChange();
	}//end if(user start point)
	tr.setPlayer(player);
	tr.getWorld().add(player);
	System.out.println("\t...Done.");
	
	startMissionSequence(vox.getLevels()[getLevelIndex()].getLvlFile());
	}catch(Exception e){throw new RuntimeException(e);}
    }// end go()

    public Mission getCurrentMission() {
	return currentMission;
    }
    
    public Player getPlayer(){
	return player;
    }
}// end Game
