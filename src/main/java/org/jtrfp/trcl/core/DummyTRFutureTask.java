package org.jtrfp.trcl.core;


public class DummyTRFutureTask<T> extends TRFutureTask<T> {
    private final T valueToReturn;
    public DummyTRFutureTask(T valueToReturn) {
	super(null,null);
	this.valueToReturn=valueToReturn;
    }

    @Override
    public T get(){
	return valueToReturn;
    }
}//end DummyTRFutureTask
