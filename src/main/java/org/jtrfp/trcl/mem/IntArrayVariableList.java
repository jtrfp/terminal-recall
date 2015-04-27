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

package org.jtrfp.trcl.mem;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jtrfp.trcl.mem.MemoryWindow.IntArrayVariable;

public class IntArrayVariableList implements List<Integer> {
 private final IntArrayVariable   delegate;
 private final int                objectIndex;
 private volatile int		  counter;
 
 public IntArrayVariableList(IntArrayVariable delegate, int objectIndex){
     this.delegate    = delegate;
     this.objectIndex = objectIndex;
 }
/**
 * @param index
 * @param element
 * @see java.util.ArrayList#add(int, java.lang.Object)
 */
public synchronized void add(int index, Integer element) {
    throw new UnsupportedOperationException();
}

/**
 * @param e
 * @return
 * @see java.util.ArrayList#add(java.lang.Object)
 */
public synchronized boolean add(Integer e) {
    if(e==null)
	throw new NullPointerException("Element is intolerably null.");
    delegate.setAt(objectIndex, counter++, e);
    return true;
}

/**
 * @param c
 * @return
 * @see java.util.ArrayList#addAll(java.util.Collection)
 */
public synchronized boolean addAll(Collection<? extends Integer> c) {
    if(c.contains(null))
	throw new NullPointerException("Element is intolerably null.");
    delegate.setAt(objectIndex, counter, c);
    counter+=c.size();
    return !c.isEmpty();
}

/**
 * @param index
 * @param c
 * @return
 * @see java.util.ArrayList#addAll(int, java.util.Collection)
 */
public synchronized boolean addAll(int index, Collection<? extends Integer> c) {
    if(c.contains(null))
   	throw new NullPointerException("Element is intolerably null.");
    delegate.setAt(objectIndex, this.counter+index, c);
    this.counter+=c.size();
    return !c.isEmpty();
}

/**
 * 
 * @see java.util.ArrayList#clear()
 */
public synchronized void clear() {
    for(;counter>=0; counter--)
     delegate.setAt(objectIndex, counter, 0);
}

public synchronized void rewind(){
    counter=0;
}

/**
 * @param o
 * @return
 * @see java.util.ArrayList#contains(java.lang.Object)
 */
public synchronized boolean contains(Object o) {
    throw new UnsupportedOperationException();
}

/**
 * @param arg0
 * @return
 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
 */
public synchronized boolean containsAll(Collection<?> arg0) {
    throw new UnsupportedOperationException();
}

/**
 * @param index
 * @return
 * @see java.util.ArrayList#get(int)
 */
public synchronized Integer get(int index) {
    return delegate.get(objectIndex, index);
}

/**
 * @param o
 * @return
 * @see java.util.ArrayList#indexOf(java.lang.Object)
 */
public synchronized int indexOf(Object o) {
    throw new UnsupportedOperationException();
}

/**
 * @return
 * @see java.util.ArrayList#isEmpty()
 */
public synchronized boolean isEmpty() {
    return counter==0;
}

/**
 * @return
 * @see java.util.ArrayList#iterator()
 */
public synchronized Iterator<Integer> iterator() {
    throw new UnsupportedOperationException();
}

/**
 * @param o
 * @return
 * @see java.util.ArrayList#lastIndexOf(java.lang.Object)
 */
public synchronized int lastIndexOf(Object o) {
    throw new UnsupportedOperationException();
}

/**
 * @return
 * @see java.util.ArrayList#listIterator()
 */
public synchronized ListIterator<Integer> listIterator() {
    throw new UnsupportedOperationException();
}

/**
 * @param index
 * @return
 * @see java.util.ArrayList#listIterator(int)
 */
public synchronized ListIterator<Integer> listIterator(int index) {
    throw new UnsupportedOperationException();
}

/**
 * @param index
 * @return
 * @see java.util.ArrayList#remove(int)
 */
public synchronized Integer remove(int index) {
    throw new UnsupportedOperationException();
}

/**
 * @param o
 * @return
 * @see java.util.ArrayList#remove(java.lang.Object)
 */
public synchronized boolean remove(Object o) {
    throw new UnsupportedOperationException();
}

/**
 * @param c
 * @return
 * @see java.util.ArrayList#removeAll(java.util.Collection)
 */
public synchronized boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
}

/**
 * @param c
 * @return
 * @see java.util.ArrayList#retainAll(java.util.Collection)
 */
public synchronized boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
}

/**
 * @param index
 * @param element
 * @return
 * @see java.util.ArrayList#set(int, java.lang.Object)
 */
public synchronized Integer set(int index, Integer element) {
    if(element==null)
	throw new NullPointerException("Element is intolerably null.");
    Integer previous = delegate.get(objectIndex, index);
    delegate.setAt(objectIndex, index, element);
    return previous;
}

/**
 * @return
 * @see java.util.ArrayList#size()
 */
public synchronized int size() {
    return counter;
}

/**
 * @param fromIndex
 * @param toIndex
 * @return
 * @see java.util.ArrayList#subList(int, int)
 * @deprecated
 */
public synchronized List<Integer> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
}

/**
 * @return
 * @see java.util.ArrayList#toArray()
 */
public synchronized Object[] toArray() {
    throw new UnsupportedOperationException();
}

/**
 * @param a
 * @return
 * @see java.util.ArrayList#toArray(T[])
 */
public synchronized <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException();
}
 
}//end IntArrayVariableList
