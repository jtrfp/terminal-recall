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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionActionUnpacker<E> implements Collection<CollectionActionDispatcher<E>>, Decorator<Collection<E>> {//TODO: ListActionUnpacker should extend this
    private final Collection<E> delegate;
    private final Collection<CollectionActionDispatcher<E>> collections = new ArrayList<CollectionActionDispatcher<E>>();
    
    public CollectionActionUnpacker(Collection<E> delegate){
	if(delegate == null)
	    throw new NullPointerException("Delegate intolerably null.");
	this.delegate=delegate;
    }
    @Override
    public boolean add(CollectionActionDispatcher<E> e) {
	collections.add(e);
	return e.addTarget(delegate, true);
    }
    @Override
    public boolean addAll(Collection<? extends CollectionActionDispatcher<E>> c) {
	collections.addAll(c);
	for(CollectionActionDispatcher<E> element:c)
	    element.addTarget(delegate, true);
	return !c.isEmpty();
    }
    @Override
    public void clear() {
	for(CollectionActionDispatcher<E> cad:collections)
	    cad.removeTarget(delegate, true);
	collections.clear();
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
    public Iterator<CollectionActionDispatcher<E>> iterator() {
	final ArrayList<CollectionActionDispatcher<E>> colls = new ArrayList<CollectionActionDispatcher<E>>(collections);
	final Iterator<CollectionActionDispatcher<E>> iterator = colls.iterator();
	return new Iterator<CollectionActionDispatcher<E>>(){
	    CollectionActionDispatcher<E> lastObject;
	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }
	    @Override
	    public CollectionActionDispatcher<E> next() {
		return lastObject=iterator.next();
	    }
	    @Override
	    public void remove() {
		CollectionActionUnpacker.this.remove(lastObject);
	    }};
    }//end iterator()
    @Override
    public boolean remove(Object o) {
	boolean result = false;
	if(o instanceof CollectionActionDispatcher){
	    @SuppressWarnings("unchecked")
	    CollectionActionDispatcher<E> coll = (CollectionActionDispatcher<E>)o;
	    result |= collections.remove(coll);
	    
	    if(result){
		final boolean removed = coll.removeTarget(delegate, true);
		assert removed|coll.isEmpty():"failed to remove contents of "+coll+" from "+delegate;
	    }//end if(result)
	}//end if(CollectionActionDispatcher)
	return result;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
	boolean result = false;
	for(Object e0:c)
	 result |= remove(e0);
	return result;
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	final ArrayList<CollectionActionDispatcher<E>>toRemove = new ArrayList<CollectionActionDispatcher<E>>();
	for(CollectionActionDispatcher<E> cad:collections)
	    if(!c.contains(cad))
		toRemove.add(cad);
	for(CollectionActionDispatcher<E> cad:toRemove)
	    remove(cad);
	return !toRemove.isEmpty();
    }
    @Override
    public int size() {
	return collections.size();
    }
    @Override
    public Object[] toArray() {
	return collections.toArray();
    }
    @Override
    public <T> T[] toArray(T[] a) {
	return collections.toArray(a);
    }
    @Override
    public Collection<E> getDelegate() {
	return delegate;
    }
}//end CollectionActionUnpacker
