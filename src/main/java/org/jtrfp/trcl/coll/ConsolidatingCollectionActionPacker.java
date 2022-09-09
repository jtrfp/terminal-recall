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
 * Packs actions to this Collection according to the supplied KEY key in each Pair,
 * creating new CollectionActionDispatchers when new keys are introduced and removing them when they become 
 * empty. This is often for consolidating multiple packed collections with the same key.
 * @author Chuck Ritola
 *
 * @param <E>
 */
public class ConsolidatingCollectionActionPacker<E,KEY> implements Collection<Pair<KEY,CollectionActionDispatcher<E>>>, Decorator<Collection<Pair<KEY,CollectionActionDispatcher<E>>>> {
    private final Map<KEY,Pair<KEY,CollectionActionDispatcher<E>>> map = new HashMap<KEY,Pair<KEY,CollectionActionDispatcher<E>>>();
    private final Collection<Pair<KEY,CollectionActionDispatcher<E>>> delegate;
    private final Collection<Pair<KEY,CollectionActionDispatcher<E>>> cache = new ArrayList<Pair<KEY,CollectionActionDispatcher<E>>>();
    
    public ConsolidatingCollectionActionPacker(Collection<Pair<KEY,CollectionActionDispatcher<E>>> delegate){
	this.delegate=delegate;
    }//end constructor

    @Override
    public boolean add(Pair<KEY,CollectionActionDispatcher<E>> e) {
	if(!map.containsKey(e.getKey())){
	    final CollectionActionDispatcher<E> newCollection = new CollectionActionDispatcher<E>(new ArrayList<E>());
	    final Pair<KEY,CollectionActionDispatcher<E>> newPair = 
		    new Pair<KEY,CollectionActionDispatcher<E>>(e.getKey(),newCollection);
	    map.put(e.getKey(),newPair);
	    delegate.add(newPair);
	    cache.add(newPair);
	}//end (create new entry)
	return e.getValue().addTarget(map.get(e.getKey()).getValue(), true);
    }//end add()
    
    @Override
    public void clear() {
	final Collection<Pair<KEY,CollectionActionDispatcher<E>>> temp = new ArrayList<Pair<KEY,CollectionActionDispatcher<E>>>(cache);
	final boolean result = removeAll(temp);
	assert result|temp.isEmpty();
    }//end clear()

    /**
     * Note: Based on the KEY component of Pair<KEY,VALUE>
     * Different VALUEs have no effect.
     */
    @Override
    public boolean contains(Object o) {
	if(! (o instanceof Pair))
	    return false;
	@SuppressWarnings("unchecked")
	final Pair<KEY,CollectionActionDispatcher<E>> pair = (Pair<KEY,CollectionActionDispatcher<E>>)o;
	return map.containsKey(pair.getKey());
	//return cache.contains(o);
    }


    /**
     * Note: Based on the KEY component of Pair<KEY,VALUE>
     * Different VALUEs have no effect.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
	for(Object o:c)
	    if(!contains(o))
		return false;
	return true;
	//return cache.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
	return cache.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
	if(! (o instanceof Pair))
	    return false;
	//if(!cache.remove(o))
	//    return false;
	@SuppressWarnings("unchecked")
	Pair<KEY,CollectionActionDispatcher<E>> element = (Pair<KEY,CollectionActionDispatcher<E>>)o;
	final KEY key = element.getKey();
	final Pair<KEY,CollectionActionDispatcher<E>> target = map.get(key);
	final CollectionActionDispatcher<E> targetCollection = target.getValue();
	final CollectionActionDispatcher<E> dispatcher = element.getValue();
	
	final boolean removeResult = dispatcher.removeTarget(targetCollection, true);
	assert removeResult;
	if(targetCollection.isEmpty()){
	    map.remove(key);
	    final boolean result = delegate.remove(target);
	    cache.remove(target);
	    assert result;
	    }
	return true;
    }//end remove(...)

    @Override
    public boolean removeAll(Collection<?> c) {//TODO: This should literally remove ALL
	boolean result=false;
	for(Object o:c)
	    result |= remove(o);
	return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	ArrayList<Object> toRemove = new ArrayList<Object>();
	for(Pair<KEY,CollectionActionDispatcher<E>> element:this)
	    if(!c.contains(element))toRemove.add(element);
	final int origSize = size();
	boolean result = removeAll(toRemove);
	assert origSize-toRemove.size()==size();
	return result;
    }

    @Override
    public int size() {
	assert cache.size() == map.size();
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
    public boolean addAll(Collection<? extends Pair<KEY, CollectionActionDispatcher<E>>> c) {
	boolean result = false;
	for(Pair<KEY, CollectionActionDispatcher<E>> element:c)
	    result |= add(element);
	return result;
    }

    @Override
    public Iterator<Pair<KEY, CollectionActionDispatcher<E>>> iterator() {
	final Iterator<Pair<KEY,CollectionActionDispatcher<E>>> iterator = cache.iterator();
	return new Iterator<Pair<KEY,CollectionActionDispatcher<E>>>(){
	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }

	    @Override
	    public Pair<KEY, CollectionActionDispatcher<E>> next() {
		return iterator.next();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }};
    }//end iterator()

    @Override
    public Collection<Pair<KEY, CollectionActionDispatcher<E>>> getDelegate() {
	return delegate;
    }
}//end CollectionActionPacker
