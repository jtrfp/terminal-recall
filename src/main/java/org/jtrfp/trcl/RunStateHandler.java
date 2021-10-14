/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2018 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class RunStateHandler implements PropertyChangeListener {
    private boolean isInState = false;
    
    public abstract boolean isValidRunState( Object oldRunState, Object newRunState);

    public abstract void enteredRunState(Object oldState, Object newState);
    public abstract void exitedRunState (Object oldState, Object newState);

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final Object oldState = evt.getOldValue();
	final Object newState = evt.getNewValue();
	final boolean newInState = isValidRunState(oldState, newState);
	
	if(isInState && !newInState)
	    exitedRunState(oldState, newState);
	if(!isInState && newInState)
	    enteredRunState(oldState, newState);
	
	isInState = newInState;
    }//end propertyChange(...)
}//end RunStateHandler
