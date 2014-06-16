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
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;


public class AccelleratedByPropulsion extends Behavior{
    	private Vector3D thrustVector = null;
	@Override
	public void _tick(long timeInMillis)
		{WorldObject wo = getParent();
		Propelled p = wo.getBehavior().probeForBehavior(Propelled.class);
		Velocible v = wo.getBehavior().probeForBehavior(Velocible.class);
		final double progressionInSeconds = (double)wo.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;
		Vector3D tVector = thrustVector!=null?thrustVector:wo.getHeading();
		v.accellerate(tVector.scalarMultiply(p.getPropulsion()*progressionInSeconds));
		}//end _tick(...)
	/**
	 * @return the thrustVector
	 */
	public Vector3D getThrustVector() {
	    return thrustVector;
	}
	/**
	 * @param thrustVector the thrustVector to set
	 */
	public AccelleratedByPropulsion setThrustVector(Vector3D thrustVector) {
	    this.thrustVector = thrustVector;
	    return this;
	}
}//end AccelleratedByPropulsion
