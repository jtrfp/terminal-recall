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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ochafik.util.listenable.Pair;

/**
 * Packs actions to this Collection according to the supplied Integer key in each Pair,
 * creating new CollectionActionDispatchers when new keys are introduced and removing them when they become 
 * empty. This is typically fed the results of an adapted list which analyzes each element and converts it
 * to a Pair with a computed Integer key.
 * @author Chuck Ritola
 *
 * @param <E>
 */
public class CollectionActionPacker<E> implements Collection<Pair<Integer,E>> {
    private final Map<Integer,Pair<Integer,CollectionActionDispatcher<E>>> map = new HashMap<Integer,Pair<Integer,CollectionActionDispatcher<E>>>();;
    private final Collection<Pair<Integer,CollectionActionDispatcher<E>>> delegate;
    private final Collection<Pair<Integer,E>> cache = new ArrayList<Pair<Integer,E>>();
    
    public CollectionActionPacker(Collection<Pair<Integer,CollectionActionDispatcher<E>>> delegate){
	this.delegate=delegate;
    }//end constructor

    @Override
    public boolean add(Pair<Integer,E> e) {
	cache.add(e);
	if(!map.containsKey(e.getKey())){
	    final CollectionActionDispatcher<E> newCollection = new CollectionActionDispatcher<E>(new ArrayList<E>());
	    final Pair<Integer,CollectionActionDispatcher<E>> newPair = 
		    new Pair<Integer,CollectionActionDispatcher<E>>(e.getKey(),newCollection);
	    map.put(e.getKey(),newPair);
	    delegate.add(newPair);
	}//end (create new entry)
	return map.get(e.getKey()).getValue().add(e.getValue());
    }//end add()
    
    @Override
    public void clear() {
	final Collection<Pair<Integer,E>> temp = new ArrayList<Pair<Integer,E>>(cache);
	assert removeAll(temp);
    }//end clear()

    @Override
    public boolean contains(Object o) {
	return cache.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	return cache.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
	return cache.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
	if(! (o instanceof Pair))
	    return false;
	if(!cache.remove(o))
	    return false;
	Pair<Integer,E> element = (Pair<Integer,E>)o;
	final int hash = element.getKey();
	final Pair<Integer,CollectionActionDispatcher<E>> target = map.get(hash);
	final CollectionActionDispatcher<E> targetCollection = target.getValue();
	final E value = element.getValue();
	assert targetCollection.remove(value);
	if(targetCollection.isEmpty()){
	    map.remove(hash);
	    assert delegate.remove(target);//TODO: Target collection must be Pair<Integer,CAD>
	    }
	return true;
    }//end remove(...)

    @Override
    public boolean removeAll(Collection<?> c) {
	boolean result=false;
	for(Object o:c)
	    result |= remove(o);
	return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	ArrayList<Object> toRemove = new ArrayList<Object>();
	for(Pair<Integer,E> element:this)
	    if(!c.contains(element))toRemove.add(element);
	final int origSize = size();
	boolean result = removeAll(toRemove);
	assert origSize-toRemove.size()==size();
	return result;
    }

    @Override
    public int size() {
	return cache.size();
    }

    @Override
    public Object[] toArray() {
	return cache.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	return cache.toArray(a);
    }

    @Override
    public boolean addAll(Collection<? extends Pair<Integer, E>> c) {
	boolean result = false;
	for(Pair<Integer, E> element:c)
	    result |= add(element);
	return result;
    }

    @Override
    public Iterator<Pair<Integer, E>> iterator() {
	final Iterator<Pair<Integer,E>> iterator = cache.iterator();
	return new Iterator<Pair<Integer,E>>(){
	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }

	    @Override
	    public Pair<Integer, E> next() {
		return iterator.next();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }};
    }//end iterator()
}//end CollectionActionPacker
