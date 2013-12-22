package org.jtrfp.trcl.ai;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.objects.Propelled;

public class UserInputThrottleControlBehavior extends ObjectBehavior {
   // final double nudgeUnit = TR.mapSquareSize / 9.;
    final double nudgeUnit = 30000;
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
}//end UseInputThrottleControlBehavior
