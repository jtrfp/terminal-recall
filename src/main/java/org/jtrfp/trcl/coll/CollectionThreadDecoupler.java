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
import java.util.concurrent.Executor;

import org.jtrfp.trcl.tools.Util;

public class CollectionThreadDecoupler<E> implements Collection<E>, Repopulatable<E>, Decorator<Collection<E>> {
    private final Executor 	executor;
    private final Collection<E>	delegate;
    
    public CollectionThreadDecoupler(Collection<E> delegate, Executor executor){
	this.executor=executor;
	this.delegate=delegate;
    }

    @Override
    public boolean add(final E element) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.add(element);
	    }});
	return true;
    }//end add(E)

    @Override
    public boolean addAll(final Collection<? extends E> elements) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.addAll(elements);
	    }});
	return true;
    }

    @Override
    public void clear() {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.clear();
	    }});
    }

    @Override
    @Deprecated
    public boolean contains(Object arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean containsAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean isEmpty() {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Iterator<E> iterator() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object element) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.remove(element);
	    }});
	return true;
    }

    @Override
    public boolean removeAll(final Collection<?> elements) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.removeAll(elements);
	    }});
	return true;
    }

    @Override
    public boolean retainAll(final Collection<?> elements) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		delegate.retainAll(elements);
	    }});
	return true;
    }

    @Override
    @Deprecated
    public int size() {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Object[] toArray() {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public <T> T[] toArray(T[] arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void repopulate(final Collection<E> c) {
	executor.execute(new Runnable(){
	    @Override
	    public void run() {
		Util.repopulate(delegate,c);
	    }});
    }
    
    public Collection<E> getDelegate(){
	return delegate;
    }
}//end CollectionActionSource
