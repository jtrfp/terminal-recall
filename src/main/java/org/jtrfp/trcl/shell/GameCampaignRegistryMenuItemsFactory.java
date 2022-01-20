/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.GameResourcePODRegistry;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFactory.TRRunState;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.shell.GameCampaignRegistryFactory.GameCampaignRegistry;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class GameCampaignRegistryMenuItemsFactory
	implements FeatureFactory<GameCampaignRegistry> {
    
    public class GameCampaignRegistryMenuItems implements Feature<GameCampaignRegistry> {
	@Setter
	private CollectionActionDispatcher<GameCampaignData> campaigns;
	private final GameCampaignMenuItemMappingCollection mapper = new GameCampaignMenuItemMappingCollection();
	private MenuSystem menuSystem;
	private final Collection<MenuItemHandler> handlers = new ArrayList<>();
	private GameShell gameShell;
	private TR tr;
	private final TRStateListener trStateListener  = new TRStateListener();
	private RootWindow rw;
	
	public GameCampaignRegistryMenuItems() {
	    tr          = Features.get(Features.getSingleton(),TR.class);
	    rw = Features.get(tr, RootWindow.class);
	    menuSystem  = Features.get(rw, MenuSystem.class);
	    gameShell   = Features.get(tr, GameShell.class);
	}//end constructor

	@Override
	public void apply(GameCampaignRegistry target) {
	    setCampaigns(target.getCampaigns());
	    campaigns.addTarget(mapper, true);
	    
	    tr.addPropertyChangeListener(TRFactory.RUN_STATE, trStateListener);
	    reEvaluateRunState();
	}//end apply(...)
	
	private class TRStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		reEvaluateRunState();
	    }//end propertyChange(...)
	}//end PropertyChangeListener
	
	private void reEvaluateRunState(){
	    final TRRunState runState = tr.getRunState();
	    for(MenuItemHandler handler : handlers) {
		final boolean enable = !(runState instanceof Game.GameRunMode) &&
			    runState instanceof GameShellFactory.GameShellConstructed;
		menuSystem.setMenuItemEnabled(enable, handler.getPath());
	    }//end for(menuItems)
	}//end reEvaluateRunState()
	
	private class MenuItemHandler implements ActionListener {
	    private final GameCampaignData gameCampaignData;
	    @Getter
	    private final String [] path;
	    
	    public MenuItemHandler(GameCampaignData gameResourceData, String[] path) {
		this.gameCampaignData = gameResourceData;
		this.path = path;
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if(gameCampaignData.getPodURIs().isEmpty()) {
		    JOptionPane.showMessageDialog(rw, "This game campaign's resource data specifies no POD sources.\n"
		    	+ "To specify POD sources, navigate to File->Features->GameCampaignRegistry\n...and add POD URIs by editing the item `"+gameCampaignData.getName()+"`", 
		    	"No PODs Specified",
		    	JOptionPane.WARNING_MESSAGE);
		    return;
		}
		final ResourceManager rm = tr.getResourceManager();
		rm.clearCaches();
		rm.setPodRegistry(new GameResourcePODRegistry(gameCampaignData));
		TRFactory.nuclearGC();
		final String voxURI = gameCampaignData.getVoxURI();
		VOXFile vox = null;
		if(voxURI != null) {
		    if(voxURI.length() > 0)
			try {vox = rm.getVOXFile(voxURI);}
		    catch(FileLoadException | IOException | IllegalAccessException ex) {
			JOptionPane.showMessageDialog(rw, 
				"Could not load custom VOX URI; attempting to to use a default VOX.\n"+voxURI+"\nMessage was: "+ex.getLocalizedMessage(), 
				"VOX Load Error", 
				JOptionPane.WARNING_MESSAGE);
			}
		}//end if(!null)
		gameShell.newGame(vox, gameCampaignData.getGameVersion());
	    }//end actionPerformed
	    
	}//end MenuItemListenerImpl

	@Override
	public void destruct(GameCampaignRegistry target) {
	    campaigns.removeTarget(mapper, true);
	}
	
	public class GameCampaignMenuItemMappingCollection implements Collection<GameCampaignData> {
	    

	    @Override
	    public int size() {
		return campaigns.size();
	    }

	    @Override
	    public boolean isEmpty() {
		return campaigns.isEmpty();
	    }

	    @Override
	    public boolean contains(Object o) {
		return campaigns.contains(o);
	    }

	    @Override
	    public Iterator<GameCampaignData> iterator() {
		return campaigns.iterator();
	    }

	    @Override
	    public Object[] toArray() {
		return campaigns.toArray();
	    }

	    @Override
	    public <T> T[] toArray(T[] a) {
		return campaigns.toArray(a);
	    }

	    @Override
	    public boolean add(GameCampaignData e) {
		final String [] menuItem = {"Game", "New "+e.getName()+" Game"};//TODO: Create a configurator for customizing this
		final MenuItemHandler mil = new MenuItemHandler(e, menuItem);
		menuSystem.addMenuItem(MenuSystem.BEGINNING, menuItem);
		handlers.add(mil);
		menuSystem.addMenuItemListener(mil, mil.getPath());
		reEvaluateRunState();
		return true;
	    }

	    @Override
	    public boolean remove(Object o) {
		boolean result = false;
		if( o instanceof GameCampaignData ) {
		    final GameCampaignData grd = (GameCampaignData)o;
		    final String [] menuItem = {"Game", "New "+grd.getName()+" Game"};
		    final Iterator<MenuItemHandler> it = handlers.iterator();
		    MenuItemHandler menuItemHandler;
		    while(it.hasNext()) {
			menuItemHandler = it.next();
			if(Arrays.equals(menuItemHandler.getPath(), menuItem)) {
			    it.remove();
			    menuSystem.removeMenuItemListener(menuItemHandler, menuItemHandler.getPath());
			    menuSystem.removeMenuItem(menuItemHandler.getPath());
			    result = true;
			}
		    }//end while(it.hasNext())
		}//end if(GameCampaignData)
		return result;
	    }//end remove(...)

	    @Override
	    public boolean containsAll(Collection<?> c) {
		return campaigns.containsAll(c);
	    }

	    @Override
	    public boolean addAll(Collection<? extends GameCampaignData> c) {
		boolean result = false;
		for(GameCampaignData d : c)
		    result |= add(d);
		return result;
	    }

	    @Override
	    public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for(Object d : c)
		    result |= remove(d);
		return result;
	    }

	    @Override
	    public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Please file a bug report.");
	    }

	    @Override
	    public void clear() {//Low-volume, low-frequency operation, no need to be efficient here.
		MenuItemHandler [] items = handlers.toArray(new MenuItemHandler [handlers.size()]);
		for(MenuItemHandler menuItem : items)
		    remove(menuItem.gameCampaignData);
	    }//end clear()
	    
	}//end GameCampaignMenuItemMappingCollection
	
    }//end GameCampaignRegistryMenuItems

    @Override
    public Feature<GameCampaignRegistry> newInstance(
	    GameCampaignRegistry target) throws FeatureNotApplicableException {
	return new GameCampaignRegistryMenuItems();
    }

    @Override
    public Class<GameCampaignRegistry> getTargetClass() {
	return GameCampaignRegistry.class;
    }

    @Override
    public Class<? extends Feature<GameCampaignRegistry>> getFeatureClass() {
	return GameCampaignRegistryMenuItems.class;
    }

}//end GameCampaignRegistryMenuItemFactory
