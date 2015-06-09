/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class VerboseExecutorService implements ExecutorService {
    private final ExecutorService delegate;
    public VerboseExecutorService(ExecutorService delegate){
	this.delegate=delegate;
    }
    /**
     * @param command
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
     */
    public void execute(Runnable command) {
	delegate.execute(new VerboseRunnable(command));
    }
    /**
     * 
     * @see java.util.concurrent.ExecutorService#shutdown()
     */
    public void shutdown() {
	delegate.shutdown();
    }
    /**
     * @return
     * @see java.util.concurrent.ExecutorService#shutdownNow()
     */
    public List<Runnable> shutdownNow() {
	return delegate.shutdownNow();
    }
    /**
     * @return
     * @see java.util.concurrent.ExecutorService#isShutdown()
     */
    public boolean isShutdown() {
	return delegate.isShutdown();
    }
    /**
     * @return
     * @see java.util.concurrent.ExecutorService#isTerminated()
     */
    public boolean isTerminated() {
	return delegate.isTerminated();
    }
    /**
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
	    throws InterruptedException {
	return delegate.awaitTermination(timeout, unit);
    }
    /**
     * @param task
     * @return
     * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
     */
    public <T> Future<T> submit(Callable<T> task) {
	return delegate.submit(task);
    }
    /**
     * @param task
     * @param result
     * @return
     * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
     */
    public <T> Future<T> submit(Runnable task, T result) {
	return delegate.submit(new VerboseRunnable(task), result);
    }
    /**
     * @param task
     * @return
     * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
     */
    public Future<?> submit(Runnable task) {
	return delegate.submit(new VerboseRunnable(task));
    }
    /**
     * @param tasks
     * @return
     * @throws InterruptedException
     * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection)
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
	    throws InterruptedException {
	return delegate.invokeAll(tasks);
    }
    /**
     * @param tasks
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)
     */
    public <T> List<Future<T>> invokeAll(
	    Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
	    throws InterruptedException {
	return delegate.invokeAll(tasks, timeout, unit);
    }
    /**
     * @param tasks
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection)
     */
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
	    throws InterruptedException, ExecutionException {
	return delegate.invokeAny(tasks);
    }
    /**
     * @param tasks
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)
     */
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
	    long timeout, TimeUnit unit) throws InterruptedException,
	    ExecutionException, TimeoutException {
	return delegate.invokeAny(tasks, timeout, unit);
    }
    
    private static final class VerboseRunnable implements Runnable{
	private final Runnable delegate;
	
	public VerboseRunnable(Runnable delegate){
	    this.delegate=delegate;
	}

	@Override
	public void run() {
	    try{delegate.run();}catch(Exception e){e.printStackTrace();}
	}
    }//end VerboseRunnable
    
    private static final class VerboseCallable<T> implements Callable<T>{
	private final Callable<T> delegate;
	
	public VerboseCallable(Callable<T> delegate){
	    this.delegate=delegate;
	}

	@Override
	public T call() throws Exception {
	    try{return delegate.call();}catch(Exception e){e.printStackTrace();}
	    return null;
	}
    }//end VerboseRunnable

}//end VerboseExecutorService
