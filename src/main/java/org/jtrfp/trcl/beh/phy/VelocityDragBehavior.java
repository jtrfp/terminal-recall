package org.jtrfp.trcl.beh.phy;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.Velocible;

public class VelocityDragBehavior extends Behavior
	{
	private double dragCoeff=1;
	public VelocityDragBehavior setDragCoefficient(double dragCoefficientPerSecond)
		{dragCoeff=dragCoefficientPerSecond; return this;}

	public double getVelocityDrag()
		{return dragCoeff;}
	
	@Override
	public void _tick(long tickTimeMillis){
	    	final double timeProgressedInFrames=((double)getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/(1000./ThreadManager.GAMEPLAY_FPS));
	    	if(timeProgressedInFrames<=0)return;
	    	final double finalCoeff=Math.pow(dragCoeff,timeProgressedInFrames);
	    	Velocible v=getParent().getBehavior().probeForBehavior(Velocible.class);
	    	v.setVelocity(v.getVelocity().scalarMultiply(finalCoeff));
		}

	}//end VelocityDragBehavior
