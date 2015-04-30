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

public class CollectionActionUnpacker<E> implements Collection<Collection<E>> {//TODO: ListActionUnpacker should extend this
    private final Collection<E> delegate;
    
    public CollectionActionUnpacker(Collection<E> delegate){
	this.delegate=delegate;
    }
    @Override
    public boolean add(Collection<E> e) {
	return delegate.addAll(e);
    }
    @Override
    public boolean addAll(Collection<? extends Collection<E>> c) {
	boolean result = false;
	for(Collection<E> element:c)
	    result |= delegate.addAll(element);
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
    public boolean isEmpty() {
	throw new UnsupportedOperationException();
    }
    @Override
    public Iterator<Collection<E>> iterator() {
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
    public boolean removeAll(Collection<?> c) {
	boolean result = false;
	for(Object e0:c)
	    if(e0 instanceof Collection)
		result |= remove(e0);
	return result;
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	boolean result = false;
	for(Object e0:c)
	    if(e0 instanceof Collection)
		result |= delegate.retainAll((Collection)e0);
	return result;
    }
    @Override
    public int size() {
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
}//end CollectionActionUnpacker
