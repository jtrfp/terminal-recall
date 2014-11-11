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
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.WorldObject;

public class WeaponSelectionBehavior extends Behavior implements PlayerControlBehavior{
    private ProjectileFiringBehavior [] behaviors;
    private ProjectileFiringBehavior activeBehavior;
    private int ammoDisplayUpdateCounter=0;
    public static final int AMMO_DISPLAY_UPDATE_INTERVAL_MS=80;
    private static final int AMMO_DISPLAY_COUNTER_INTERVAL=(int)Math.ceil(AMMO_DISPLAY_UPDATE_INTERVAL_MS/ (1000./ThreadManager.GAMEPLAY_FPS));
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final KeyStatus keyStatus = parent.getTr().getKeyStatus();
	if(++ammoDisplayUpdateCounter%AMMO_DISPLAY_COUNTER_INTERVAL==0){
	    final int ammo = activeBehavior.getAmmo();
	    parent.getTr().getGame().getHUDSystem().getAmmo().setContent(""+(ammo!=-1?ammo:"INF"));
	}//end if(update ammo display)
	for(int k=0; k<7;k++){
	    if(keyStatus.isPressed(KeyEvent.VK_1+k)){
		final ProjectileFiringBehavior newBehavior = behaviors[k];
		if(activeBehavior!=newBehavior){
		    activeBehavior=behaviors[k];
		    final Weapon w = activeBehavior.getProjectileFactory().getWeapon();
		    final TR tr = parent.getTr();
		    String content="???";
		    switch(tr.getTrConfig()[0].getGameVersion()){
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
		}//end if(New Behavior)
	    }//end if (selection key is pressed)
	}//end for(keys)
	if(keyStatus.isPressed(KeyEvent.VK_SPACE)){
	    activeBehavior.requestFire();
	}//end if(SPACE)
    }//end _tick(...)
    /**
     * @return the behaviors
     */
    public ProjectileFiringBehavior[] getBehaviors() {
        return behaviors;
    }
    /**
     * @param behaviors the behaviors to set
     */
    public WeaponSelectionBehavior setBehaviors(ProjectileFiringBehavior[] behaviors) {
        this.behaviors = behaviors;
        activeBehavior=behaviors[0];
        return this;
    }
}//end WeaponSelectionBehavior
