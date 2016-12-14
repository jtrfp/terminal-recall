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
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.WorldObject;


public class AccelleratedByPropulsion extends Behavior{
    	private Vector3D thrustVector = null;
    	private ThreadManager threadManager;
    	private Velocible velocible;
    	private Propelled propelled;
    	@Override
    	public void tick(long timeInMillis){
    	    double progressionInSeconds = (double)getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;//TODO
    	    if(progressionInSeconds>.25)progressionInSeconds=.25;
    	    Vector3D tVector = thrustVector!=null?thrustVector:getParent().getHeading();//TODO
    	    getVelocible().accellerate(tVector.scalarMultiply(getPropelled().getPropulsion()*progressionInSeconds).toArray());//TODO: Optimize
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
	
	public ThreadManager getThreadManager() {
	    if( threadManager == null )
		threadManager = getParent().getTr().getThreadManager();
	    return threadManager;
	}

	public void setThreadManager(ThreadManager threadManager) {
	    this.threadManager = threadManager;
	}
	
	public Velocible getVelocible() {
	    if( velocible == null )
		velocible = getParent().probeForBehavior(Velocible.class);
	    return velocible;
	}

	public void setVelocible(Velocible velocible) {
	    this.velocible = velocible;
	}
	
	public Propelled getPropelled() {
	    if( propelled == null )
		propelled = getParent().probeForBehavior(Propelled.class);
	    return propelled;
	}

	public void setPropelled(Propelled propelled) {
	    this.propelled = propelled;
	}
}//end AccelleratedByPropulsion
