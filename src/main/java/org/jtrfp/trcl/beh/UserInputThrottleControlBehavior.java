package org.jtrfp.trcl.beh;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.obj.Propelled;

public class UserInputThrottleControlBehavior extends Behavior {
    private double nudgeUnit = 40000;
    @Override
    public void _tick(long timeInMillis){
	final KeyStatus keyStatus=getParent().getTr().getKeyStatus(); 
	if (keyStatus.isPressed(KeyEvent.VK_PAGE_UP)){
	    	Propelled p=getParent().getBehavior().probeForBehavior(Propelled.class);
	    	p.deltaPropulsion(nudgeUnit);
		}
	if (keyStatus.isPressed(KeyEvent.VK_PAGE_DOWN)){
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
