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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.IteratorChain;

public class PredicatedORCollectionActionFilter<E> implements Collection<Predicate<E>>, Repopulatable<E> {
    private Collection<E> 
    	    used  = new ArrayList<E>(),
	    unused= new ArrayList<E>();
    private Set<Predicate<E>> predicates  = new HashSet<Predicate<E>>();
    private final Collection<E> delegate;
    
    public PredicatedORCollectionActionFilter(Collection<E> delegate){
	this.delegate=delegate;
    }

    @Override
    public boolean add(Predicate<E> predicatesToAdd) {
	return addAll(Arrays.asList(predicatesToAdd));
    }

    @Override
    public boolean addAll(Collection<? extends Predicate<E>> _toAdd) {
	final ArrayList<? extends Predicate<E>> toAdd = new ArrayList<Predicate<E>>(_toAdd);
	Iterator<? extends Predicate<E>> taIterator = toAdd.iterator();
	//Don't add what's already there.
	while(taIterator.hasNext())
	    if(!predicates.add(taIterator.next()))
		taIterator.remove();
	
	final Iterator<E> unusedIterator = unused.iterator();
	E element;
	final ArrayList<E> addToUsedAndDelegate = new ArrayList<E>(unusedIterator.hasNext()?8:0);
	while(unusedIterator.hasNext()){
	    element = unusedIterator.next();
	    if(matchesPredicates(element,toAdd)){
		unusedIterator.remove();
		addToUsedAndDelegate.add(element);
	    }//end if(matchesPredicates)
	}//end while(hashNext())
	if(!addToUsedAndDelegate.isEmpty()){
	    used.addAll(addToUsedAndDelegate);
	    delegate.addAll(addToUsedAndDelegate);
	}
	return !toAdd.isEmpty();
    }//end addAll(...)
    
    private boolean matchesPredicates(E element, Collection<? extends Predicate<E>> predicates){
	for(Predicate<E> predicate:predicates)
	    if(predicate.evaluate(element))
		return true;
	return false;
    }//end matchesPredicates(...)

    @Override
    public void clear() {
	ArrayList<E> toRemove = new ArrayList<E>(used);
	delegate  .removeAll(toRemove);
	predicates.clear();
	unused    .addAll(toRemove);
	used      .clear();
    }

    @Override
    public boolean contains(Object predicate) {
	return predicates.contains(predicate);
    }

    @Override
    public boolean containsAll(Collection<?> predicates) {
	return this.predicates.containsAll(predicates);
    }

    @Override
    public boolean isEmpty() {
	return predicates.isEmpty();
    }

    @Override
    public Iterator<Predicate<E>> iterator() {
	return UnmodifiableIterator.decorate(predicates.iterator());
    }

    @Override
    public boolean remove(Object element) {//TODO: This is single instance
	return removeAll(Arrays.asList(element));
    }

    @Override
    public boolean removeAll(Collection<?> _toRemove) {
	final ArrayList<Predicate<E>> toRemove = new ArrayList<Predicate<E>>(_toRemove.size());
	//Remove only Predicates
	for(Object r:_toRemove)
	    if(r instanceof Predicate) toRemove.add((Predicate<E>)r);
	Iterator<Predicate<E>> trIterator = toRemove.iterator();
	//Don't remove what isn't present
	while(trIterator.hasNext())
	    if(!predicates.contains(trIterator.next()))
		trIterator.remove();
	final ArrayList<E> addToUnusedAndDelegate = new ArrayList<E>(trIterator.hasNext()?8:0);
	predicates.removeAll(toRemove);
	final Iterator<E> usedIterator = used.iterator();
	E element;
	while(usedIterator.hasNext()){
	    element = usedIterator.next();
	    if(!matchesPredicates(element,toRemove)){
		usedIterator.remove();
		addToUnusedAndDelegate.add(element);
	    }//end if(matchesPredicates)
	}//end while(hashNext())
	if(!addToUnusedAndDelegate.isEmpty()){
	    used.addAll(addToUnusedAndDelegate);
	    delegate.addAll(addToUnusedAndDelegate);
	}
	return !toRemove.isEmpty();
    }//end removeAll(...)

    @Override
    public boolean retainAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
	return predicates.size();
    }

    @Override
    public Object[] toArray() {
	return predicates.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
	return (T[])predicates.toArray(array);
    }

    public final Collection<E> input = new Collection<E>(){
	@Override
	public boolean add(E e) {
	    boolean result;
	    if(matchesPredicates(e,predicates)){
		result = used.add(e);
		delegate.add(e);
	    }else{result = unused.add(e);}
	    return result;
	}//end add(E)

	@Override
	public boolean addAll(Collection<? extends E> c) {
	    boolean result = false;
	    for(E element:c)
		result |= add(element);
	    return result;
	}//end addAll(...)

	@Override
	public void clear() {
	    delegate.removeAll(used);
	    used    .clear();
	    unused  .clear();
	}

	@Override
	public boolean contains(Object o) {
	    if(used.contains(o))
		return true;
	    else if(unused.contains(o))
		return true;
	    else return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
	    Iterator<?> iterator = c.iterator();
	    boolean result = !c.isEmpty();
	    while(!result && iterator.hasNext()){
		Object element = iterator.next();
		result &= used.contains(element) || unused.contains(element);
	    }
	    return result;
	}//end containsAll(...)

	@Override
	public boolean isEmpty() {
	    return used.isEmpty() && unused.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
	    return UnmodifiableIterator.decorate(new IteratorChain(Arrays.asList(used.iterator(),unused.iterator())));
	}

	@Override
	public boolean remove(Object o) {
	    boolean result = false;
	    result |= used.remove(o);
	    result |= unused.remove(o);
	    result |= delegate.remove(o);
	    return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
	    boolean result = false;
	    result |= used.removeAll(c);
	    result |= unused.removeAll(c);
	    result |= delegate.removeAll(c);
	    return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	    boolean result = false;
	    result |= used.retainAll(c);
	    result |= unused.retainAll(c);
	    result |= delegate.retainAll(c);
	    return result;
	}

	@Override
	public int size() {
	    return used.size()+unused.size();
	}

	@Override
	public Object[] toArray() {
	    Object [] result = new Object[size()];
	    int index=0;
	    Iterator<E> iterator = iterator();
	    while(iterator.hasNext()){
		result[index]=iterator.next();
		index++;
	    }
	    return result;
	}//end toArray()

	@Override
	public <T> T[] toArray(T[] a) {
	    final int size = size();
	    if(a.length<size) a = (T[])Array.newInstance(a.getClass(), size);
	    int index=0;
	    Iterator<E> iterator = iterator();
	    while(iterator.hasNext()){
		a[index]=(T)iterator.next();
		index++;
	    }
	    return a;
	}};

    @Override
    public void repopulate(Collection<E> c) {
	// TODO Auto-generated method stub
	throw new UnsupportedOperationException("Chuck forgot to implement.");
    }
}//end PredicatedORListActionFilter
