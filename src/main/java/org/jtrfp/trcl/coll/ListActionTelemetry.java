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
 * A  List delegate which tracks modifications for more efficient deferred updates based on
 * this List's contents. The 'modified' property starts out as true by default.
 * @author Chuck Ritola
 *
 * @param <E>
 */
public class ListActionTelemetry<E> implements List<E> {
    //// BEAN PROPERTIES
    public static final String MODIFIED = "modified";
    
    private final List<E>      delegate, subList;
    private volatile boolean   modified = true;
    private final boolean      isSubList;
    private final int          startIndex,endIndex;
    private final ListActionTelemetry<E> root;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ListActionTelemetry(){
	this(new ArrayList<E>());
    }
    
    public ListActionTelemetry(List<E> delegate){
	this(delegate, null, false, -1, -1);
    }
    
    protected ListActionTelemetry(List<E> delegate, ListActionTelemetry<E> root, boolean isSubList, int startIndex, int endIndex){
	this.delegate  = delegate;
	this.isSubList = isSubList;
	this.startIndex=startIndex;
	this.endIndex  =endIndex;
	this.root = root!=null?root:this;
	subList = isSubList?delegate.subList(startIndex, endIndex):delegate;
    }
    
    protected ListActionTelemetry(List<E> delegate, ListActionTelemetry<E> root, int startIndex, int endIndex){
	this(delegate, root, true, startIndex, endIndex);
    }
    
    public  boolean isModified(){
	return modified;
    }
    
    private List<E> getDelegate(){
	return subList;
    }
    
    /**
     * Drains this List's contents to the provided destination and resets all
     * modification hint flags. The entire List's state is added to the destination regardless
     * of what has been changed. This is a  operation which guarantees to be atomic of any
     * changes to this List.
     * @param dest A usually-empty List to which to drain this ListActionTelemetry's current contents.
     * @return A ListState providing hints to accelerate changes based on those drained to dest.
     * @since Mar 24, 2015
     */
    public ListState drainListStateTo(List<? super E> dest){
	if(dest!=null)
	 dest.addAll(getDelegate());
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
    public boolean add(E e) {
	root.modified();
	return getDelegate().add(e);
    }

    /**
     * @param index
     * @param element
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    public void add(int index, E element) {
	root.modified();
	getDelegate().add(index, element);
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
	root.modified();
	return getDelegate().addAll(c);
    }

    /**
     * @param index
     * @param c
     * @return
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends E> c) {
	root.modified();
	return getDelegate().addAll(index, c);
    }

    /**
     * 
     * @see java.util.ArrayList#clear()
     */
    public void clear() {
	root.modified();
	getDelegate().clear();
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
	return getDelegate().contains(o);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> arg0) {
	return getDelegate().containsAll(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
	return getDelegate().equals(arg0);
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#get(int)
     */
    public E get(int index) {
	return getDelegate().get(index);
    }

    /**
     * @return
     * @see java.util.AbstractList#hashCode()
     */
    public int hashCode() {
	return getDelegate().hashCode();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#indexOf(java.lang.Object)
     */
    public int indexOf(Object arg0) {
	return getDelegate().indexOf(arg0);
    }

    /**
     * @return
     * @see java.util.ArrayList#isEmpty()
     */
    public boolean isEmpty() {
	return getDelegate().isEmpty();
    }

    /**
     * @return
     * @see java.util.ArrayList#iterator()
     */
    public Iterator<E> iterator() {
	throw new UnsupportedOperationException();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object arg0) {
	return getDelegate().lastIndexOf(arg0);
    }

    /**
     * @return
     * @see java.util.ArrayList#listIterator()
     */
    public  ListIterator<E> listIterator() {
	throw new UnsupportedOperationException();
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#listIterator(int)
     */
    public  ListIterator<E> listIterator(int index) {
	throw new UnsupportedOperationException();
    }

    /**
     * @param index
     * @return
     * @see java.util.ArrayList#remove(int)
     */
    public E remove(int index) {
	root.modified();
	return getDelegate().remove(index);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public boolean remove(Object arg0) {
	final boolean result = getDelegate().remove(arg0);
	if(result)
	    root.modified();
	return result;
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
	final boolean result = getDelegate().removeAll(c);
	if(result)
	    root.modified();
	return result;
    }

    /**
     * @param c
     * @return
     * @see java.util.ArrayList#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
	final boolean result = getDelegate().retainAll(c);
	if(result)
	    root.modified();
	return result;
    }

    /**
     * @param index
     * @param element
     * @return
     * @see java.util.ArrayList#set(int, java.lang.Object)
     */
    public E set(int index, E element) {
	root.modified();
	return getDelegate().set(index, element);
    }

    /**
     * @return
     * @see java.util.ArrayList#size()
     */
    public int size() {
	return getDelegate().size();
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see java.util.ArrayList#subList(int, int)
     */
    public List<E> subList(int fromIndex, int toIndex) {
	if(isSubList)
	    return new ListActionTelemetry<E>(delegate, this ,fromIndex+this.startIndex, toIndex+this.startIndex);
	else
	    return new ListActionTelemetry<E>(delegate, this, fromIndex, toIndex);
    }

    /**
     * @return
     * @see java.util.ArrayList#toArray()
     */
    public Object[] toArray() {
	return getDelegate().toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.ArrayList#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
	return getDelegate().toArray(a);
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    public  String toString() {
	return getDelegate().toString();
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
