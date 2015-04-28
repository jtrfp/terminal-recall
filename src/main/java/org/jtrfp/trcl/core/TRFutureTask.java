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
import java.util.concurrent.FutureTask;

public class TRFutureTask<V> extends FutureTask<V> implements TRFuture<V>{
    protected final TR tr;
    private boolean handleException=true;

    public TRFutureTask(TR tr, Callable<V> callable) {
	super(callable);
	this.tr=tr;
    }//end constructor
    public TRFutureTask(TR tr, Runnable runnable, V result) {
	super(runnable,result);
	this.tr=tr;
    }//end constructor
    
    @Override
    public void run(){
	try{super.run();super.get();}
	catch(Exception e)
	 {if(handleException)tr.showStopper(e.getCause());
	    else throw new RuntimeException(e.getCause());}//Re=wrap
    }//end run()
    
    @Override
    public V getRealtime() throws NotReadyException{
	if(!this.isDone())
	    throw new NotReadyException();
	return get();
    }//end getRealtime()

    @Override
    public V get() {
	try {
	    return super.get();//Get or block and then get.
	}
	catch(InterruptedException e){}
	catch(Exception e){
	    if(handleException)tr.showStopper(e);
	    else throw new RuntimeException(e);}
	return null;
    }//end get()
    public void setHandleException(boolean handleException) {
	this.handleException=handleException;
    }
    
}//end TRFutureTask
