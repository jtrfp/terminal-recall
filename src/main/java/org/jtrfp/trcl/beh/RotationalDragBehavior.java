package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.WorldObject;

public class RotationalDragBehavior extends Behavior {
    private double dragCoeff=1;
    @Override
    public void _tick(long tickTimeInMillis){
	final double timeProgressedInFrames=((double)getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/(1000./ThreadManager.GAMEPLAY_FPS));
    	if(timeProgressedInFrames<=0)return;
    	final double finalCoeff=Math.pow(dragCoeff,timeProgressedInFrames);
	final WorldObject p = getParent();
	final RotationalMomentumBehavior rmb = (RotationalMomentumBehavior)p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*finalCoeff);
	rmb.setLateralMomentum(rmb.getLateralMomentum()*finalCoeff);
	rmb.setPolarMomentum(rmb.getPolarMomentum()*finalCoeff);
    }//end _tick()
    
    public void setDragCoefficient(double drag){dragCoeff=drag;}
    public double getDragCoefficient(){return dragCoeff;}
}
