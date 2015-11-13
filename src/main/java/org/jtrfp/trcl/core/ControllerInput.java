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
package org.jtrfp.trcl.core;


public interface ControllerInput {
    /**
     * One-dimensional value. Buttons are 0 or 1. A joystick axis is [-1,1] where zero is center.
     * Triggers are [0,1] where 0 is relaxed.
     * @return
     * @throws IllegalStateException
     * @since Nov 12, 2015
     */
 public double getState();
 /**
  * Gets the controller name of this input.
  * @return
  * @since Nov 12, 2015
  */
 public void setState(double newState);
 public String getName();
}//end ControllerInput
