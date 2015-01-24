/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.math;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class NormalizedAngularVelocity extends AbstractNormalizedObject
	implements AngularVelocity {
    @Override
    public Angle getAngleDeltaNormalizedInTimeDelta(final int timeInMillis) {
	return new AbstractNormalizedAngle(){
	    {NormalizedAngularVelocity.this.addPropertyChangeListener(
		    NORMALIZED, 
		    new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
			    NormalizedAngularVelocity.this.firePropertyChange(evt);
			}});}

	    @Override
	    public int toNormalized() {
		return NormalizedAngularVelocity.this.toNormalized()*timeInMillis;
	    }};
    }//end getAngleDeltaNormalizedInTimeDelta(...)
    
    public static class Source extends NormalizedAngularVelocity{
	private volatile int normalizedAngularVelocity=0;
	public void setAngleDeltaPerTimeDelta(Angle angleDelta, int timeDeltaMillis){
	    final int newValue = angleDelta.toNormalized()/timeDeltaMillis;
	    this.firePropertyChange(NORMALIZED, normalizedAngularVelocity, newValue);
	    normalizedAngularVelocity = newValue;
	}
	@Override
	public int toNormalized() {
	    return normalizedAngularVelocity;
	}
    }//end Source
}//end AbstractNormalizedAngularVelocity
