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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IndexShiftingList<E> implements List<E> {
    private final List<E> delegate;
    private final int     startIndex,endIndex;
    
    public IndexShiftingList(List<E> delegate, int startIndex){
	this(delegate,0,-1);
    }//end constructor
    
    public IndexShiftingList(List<E> delegate, int startIndex, int endIndex){
	this.delegate  =delegate;
	this.startIndex=startIndex;
	this.endIndex  =endIndex;
    }//end constructor

    /**
     * @return the startIndex
     */
    
    public int getStartIndex() {
        return startIndex;
    }
    
    public int getTrueStartIndex(){
	int result = getStartIndex();
	result = Math.min(result, getTrueEndIndex()-1);
	return result;
    }

    /**
     * @return the endIndex
     */
    public int getEndIndex() {
	return endIndex;
    }//end getEndIndex()
    
    public int getTrueEndIndex(){
	final int ei = getEndIndex();
	if(ei==-1)
	    return delegate.size();
	else
	    return ei;
    }//end getTrueEndIndex()
    
    private void checkIndex(int localIndex){
	if(!isInRange(localIndex))
	    throw new IllegalArgumentException("Index "+localIndex+" out of bounds.");
    }
    
    private boolean isInRange(int localIndex){
	return localIndex > -1 && localIndex < size();
    }

    /**
     * @param e
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(E e) {
	delegate.add(getTrueEndIndex(), e);
	return true;
    }

    /**
     * @param index
     * @param element
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, E element) {
	checkIndex(index);
	delegate.add(index+getStartIndex(), element);
    }

    /**
     * @param c
     * @return
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
	return delegate.addAll(getTrueEndIndex(),c);
    }

    /**
     * @param index
     * @param c
     * @return
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends E> c) {
	checkIndex(index);
	return delegate.addAll(index+getStartIndex(), c);
    }

    /**
     * 
     * @see java.util.List#clear()
     */
    public void clear() {
	int remainingToRemove = size();
	while(remainingToRemove-->=0)
	    delegate.remove(getStartIndex());
    }

    /**
     * @param o
     * @return
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
	for(int i=0; i<size(); i++)
	    if(get(i)==o)return true;
	return false;
    }

    /**
     * @param c
     * @return
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final ArrayList<?> temp = new ArrayList(c);
	final int size = size();
	for(int i=0; i<size; i++)
	    temp.remove(get(i));
	return temp.isEmpty();
	}

    /**
     * @param index
     * @return
     * @see java.util.List#get(int)
     */
    public E get(int index) {
	checkIndex(index);
	return delegate.get(index+getStartIndex());
    }

    /**
     * @param o
     * @return
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
	final int size = size();
	Iterator<E> it = iterator();
	for(int i=0; i<size; i++){
	    if(it.next().equals(o))
		return i;
	}return -1;
    }

    /**
     * @return
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
	return getStartIndex()>=delegate.size();
    }

    /**
     * @return
     * @see java.util.List#iterator()
     */
    public Iterator<E> iterator() {
	return new Iterator<E>(){
	    int index=getStartIndex();
	    @Override
	    public boolean hasNext() {
		return index+1<getTrueEndIndex();
	    }

	    @Override
	    public E next() {
		if(!hasNext())
		    throw new NoSuchElementException("End of list.");
		index++;
		return delegate.get(index);
	    }

	    @Override
	    public void remove() {
		delegate.remove(index);
		index--;
	    }};
    }//end iterator()

    /**
     * @param o
     * @return
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
	final int start = getTrueEndIndex()-1;
	final int end   = getStartIndex()-1;
	for(int i=start; i>end; i--){
	    if(delegate.get(i).equals(o))
		return i-getStartIndex();
	}return -1;
    }

    /**
     * @return
     * @see java.util.List#listIterator()
     */
    public ListIterator<E> listIterator() {
	return listIterator(0);
    }

    /**
     * @param index
     * @return
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<E> listIterator(int index) {
	return new ListIterator<E>(){
	    int index=getStartIndex();
	    @Override
	    public void add(E element) {
		delegate.add(index,element);
	    }

	    @Override
	    public boolean hasNext() {
		return index+1<getTrueEndIndex();
	    }

	    @Override
	    public boolean hasPrevious() {
		return index-1>=getStartIndex();
	    }

	    @Override
	    public E next() {
		if(!hasNext())
		    throw new NoSuchElementException("End of list.");
		index++;
		return delegate.get(index);
	    }

	    @Override
	    public int nextIndex() {
		return (index+1)-getStartIndex();
	    }

	    @Override
	    public E previous() {
		if(!hasPrevious())
		    throw new NoSuchElementException("Start of list.");
		index--;
		return delegate.get(index);
	    }

	    @Override
	    public int previousIndex() {
		return Math.max((index-1)-getStartIndex(),-1);
	    }

	    @Override
	    public void remove() {
		delegate.remove(index);
		index--;
	    }

	    @Override
	    public void set(E element) {
		delegate.set(index, element);
	    }};
    }//end listIterator()

    /**
     * @param index
     * @return
     * @see java.util.List#remove(int)
     */
    public E remove(int index) {
	checkIndex(index);
	return delegate.remove(getStartIndex()+index);
    }

    /**
     * @param o
     * @return
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
	Iterator<E> it = iterator();
	while(it.hasNext()){
	    if(it.next().equals(o))
		{it.remove();return true;}
	}return false;
    }//end remove(object)

    /**
     * @param c
     * @return
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
	Iterator<E> it = iterator();
	while(it.hasNext()){
	    if(c.contains(it.next()))
		{it.remove();return true;}
	}return false;
    }//end removeAll(...)

    /**
     * @param c
     * @return
     * @see java.util.List#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
	Iterator<E> it = iterator();
	boolean result = false;
	while(it.hasNext()){
	    if(!c.contains(it.next()))
		{it.remove();result=true;}
	}return result;
    }//end retainAll(...)

    /**
     * @param index
     * @param element
     * @return
     * @see java.util.List#set(int, java.lang.Object)
     */
    public E set(int index, E element) {
	checkIndex(index);
	return delegate.set(getStartIndex()+index, element);
    }

    /**
     * @return
     * @see java.util.List#size()
     */
    public int size() {
	int result = getTrueEndIndex()-getStartIndex();
	result     = Math.max(0,result);
	return result;
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @return
     * @see java.util.List#subList(int, int)
     */
    public List<E> subList(int fromIndex, int toIndex) {
	if(fromIndex<0)
	    throw new IllegalArgumentException("FromIndex intolerably negative.");
	if(toIndex<0)
	    throw new IllegalArgumentException("FromIndex intolerably negative.");
	return new IndexShiftingList<E>(delegate,getStartIndex()+fromIndex, getStartIndex()+toIndex);
    }

    /**
     * @return
     * @see java.util.List#toArray()
     */
    public Object[] toArray() {
	Object [] result = new Object[size()];
	int index=0;
	Iterator<E> it = iterator();
	while(it.hasNext()){
	    result[index++]=it.next();
	}return result;
    }

    /**
     * @param a
     * @return
     * @see java.util.List#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
	if(a==null)
	    throw new NullPointerException("Supplied array intolerably null.");
	final T [] result = a.length==size()?a:(T[])Array.newInstance(a.getClass(), size());
	int index=0;
	Iterator<E> it = iterator();
	while(it.hasNext()){
	    result[index++]=(T)it.next();
	}return result;
    }//end toArray(...)
 
}//end IndexShiftingList
