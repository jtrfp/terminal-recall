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

import java.util.Collection;
import java.util.Iterator;

/**
 * Debugging helper to catch if an unauthorized thread 
 * @author Chuck Ritola
 *
 * @param <E>
 */
public final class ThreadEnforcementCollection<E> implements Collection<E> {
    private final Collection<E> delegate;
    private final Thread threadToEnforce;
    
    public ThreadEnforcementCollection(Collection<E> delegate, Thread threadToEnforce){
	this.delegate       = delegate;
	this.threadToEnforce= threadToEnforce;
    }
    
    private void enforceThread(){
	assert Thread.currentThread().equals(threadToEnforce):"Wrong thread. Expected "+threadToEnforce.getName()+" got "+Thread.currentThread().getName();
    }

    /**
     * @return
     * @see java.util.Collection#size()
     */
    public int size() {
	enforceThread();
	return delegate.size();
    }

    /**
     * @return
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
	enforceThread();
	return delegate.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
	enforceThread();
	return delegate.contains(o);
    }

    /**
     * @return
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
	enforceThread();
	return delegate.iterator();
    }

    /**
     * @return
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
	enforceThread();
	return delegate.toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
	enforceThread();
	return delegate.toArray(a);
    }

    /**
     * @param e
     * @return
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E e) {
	enforceThread();
	return delegate.add(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
	enforceThread();
	return delegate.remove(o);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
	enforceThread();
	return delegate.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
	enforceThread();
	return delegate.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
	enforceThread();
	return delegate.removeAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
	enforceThread();
	return delegate.retainAll(c);
    }

    /**
     * 
     * @see java.util.Collection#clear()
     */
    public void clear() {
	enforceThread();
	delegate.clear();
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
	enforceThread();
	return delegate.equals(o);
    }

    /**
     * @return
     * @see java.util.Collection#hashCode()
     */
    public int hashCode() {
	enforceThread();
	return delegate.hashCode();
    }
}//end ThreadEnforcementCollection
