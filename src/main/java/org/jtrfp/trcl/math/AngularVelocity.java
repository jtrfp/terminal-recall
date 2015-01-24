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

import org.jtrfp.trcl.flow.PropertyChangeListenable;

/**
 * Representation of angular velocity. Underlying implementation may change values
 * if state-dependent. Just-in-time querying is recommended.
 * @author Chuck Ritola
 *
 */
public interface AngularVelocity extends Normalized, PropertyChangeListenable {
    /**
     * Returns this object's angular delta per millisecond, negative values supported for
     * counter-clockwise rotation.
     * @return
     * @since Jan 23, 2015
     */
    public int toNormalized();
    
    public Angle getAngleDeltaNormalizedInTimeDelta(int timeInMillis);
}//end AngularVelocity
