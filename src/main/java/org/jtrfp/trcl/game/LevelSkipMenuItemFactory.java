/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.LevelSkipWindow;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class LevelSkipMenuItemFactory implements FeatureFactory<TVF3Game> {
    private MenuSystem      menuSystem;
    private LevelSkipWindow levelSkipWindow;
    
    protected static final String [] MENU_ITEM_PATH = new String [] {"Game","Skip To Level"};

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	return new LevelSkipMenuItem();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return LevelSkipMenuItem.class;
    }
    
    class LevelSkipMenuItem implements Feature<TVF3Game>{
	private final MenuItemListener menuItemListener = new MenuItemListener();
	private final RunStateListener runStateListener = new RunStateListener();
	private final VoxListener      voxListener       = new VoxListener();
	private WeakReference<Game> target;

	@Override
	public void apply(TVF3Game target) {
	    this.target = new WeakReference<Game>(target);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.addMenuItem(MENU_ITEM_PATH);
	    menuSystem.addMenuItemListener(menuItemListener, MENU_ITEM_PATH);
	    final TR tr = target.getTr();
	    tr.addPropertyChangeListener    (TRFactory.RUN_STATE, runStateListener);
	    target.addPropertyChangeListener(TVF3Game.VOX       , voxListener);
	    this.getLevelSkipWindow();//This make sure the cache is set so that display() doesn't get stuck
	}

	@Override
	public void destruct(TVF3Game target) {
	    target.getTr().removePropertyChangeListener(TRFactory.RUN_STATE,runStateListener);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.removeMenuItemListener(menuItemListener, MENU_ITEM_PATH);
	    menuSystem.removeMenuItem(MENU_ITEM_PATH);
	    target.removePropertyChangeListener(TVF3Game.VOX, voxListener);
	}
	
	private class MenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		//TODO: Use non-display() thread.
		getLevelSkipWindow().setVisible(true);
	    }
	}//end MenuItemListener
	
	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final MenuSystem menuSystem = getMenuSystem();
		menuSystem.setMenuItemEnabled(
			evt.getNewValue()   instanceof Game.GameLoadedMode,
			MENU_ITEM_PATH);
	    }//end propertyChange(...)
	}//end RunStateListener
	
	private class VoxListener implements PropertyChangeListener{

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		getLevelSkipWindow().setGame(target.get());
	    }
	    
	}//end VoxListener
	
	public MenuSystem getMenuSystem() {
	    final Frame frame = ((TVF3Game)(target.get())).getTr().getRootWindow();
	    return Features.get(frame, MenuSystem.class);
	}

	private LevelSkipWindow getLevelSkipWindow(){
	    if(levelSkipWindow==null){
		final TVF3Game game = ((TVF3Game)(target.get()));
		final TR tr = game.getTr();
		final LevelSkipWindow levelSkipWindow = new LevelSkipWindow(game.getTr());
		final GameShell gameShell = Features.get(tr,GameShell.class);
		assert gameShell != null;
		levelSkipWindow.setGameShell(gameShell);
		LevelSkipMenuItemFactory.this.levelSkipWindow = levelSkipWindow;
		}
	    return levelSkipWindow;
	}//end getLevelSkipWindow()
    }//end LevelSkipMenuItem
}//end LevelSkipMenuItemFactory
