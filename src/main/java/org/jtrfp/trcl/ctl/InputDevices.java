/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.ctl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Collects all InputDevices and presents them
 * as a single unmodifiable Collection.
 * @author Chuck Ritola
 *
 */

@Component
public class InputDevices implements Collection<InputDevice> {
    private Collection<InputDevice> delegate = new ArrayList<InputDevice>();

    @Autowired
    public InputDevices(Collection<InputDeviceService> services){
	for(InputDeviceService srv:services){
	    delegate.addAll(srv.getInputDevices());}
    }//end constructor

    public int size() {
	return delegate.size();
    }

    public boolean isEmpty() {
	return delegate.isEmpty();
    }

    public boolean contains(Object o) {
	return delegate.contains(o);
    }

    public Iterator<InputDevice> iterator() {
	return IteratorUtils.unmodifiableIterator(delegate.iterator());
    }

    public Object[] toArray() {
	return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
	return delegate.toArray(a);
    }

    public boolean add(InputDevice e) {
	throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
	throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
	return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends InputDevice> c) {
	throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    public void clear() {
	throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
	return delegate.equals(o);
    }

    public int hashCode() {
	return delegate.hashCode();
    }
}//end InputDevices
