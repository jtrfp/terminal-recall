package org.jtrfp.trcl.beh.ui;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.obj.Player;

public class UserInputRudderElevatorControlBehavior extends Behavior implements PlayerControlBehavior {
    private  double accellerationFactor=.0005;
    @Override
    public void _tick(long tickTimeMillis){
	final Player p = (Player)getParent();
	final KeyStatus keyStatus = p.getTr().getKeyStatus();
	final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	if (keyStatus.isPressed(KeyEvent.VK_UP)){
		rmb.accelleratePolarMomentum(-2.*Math.PI*accellerationFactor*1.2);
		}
	if (keyStatus.isPressed(KeyEvent.VK_DOWN)){
	    	rmb.accelleratePolarMomentum(2.*Math.PI*accellerationFactor*1.2);
		}
	if (keyStatus.isPressed(KeyEvent.VK_LEFT)){
	    	//Tilt
		rmb.accellerateLateralMomentum(-2.*Math.PI*accellerationFactor*.8);
		//Turn
		rmb.accellerateEquatorialMomentum(2*Math.PI*accellerationFactor);
		}
	if (keyStatus.isPressed(KeyEvent.VK_RIGHT)){
	    	//Tilt
		rmb.accellerateLateralMomentum(2.*Math.PI*accellerationFactor*.8);
		//Turn
		rmb.accellerateEquatorialMomentum(-2*Math.PI*accellerationFactor);
		}
    }//end UserInputRudderElevatorControlBehavior
    /**
     * @return the accellerationFactor
     */
    public double getAccellerationFactor() {
        return accellerationFactor;
    }
    /**
     * @param accellerationFactor the accellerationFactor to set
     */
    public void setAccellerationFactor(double accellerationFactor) {
        this.accellerationFactor = accellerationFactor;
    }
}
