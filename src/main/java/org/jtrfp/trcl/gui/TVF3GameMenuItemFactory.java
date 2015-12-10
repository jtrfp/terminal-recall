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

package org.jtrfp.trcl.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.GameShell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TVF3GameMenuItemFactory implements FeatureFactory<GameShell> {
    public static final String [] NEW_GAME_PATH = new String[]{"Game","New TV/F3 Game"};
    private MenuSystem menuSystem;
    private TR         tr;
    
    @Override
    public Feature<GameShell> newInstance(GameShell target) {
	return new TVF3GameMenuItem();
    }

    @Override
    public Class<GameShell> getTargetClass() {
	return GameShell.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TVF3GameMenuItem.class;
    }
    
    public class TVF3GameMenuItem implements Feature<GameShell>{
	private GameShell target;

	@Override
	public void apply(GameShell target) {
	    System.out.println("TVF3GameMenuItem.apply()");
	    setTarget(target);
	    installMenuItems();
	    installMenuItemListeners();
	}

	@Override
	public void destruct(GameShell target) {
	    uninstallMenuItems();
	}

	public GameShell getTarget() {
	    return target;
	}

	public void setTarget(GameShell target) {
	    this.target = target;
	}
	
	private class NewGameListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		getTr().getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			TVF3GameMenuItem.this.getTarget().newGame(null);
			return null;
		    }});
	    }//end actionPerforment(...)
	}//end NewGameListener
	
	private class TRStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		getMenuSystem().setMenuItemEnabled(
			!(evt.getNewValue() instanceof Game.GameRunMode) &&
			evt.getNewValue() instanceof GameShell.GameShellConstructed, 
			NEW_GAME_PATH);
	    }//end propertyChange(...)
	}//end PropertyChangeListener
	
	private final NewGameListener newGameListener  = new NewGameListener();
	private final TRStateListener trStateListener  = new TRStateListener();
	
	    private void installMenuItems(){
		getMenuSystem().addMenuItem(NEW_GAME_PATH);
	    }
	    
	    private void installMenuItemListeners(){
		getMenuSystem().addMenuItemListener(newGameListener, NEW_GAME_PATH);
		getTr().addPropertyChangeListener(TR.RUN_STATE, trStateListener);
	    }
	    
	    private void uninstallMenuItems(){
		getMenuSystem().removeMenuItem(NEW_GAME_PATH);
	    }
	
    }//end TVF3GameMenuItem
    
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
    
}//end TVF3GameMenuItemFactory
