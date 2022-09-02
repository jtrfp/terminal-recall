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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;


public class DummyTRFutureTask<T> extends TRFutureTask<T> {
    private final T valueToReturn;
    private static final Callable<?> CALLABLE = new Callable<>(){
	@Override
	public Object call() throws Exception {
	    return null;
	}
    };
    @SuppressWarnings("unchecked")
    public DummyTRFutureTask(T valueToReturn) {
	super((Callable<T>)CALLABLE);
	this.valueToReturn=valueToReturn;
    }
    @SuppressWarnings("unchecked")
    public DummyTRFutureTask(T valueToReturn, UncaughtExceptionHandler handler) {
	super((Callable<T>)CALLABLE,handler);
	this.valueToReturn=valueToReturn;
    }

    @Override
    public T get(){
	return valueToReturn;
    }
    
    @Override
    public boolean isDone(){
	return true;
    }
}//end DummyTRFutureTask
