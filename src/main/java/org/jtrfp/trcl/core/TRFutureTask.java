package org.jtrfp.trcl.core;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;

public class TRFutureTask<V> extends FutureTask<V> implements TRFuture<V>{
    protected final TR tr;

    public TRFutureTask(TR tr, Callable<V> callable) {
	super(callable);
	this.tr=tr;
    }
    public TRFutureTask(TR tr, Runnable runnable, V result) {
	super(runnable,result);
	this.tr=tr;
    }
    
    @Override
    public void run(){
	super.run();
	Thread.currentThread().setName("Finished TRFutureTask");
    }

    @Override
    public V get() {
	try {
	    return super.get();//Get or block and then get.
	}
	catch(Exception e){tr.showStopper(e);}
	return null;
    }
    
}//end TRFutureTask
