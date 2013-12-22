package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.Velocible;
import org.jtrfp.trcl.objects.VelocityDragged;

public class VelocityDragBehavior extends ObjectBehavior implements
		VelocityDragged
	{
	private double dragCoeff=1;
	@Override
	public void setVelocityDrag(double dragCoefficientPerSecond)
		{dragCoeff=dragCoefficientPerSecond;}

	@Override
	public double getVelocityDrag()
		{return dragCoeff;}
	
	@Override
	public void _tick(long tickTimeMillis){
	    	final double timeProgressed=((double)getParent().getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.);
	    	if(timeProgressed<=0)return;
	    	Velocible v=getParent().getBehavior().probeForBehavior(Velocible.class);
	    	//System.out.println(dragCoeff);
	    	final double slowdown = 1-dragCoeff;
	    	final double timeAdjSlowdown=1-(slowdown*timeProgressed);
	    	v.setVelocity(v.getVelocity().scalarMultiply(Math.pow(timeAdjSlowdown,4)));
		}

	}//end VelocityDragBehavior
