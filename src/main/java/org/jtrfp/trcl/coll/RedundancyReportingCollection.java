/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;

public class RedundancyReportingCollection<E> implements Collection<E> {
    private Set<E> delegate = new HashSet<E>();
    private Submitter<E> redundancyHandler;


    protected Submitter<E> getRedundancyHandler() {
	if(redundancyHandler == null)
	    setRedundancyHandler(new PrintingRedundancyHandler<E>());
        return redundancyHandler;
    }

    protected void setRedundancyHandler(Submitter<E> redundancyHandler) {
        this.redundancyHandler = redundancyHandler;
    }
    
    private void redundancy(E item){
	getRedundancyHandler().submit(item);
    }
    
    class PrintingRedundancyHandler<E> extends AbstractSubmitter<E>{
	@Override
	public void submit(E item) {
	    new RuntimeException("Redundancy found: "+item).printStackTrace();
	}
    }//end PrintingRedundancyHandler

    public boolean add(E e) {
	if(!delegate.add(e))
	    redundancy(e);
	return true;
    }

    public boolean addAll(Collection<? extends E> c) {
	if(!delegate.addAll(c)){
	    for(E element:c)
	     redundancy(element);}
	return true;
    }

    public void clear() {
	delegate.clear();
    }

    public boolean contains(Object o) {
	return delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
	return delegate.containsAll(c);
    }

    public boolean equals(Object o) {
	return delegate.equals(o);
    }

    public int hashCode() {
	return delegate.hashCode();
    }

    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    public Iterator<E> iterator() {
	return delegate.iterator();
    }

    public boolean remove(Object o) {
	return delegate.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
	return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
	return delegate.retainAll(c);
    }

    public int size() {
	return delegate.size();
    }

    public Object[] toArray() {
	return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
	return delegate.toArray(a);
    }

}//end RedundancyReportingCollection
