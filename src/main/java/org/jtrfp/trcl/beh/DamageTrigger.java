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
package org.jtrfp.trcl.beh;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class DamageTrigger extends Behavior {
    public static final String TRIGGERED = "triggered";
private boolean triggered=false;
private int threshold=2048;
private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void tick(long timeInMillis){
	    if(!triggered){
		if(getParent().probeForBehavior(DamageableBehavior.class).getHealth()<threshold){
		    setTriggered(true);//Just in case disabling has latency
		}//end if(damaged)
	    }//end if(!triggered)
	}//end _tick(...)
	
	public abstract void healthBelowThreshold();

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
	    return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public DamageTrigger setThreshold(int threshold) {
	    this.threshold = threshold;
	    return this;
	}

	public boolean isTriggered() {
	    return triggered;
	}

	public void setTriggered(boolean triggered) {
	    if(triggered==this.triggered)
		return;
	    final boolean oldValue = this.triggered;
	    this.triggered = triggered;
	    pcs.firePropertyChange(TRIGGERED, oldValue, triggered);
	    if(triggered){
		setEnable(false);///zzz...
		healthBelowThreshold();
	    }//end if(triggered)
	}//end setTriggered()

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}
}//end DamageTrigger
