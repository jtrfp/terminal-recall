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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AttribAnimator;
import org.jtrfp.trcl.IndirectDouble;
import org.jtrfp.trcl.Sequencer;

public class RotatingObjectBehavior extends ObjectBehavior
	{
	private final IndirectDouble angle = new IndirectDouble();
	private final Sequencer seq;
	private final AttribAnimator angleAnimator;
	private final Vector3D rotationAxisTop,originalHeading,originalTop;
	
	public RotatingObjectBehavior(Vector3D rotationAxisTop, Vector3D originalHeading, Vector3D originalTop, int periodLengthMsec, double phaseShift)
		{
		seq=new Sequencer(periodLengthMsec,2,true);
		angleAnimator= new AttribAnimator(angle,seq,new double [] {0+phaseShift,2.*Math.PI+phaseShift},false);
		this.rotationAxisTop=rotationAxisTop;
		this.originalHeading=originalHeading;
		this.originalTop=originalTop;
		}
	
	@Override
	protected void _tick(long tickTimeInMillis)
		{
		angleAnimator.updateAnimation();
		super.getParent().setTop(new Rotation(rotationAxisTop,angle.get()).applyTo(originalTop));
		super.getParent().setHeading(new Rotation(rotationAxisTop,angle.get()).applyTo(originalHeading));
		}

	}//end RotatingObjectBehavior
