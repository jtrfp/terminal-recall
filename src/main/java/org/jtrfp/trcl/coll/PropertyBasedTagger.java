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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.apache.commons.beanutils.PropertyUtils;

import com.ochafik.util.listenable.Adapter;
import com.ochafik.util.listenable.Pair;

public class PropertyBasedTagger<E extends PropertyListenable,KEY,PROPERTY_TYPE> implements Collection<E>, Decorator<Collection<Pair<KEY,E>>> {
    private final Collection<Pair<KEY,E>>    delegate;
    private final Adapter<PropertyChangeEvent,KEY> propertyAdapter;
    private final Map<E,PropertyChangeListener> listeners= new HashMap<E,PropertyChangeListener>();
    private final Map<E,Pair<KEY,E>> pairs               = new HashMap<E,Pair<KEY,E>>();
    private final String propertyName;
    private final ExecutorService executor;
    
    public PropertyBasedTagger(Collection<Pair<KEY,E>> delegate, Adapter<PropertyChangeEvent,KEY> nonCachedPropertyAdapter, String propertyName, ExecutorService executor){
	this.delegate       =delegate;
	this.propertyAdapter=nonCachedPropertyAdapter;
	this.propertyName   =propertyName;
	this.executor       =executor;
    }
    
    @Override
    public boolean add(final E e) {
	if(e==null)
	    throw new NullPointerException("Passed element intolerably null.");
	Pair<KEY,E>pair= pairs.get(e);
	if(pair!=null)
	    return false;
	try{pair= new Pair<KEY,E>(propertyAdapter.adapt(new PropertyChangeEvent(e, propertyName, null, (PROPERTY_TYPE)PropertyUtils.getProperty(e, propertyName))),e);}
	catch(NoSuchMethodException     ex){throw new IllegalArgumentException("Supplied element (bean) does not expose method for property: `"+propertyName+"`.");}
	catch(InvocationTargetException ex){throw new RuntimeException(ex);}
	catch(IllegalAccessException    ex){throw new IllegalArgumentException("Supplied element (bean) does not allow access for property `"+propertyName+"`.",ex);}
	
	PropertyChangeListener pcl = new PropertyChangeListener(){
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		final Runnable r = new Runnable(){
		    @Override
		    public void run() {
			final KEY newKey     = propertyAdapter.adapt(evt);
			final Pair<KEY,E>pair= pairs.get(e);
			assert pair!=null:"pair unexpectedly null.";
			final KEY oldKey     = pair.getKey();
			if(!newKey.equals(oldKey)){
			    //Remove old
			    delegate.remove(pair);
			    pairs   .remove(e);
			    //Add new
			    final Pair<KEY,E> newPair = new Pair<KEY,E>(newKey,e);
			    assert newPair!=null:"pair unexpectedly null.";
			    final boolean addResult = delegate.add(newPair); 
			    assert addResult:"Failed to add pair to delegate.";
			    pairs.put(e,newPair);
			}//end if(changed key)
		    }};
		if(executor==null)
		    r.run();
		else
		    try{executor.submit(r);}catch(Exception e){throw new RuntimeException(e);}
	    }};
	listeners.put(e,pcl);
	e.addPropertyChangeListener(propertyName, pcl);
	assert pair!=null:"pair unexpectedly null.";
	delegate.add(pair);
	pairs.put(e,pair);
	return true;
    }//end add(...)

    @Override
    public boolean addAll(Collection<? extends E> c) {
	for(E element:c)
	    add(element);
	return !c.isEmpty();
    }

    @Override
    public void clear() {
	//Remove all listeners
	for(Entry<E,PropertyChangeListener> entry:listeners.entrySet())
	    entry.getKey().removePropertyChangeListener(entry.getValue());
	listeners.clear();
	pairs.clear();
	delegate.clear();
    }

    @Override
    public boolean contains(Object o) {
	for(Entry<E,Pair<KEY,E>> entry:pairs.entrySet())
	    if(entry.getKey().equals(o))
		return true;
	return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	final ArrayList<?> toFind = new ArrayList<Object>(c);
	for(Entry<E,Pair<KEY,E>> entry:pairs.entrySet()){
	    if(toFind.isEmpty())break;
	    toFind.remove(entry.getKey());
	    }
	return toFind.isEmpty();
    }

    @Override
    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
	final Iterator<E> iterator = pairs.keySet().iterator();
	return new Iterator<E>(){
	    @Override
	    public boolean hasNext() {
		return iterator.hasNext();
	    }

	    @Override
	    public E next() {
		return iterator.next();
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();
	    }};
    }//end iterator()

    @Override
    public boolean remove(Object o) {
	final PropertyChangeListener pcl = listeners.remove(o);
	if(pcl!=null){
	    final Pair<KEY,E> pair = pairs.remove(o);
	    pair.getValue().removePropertyChangeListener(pcl);
	    delegate.remove(pair);
	    return true;
	}return false;
    }//end remove(...)

    @Override
    public boolean removeAll(Collection<?> c) {
	boolean result = false;
	for(Object o:c)
	    result |= remove(o);
	return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	final ArrayList<E> toRemove = new ArrayList<E>();
	for(E element:pairs.keySet())
	    if(!c.contains(element))
		toRemove.add(element);
	return removeAll(toRemove);
    }

    @Override
    public int size() {
	return delegate.size();
    }

    @Override
    public Object[] toArray() {
	return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	return delegate.toArray(a);
    }

    @Override
    public Collection<Pair<KEY,E>> getDelegate() {
	return delegate;
    }
    
}//end PropertyBasedTagger
