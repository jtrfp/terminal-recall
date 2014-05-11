package org.jtrfp.trcl.core;

import java.util.concurrent.Callable;

public class OriginTrackingTRFutureTask<V> extends TRFutureTask<V> {
    private final StackTraceElement [] stackTraceElements;
    public OriginTrackingTRFutureTask(TR tr, Callable<V> callable) {
	super(tr, callable);
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
