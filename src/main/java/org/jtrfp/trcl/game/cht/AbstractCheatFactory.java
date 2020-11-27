/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2020 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.game.cht;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gui.MenuSystem;

//@Component
public abstract class AbstractCheatFactory implements FeatureFactory<TVF3Game> {
    protected final String [] menuItemPath;
    private AbstractCheatItem cheatItem;
    
    protected AbstractCheatFactory(String cheatName) {
	menuItemPath = new String [] {"Cheat", cheatName};
    }//end constructor

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	try {
	    cheatItem = (AbstractCheatItem)getFeatureClass().getConstructor(this.getClass()).newInstance(this);
	    final Frame frame = (target).getTr().getRootWindow();
	    cheatItem.setMenuSystem(Features.get(frame, MenuSystem.class));
	    cheatItem.setMenuItemPath(menuItemPath);
	} catch(InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e)
		{e.printStackTrace();}
	return cheatItem;
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }
    
    protected static abstract class AbstractCheatItem implements Feature<TVF3Game>{
	private final MenuItemListener menuItemListener = new MenuItemListener();
	private final RunStateListener runStateListener = new RunStateListener();
	//private final VoxListener      voxListener       = new VoxListener();
	private WeakReference<TVF3Game> target;
	private MenuSystem             menuSystem;
	private String []              menuItemPath;
	//private TVF3Game game;
	
	public void setMenuItemPath(String [] path) {
	    this.menuItemPath = path;
	}

	@Override
	public void apply(TVF3Game target) {
	    this.target = new WeakReference<TVF3Game>(target);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, menuItemPath);
	    menuSystem.addMenuItemListener(menuItemListener, menuItemPath);
	    final TR tr = target.getTr();
	    tr.addPropertyChangeListener    (TRFactory.RUN_STATE, runStateListener);
	    //target.addPropertyChangeListener(TVF3Game.VOX       , voxListener);
	    //this.getLevelSkipWindow();//This make sure the cache is set so that display() doesn't get stuck
	}

	@Override
	public void destruct(TVF3Game target) {
	    target.getTr().removePropertyChangeListener(TRFactory.RUN_STATE,runStateListener);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.removeMenuItemListener(menuItemListener, menuItemPath);
	    menuSystem.removeMenuItem(menuItemPath);
	   // target.removePropertyChangeListener(TVF3Game.VOX, voxListener);
	}
	
	protected TVF3Game getTarget() {
	    return target.get();
	}
	
	private class MenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		//XXX: Is this the right thread to be calling from?
		invokeCheat();
	    }
	}//end MenuItemListener
	
	protected abstract void invokeCheat();
	
	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final MenuSystem menuSystem = getMenuSystem();
		menuSystem.setMenuItemEnabled(
			evt.getNewValue()   instanceof Game.GameLoadedMode,
			menuItemPath);
	    }//end propertyChange(...)
	}//end RunStateListener
	/*
	private class VoxListener implements PropertyChangeListener{

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		getLevelSkipWindow().setGame(target.get());
	    }
	    
	}//end VoxListener
	*/
	public MenuSystem getMenuSystem() {
	    return menuSystem;
	}
	
	public void setMenuSystem(MenuSystem menuSystem) {
	    this.menuSystem = menuSystem;
	}
    }//end LevelSkipMenuItem
}//end LevelSkipMenuItemFactory
