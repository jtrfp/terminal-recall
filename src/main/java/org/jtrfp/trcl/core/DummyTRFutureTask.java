package org.jtrfp.trcl.core;

import java.util.concurrent.Callable;


public class DummyTRFutureTask<T> extends TRFutureTask<T> {
    private final T valueToReturn;
    private static final Callable CALLABLE = new Callable(){
	@Override
	public Object call() throws Exception {
	    return null;
	}
    };
    public DummyTRFutureTask(T valueToReturn) {
	super(null,CALLABLE);
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
