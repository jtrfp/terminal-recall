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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;

import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.game.GameConfigRootFactory.GameConfigRootFeature;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class GamestateSaveLoadMenuItemFactory implements FeatureFactory<TVF3Game> {
    static final String [] LOAD_GAME_PATH = new String[]{"Game","Load State..."};
    static final String [] SAVE_GAME_PATH = new String[]{"Game","Save State..."};
    
    public static class GameSaveLoadMenuItemFeature implements Feature<TVF3Game>{
	private GamestateSaveLoadDialogSystem dialogSystem;
	private LoadGameMenuItemListener   loadGameMenuItemListener;
	private SaveGameMenuItemListener   saveGameMenuItemListener;
	private ExecutorService            executor;
	private GameShell                  gameShell;
	private MenuSystem                 menuSystem;
	//HARD REFERENCES; DO NOT REMOVE
	@SuppressWarnings("unused")
	private RunStateListener           runStateListener;
	@SuppressWarnings("unused")
	private WeakPropertyChangeListener weakRunStateListener;
	private boolean                    destructed = false;

	@Override
	public void apply(TVF3Game target) {
	    final GamestateSaveLoadDialogSystem ds;
	    setDialogSystem(ds = new GamestateSaveLoadDialogSystem());
	    ds.setGame(target);
	    ds.setConfigRoot(Features.get(target, GameConfigRootFeature.class));
	    
	    final GameShell gameShell = target.getGameShell();
	    setGameShell(gameShell);
	    final TR tr = gameShell.getTr();
	    final RootWindow rootWindow = Features.get(tr,         RootWindow.class);
	    final MenuSystem menuSystem = Features.get(rootWindow, MenuSystem.class);
	    final ThreadManager threadManager = Features.get(tr, ThreadManager.class);
	    setMenuSystem(menuSystem);
	    setExecutor  (threadManager.threadPool);
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, LOAD_GAME_PATH);
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, SAVE_GAME_PATH);
	    menuSystem.addMenuItemListener(loadGameMenuItemListener = new LoadGameMenuItemListener(), LOAD_GAME_PATH);
	    menuSystem.addMenuItemListener(saveGameMenuItemListener = new SaveGameMenuItemListener(), SAVE_GAME_PATH);
	    
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener = new WeakPropertyChangeListener(runStateListener = new RunStateListener(),tr));
	    
	    menuSystem.setMenuItemEnabled(false, LOAD_GAME_PATH);
	    menuSystem.setMenuItemEnabled(false, SAVE_GAME_PATH);
	}//end apply()

	@Override
	public void destruct(TVF3Game target) {
	    destructed = true;
	    final TR tr = target.getGameShell().getTr();
	    final RootWindow rootWindow = Features.get(tr,         RootWindow.class);
	    final MenuSystem menuSystem = Features.get(rootWindow, MenuSystem.class);
	    menuSystem.removeMenuItemListener(loadGameMenuItemListener, LOAD_GAME_PATH);
	    menuSystem.removeMenuItemListener(saveGameMenuItemListener, SAVE_GAME_PATH);
	    menuSystem.removeMenuItem(LOAD_GAME_PATH);
	    menuSystem.removeMenuItem(SAVE_GAME_PATH);
	}//end destruct()
	
	class LoadGameMenuItemListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		final GamestateSaveLoadDialogSystem ds = getDialogSystem();
		final ExecutorService executor = getExecutor();
		
		executor.submit(new Runnable(){
		    @Override
		    public void run() {
			try{
			    System.out.println("Game loading executed by thread "+Thread.currentThread().getName());
			    System.out.println("Loader calling dialogSystem.load().");
			    ds.load();
			}catch(Exception e){e.printStackTrace();}
		    }});
	    }//end actionPerformed
	}//end MenuItemListener
	
	class SaveGameMenuItemListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		final GamestateSaveLoadDialogSystem ds = getDialogSystem();
		System.out.println("SaveGAmeMenuItemListener thread="+Thread.currentThread().getName());
		final ExecutorService executor = getExecutor();
		executor.submit(new Runnable(){
		    @Override
		    public void run() {
			System.out.println("Thread "+Thread.currentThread().getName()+" performing save...");
			ds.save();
			System.out.println("Save complete.");
		    }});
	    }
	}//end MenuItemListener

	protected GamestateSaveLoadDialogSystem getDialogSystem() {
	    return dialogSystem;
	}

	protected void setDialogSystem(GamestateSaveLoadDialogSystem dialogSystem) {
	    this.dialogSystem = dialogSystem;
	}

	protected ExecutorService getExecutor() {
	    return executor;
	}

	protected void setExecutor(ExecutorService executor) {
	    this.executor = executor;
	}

	protected GameShell getGameShell() {
	    return gameShell;
	}

	protected void setGameShell(GameShell gameShell) {
	    this.gameShell = gameShell;
	}
	
	private class RunStateListener implements PropertyChangeListener {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(destructed)
		    return;
		final Object newValue = evt.getNewValue();
		//Can't Save In Tunnel! (or in chamber)
		final boolean loadEnabled = (newValue instanceof Game.GameLoadedMode);
		getMenuSystem().setMenuItemEnabled(loadEnabled, LOAD_GAME_PATH);
		final boolean saveEnabled = (
			(newValue  instanceof Mission.OverworldState) &&
			!(newValue instanceof Mission.SatelliteState));
		getMenuSystem().setMenuItemEnabled(saveEnabled, SAVE_GAME_PATH);
	    }
	}//end RunStateListener

	protected MenuSystem getMenuSystem() {
	    return menuSystem;
	}

	protected void setMenuSystem(MenuSystem menuSystem) {
	    this.menuSystem = menuSystem;
	}
    }//end GameSaveLoadMenuItemFeature

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	return new GameSaveLoadMenuItemFeature();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return GameSaveLoadMenuItemFeature.class;
    }

}//end GamestateSaveLoadMenuItemFactory
