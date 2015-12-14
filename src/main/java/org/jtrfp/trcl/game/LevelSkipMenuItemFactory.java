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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gui.LevelSkipWindow;
import org.jtrfp.trcl.gui.MenuSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LevelSkipMenuItemFactory implements FeatureFactory<TVF3Game> {
    private MenuSystem      menuSystem;
    private TR              tr;
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
	    getTr().addPropertyChangeListener(TR.RUN_STATE, runStateListener);
	    target.addPropertyChangeListener(TVF3Game.VOX, voxListener);
	}

	@Override
	public void destruct(TVF3Game target) {
	    getTr().removePropertyChangeListener(TR.RUN_STATE,runStateListener);
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.removeMenuItemListener(menuItemListener, MENU_ITEM_PATH);
	    menuSystem.removeMenuItem(MENU_ITEM_PATH);
	    target.removePropertyChangeListener(TVF3Game.VOX, voxListener);
	}
	
	private class MenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
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
    }//end LevelSkipMenuItem

    public MenuSystem getMenuSystem() {
        return menuSystem;
    }

    @Autowired
    public void setMenuSystem(MenuSystem menuSystem) {
        this.menuSystem = menuSystem;
    }

    public TR getTr() {
        return tr;
    }

    @Autowired
    public void setTr(TR tr) {
        this.tr = tr;
    }
    
    private LevelSkipWindow getLevelSkipWindow(){
	if(levelSkipWindow==null)
	    levelSkipWindow = new LevelSkipWindow(getTr());
	return levelSkipWindow;
    }//end getLevelSkipWindow()

}//end LevelSkipMenuItemFactory
