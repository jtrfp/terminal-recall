/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh.ui;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.core.ControllerInput;
import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.WorldObject;

public class UserInputWeaponSelectionBehavior extends Behavior implements PlayerControlBehavior{
    public static final String FIRE = "Fire";
    
    private final ControllerInput fire;
    
    private ProjectileFiringBehavior [] behaviors;
    private ProjectileFiringBehavior activeBehavior;
    private ProjectileFiringBehavior defaultBehavior;
    private int ammoDisplayUpdateCounter=0;
    public static final int AMMO_DISPLAY_UPDATE_INTERVAL_MS=80;
    private static final int AMMO_DISPLAY_COUNTER_INTERVAL=(int)Math.ceil(AMMO_DISPLAY_UPDATE_INTERVAL_MS/ (1000./ThreadManager.GAMEPLAY_FPS));
    
    
    public UserInputWeaponSelectionBehavior(ControllerInputs controllerInputs){
	fire = controllerInputs.getInput(FIRE);
    }//end constuctor
    
    @Override
    public void tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final KeyStatus keyStatus = parent.getTr().getKeyStatus();
	if(++ammoDisplayUpdateCounter%AMMO_DISPLAY_COUNTER_INTERVAL==0){
	    final int ammo = getActiveBehavior().getAmmo();
	    parent.getTr().getGame().getHUDSystem().getAmmo().setContent(""+(ammo!=-1?ammo:"INF"));
	}//end if(update ammo display)
	for(int k=0; k<7;k++){
	    if(keyStatus.isPressed(KeyEvent.VK_1+k)){
		final ProjectileFiringBehavior proposed = behaviors[k];
		boolean force = false;
		//if(proposed instanceof UpgradeableProjectileFiringBehavior)
		//    force = ((UpgradeableProjectileFiringBehavior)proposed).isLimitlessBottomLevel() && proposed.canFire();
		if(proposed!=null)
		 setActiveBehavior(proposed,force);
	    }//end if (selection key is pressed)
	}//end for(keys)
	if(fire.getState()>.75){
	//if(keyStatus.isPressed(KeyEvent.VK_SPACE)){
	    if(!getActiveBehavior().canFire())
		setActiveBehavior(getDefaultBehavior(),true);
	   getActiveBehavior().requestFire();
	}//end if(SPACE)
    }//end _tick(...)
    
    public boolean setActiveBehavior(ProjectileFiringBehavior newBehavior, boolean force){
	if(force || (activeBehavior!=newBehavior && (newBehavior.canFire() || newBehavior == getDefaultBehavior()) )){
	    activeBehavior=newBehavior;
	    final Weapon w = activeBehavior.getProjectileFactory().getWeapon();
	    final TR tr = getParent().getTr();
	    String content="???";
	    switch(tr.config.getGameVersion()){//TODO: Get from Game object instead.
	    case F3:{
		content=w.getF3DisplayName();
		break;
	       }
	    case TV:{
		content=w.getTvDisplayName();
		break;
	        }
	    }//end switch(game version)
	    tr.getGame().getHUDSystem().getWeapon().setContent(content);
	    return true;
	}//end if(New Behavior)
	return false;
    }//end proposeSwitchTo(...)
    /**
     * @return the behaviors
     */
    public ProjectileFiringBehavior[] getBehaviors() {
        return behaviors;
    }
    /**
     * @param behaviors the behaviors to set
     */
    public UserInputWeaponSelectionBehavior setBehaviors(ProjectileFiringBehavior[] behaviors) {
        this.behaviors = behaviors;
        return this;
    }

    /**
     * @return the defaultBehavior
     */
    public ProjectileFiringBehavior getDefaultBehavior() {
	if(defaultBehavior==null)
	    setDefaultBehavior(behaviors[0]);
        return defaultBehavior;
    }

    /**
     * @param defaultBehavior the defaultBehavior to set
     */
    public UserInputWeaponSelectionBehavior setDefaultBehavior(ProjectileFiringBehavior defaultBehavior) {
        this.defaultBehavior = defaultBehavior;
        return this;
    }

    /**
     * @return the activeBehavior
     */
    public ProjectileFiringBehavior getActiveBehavior() {
	if(activeBehavior==null)
	    setActiveBehavior(getDefaultBehavior(),true);
        return activeBehavior;
    }
}//end WeaponSelectionBehavior
