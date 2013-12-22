package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.WorldObject;

public class RotationalDragBehavior extends ObjectBehavior {
    private double dragCoeff=1;
    @Override
    public void _tick(long tickTimeInMillis){
	final double timeProgressed=((double)getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.);
    	if(timeProgressed<=0)return;
    	final double slowdown = 1-dragCoeff;
    	final double timeAdjSlowdown=1-(slowdown*timeProgressed);
    	final double finalCoeff=Math.pow(timeAdjSlowdown,4);
	final WorldObject p = getParent();
	final RotationalMomentumBehavior rmb = (RotationalMomentumBehavior)p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*finalCoeff);
	rmb.setLateralMomentum(rmb.getLateralMomentum()*finalCoeff);
	rmb.setPolarMomentum(rmb.getPolarMomentum()*finalCoeff);
    }//end _tick()
    
    public void setDragCoefficient(double drag){dragCoeff=drag;}
    public double getDragCoefficient(){return dragCoeff;}
}
