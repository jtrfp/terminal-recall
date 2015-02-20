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

package org.jtrfp.trcl.pool;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Minimally-implemented fixed-limit List wrapper for primitive ints[] strictly for allowing processing
 * of int[] arrays in methods which expect Lists. Read code carefully before using as this is not a 
 * complete implementation and may be prone to bugs.
 * @author Chuck Ritola
 *
 */

public final class IntArrayList implements List<Integer> {
    private final int [] delegate;
    private int          counter = 0;
    private final int    delegateOffset;
    private boolean	 representFullSize = false;
    
    public IntArrayList(int [] delegate){
	this(delegate,0);
    }
    
    public IntArrayList(int [] delegate, int counterOffset){
	if(delegate==null)
	    throw new NullPointerException("Passed delegate is intolerably null.");
	this.delegateOffset=counterOffset;
	this.delegate      =delegate;
	counter            = counterOffset;
    }

    @Override
    public boolean add(Integer e) {
	delegate[counter++]=e;
	return true;
    }

    @Override
    public void add(int index, Integer element) {
	delegate[index]=element;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
	for(Integer i:c)
	    delegate[counter++]=i;
	return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
	for(Integer i:c)
	    delegate[index++]=i;
	return false;
    }

    @Override
    public void clear() {
	throw new RuntimeException("Not implemented."); 
    }

    @Override
    public boolean contains(Object o) {
	if(o==null)return false;
	final Integer v = (Integer)o;
	return ArrayUtils.contains(delegate, v.intValue());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public Integer get(int index) {
	return delegate[index];
    }

    @Override
    public int indexOf(Object o) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public boolean isEmpty() {
	return counter==0;
    }

    @Override
    public Iterator<Integer> iterator() {
	return new Iterator<Integer>(){
	    int localCounter=delegateOffset;

	    @Override
	    public boolean hasNext() {
		if(representFullSize)
		    return localCounter < delegate.length;
		return localCounter<counter;
	    }

	    @Override
	    public Integer next() {
		return delegate[localCounter++];
	    }

	    @Override
	    public void remove() {
		throw new RuntimeException("Not implemented.");
	    }};
    }

    @Override
    public int lastIndexOf(Object o) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public ListIterator<Integer> listIterator() {
	return listIterator(0);
    }

    @Override
    public ListIterator<Integer> listIterator(int index) {
	final int off=0;
	return new ListIterator<Integer>(){
	    int localCounter=delegateOffset+off;

	    @Override
	    public void add(Integer arg0) {
		throw new RuntimeException("Not implemented.");
	    }
	    
	    @Override
	    public boolean hasNext() {
		if(representFullSize)
		    return localCounter < delegate.length;
		return localCounter<counter;
	    }

	    @Override
	    public Integer next() {
		return delegate[localCounter++];
	    }

	    @Override
	    public void remove() {
		throw new RuntimeException("Not implemented.");
	    }

	    @Override
	    public boolean hasPrevious() {
		return localCounter>0;
	    }

	    @Override
	    public int nextIndex() {
		if(representFullSize)
		    return localCounter+1<delegate.length?localCounter+1:delegate.length;
		return localCounter+1<counter?localCounter+1:counter;
	    }

	    @Override
	    public Integer previous() {
		if(hasPrevious())
		    return delegate[previousIndex()];
		else return null;
	    }

	    @Override
	    public int previousIndex() {
		if(hasPrevious())
		    return localCounter-1;
		else return -1;
	    }

	    @Override
	    public void set(Integer v) {
		delegate[localCounter++]=v;
	    }};
    }

    @Override
    public boolean remove(Object o) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public Integer remove(int index) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public Integer set(int index, Integer element) {
	int old = delegate[index];
	delegate[index]=element;
	return old;
    }

    @Override
    public int size() {
	final int len = delegate.length;
	if(representFullSize)
	    return len;
	else
	    return counter<len?counter:len;
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public Object[] toArray() {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
	throw new RuntimeException("Not implemented.");
    }
    
    public int getDelegateSize(){
	return delegate.length;
    }

    /**
     * @return the representFullSize
     */
    public boolean isRepresentFullSize() {
        return representFullSize;
    }

    /**
     * @param representFullSize the representFullSize to set
     */
    public IntArrayList setRepresentFullSize(boolean representFullSize) {
        this.representFullSize = representFullSize;
        return this;
    }
 
}
