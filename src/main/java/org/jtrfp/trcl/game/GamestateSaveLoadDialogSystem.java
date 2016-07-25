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

package org.jtrfp.trcl.game;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.game.Game.CanceledException;
import org.jtrfp.trcl.game.GameConfigRootFactory.GameConfigRootFeature;
import org.jtrfp.trcl.miss.GamePauseFactory.GamePause;

public class GamestateSaveLoadDialogSystem {
    private static final String SAVE_PATH_PROPERTY 
        = "org.jtrfp.trcl.game.GamestateSaveLoad.defaultSavePath";
    private JFileChooser   chooser;
    private JFrame         rootFrame;
    private Game           game;
    private GameConfigRootFeature configRoot;

    public void save(){
	final GameConfigRootFeature configRoot = getConfigRoot();
	final String path = configRoot.getConfigSaveURI();
	final GamePause gamePause = Features.get(game.getCurrentMission(),GamePause.class);
	if(gamePause != null)
	    gamePause.proposePause(true);
	final File   file = doSaveDialog(path);
	if(file == null)
	    return;
	System.out.println("Saving to "+file.getAbsolutePath());
	if(!file.getAbsolutePath().contentEquals(path)){
	    String newPath = file.getAbsolutePath();
	    if(!newPath.toLowerCase().endsWith(GameConfigRootFactory.SAVE_URI_SUFFIX.toLowerCase()))
		newPath += GameConfigRootFactory.SAVE_URI_SUFFIX;
	    configRoot.setConfigSaveURI(newPath);
	    }
	try{configRoot.saveConfigurations();}
	catch(IOException e){
	    e.printStackTrace();//TODO: Use a JDialog?
	}
    }//end save()

    public void load(){
	final GameConfigRootFeature configRoot = getConfigRoot();
	final String path = configRoot.getConfigSaveURI();
	final GamePause gamePause = Features.get(game.getCurrentMission(),GamePause.class);
	if(gamePause != null)
	    gamePause.proposePause(true);
	final File   file = doLoadDialog(path);
	System.out.println("Selected file is "+file);
	if(file == null)
	    return;
	if(!file.getAbsolutePath().contentEquals(path))
	    configRoot.setConfigSaveURI(file.getAbsolutePath());
	final Game game = getGame();
	System.out.println("State loader: Aborting current mission. Game hash="+game.hashCode());
	game.abortCurrentMission();
	configRoot.loadConfigurations();
	System.out.println("Loader calling doGamePlay()...");
	try{game.doGameplay();}
	catch(CanceledException e){}
	catch(Exception e){e.printStackTrace();}
    }//end load()
    
    protected File doSaveDialog(String path){
	final JFileChooser chooser = getChooser();
	chooser.setSelectedFile(null);
	final int           result = chooser.showSaveDialog(getRootFrame());
	if(result == JFileChooser.APPROVE_OPTION)
	    return chooser.getSelectedFile();
	return null;
    }//end doSaveDialog(...)
    
    protected File doLoadDialog(String path){
	final JFileChooser chooser = getChooser();
	chooser.setSelectedFile(null);
	final int           result = chooser.showOpenDialog(getRootFrame());
	if(result == JFileChooser.APPROVE_OPTION)
	    return chooser.getSelectedFile();
	return null;
    }//end doLoadDialog(...)
    
    protected JFileChooser getChooser() {
	if(chooser == null){
	    chooser = new JFileChooser();
	    chooser.setFileFilter(new GamestateFileFilter());
	    }
        return chooser;
    }

    protected void setChooser(JFileChooser chooser) {
        this.chooser = chooser;
    }

    protected JFrame getRootFrame() {
        return rootFrame;
    }

    protected void setRootFrame(JFrame rootFrame) {
        this.rootFrame = rootFrame;
    }
    
    private static class GamestateFileFilter extends FileFilter {
	@Override
	public boolean accept(File file) {
	    return file.getName().endsWith(GameConfigRootFactory.SAVE_URI_SUFFIX) ||
		   file.isDirectory();
	}

	@Override
	public String getDescription() {
	    return "TV/F3 Game Savestate";
	}
    }//end GamestateFileFilter

    protected Game getGame() {
        return game;
    }

    protected void setGame(Game game) {
        this.game = game;
    }

    protected GameConfigRootFeature getConfigRoot() {
        return configRoot;
    }

    protected void setConfigRoot(GameConfigRootFeature configRoot) {
        this.configRoot = configRoot;
    }
}//end GamestateSaveLoad
