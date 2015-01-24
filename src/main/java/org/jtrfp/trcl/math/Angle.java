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
 * Representation of an angle. Underlying implementation may change values if state-dependent.
 * Just-in-time querying is recommended.
 * @author Chuck Ritola
 *
 */

public interface Angle extends Normalized, PropertyChangeListenable {
    /**
     * Returns this object's normalized angle in the range of [-Integer.MIN_VALUE,Integer.MAX_VALUE]
     * scaling to the range [-pi,pi].
     * @return
     * @since Jan 23, 2015
     */
    public int toNormalized();
}
