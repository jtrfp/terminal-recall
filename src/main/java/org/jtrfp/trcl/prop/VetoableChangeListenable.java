/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 22016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.prop;

import java.beans.VetoableChangeListener;

public interface VetoableChangeListenable {
    public void removeVetoableChangeListener(VetoableChangeListener listener);
    public void removeVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener);
    public VetoableChangeListener[] getVetoableChangeListeners(
	    String propertyName);
    public VetoableChangeListener[] getVetoableChangeListeners();
    public void addVetoableChangeListener(VetoableChangeListener listener);
    public void addVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener);
    
}//end VetoableChangeListenable
