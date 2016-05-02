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

package org.jtrfp.trcl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.jtrfp.trcl.coll.CachedAdapter;

public class WeakPropertyChangeSupport {
    private final PropertyChangeSupport delegate;
    private final CachedAdapter<PropertyChangeListener,WeakPropertyChangeListener> pclAdapter =
	    new CachedAdapter<PropertyChangeListener,WeakPropertyChangeListener>(){
		@Override
		protected WeakPropertyChangeListener _adapt(
			PropertyChangeListener value)
			throws UnsupportedOperationException {
		    return new WeakPropertyChangeListener(value,delegate);
		}

		@Override
		protected PropertyChangeListener _reAdapt(
			WeakPropertyChangeListener value)
			throws UnsupportedOperationException {
		    throw new UnsupportedOperationException();
		}};
    
    public WeakPropertyChangeSupport(PropertyChangeSupport delegate){
	this.delegate = delegate;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
	return delegate.hashCode();
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	if(listener == null)
	    throw new NullPointerException("Listener intolerably null.");
	delegate.addPropertyChangeListener(pclAdapter.adapt(listener));
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
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	if(listener == null)
	    throw new NullPointerException("Listener intolerably null.");
	delegate.removePropertyChangeListener(pclAdapter.adapt(listener));
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
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	if(listener == null)
	    throw new NullPointerException("Listener intolerably null.");
	if(propertyName == null)
	    throw new NullPointerException("PropertyName intolerably null.");
	delegate.addPropertyChangeListener(propertyName, pclAdapter.adapt(listener));
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	if(listener == null)
	    throw new NullPointerException("Listener intolerably null.");
	if(propertyName == null)
	    throw new NullPointerException("PropertyName intolerably null.");
	delegate.removePropertyChangeListener(propertyName, pclAdapter.adapt(listener));
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	final PropertyChangeListener[] preResult = delegate.getPropertyChangeListeners(propertyName); 
	final ArrayList<PropertyChangeListener> result = new ArrayList<PropertyChangeListener>(preResult.length);
	for(PropertyChangeListener l : preResult)
	    result.add(pclAdapter.adapt(l));
	return result.toArray(preResult);//Recycle the old array (:
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
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return delegate.toString();
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
    }

    /**
     * @param event
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)
     */
    public void firePropertyChange(PropertyChangeEvent event) {
	delegate.firePropertyChange(event);
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
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return delegate.hasListeners(propertyName);
    }

}//end WeakPropertySupport
