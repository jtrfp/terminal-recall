/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
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
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StackTracingPropertyChangeSupport {
    private final PropertyChangeSupport delegate;
    private final HashMap<String,StackTraceElement[]> stackTraces = new HashMap<String,StackTraceElement[]>();
    public StackTracingPropertyChangeSupport(PropertyChangeSupport delegate){
	this.delegate=delegate;
    }
    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
	delegate.addPropertyChangeListener(arg0);
    }
    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	delegate.addPropertyChangeListener(propertyName, listener);
    }
    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
	return delegate.equals(obj);
    }
    /**
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, boolean, boolean)
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
	    boolean oldValue, boolean newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue,
		newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, int, int)
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
	    int oldValue, int newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue,
		newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @param propertyName
     * @param index
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#fireIndexedPropertyChange(java.lang.String, int, java.lang.Object, java.lang.Object)
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
	    Object oldValue, Object newValue) {
	delegate.fireIndexedPropertyChange(propertyName, index, oldValue,
		newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)
     */
    public void firePropertyChange(PropertyChangeEvent arg0) {
	delegate.firePropertyChange(arg0);
	notifyUpdatedProperty(arg0.getPropertyName());
    }
    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, boolean, boolean)
     */
    public void firePropertyChange(String propertyName, boolean oldValue,
	    boolean newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, int, int)
     */
    public void firePropertyChange(String propertyName, int oldValue,
	    int newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void firePropertyChange(String propertyName, Object oldValue,
	    Object newValue) {
	delegate.firePropertyChange(propertyName, oldValue, newValue);
	notifyUpdatedProperty(propertyName);
    }
    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return delegate.getPropertyChangeListeners();
    }
    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return delegate.getPropertyChangeListeners(propertyName);
    }
    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return delegate.hasListeners(propertyName);
    }
    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
	return delegate.hashCode();
    }
    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
	delegate.removePropertyChangeListener(arg0);
    }
    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	delegate.removePropertyChangeListener(propertyName, listener);
    }
    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return delegate.toString();
    }
    
    private void notifyUpdatedProperty(String propertyName){
	StackTraceElement [] trace = Thread.currentThread().getStackTrace();
	stackTraces.put(propertyName, Arrays.copyOfRange(trace, 5, trace.length));
    }
    
    public void getStackTraces(Map<String,StackTraceElement[]> dest){
	for(Entry<String,StackTraceElement[]> elements:stackTraces.entrySet())
	    dest.put(elements.getKey(),elements.getValue());
    }
}//end StackTracingPropertyChangeSupport
