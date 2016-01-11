/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ObjectTallyCollection<T> implements Collection<T>, Decorator<Collection<T>>{
    private final Collection<T> delegate;
    private final Map<T,Integer> tallies = new HashMap<T,Integer>();
    private Map<T,Set<ObjectTallyListener<T>>> objectTallyListeners = new HashMap<T,Set<ObjectTallyListener<T>>>();
    
    public ObjectTallyCollection(Collection<T> delegate){
	this.delegate = delegate;
    }
    
    public int getTallyOf(T objectToTally){
	final Integer result = tallies.get(objectToTally);
	return result==null?0:result;
    }
    
    private void increment(T e){
	Integer tally = tallies.get(e);
	tally = tally==null?0:tally;
	fireObjectTallyChange(e, tally, tally+1);
	tally++;
	tallies.put(e,tally);
    }//end increment
    
    private void decrement(T e){
	Integer tally = tallies.get(e);
	tally = tally==null?0:tally;
	fireObjectTallyChange(e, tally, tally-1);
	tally--;
	if(tally<0)
	    throw new NegativeTallyException();
	tallies.put(e,tally);
    }
    
    private Set<ObjectTallyListener<T>> getObjectTallyListeners(T object){
	Set<ObjectTallyListener<T>> listenerSet = objectTallyListeners.get(object);
	if(listenerSet == null)
	    objectTallyListeners.put(object, listenerSet = new HashSet<ObjectTallyListener<T>>());
	return listenerSet;
    }
    
    private void fireObjectTallyChange(T object, int oldValue, int newValue){
	final Set<ObjectTallyListener<T>> listenerSet = getObjectTallyListeners(object);
	for(ObjectTallyListener<T> l:listenerSet)
	    l.tallyChanged(object, oldValue, newValue);
    }
    
    public boolean add(T e) {
	increment(e);
	return delegate.add(e);
    }

    public boolean addAll(Collection<? extends T> c) {
	for(T e:c)
	    increment(e);
	return delegate.addAll(c);
    }

    public void clear() {
	for(T e:delegate)
	    increment(e);
	delegate.clear();
    }

    public boolean contains(Object o) {
	return delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
	return delegate.containsAll(c);
    }

    public boolean equals(Object o) {
	return delegate.equals(o);
    }


    public int hashCode() {
	return delegate.hashCode();
    }

    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    public Iterator<T> iterator() {
	return delegate.iterator();
    }

    public boolean remove(Object o) {
	decrement((T)o);//TODO: Risky business here.
	return delegate.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
	for(Object e : c)
	    tallies.remove(e);
	return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    public int size() {
	return delegate.size();
    }

    public Object[] toArray() {
	return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
	return delegate.toArray(a);
    }

    @Override
    public Collection<T> getDelegate() {
	return delegate;
    }
    
    public void addObjectTallyListener(T object, ObjectTallyListener<T> listener){
	getObjectTallyListeners(object).add(listener);
    }
    
    public void removeObjectTallyListener(T object, ObjectTallyListener<T> listener){
	getObjectTallyListeners(object).remove(listener);
    }
    
    public static final class NegativeTallyException extends IllegalStateException{
	public NegativeTallyException(){super();}
    }
    
    public static interface ObjectTallyListener<T> {
	public void tallyChanged(T object, int oldTally, int newTally);
    }
}//end ObjectTallyList
