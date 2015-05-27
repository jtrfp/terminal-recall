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

public class OriginTrackingTRFutureTask<V> extends TRFutureTask<V> {
    private final StackTraceElement [] stackTraceElements;
    public OriginTrackingTRFutureTask(TR tr, Callable<V> callable) {
	super(callable);
	stackTraceElements = new Exception().getStackTrace();
    }
    
    public StackTraceElement [] getCreationStackTraceElements(){
	return stackTraceElements;
    }
    
    @Override
    public void run(){
	System.out.println("OriginTrackingFutureTask.run invoked. Origin trace below:");
	for(StackTraceElement ste:stackTraceElements){
	    System.out.println("\tat "+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+ste.getLineNumber()+")");
	}
	super.run();
    }

}//end OriginTrackingTRFutureTask
