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

public class ListActionDispatcher<E> implements List<E> {
    protected final List<E>             cache;
    protected final Map<List<E>,Object> targetsMap = new IdentityHashMap<List<E>,Object>();
    protected final Set<List<E>>        targets = targetsMap.keySet();
    
    public ListActionDispatcher(List<E> cache){
	if(cache==null)
	    throw new NullPointerException("Supplied cache is intolerably null.");
	this.cache=cache;
    }//end constructor
    
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
	for(List<E> targ:targets)
	    targ.add(e);
	return cache.add(e);
    }
    @Override
    public void add(int index, E element) {
	for(List<E> targ:targets)
	    targ.add(index,element);
	cache.add(index,element);
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
	for(List<E> targ:targets)
	    targ.addAll(c);
	return cache.addAll(c);
    }
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
	for(List<E> targ:targets)
	    targ.addAll(index,c);
	return cache.addAll(index,c);
    }
    @Override
    public void clear() {
	for(List<E> targ:targets)
	    targ.clear();
	cache.clear();
    }
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
	return cache.iterator();
    }
    @Override
    public int lastIndexOf(Object o) {
	return cache.lastIndexOf(o);
    }
    @Override
    public ListIterator<E> listIterator() {
	return cache.listIterator();
    }
    @Override
    public ListIterator<E> listIterator(int index) {
	return cache.listIterator(index);
    }
    @Override
    public boolean remove(Object o) {
	for(List<E> targ:targets)
	    targ.remove(o);
	return cache.remove(o);
    }
    @Override
    public E remove(int index) {
	for(List<E> targ:targets)
	    try{targ.remove(index);} catch(IndexOutOfBoundsException e){}//Ignore
	return cache.remove(index);
    }
    @Override
    public boolean removeAll(Collection<?> c) {
	for(List<E> targ:targets)
	    targ.removeAll(c);
	return cache.removeAll(c);
    }
    @Override
    public boolean retainAll(Collection<?> c) {
	for(List<E> targ:targets)
	    targ.retainAll(c);
	return cache.retainAll(c);
    }
    @Override
    public E set(int index, E element) {
	for(List<E> targ:targets)
	    targ.set(index,element);
	return cache.set(index, element);
    }
    @Override
    public int size() {
	return cache.size();
    }
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
	return cache.subList(fromIndex, toIndex);
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
}//end ListActionDispatcher
