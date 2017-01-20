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

package org.jtrfp.trcl.shell;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFactory.TRRunState;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class TVF3GameMenuItemFactory implements FeatureFactory<GameShell> {
    public static final String [] NEW_GAME_PATH = new String[]{"Game","New TV/F3 Game"};
    //private MenuSystem menuSystem;
    //private TR         tr;
    
    @Override
    public Feature<GameShell> newInstance(GameShell target) {
	final TVF3GameMenuItem result = new TVF3GameMenuItem();
	return result;
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
	private MenuSystem menuSystem;

	@Override
	public void apply(GameShell target) {
	    final Frame frame = target.getTr().getRootWindow();
	    setMenuSystem(Features.get(frame, MenuSystem.class));
	    setTarget(target);
	    installMenuItems();
	    installMenuItemListeners();
	    reEvaluateRunState();
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
		reEvaluateRunState();
	    }//end propertyChange(...)
	}//end PropertyChangeListener

	private void reEvaluateRunState(){
	    final TRRunState runState = getTr().getRunState();
	    getMenuSystem().setMenuItemEnabled(!(runState instanceof Game.GameRunMode) &&
		    runState instanceof GameShellFactory.GameShellConstructed, 
		    NEW_GAME_PATH);
	}//end reEvaluateRunState()
	
	private final NewGameListener newGameListener  = new NewGameListener();
	private final TRStateListener trStateListener  = new TRStateListener();
	
	    private void installMenuItems(){
		getMenuSystem().addMenuItem(MenuSystem.BEGINNING, NEW_GAME_PATH);
	    }
	    
	    private void installMenuItemListeners(){
		getMenuSystem().addMenuItemListener(newGameListener, NEW_GAME_PATH);
		getTr().addPropertyChangeListener(TRFactory.RUN_STATE, trStateListener);
	    }
	    
	    private void uninstallMenuItems(){
		getMenuSystem().removeMenuItem(NEW_GAME_PATH);
	    }
	    
	    public TR getTr(){
		return target.getTr();
	    }
	    
	    public MenuSystem getMenuSystem() {
		return menuSystem;
	    }

	    public void setMenuSystem(MenuSystem menuSystem) {
	        this.menuSystem = menuSystem;
	    }
	
    }//end TVF3GameMenuItem
}//end TVF3GameMenuItemFactory
