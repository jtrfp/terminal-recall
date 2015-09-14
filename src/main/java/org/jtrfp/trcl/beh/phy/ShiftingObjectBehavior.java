/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh.phy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AttribAnimator;
import org.jtrfp.trcl.IndirectDouble;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.beh.Behavior;

public class ShiftingObjectBehavior extends Behavior
	{
	private final Sequencer seq;
	private final AttribAnimator xAnimator;
	private final AttribAnimator yAnimator;
	private final AttribAnimator zAnimator;
	private final IndirectDouble xPos = new IndirectDouble();
	private final IndirectDouble yPos = new IndirectDouble();
	private final IndirectDouble zPos = new IndirectDouble();
	
	public ShiftingObjectBehavior(int totalShiftPeriodMsec,Vector3D startPos, Vector3D endPos)
		{
		seq=new Sequencer(totalShiftPeriodMsec,2,true);
		xAnimator=new AttribAnimator(xPos,seq,new double [] {startPos.getX(),endPos.getX()});
		yAnimator=new AttribAnimator(yPos,seq,new double [] {startPos.getY(),endPos.getY()});
		zAnimator=new AttribAnimator(zPos,seq,new double [] {startPos.getZ(),endPos.getZ()});
		}
	@Override
	public void tick(long tickTimeInMillis){
		xAnimator.updateAnimation();
		yAnimator.updateAnimation();
		zAnimator.updateAnimation();
		super.getParent().setPosition(xPos.get(),yPos.get(),zPos.get());
		}
	}//end TunnelObjectBehavior
