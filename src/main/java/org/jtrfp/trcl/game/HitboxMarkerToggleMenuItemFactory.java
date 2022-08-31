/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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

import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.HitboxMarkerFactory.HitboxMarkerFeature;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class HitboxMarkerToggleMenuItemFactory implements FeatureFactory<GameShell> {
    public static final String [] HITBOX_MARKER_TOGGLE_MENU_ITEM_PATH =  new String[]{"Debug","Show Hitbox Markers (Toggle)"};

    @Override
    public Feature<GameShell> newInstance(GameShell target)
	    throws FeatureNotApplicableException {
	return new HitboxMarkerToggleMenuItemFeature();
    }

    @Override
    public Class<GameShell> getTargetClass() {
	return GameShell.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return HitboxMarkerToggleMenuItemFeature.class;
    }
    
    public static class HitboxMarkerToggleMenuItemFeature implements Feature<GameShell>{
	private MenuItemListener menuItemListener = new MenuItemListener();
	private WeakPropertyChangeListener weakStatePCL;
	private GameShell target;
	private MenuSystem menuSystem;
	private volatile boolean destructed = false;

	@Override
	public void apply(GameShell target) {
	    setTarget(target);
	    final TR tr =  target.getTr();
	    final RootWindow rootWindow = Features.get(tr, RootWindow.class);
	    final MenuSystem menuSystem = Features.get(rootWindow, MenuSystem.class);
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, HITBOX_MARKER_TOGGLE_MENU_ITEM_PATH);
	    menuSystem.addMenuItemListener(menuItemListener, HITBOX_MARKER_TOGGLE_MENU_ITEM_PATH);
	    this.menuSystem = menuSystem;
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakStatePCL);
	    menuSystem.setMenuItemEnabled(true, HITBOX_MARKER_TOGGLE_MENU_ITEM_PATH);
	}//end apply(...)

	@Override
	public void destruct(GameShell target) {
	    if(destructed)
		return;
	    destructed = true;
	    target.getTr().removePropertyChangeListener(weakStatePCL);
	    menuSystem.removeMenuItem(HITBOX_MARKER_TOGGLE_MENU_ITEM_PATH);
	}
	
	private class MenuItemListener implements ActionListener {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		final GameShell target = getTarget();
		final Game game = target.getGame();
		if( game != null ) {
		    final HitboxMarkerFeature toggle = Features.get(game, HitboxMarkerFeature.class);
		    toggle.setHitboxVisibilityEnabled(!toggle.isHitboxVisibilityEnabled());
		}
	    }//end actionPerformed(...)
	}//end MenuItemListener

	protected GameShell getTarget() {
	    return target;
	}

	protected void setTarget(GameShell target) {
	    this.target = target;
	}
	
    }//end HitboxMarkerToggleMenuItemFeature

}//end HitboxMarkerToggleMenuItemFactory
