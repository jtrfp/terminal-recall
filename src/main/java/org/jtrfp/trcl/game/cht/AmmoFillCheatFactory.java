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

import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.UpgradeableProjectileFiringBehavior;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class AmmoFillCheatFactory implements FeatureFactory<TVF3Game> {
    private AmmoFillCheatGroup cheatItem;
    private final Weapon [] playerWeapons = new Weapon[7];
    
    public AmmoFillCheatFactory() {
	final Weapon [] all = Weapon.values();
	final int n = all.length;
	for(int i = 0  ; i < n ; i++) {
	    Weapon w = all[i];
	    if(w.getButtonToSelect() != -1)
		playerWeapons[w.getButtonToSelect()-1] = w;
	}//end for(i)
    }//end constructor

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	    //cheatItem = (AbstractCheatItem)getFeatureClass().getConstructor(this.getClass()).newInstance(this);
	    final Frame frame = (target).getTr().getRootWindow();
	    cheatItem = new AmmoFillCheatGroup(Features.get(frame, MenuSystem.class));
	    //cheatItem.setMenuItemPath(menuItemPath);
	return cheatItem;
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }
    
    protected class AmmoFillCheatGroup implements Feature<TVF3Game> {
	private AmmoFillCheatItem [] items = new AmmoFillCheatItem[6];
	
	public AmmoFillCheatGroup(MenuSystem menuSystem) {
	    final int n = items.length;
	    for(int i  = 0 ; i < n; i++) {
		items[i] = new AmmoFillCheatItem(i);
		items[i].setMenuSystem(menuSystem);
	    }//end for(i)
	}

	@Override
	public void apply(TVF3Game target) {
	    final int n = items.length;
	    for(int i  = 0 ; i < n; i++) {
		items[i].apply(target);
	    }//end for(i)
	}

	@Override
	public void destruct(TVF3Game target) {
	    final int n = items.length;
	    for(int i  = 0 ; i < n; i++) {
		items[i].destruct(target);
	    }//end for(i)
	}
    }//end AmmoFillCheatGroup
    
    protected class AmmoFillCheatItem implements Feature<TVF3Game>{
	private final MenuItemListener menuItemListener = new MenuItemListener();
	private final RunStateListener runStateListener = new RunStateListener();
	//private final VoxListener      voxListener       = new VoxListener();
	private WeakReference<TVF3Game> target;
	private MenuSystem             menuSystem;
	private String []              menuItemPath;
	private final int 	       weaponIndex;
	//private TVF3Game game;
	
	public AmmoFillCheatItem(int weaponIdx) {
	    weaponIndex = weaponIdx;
	}

	@Override
	public void apply(TVF3Game target) {
	    this.target = new WeakReference<TVF3Game>(target);
	    final MenuSystem menuSystem = getMenuSystem();
	    final Player player = target.getPlayer();
	    this.menuItemPath = new String [] {"Cheat", "Fill "+getWeaponName(player, target)};
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, menuItemPath);
	    menuSystem.addMenuItemListener(menuItemListener, menuItemPath);
	    final TR tr = target.getTr();
	    tr.addPropertyChangeListener    (TRFactory.RUN_STATE, runStateListener);
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
	
	private String getWeaponName(Player player, Game game) {
	    if(game instanceof TVF3Game) {
		final TVF3Game tvf3 = (TVF3Game)game;
		final GameVersion gv = tvf3.getGameVersion();
		final Weapon weaponEnum = playerWeapons[weaponIndex];
		if(gv == GameVersion.F3 || gv == GameVersion.FURYSE)
		    return weaponEnum.getF3DisplayName();
		else if(gv == GameVersion.TV)
		    return weaponEnum.getTvDisplayName();
		return weaponEnum.toString();
	    }
	    return "#"+(weaponIndex+1);
	}//end getWeaponName()
	
	private void invokeCheat() {
	    final Player player = target.get().getPlayer();
	    final ProjectileFiringBehavior [] weapons = player.getWeapons();
	    final ProjectileFiringBehavior weapon = weapons[weaponIndex];
	    
	    if(weapon != null) {
		    weapon.setAmmoLimit(Integer.MAX_VALUE);
		    weapon.setAmmo(Integer.MAX_VALUE);
		    if(weapon instanceof UpgradeableProjectileFiringBehavior ) {
			final UpgradeableProjectileFiringBehavior upfb = (UpgradeableProjectileFiringBehavior)weapon;
			upfb.setCapabilityLevel(upfb.getMaxCapabilityLevel());
		    }
		    final UpfrontDisplay disp = getTarget().getUpfrontDisplay();
		    disp.submitMomentaryUpfrontMessage("Fill "+getWeaponName(player, target.get()));
		  //Ding
		    final TR tr = getTarget().getTr();
		    final SoundSystemFeature ssf = Features.get(tr, SoundSystemFeature.class);
		    ssf.enqueuePlaybackEvent(ssf.getPlaybackFactory().create(tr.getResourceManager().soundTextures.get("POWER-1.WAV"), SoundSystem.DEFAULT_SFX_VOLUME_STEREO));
		}//end if(weapon != null)
	    
	}//end invokeCheat()
	
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

    @Override
    public Class<AmmoFillCheatGroup> getFeatureClass() {
	return AmmoFillCheatGroup.class;
    }
}//end LevelSkipMenuItemFactory
