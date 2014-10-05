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

import java.util.concurrent.Callable;

public class LazyTRFuture<V> implements TRFuture<V> {
    private final Callable<V> callable;
    private final TR tr;
    private volatile TRFutureTask<V> result;

    public LazyTRFuture(TR tr, Callable<V> callable) {
	this.callable=callable;
	this.tr=tr;
    }//constructor

    @Override
    public synchronized V get(){
	if(result==null)
	    ensureGet();
	return result.get();
    }//end get()
    
    @Override
    public synchronized V getRealtime() throws NotReadyException{
	ensureGet();
	if(!result.isDone())
	    throw new NotReadyException();
	return result.get();
    }//end getRealtime()
    
    private void ensureGet(){//Decoupled from execution thread.
	if(result!=null)return;
	result=(tr.getThreadManager().submitToThreadPool(new Callable<V>(){
		@Override
		public V call() throws Exception {
		    V r = callable.call();
		    return r;
		}}));
    }//end performGet()
    
    /**
     * Invalidate the current value (if any) for re-execuation of the callable on next get*()
     * 
     * @since Oct 5, 2014
     */
    public synchronized void reset() {
	result=null;
    }//end reset()

}//end LazyTRFutureTask
