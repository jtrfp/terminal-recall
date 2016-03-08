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
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class MovesByVelocity extends Behavior implements Velocible {
	private Vector3D velocity=Vector3D.ZERO;
	@Override
	public void tick(long tickTimeMillis){
		final WorldObject p = getParent();
		double progressionInSeconds = (double)p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;
		if(progressionInSeconds>.25)progressionInSeconds=.25;
		assert !Vect3D.isAnyNaN(p.getPosition());
		p.movePositionBy(getVelocity().scalarMultiply(progressionInSeconds));
		assert !Vect3D.isAnyNaN(p.getPosition());
		}

	@Override
	public void setVelocity(Vector3D vel)
		{velocity=vel;}

	@Override
	public Vector3D getVelocity()
		{return velocity;}

	@Override
	public void accellerate(Vector3D accelVector)
		{velocity=velocity.add(accelVector);}
	}//end MovesWithVelocity
