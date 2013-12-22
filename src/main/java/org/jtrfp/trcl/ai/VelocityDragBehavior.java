package org.jtrfp.trcl.ai;

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

	}//end VelocityDragBehavior
