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
import java.util.List;
import java.util.ListIterator;

public class DummyList<E> implements List<E> {
    private final String throwWhenReadingOrIterating;
    
    public DummyList(){
	this("Cannot read or iterate a DummyList.");
    }
    
    public DummyList(String throwWhenReadingOrIterating){
	this.throwWhenReadingOrIterating = throwWhenReadingOrIterating;
    }

    @Override
    public boolean add(E e) {
	return true;
    }

    @Override
    public void add(int index, E element) {
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
	return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
	return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean contains(Object o) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public E get(int index) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public int indexOf(Object o) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public boolean isEmpty() {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public Iterator<E> iterator() {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public int lastIndexOf(Object o) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public ListIterator<E> listIterator() {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public boolean remove(Object o) {
	return true;
    }

    @Override
    public E remove(int index) {
	return null;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	return true;
    }

    @Override
    public E set(int index, E element) {
	return null;
    }

    @Override
    public int size() {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
	return new DummyList<E>(throwWhenReadingOrIterating);
    }

    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

    @Override
    public <T> T[] toArray(T[] a) {
	throw new UnsupportedOperationException(throwWhenReadingOrIterating);
    }

}//end DummyList
