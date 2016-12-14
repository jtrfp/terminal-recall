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

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class MovesByVelocity extends Behavior implements Velocible {
	private double [] velocity = new double[3];
	private ThreadManager threadManager;
	@Override
	public void tick(long tickTimeMillis){
		final WorldObject p = getParent();
		double progressionInSeconds = (double)getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;
		if(progressionInSeconds>.25)progressionInSeconds=.25;
		assert !Vect3D.isAnyNaN(p.getPosition());
		final double [] position = p.getPosition();
		final double [] velocity = getVelocity();
		position[0] += velocity[0]*progressionInSeconds;
		position[1] += velocity[1]*progressionInSeconds;
		position[2] += velocity[2]*progressionInSeconds;
		p.notifyPositionChange();
		//p.movePositionBy(getVelocity().scalarMultiply(progressionInSeconds));
		assert !Vect3D.isAnyNaN(p.getPosition());
		}

	@Override
	public void setVelocity(double [] vel)
		{velocity=vel;}

	@Override
	public double [] getVelocity()
		{return velocity;}

	@Override
	public void accellerate(double [] accelVector){
	    Vect3D.add(velocity, accelVector, velocity);
	}

	public ThreadManager getThreadManager() {
	    if( threadManager == null )
		threadManager = getParent().getTr().getThreadManager();
	    return threadManager;
	}

	public void setThreadManager(ThreadManager threadManager) {
	    this.threadManager = threadManager;
	}
	}//end MovesWithVelocity
