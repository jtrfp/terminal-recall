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

public class CollectionActionPrinter<E> implements Collection<E>, Decorator<Collection<E>> {
    private final String        prefix;
    private final Collection<E> delegate;
    private final boolean       stackTrace;
    
    public CollectionActionPrinter(String prefix, Collection<E> delegate, boolean stackTrace){
	this.prefix=prefix;
	this.delegate=delegate;
	this.stackTrace=stackTrace;
    }
    
    public CollectionActionPrinter(String prefix, Collection<E> delegate){
	this(prefix, delegate, false);
    }
    
    private void proposeStackPrint(){
	if(stackTrace)
	    new Exception().printStackTrace();
    }

    @Override
    public boolean add(E e) {
	System.out.println(prefix+" add "+e);
	proposeStackPrint();
	return delegate.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
	System.out.println(prefix+" addAll "+c);
	proposeStackPrint();
	return delegate.addAll(c);
    }

    @Override
    public void clear() {
	System.out.println(prefix+" clear() ");
	proposeStackPrint();
	delegate.clear();
    }

    @Override
    public boolean contains(Object o) {
	return delegate.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	return delegate.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
	return delegate.iterator();
    }

    @Override
    public boolean remove(Object o) {
	System.out.println(prefix+" remove "+o);
	proposeStackPrint();
	return delegate.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	System.out.println(prefix+" removeAll "+c);
	proposeStackPrint();
	return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	System.out.println(prefix+" retainAll "+c);
	proposeStackPrint();
	return delegate.retainAll(c);
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
    public Collection<E> getDelegate() {
	return delegate;
    }

}//end CollectionActionPrinter
