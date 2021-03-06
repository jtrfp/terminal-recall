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
package org.jtrfp.trcl.coll;

import java.beans.PropertyChangeListener;

public interface PropertyListenable {
 public void addPropertyChangeListener   (PropertyChangeListener toAdd);
 public void addPropertyChangeListener   (String propertyName, PropertyChangeListener toAdd);
 public void removePropertyChangeListener(PropertyChangeListener toRemove);
 public void removePropertyChangeListener(String propertyName,PropertyChangeListener listener);
 public PropertyChangeListener [] getPropertyChangeListeners();
 public PropertyChangeListener [] getPropertyChangeListeners(String propertyName);
 public boolean hasListeners(String propertyName);
}//end PropertyListenable
