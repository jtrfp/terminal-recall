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
