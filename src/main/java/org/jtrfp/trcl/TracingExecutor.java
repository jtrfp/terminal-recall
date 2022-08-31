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

package org.jtrfp.trcl;

import java.util.concurrent.Executor;

import org.jtrfp.trcl.coll.Decorator;

public class TracingExecutor implements Executor, Decorator<Executor> {
    private final Executor delegate;
    
    public TracingExecutor(Executor delegate){
	this.delegate=delegate;
    }

    @Override
    public Executor getDelegate() {
	return delegate;
    }

    @Override
    public void execute(Runnable command) {
	delegate.execute(new TracingRunnable(command,new PreemptiveException()));
    }
    
    private class PreemptiveException extends RuntimeException {
	private static final long serialVersionUID = -8014686643494726126L;}
    
    private class TracingRunnable implements Runnable, Decorator<Runnable> {
	private final Runnable delegate;
	private final Throwable causingTracer;
	
	public TracingRunnable(Runnable delegate, Throwable causingTracer){
	    this.delegate=delegate;
	    this.causingTracer=causingTracer;
	}

	@Override
	public void run() {
	    try{
		delegate.run();
	    }catch(Throwable t){
		t.printStackTrace();
		System.err.println("Submitted by the following trace:");
		causingTracer.printStackTrace();
	    }
	}

	@Override
	public Runnable getDelegate() {
	    return delegate;
	}
	
    }//end TracingRunnable
}//end TracingExecutor
