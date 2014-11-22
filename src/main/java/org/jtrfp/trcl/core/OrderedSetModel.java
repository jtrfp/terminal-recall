/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.AbstractListModel;

/**
 * WARNING: "Ordered" != "Sorted." This does not sort items in any way, rather it 
 * retains the order of the items as they are added in the style of a List.
 * @author Chuck Ritola
 *
 * @param <E>
 */
public class OrderedSetModel<E> extends AbstractListModel<E> implements Set<E>, List<E> {
    /**
     * 
     */
    private static final long serialVersionUID = -1729987277141298234L;
    private final Set<E>  set  = new HashSet<E>();
    private final List<E> list = new ArrayList<E>();

    @Override
    public E getElementAt(int index) {
	return list.get(index);
    }

    @Override
    public int getSize() {
	return list.size();
    }

    /**
     * @param item
     * @return
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(E item) {
	if(set.contains(item))
	    return false;
	list.add(item);
	set.add(item);
	this.fireIntervalAdded(this, list.size()-1, list.size()-1);
	return true;
    }

    /**
     * @param items
     * @return
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> items) {
	boolean result=false;
	int itemsAdded=0;
	for(E item:items)
	 if(!set.contains(item))
	  {set.add(item);list.add(item);result=true;itemsAdded++;}
	final int startInterval= list.size()-itemsAdded;
	final int endInterval  = list.size()-1;
	this.fireIntervalAdded(this, startInterval, endInterval);
	return result;
    }//end addAll(...)

    /**
     * 
     * @see java.util.Set#clear()
     */
    public void clear() {
	final int endInterval = list.size()-1;
	list.clear();
	set.clear();
	this.fireIntervalRemoved(this, 0, endInterval);
    }

    /**
     * @param item
     * @return
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object item) {
	return set.contains(item);
    }

    /**
     * @param items
     * @return
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> items) {
	return set.containsAll(items);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.Set#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
	return set.equals(arg0);
    }

    /**
     * @return
     * @see java.util.Set#hashCode()
     */
    public int hashCode() {
	return set.hashCode();
    }

    /**
     * @return
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
	return list.isEmpty();
    }

    /**
     * @return
     * @see java.util.Set#iterator()
     */
    public Iterator<E> iterator() {
	final Iterator<E> it = list.iterator();
	return new Iterator<E>(){
	    private E current;
	    @Override
	    public boolean hasNext() {
		return it.hasNext();
	    }

	    @Override
	    public E next() {
		return current=it.next();
	    }

	    @Override
	    public void remove() {
		set.remove(current);
		it.remove();
	    }};
    }//end iterator()

    /**
     * @param item
     * @return
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object item) {
	list.remove(item);
	return set.remove(item);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> items) {
	boolean result=false;
	final int endInterval = list.size()-1;
	for(Object item:items)
	 if(set.contains(item))
	  {set.remove(item);list.remove(item);result=true;}
	this.fireIntervalRemoved(this, 0, endInterval);
	return result;
    }

    /**
     * @param items
     * @return
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> items) {
	boolean result = false;
	for(Object item:items){
	    if(!set.contains(item))
		{this.remove(item);result=true;}
	}//end for(items)
	return result;
    }//end retainAll()

    /**
     * @return
     * @see java.util.Set#size()
     */
    public int size() {
	return list.size();
    }

    /**
     * @return
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
	return list.toArray();
    }

    /**
     * @param dest
     * @return
     * @see java.util.Set#toArray(T[])
     */
    public <T> T[] toArray(T[] dest) {
	return list.toArray(dest);
    }

    /**
     * @param arg0
     * @param arg1
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, E item) {
	if(set.contains(item))
	    return;
	set.add(item);
	list.add(index, item);
	this.fireIntervalAdded(this, index, index);
    }

    /**
     * @param startIndex
     * @param src
     * @return
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int startIndex, Collection<? extends E> src) {
	set.addAll(src);
	boolean result = list.addAll(startIndex, src);
	if(result)
	    this.fireIntervalAdded(this, startIndex, src.size());
	return result;
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#get(int)
     */
    public E get(int arg0) {
	return list.get(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object arg0) {
	return list.indexOf(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object arg0) {
	return list.lastIndexOf(arg0);
    }

    /**
     * @return
     * @see java.util.List#listIterator()
     */
    public ListIterator<E> listIterator() {
	return list.listIterator();
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<E> listIterator(int arg0) {
	return list.listIterator(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see java.util.List#remove(int)
     */
    public E remove(int arg0) {
	set.remove(arg0);
	return list.remove(arg0);
    }

    /**
     * @param index
     * @param newElement
     * @return
     * @see java.util.List#set(int, java.lang.Object)
     */
    public E set(int index, E newElement) {
	E oldElement = list.set(index, newElement);
	if(oldElement==newElement)
	    return oldElement;
	set.remove(oldElement);
	set.add(newElement);
	return oldElement;
    }

    /**
     * @param lo
     * @param hi
     * @return
     * @see java.util.List#subList(int, int)
     */
    public List<E> subList(int lo, int hi) {
	return list.subList(lo, hi);
    }
}//end SetListModel
