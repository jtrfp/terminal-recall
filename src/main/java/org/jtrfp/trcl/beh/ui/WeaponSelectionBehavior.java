package org.jtrfp.trcl.beh.ui;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.WorldObject;

public class WeaponSelectionBehavior extends Behavior {
    private ProjectileFiringBehavior [] behaviors;
    private ProjectileFiringBehavior activeBehavior;
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final KeyStatus keyStatus = parent.getTr().getKeyStatus();
	for(int k=0; k<7;k++){
	    if(keyStatus.isPressed(KeyEvent.VK_1+k)){
		final ProjectileFiringBehavior newBehavior = behaviors[k];
		if(activeBehavior!=newBehavior){
		    activeBehavior=behaviors[k];
		    final Weapon w = activeBehavior.getProjectileFactory().getWeapon();
		    final TR tr = parent.getTr();
		    String content="???";
		    switch(tr.getTrConfig().getGameVersion()){
		    case F3:{
			content=w.getF3DisplayName();
			break;
		       }
		    case TV:{
			content=w.getTvDisplayName();
			break;
		        }
		    }//end switch(game version)
		    tr.getHudSystem().getWeapon().setContent(content);
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
