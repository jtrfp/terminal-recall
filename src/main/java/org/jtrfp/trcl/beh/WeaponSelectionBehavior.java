package org.jtrfp.trcl.beh;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.obj.WorldObject;

public class WeaponSelectionBehavior extends Behavior {
    private ProjectileFiringBehavior [] behaviors;
    private ProjectileFiringBehavior activeBehavior;
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final KeyStatus keyStatus = parent.getTr().getKeyStatus();
	for(int k=KeyEvent.VK_1; k<KeyEvent.VK_7;k++){
	    if(keyStatus.isPressed(KeyEvent.VK_1+k)){
		activeBehavior=behaviors[k];
		}//end if (selection key is pressed)
	}//end for(keys)
	if(keyStatus.isPressed(KeyEvent.VK_SPACE)){
	    activeBehavior.requestFire();
	}//end if(SPACE)
    }//end _tick(...)
    
    private void resetBehaviors(){
	for(ProjectileFiringBehavior b:behaviors){
	    b.setEnable(false);
	}
    }
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
        if(behaviors.length==7)this.behaviors = behaviors;
        else throw new IllegalArgumentException("Need seven behaviors. Got "+behaviors.length);
        activeBehavior=behaviors[0];
        return this;
    }
}//end WeaponSelectionBehavior
