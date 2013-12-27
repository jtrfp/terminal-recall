package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.VelocityDragged;

public class VelocityDragBehavior extends Behavior implements
		VelocityDragged
	{
	private double dragCoeff=1;
	@Override
	public void setDragCoefficient(double dragCoefficientPerSecond)
		{dragCoeff=dragCoefficientPerSecond;}

	@Override
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
