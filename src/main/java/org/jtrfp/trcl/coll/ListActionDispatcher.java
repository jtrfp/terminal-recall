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
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;

public class ListActionDispatcher<E> implements List<E>, RangeClearable<E> {
    protected final List<E>             cache;
    protected final Map<List<E>,Object> targetsMap;
    protected final Set<List<E>>        targets;
    
    public ListActionDispatcher(List<E> cache){
	this.cache  =cache;
	targetsMap  = new IdentityHashMap<List<E>,Object>();
	this.targets=targetsMap.keySet();
    }//end constructor
    
    public ListActionDispatcher() {
	this(new DummyList<E>("This ListActionDispatcher has a dummylist."));
    }

    /**
     * Registers the List 
     * to be forwarded List operations given to this dispatcher. When prefilled, the supplied target is assumed
     * to be empty when added.
     * @param target The target List to which to forward List operations.
     * @param prefill Immediately perform addAll operation to the target of the cached state.
     * @return true if this Dispatcher did not already have the given target registered.
     * @since Mar 20, 2015
     */
    public boolean addTarget(List<E> target, boolean prefill){
	if(prefill) target.addAll(cache);
	boolean result = !targets.contains(target);
	targetsMap.put(target,null);
	return result;
    }
    
    public boolean removeTarget(List<E> target, boolean removeAll){
	if(removeAll && targets.contains(target))
	    target.removeAll(cache);
	return targets.remove(target);
    }
    
    @Override
    public boolean add(E e) {
	final boolean result = cache.add(e);
	for(List<E> targ:targets)
	    (targ).add(e);
	return result;
    }
    @Override
    public void add(int index, E element) {
	cache.add(index,element);
	for(List<E> targ:targets)
	    (targ).add(index,element);
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
	final boolean result = cache.addAll(c);
	for(List<E> targ:targets)
	    (targ).addAll(c);
	return result;
    }
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
	final boolean result = cache.addAll(index,c);
	for(List<E> targ:targets)
	    (targ).addAll(index,c);
	return result;
    }
    @Override
    public void clear() {
	cache.clear();
	    for(List<E> targ:targets)
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
    public E get(int index) {
	return cache.get(index);
    }
    @Override
    public int indexOf(Object o) {
	return cache.indexOf(o);
    }
    @Override
    public boolean isEmpty() {
	return cache.isEmpty();
    }
    @Override
    public Iterator<E> iterator() {
	final Iterator<E> ci = cache.iterator();
	return new Iterator<E>(){
	    @Override
	    public boolean hasNext() {
		return ci.hasNext();
	    }

	    @Override
	    public E next() {
		return ci.next();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }};
    }
    @Override
    public int lastIndexOf(Object o) {
	return cache.lastIndexOf(o);
    }
    @Override
    public ListIterator<E> listIterator() {
	return IteratorUtils.toListIterator(iterator());
    }
    @Override
    public ListIterator<E> listIterator(int index) {
	return cache.subList(index, size()).listIterator();
    }
    @Override
    public boolean remove(Object o) {
	final boolean result = cache.remove(o);
	for(List<E> targ:targets)
	    targ.remove(o);
	return result;
    }
    @Override
    public E remove(int index) {
	final E result = cache.remove(index);
	for(List<E> targ:targets)
	    try{targ.remove(index);} catch(IndexOutOfBoundsException e){}//Ignore
	return result;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
	final boolean result = cache.removeAll(c);
	for(List<E> targ:targets)
	    targ.removeAll(c);
	return result;
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	final boolean result = cache.retainAll(c);
	for(List<E> targ:targets)
	    targ.retainAll(c);
	return result;
    }
    @Override
    public E set(int index, E element) {
	final E result = cache.set(index, element);
	for(List<E> targ:targets)
	    targ.set(index,element);
	return result;
    }
    @Override
    public int size() {
	return cache.size();
    }
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
	return new SubList<E>(this,fromIndex,toIndex);
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
	return super.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
	return super.equals(o);
    }

    @Override
    public void clearRange(int startIndex, int endIndex) {
	cache.subList(startIndex, endIndex).clear();
	for(List<E> targ:targets)
	    targ.subList(startIndex, endIndex).clear();
    }//end clearRange(...)
}//end ListActionDispatcher
