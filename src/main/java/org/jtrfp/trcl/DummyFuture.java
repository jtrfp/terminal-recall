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
package org.jtrfp.trcl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DummyFuture<T> implements Future<T> {
    private final T toWrap;
    public DummyFuture(T toWrap){
	this.toWrap=toWrap;}
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
	return false;
    }
    @Override
    public T get() throws InterruptedException, ExecutionException {
	return toWrap;
    }
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException,
	    ExecutionException, TimeoutException {
	return toWrap;
    }
    @Override
    public boolean isCancelled() {
	return false;
    }
    @Override
    public boolean isDone() {
	return true;
    }
}//end DummyFuture
