package org.jtrfp.trcl.beh.ui;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Propelled;

public class UserInputThrottleControlBehavior extends Behavior implements PlayerControlBehavior {
    private double nudgeUnit = 40000;
    @Override
    public void _tick(long timeInMillis){
	final KeyStatus keyStatus=getParent().getTr().getKeyStatus(); 
	if (keyStatus.isPressed(KeyEvent.VK_A)){
	    	Propelled p=getParent().getBehavior().probeForBehavior(Propelled.class);
	    	p.deltaPropulsion(nudgeUnit);
		}
	if (keyStatus.isPressed(KeyEvent.VK_Z)){
	    	Propelled p=getParent().getBehavior().probeForBehavior(Propelled.class);
	    	p.deltaPropulsion(-nudgeUnit);
		}
    }//end _tick(...)
    /**
     * @return the nudgeUnit
     */
    public double getNudgeUnit() {
        return nudgeUnit;
    }
    /**
     * @param nudgeUnit the nudgeUnit to set
     */
    public void setNudgeUnit(double nudgeUnit) {
        this.nudgeUnit = nudgeUnit;
    }
}//end UseInputThrottleControlBehavior
