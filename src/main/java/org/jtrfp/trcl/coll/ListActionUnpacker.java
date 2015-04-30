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
import java.util.List;
import java.util.ListIterator;

public class ListActionUnpacker<E> implements List<Collection<E>> {
    private final List<E> delegate;
    private final List<Collection<E>> cache;
    
    public ListActionUnpacker(List<E> delegate){
	this(delegate,null);
    }
    
    /**
     * 
     * @param delegate
     * @param cache The list acting on this ListActionUnpacker containing the original Collection content.
     */
    public ListActionUnpacker(List<E> delegate, List<Collection<E>> cache){
	this.delegate=delegate;
	this.cache=cache;
    }
    @Override
    public boolean add(Collection<E> e) {
	if(e.isEmpty())
	    return false;
	return delegate.addAll(e);
    }
    @Override
    public void add(int index, Collection<E> element) {
	if(element.isEmpty())
	    return;
	delegate.addAll(index,element);
    }
    @Override
    public boolean addAll(Collection<? extends Collection<E>> c) {
	if(c.isEmpty())
	    return false;
	boolean result = false;
	for(Collection<E> element:c)
	    result |= delegate.addAll(element);
	return result;
    }
    @Override
    public boolean addAll(int index, Collection<? extends Collection<E>> c) {
	if(c.isEmpty())
	    return false;
	boolean result = false;
	for(Collection<E> element:c)
	    result |= delegate.addAll(index,element);
	return result;
    }
    @Override
    public void clear() {
	delegate.clear();
    }
    @Override
    public boolean contains(Object o) {
	throw new UnsupportedOperationException();
    }
    @Override
    public boolean containsAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }
    @Override
    public Collection<E> get(int index) {
	throw new UnsupportedOperationException();
    }
    @Override
    public int indexOf(Object o) {
	throw new UnsupportedOperationException();
    }
    @Override
    public boolean isEmpty() {
	throw new UnsupportedOperationException();
    }
    @Override
    public Iterator<Collection<E>> iterator() {
	throw new UnsupportedOperationException();
    }
    @Override
    public int lastIndexOf(Object o) {
	throw new UnsupportedOperationException();
    }
    @Override
    public ListIterator<Collection<E>> listIterator() {
	throw new UnsupportedOperationException();
    }
    @Override
    public ListIterator<Collection<E>> listIterator(int index) {
	throw new UnsupportedOperationException();
    }
    @Override
    public boolean remove(Object o) {
	boolean result = false;
	if(o instanceof Collection){
	    Collection<E> coll = (Collection<E>)o;
	    result |= delegate.removeAll(coll);
	}
	return result;
    }
    @Override
    public Collection<E> remove(int index) {
	if(cache==null)
	    throw new UnsupportedOperationException("Cannot perform remove(index): Cache not supplied. See javadoc.");
	final Collection<E> result;
	remove(result = cache.get(index));
	return result;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
	if(c.isEmpty())
	    return false;
	boolean result = false;
	for(Object e0:c)
	    if(e0 instanceof Collection)
		result |= remove(e0);
	return result;
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	if(c.isEmpty())
	    return false;
	boolean result = false;
	for(Object e0:c)
	    if(e0 instanceof Collection)
		result |= delegate.retainAll((Collection)e0);
	return result;
    }
    @Override
    public Collection<E> set(int index, Collection<E> element) {
	throw new UnsupportedOperationException();
    }
    @Override
    public int size() {
	throw new UnsupportedOperationException();
    }
    @Override
    public List<Collection<E>> subList(int fromIndex, int toIndex) {
	throw new UnsupportedOperationException();
    }
    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException();
    }
    @Override
    public <T> T[] toArray(T[] a) {
	throw new UnsupportedOperationException();
    }
}//end ListActionUnpacker
