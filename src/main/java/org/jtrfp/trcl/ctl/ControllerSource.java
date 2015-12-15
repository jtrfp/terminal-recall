/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ctl;

import java.beans.PropertyChangeListener;

public interface ControllerSource {
    public static final String STATE = "State";
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);
    public String getName();
    public InputDevice getInputDevice();
}//end ControllerSource
