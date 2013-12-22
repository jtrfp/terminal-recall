/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AttribAnimator;
import org.jtrfp.trcl.IndirectDouble;
import org.jtrfp.trcl.Sequencer;

public class ShiftingObjectBehavior extends ObjectBehavior
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
	public void _tick(long tickTimeInMillis)
		{
		xAnimator.updateAnimation();
		yAnimator.updateAnimation();
		zAnimator.updateAnimation();
		super.getParent().setPosition(new Vector3D(xPos.get(),yPos.get(),zPos.get()));
		}
	}//end TunnelObjectBehavior
