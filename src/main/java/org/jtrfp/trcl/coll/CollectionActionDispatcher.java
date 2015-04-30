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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionActionDispatcher<E> implements Collection<E> {
    protected final Collection<E>             cache;
    protected final Map<Collection<E>,Object> targetsMap;
    protected final Set<Collection<E>>        targets;
    private final int                         startIndex, endIndex;
    
    protected CollectionActionDispatcher(Collection<E> cache, Map<Collection<E>, Object> targetsMap, int startIndex, int endIndex){
	this.cache=cache;
	this.targetsMap=targetsMap;
	this.targets=targetsMap.keySet();
	this.startIndex = startIndex;
	this.endIndex = endIndex;
    }
    
    public CollectionActionDispatcher(List<E> cache){
	this(cache,new IdentityHashMap<Collection<E>,Object>(), 0, Integer.MAX_VALUE);
    }//end constructor
    
    public CollectionActionDispatcher() {
	this(new DummyList<E>("This CollectionActionDispatcher has a DummyList as a cache. Instantiate it with a real list to make this work."));
    }

    /**
     * Registers the Collection 
     * to be forwarded Collection operations given to this dispatcher. When prefilled, the supplied target is assumed
     * to be empty when added.
     * @param target The target List to which to forward List operations.
     * @param prefill Immediately perform addAll operation to the target of the cached state.
     * @return true if this Dispatcher did not already have the given target registered.
     * @since Mar 20, 2015
     */
    public boolean addTarget(Collection<E> target, boolean prefill){
	if(prefill) target.addAll(cache);
	boolean result = !targets.contains(target);
	targetsMap.put(target,null);
	return result;
    }
    
    public boolean removeTarget(Collection<E> target, boolean removeAll){
	if(removeAll && targets.contains(target))
	    target.removeAll(cache);
	return targets.remove(target);
    }
    
    @Override
    public boolean add(E e) {
	final boolean result = cache.add(e);
	for(Collection<E> targ:targets)
	    targ.add(e);
	return result;
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
	boolean result = cache.addAll(c);
	for(Collection<E> targ:targets)
	    result |= targ.addAll(c);
	return result;
    }
    @Override
    public void clear() {
	cache.clear();
	for(Collection<E> targ:targets)
	    targ.clear();
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
    public Iterator<E> iterator() {
	return cache.iterator();
    }
    @Override
    public boolean remove(Object o) {
	final boolean result = cache.remove(o);
	for(Collection<E> targ:targets)
	    targ.remove(o);
	return result;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
	final boolean result = cache.removeAll(c);
	for(Collection<E> targ:targets)
	    targ.removeAll(c);
	return result;
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	final boolean result = cache.retainAll(c);
	for(Collection<E> targ:targets)
	    targ.retainAll(c);
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
    public int hashCode(){
	return cache.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
	return cache.equals(o);
    }
}//end CollectionActionDispatcher