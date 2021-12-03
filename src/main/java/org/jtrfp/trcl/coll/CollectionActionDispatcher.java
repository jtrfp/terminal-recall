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
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.jtrfp.trcl.tools.Util;

public class CollectionActionDispatcher<E> implements Collection<E>, Repopulatable<E>, BulkRemovable<E>, Decorator<Collection<E>> {
    protected final Collection<E>             cache;
    protected final Set<Collection<E>>        targets;
    
    public CollectionActionDispatcher(Collection<E> cache){
	this.cache     =cache;
	this.targets   =SetUtils.newIdentityHashSet();
    }
    
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
	if(target == null)
	    throw new NullPointerException("Target intolerably null.");
	if(targets.contains(target))
	    throw new RuntimeException("Redundant add: "+target);
	if(prefill && !cache.isEmpty())
	    target.addAll(cache);
	boolean result = !targets.contains(target);
	targets.add(target);
	return result;
    }
    
    public boolean removeTarget(Collection<E> target, boolean unfill){
	if(target == null)
	    throw new NullPointerException("Target intolerably null.");
	if(!targets.contains(target))
	    throw new RuntimeException("Target not present: "+target);
	if(unfill && targets.contains(target))
	    Util.bulkRemove(cache, target);
	final boolean success = targets.contains(target);
	targets.remove(target);
	return success;
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
	for(Collection<E> target:targets)
	    Util.bulkRemove(cache, target);
	cache.clear();
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
	final Iterator<E> iterator = new ArrayList<E>(cache).iterator();
	return new Iterator<E>(){
	    E lastReturned;

	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }

	    @Override
	    public E next() {
		return lastReturned = iterator.next();
	    }

	    @Override
	    public void remove() {
		iterator.remove();
		CollectionActionDispatcher.this.remove(lastReturned);
	    }};
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
    public boolean equals(Object o){
	if(o instanceof CollectionActionDispatcher)
	    o=((CollectionActionDispatcher) o).cache;
	if(o instanceof Collection) {
	    final Collection<?> coll = (Collection<?>)o;
	    if(cache.size() != coll.size())
		return false;
	    else
		return cache.containsAll(coll) && coll.containsAll(cache);
	    }//end if(Collection)
	else
	    return cache.equals(o);
    }

    @Override
    public void repopulate(Collection<E> c) {
	Util.repopulate(cache,c);
	for(Collection<E> targ:targets)
	    Util.repopulate(targ, c);
    }
    

    public static final CollectionActionDispatcher<?> EMPTY = new CollectionActionDispatcher(){
	@Override
	public boolean addTarget(Collection c, boolean populate){return false;}
	@Override
	public boolean removeTarget(Collection c, boolean populate){return false;}
    };

    @Override
    public Collection<E> getDelegate() {
	return cache;
    }
    
    @Override
    public String toString(){
	StringBuilder sb = new StringBuilder();
	for(E element:cache)
	    sb.append(" "+element+"; ");
	return "CollectionActionDispatcher ["+sb+"] ";
    }

    @Override
    public void bulkRemove(Collection<E> c) {
	Util.bulkRemove(c,cache);
	for(Collection<E> targ:targets)
	    Util.bulkRemove(c, targ);
    }
}//end CollectionActionDispatcher
