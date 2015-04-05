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
package org.jtrfp.trcl.dbg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;

/**
 * An ArrayDeque which implements PropertyChangeListener to queue property changes.
 * New PropertyChangeEvents are added to the end of this Deque, such that a pop() invocation 
 * retrieves the events in-order.
 * @author Chuck Ritola
 *
 */
public class PropertyChangeQueue extends ArrayDeque<PropertyChangeEvent>
	implements PropertyChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 4488917795740229165L;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	add(evt);
    }

}//end PropertyChangeQueue
