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
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A synchronized List delegate which tracks modifications for more efficient deferred updates based on
 * this List's contents. The 'modified' property starts out as true by default.
 * @author Chuck Ritola
 *
 * @param <E>
 */
public class ListActionTelemetry<E> implements List<E> {
    //// BEAN PROPERTIES
    public static final String MODIFIED = "modified";
    
    private final List<E>      delegate;
    private volatile boolean   modified = true;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ListActionTelemetry(){
	this(new ArrayList<E>());
    }
    
    public ListActionTelemetry(List<E> delegate){
	this.delegate = delegate;
    }
    
    public synchronized boolean isModified(){
	return modified;
    }
    
    /**
     * Drains this List's contents to the provided destination and resets all
     * modification hint flags. The entire List's state is added to the destination regardless
     * of what has been changed. This is a synchronized operation which guarantees to be atomic of any
     * changes to this List.
     * @param dest A usually-empty List to which to drain this ListActionTelemetry's current contents.
     * @return A ListState providing hints to accelerate changes based on those drained to dest.
     * @since Mar 24, 2015
     */
    public synchronized ListState drainListStateTo(List<? super E> dest){
	if(dest!=null)
	 dest.addAll(delegate);
	pcs.firePropertyChange(MODIFIED, this.modified, false);
	modified = false;
	return new ListState();
    }
    
    private void modified(){
	pcs.firePropertyChange(MODIFIED, this.modified, true);
	modified=true;
    }

    /**
     * @param e
     * @return
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    public synchronized boolean add(E e) {
	modified();
	return delegate.add(e);
    }

    /**
     * @param index
     * @param element
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    public synchronized void add(int index, E element) {
	modified();
	delegate.add(index, element);
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    public synchronized boolean addAll(Collection<? extends E> c) {
	modified();
	return delegate.addAll(c);
    }

    /**
     * @param index
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
	modified();
	return delegate.addAll(index, c);
    }

    /**
     * 
     * @see java.util.ArrayList#clear()
     */
    public synchronized void clear() {
	modified();
	delegate.clear();
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#contains(java.lang.Object)
     */
    public synchronized boolean contains(Object o) {
	return delegate.contains(o);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    public synchronized boolean containsAll(Collection<?> arg0) {
	return delegate.containsAll(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    public synchronized boolean equals(Object arg0) {
	return delegate.equals(arg0);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#get(int)
     */
    public synchronized E get(int index) {
	return delegate.get(index);
    }

    /**
     * @return
     * @see java.util.AbstractList#hashCode()
     */
    public synchronized int hashCode() {
	return delegate.hashCode();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#indexOf(java.lang.Object)
     */
    public synchronized int indexOf(Object arg0) {
	return delegate.indexOf(arg0);
    }

    /**
     * @return
     * @see java.util.ArrayList#isEmpty()
     */
    public synchronized boolean isEmpty() {
	return delegate.isEmpty();
    }

    /**
     * @return
     * @see java.util.ArrayList#iterator()
     */
    public synchronized Iterator<E> iterator() {
	return delegate.iterator();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
     */
    public synchronized int lastIndexOf(Object arg0) {
	return delegate.lastIndexOf(arg0);
    }

    /**
     * @return
     * @see java.util.ArrayList#listIterator()
     */
    public synchronized ListIterator<E> listIterator() {
	return delegate.listIterator();
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#listIterator(int)
     */
    public synchronized ListIterator<E> listIterator(int index) {
	return delegate.listIterator(index);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#remove(int)
     */
    public synchronized E remove(int index) {
	modified();
	return delegate.remove(index);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public synchronized boolean remove(Object arg0) {
	final boolean result = delegate.remove(arg0);
	if(result)
	    modified();
	return result;
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#removeAll(java.util.Collection)
     */
    public synchronized boolean removeAll(Collection<?> c) {
	final boolean result = delegate.removeAll(c);
	if(result)
	    modified();
	return result;
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#retainAll(java.util.Collection)
     */
    public synchronized boolean retainAll(Collection<?> c) {
	final boolean result = delegate.retainAll(c);
	if(result)
	    modified();
	return result;
    }

    /**
     * @param index
     * @param element
     * @return
     * @see java.util.ArrayList#set(int, java.lang.Object)
     */
    public synchronized E set(int index, E element) {
	modified();
	return delegate.set(index, element);
    }

    /**
     * @return
     * @see java.util.ArrayList#size()
     */
    public synchronized int size() {
	return delegate.size();
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see java.util.ArrayList#subList(int, int)
     */
    public synchronized List<E> subList(int fromIndex, int toIndex) {
	return delegate.subList(fromIndex, toIndex);
    }

    /**
     * @return
     * @see java.util.ArrayList#toArray()
     */
    public synchronized Object[] toArray() {
	return delegate.toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.ArrayList#toArray(T[])
     */
    public synchronized <T> T[] toArray(T[] a) {
	return delegate.toArray(a);
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    public synchronized String toString() {
	return delegate.toString();
    }
    
    /**
     * Future implementation to provide details as to which portions 
     * of a list have been changed since last drain.
     * @author Chuck Ritola
     *
     */
    public static class ListState{
    }

    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
	pcs.addPropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }

    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
	pcs.removePropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }
    
    
}//end ListActionTelemetry
