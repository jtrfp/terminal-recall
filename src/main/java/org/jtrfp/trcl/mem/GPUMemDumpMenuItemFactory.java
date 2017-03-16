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

package org.jtrfp.trcl.mem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.MenuSystem;
import org.springframework.stereotype.Component;

@Component
public class GPUMemDumpMenuItemFactory implements FeatureFactory<MenuSystem> {
    protected static final String [] MEM_DUMP_MENU_ITEM_PATH = new String[]{"Debug","Dump GPU Memory"};

    @Override
    public Feature<MenuSystem> newInstance(MenuSystem target)
	    throws FeatureNotApplicableException {
	return new GPUMemDumpMenuItemFeature();
    }

    @Override
    public Class<MenuSystem> getTargetClass() {
	return MenuSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GPUMemDumpMenuItemFeature.class;
    }
    
    public static class GPUMemDumpMenuItemFeature implements Feature<MenuSystem>{
	private WeakReference<MenuSystem> target;
	private MenuItemListener menuItemListener;
	private TR tr;
	
	@Override
	public void apply(MenuSystem target) {
	    this.target = new WeakReference<MenuSystem>(target);
	    setTr(Features.get(Features.getSingleton(), TR.class));
	    target.addMenuItem(MenuSystem.MIDDLE, MEM_DUMP_MENU_ITEM_PATH);
	    target.setMenuItemEnabled(true, MEM_DUMP_MENU_ITEM_PATH);
	    menuItemListener = new MenuItemListener();
	    target.addMenuItemListener(menuItemListener, MEM_DUMP_MENU_ITEM_PATH);
	}

	@Override
	public void destruct(MenuSystem target) {
	    final MenuSystem menuSystem = Features.get(Features.getSingleton(), MenuSystem.class);
	    menuSystem.removeMenuItemListener(menuItemListener, MEM_DUMP_MENU_ITEM_PATH);
	}
	
	protected class MenuItemListener implements ActionListener {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		final TR tr = getTr();
		if(tr != null)
		 try{new GPUMemDump(tr).dumpRootMemory();}
		catch(Exception e){e.printStackTrace();}
	    }
	}//end MenuItemListener

	public TR getTr() {
	    return tr;
	}

	public void setTr(TR tr) {
	    this.tr = tr;
	}
    }//end GPUMemDumpMenuItemFeature

}//GPUMemDumpMenuItemFactory
