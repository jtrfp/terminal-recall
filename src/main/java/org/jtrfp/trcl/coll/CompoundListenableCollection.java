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
import java.util.Iterator;

import com.ochafik.util.CompoundCollection;
import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableSupport;

public class CompoundListenableCollection<T> implements ListenableCollection<T> {
    private final ListenableSupport<T>  ls = new ListenableSupport<T>();
    private final CompoundCollection<T> delegate;
    private final CollectionListener<T> innerListener = new CollectionListener<T>(){
	@Override
	public void collectionChanged(CollectionEvent<T> e) {
	    switch(e.getType()){
	    case ADDED:
		ls.fireAdded(CompoundListenableCollection.this, e.getElements());
		break;
	    case REMOVED:
		ls.fireRemoved(CompoundListenableCollection.this, e.getElements());
		break;
	    case UPDATED:
		ls.fireUpdated(CompoundListenableCollection.this, e.getElements());
		break;
	    default:
		break;
	    }//end EventTypes
	}};
	private final CollectionListener<ListenableCollection<T>> outerListener = new CollectionListener<ListenableCollection<T>>(){
		@Override
		public void collectionChanged(CollectionEvent<ListenableCollection<T>> e) {
		    switch(e.getType()){
		    case ADDED:
			for(ListenableCollection<T> coll:e.getElements()){
			    delegate.addComponent(coll);
			    coll.addCollectionListener(innerListener);
			    ls.fireAdded(CompoundListenableCollection.this, coll);
			}//end for(collections added)
			break;
		    case REMOVED:
			for(ListenableCollection<T> coll:e.getElements()){
			    delegate.removeComponent(coll);
			    coll.removeCollectionListener(innerListener);
			    ls.fireRemoved(CompoundListenableCollection.this, coll);
			}//end for(collections removed)
			break;
		    case UPDATED:
			// ????
			break;
		    default:
			break;
		    }//end EventTypes
		}};
    
    public CompoundListenableCollection(ListenableCollection<ListenableCollection<T>> adapted){
	final Object wildcardObject = adapted;//Just don't look at it and it won't hurt as much.
	delegate = new CompoundCollection<T>((Collection<Collection<? extends T>>)wildcardObject);
	adapted.addCollectionListener(outerListener);
    }//end constructor

    @Override
    public void addCollectionListener(CollectionListener<T> l) {
	ls.addCollectionListener(l);
    }

    @Override
    public void removeCollectionListener(CollectionListener<T> l) {
	ls.removeCollectionListener(l);
    }

    /**
     * @param col
     * @see com.ochafik.util.CompoundCollection#addComponent(java.util.Collection)
     */
    public void addComponent(Collection<T> col) {
	delegate.addComponent(col);
    }

    /**
     * 
     * @see com.ochafik.util.CompoundCollection#clearComponents()
     */
    public void clearComponents() {
	delegate.clearComponents();
    }

    /**
     * 
     * @see com.ochafik.util.CompoundCollection#clear()
     */
    public void clear() {
	delegate.clear();
    }

    /**
     * @param object
     * @return
     * @deprecated
     * @see com.ochafik.util.CompoundCollection#add(java.lang.Object)
     */
    public boolean add(T object) {
	return delegate.add(object);
    }

    /**
     * @param object
     * @return
     * @see com.ochafik.util.CompoundCollection#contains(java.lang.Object)
     */
    public boolean contains(Object object) {
	return delegate.contains(object);
    }

    /**
     * @param col
     * @return
     * @deprecated
     * @see com.ochafik.util.CompoundCollection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends T> col) {
	return delegate.addAll(col);
    }

    /**
     * @param col
     * @return
     * @see com.ochafik.util.CompoundCollection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> col) {
	return delegate.containsAll(col);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
	return delegate.equals(obj);
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
	return delegate.hashCode();
    }

    /**
     * @param col
     * @return
     * @see com.ochafik.util.CompoundCollection#removeComponent(java.util.Collection)
     */
    public boolean removeComponent(Collection<T> col) {
	return delegate.removeComponent(col);
    }

    /**
     * @return
     * @see com.ochafik.util.CompoundCollection#size()
     */
    public int size() {
	return delegate.size();
    }

    /**
     * @return
     * @see com.ochafik.util.CompoundCollection#isEmpty()
     */
    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    /**
     * @return
     * @see com.ochafik.util.CompoundCollection#toArray()
     */
    public Object[] toArray() {
	return delegate.toArray();
    }

    /**
     * @param objects
     * @return
     * @see com.ochafik.util.CompoundCollection#toArray(T[])
     */
    public <T> T[] toArray(T[] objects) {
	return delegate.toArray(objects);
    }

    /**
     * @param object
     * @return
     * @see com.ochafik.util.CompoundCollection#remove(java.lang.Object)
     */
    public boolean remove(Object object) {
	return delegate.remove(object);
    }

    /**
     * @param col
     * @return
     * @see com.ochafik.util.CompoundCollection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> col) {
	return delegate.removeAll(col);
    }

    /**
     * @param col
     * @return
     * @see com.ochafik.util.CompoundCollection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> col) {
	return delegate.retainAll(col);
    }

    /**
     * @return
     * @see com.ochafik.util.CompoundCollection#iterator()
     */
    public Iterator<T> iterator() {
	return delegate.iterator();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return delegate.toString();
    }
}//end CompoundListenableCollection
