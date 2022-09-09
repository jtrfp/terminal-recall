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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.jtrfp.trcl.TracingExecutor;
import org.jtrfp.trcl.tools.Util;

public class CollectionThreadDecoupler<E> implements Collection<E>, Repopulatable<E>, Decorator<Collection<E>>, BulkRemovable<E> {
    private final Executor 	executor;
    private final Collection<E>	delegate;
    private static final boolean DEBUG = true;
    
    public CollectionThreadDecoupler(Collection<E> delegate, Executor executor){
	this.executor=DEBUG?new TracingExecutor(executor):executor;
	this.delegate=delegate;
    }

    @Override
    public synchronized boolean add(final E element) {
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.add(element);
	    }});
	return true;
    }//end add(E)

    @Override
    public synchronized boolean addAll(final Collection<? extends E> elements) {
	@SuppressWarnings("unchecked")
	final List<E> toAdd = (List<E>)Arrays.asList(elements.toArray());
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.addAll(toAdd);
	    }});
	return true;
    }

    @Override
    public synchronized void clear() {
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.clear();
	    }});
    }

    @Override
    @Deprecated
    public synchronized boolean contains(Object arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public synchronized boolean containsAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public synchronized boolean isEmpty() {
	throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public synchronized Iterator<E> iterator() {
	throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean remove(final Object element) {
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.remove(element);
	    }});
	return true;
    }

    @Override
    public synchronized boolean removeAll(final Collection<?> elements) {
	final List<?> toRemove = (List<?>)Arrays.asList(elements.toArray());
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.removeAll(toRemove);
	    }});
	return true;
    }

    @Override
    public synchronized boolean retainAll(final Collection<?> elements) {
	final List<?> toRetain = (List<?>)Arrays.asList(elements.toArray());
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		delegate.retainAll(toRetain);
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
    public synchronized void repopulate(final Collection<E> c) {
	@SuppressWarnings("unchecked")
	final List<E> toRepopulate = (List<E>)Arrays.asList(c.toArray());
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		Util.repopulate(delegate,toRepopulate);
	    }});
    }
    
    public Collection<E> getDelegate(){
	return delegate;
    }

    @Override
    public synchronized void bulkRemove(final Collection<E> it) {
	@SuppressWarnings("unchecked")
	final Collection<E> items = (Collection<E>)Arrays.asList(it.toArray());
	executor.execute(new Runnable(){
	    @Override
	    public synchronized void run() {
		Util.bulkRemove(items, delegate);
	    }});
    }
}//end CollectionActionSource
