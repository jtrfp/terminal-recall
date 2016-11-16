/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ThreadSafePropertyChangeSupport {
    private PropertyChangeSupport delegate;
    
    public ThreadSafePropertyChangeSupport(Object targetBean){
	delegate = new PropertyChangeSupport(targetBean);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener arg0) {
	delegate.addPropertyChangeListener(arg0);
    }

    public synchronized void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	delegate.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void fireIndexedPropertyChange(String propertyName, int index,
	    boolean oldValue, boolean newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public synchronized void fireIndexedPropertyChange(String propertyName, int index,
	    int oldValue, int newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public synchronized void fireIndexedPropertyChange(String propertyName, int index,
	    Object oldValue, Object newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    public synchronized void firePropertyChange(PropertyChangeEvent evt) {
	delegate.firePropertyChange(evt);
    }

    public synchronized void firePropertyChange(String propertyName, boolean oldValue,
	    boolean newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
    }

    public synchronized void firePropertyChange(String propertyName, int oldValue, int newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
    }

    public synchronized void firePropertyChange(String propertyName, Object oldValue,
	    Object newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
	return delegate.getPropertyChangeListeners();
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
	return delegate.getPropertyChangeListeners(propertyName);
    }

    public synchronized boolean hasListeners(String propertyName) {
	return delegate.hasListeners(propertyName);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener arg0) {
	delegate.removePropertyChangeListener(arg0);
    }

    public synchronized void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	delegate.removePropertyChangeListener(propertyName, listener);
    }

    public synchronized String toString() {
	return delegate.toString();
    }
}//end ThreadSafePropertyChangeSupport
