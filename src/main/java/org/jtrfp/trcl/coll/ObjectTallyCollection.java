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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;

public final class ObjectTallyCollection<T> implements Collection<T>, Decorator<Collection<T>>{
    //PROPERTIES
    public static final String OBJECT_TALLY = "objectTally";
    
    private final Collection<T> delegate;
    private final Map<T,Integer> tallies = new ReferenceMap<T,Integer>(ReferenceStrength.WEAK,ReferenceStrength.HARD);
    private Map<T,WeakHashMap<PropertyChangeListener,Object>> objectTallyListeners = new ReferenceMap<T,WeakHashMap<PropertyChangeListener,Object>>(ReferenceStrength.WEAK,ReferenceStrength.HARD);
    
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
	    throw new NegativeTallyException(e);
	if(tally == 0)
	    tallies.remove(e);
	else
	    tallies.put(e,tally);
    }//end decrement
    
    private WeakHashMap<PropertyChangeListener,Object> getObjectTallyListeners(T object){
	WeakHashMap<PropertyChangeListener,Object> listenerSet = objectTallyListeners.get(object);
	if(listenerSet == null)
	    objectTallyListeners.put(object, listenerSet = new WeakHashMap<PropertyChangeListener,Object>());
	return listenerSet;
    }
    
    private void fireObjectTallyChange(T object, int oldValue, int newValue){
	final Set<PropertyChangeListener> listenerSet = getObjectTallyListeners(object).keySet();
	final PropertyChangeEvent evt = new PropertyChangeEvent(this, OBJECT_TALLY, oldValue, newValue);
	for(PropertyChangeListener l:listenerSet)
	    l.propertyChange(evt);
    }//end fireObjectTallyChange
    
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
/*
    public boolean equals(Object o) {
	return delegate.equals(o);
    }
*/

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
    
    public void addObjectTallyListener(T object, PropertyChangeListener listener){
	getObjectTallyListeners(object).put(listener,Void.class);
    }
    
    public void removeObjectTallyListener(T object, PropertyChangeListener listener){
	getObjectTallyListeners(object).remove(listener);
    }
    
    public static final class NegativeTallyException extends IllegalStateException{
	public NegativeTallyException(){super();}

	public NegativeTallyException(Object e) {
	    super("Element in question: "+e);
	}
    }
}//end ObjectTallyList
