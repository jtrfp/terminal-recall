package org.jtrfp.trcl.ai;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.objects.Player;

public class UserInputRudderElevatorControlBehavior extends ObjectBehavior {
    @Override
    public void _tick(long tickTimeMillis){
	final Player p = (Player)getParent();
	final KeyStatus keyStatus = p.getTr().getKeyStatus();
	final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	rmb.setEquatorialMomentum(0);
	rmb.setLateralMomentum(0);
	rmb.setPolarMomentum(0);
	if (keyStatus.isPressed(KeyEvent.VK_UP)){
		rmb.accelleratePolarMomentum(-2.*Math.PI*.003);
		}
	if (keyStatus.isPressed(KeyEvent.VK_DOWN)){
	    	rmb.accelleratePolarMomentum(2.*Math.PI*.003);
		}
	if (keyStatus.isPressed(KeyEvent.VK_LEFT)){
	    	//Tilt
		rmb.accellerateLateralMomentum(-2.*Math.PI*.003);
		//Turn
		rmb.accellerateEquatorialMomentum(2*Math.PI*.003);
		}
	if (keyStatus.isPressed(KeyEvent.VK_RIGHT)){
	    	//Tilt
		rmb.accellerateLateralMomentum(2.*Math.PI*.003);
		//Turn
		rmb.accellerateEquatorialMomentum(-2*Math.PI*.003);
		}
    }//end UserInputRudderElevatorControlBehavior
}
