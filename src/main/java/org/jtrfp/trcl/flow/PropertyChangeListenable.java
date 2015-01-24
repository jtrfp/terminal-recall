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

package org.jtrfp.trcl.flow;

import java.beans.PropertyChangeListener;

public interface PropertyChangeListenable {
    /**
     * Add a property change listener.
     * @param l
     * @since Jan 23, 2015
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    /**
     * Removes a property change listener.
     * @param propertyName
     * @param l
     * @since Jan 23, 2015
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    /**
     * Add a property change listener.
     * @param l
     * @since Jan 23, 2015
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    /**
     * Removes a property change listener.
     * @param propertyName
     * @param l
     * @since Jan 23, 2015
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Check if any listeners are present with the specified property name.
     * @param propertyName
     * @return
     * @since Jan 24, 2015
     */
    public boolean hasListeners(String propertyName);
    
    /**
     * Returns all registered PropertyChangeListeners for this object.
     * @return
     * @since Jan 24, 2015
     */
    public PropertyChangeListener [] getPropertyChangeListeners();
    
    /**
     * Returns all registered PropertyChangeListeners for the given property name of this object.
     * @param propertyName
     * @return
     * @since Jan 24, 2015
     */
    public PropertyChangeListener [] getPropertyChangeListeners(String propertyName);
}//end PropertyChangeListenable
