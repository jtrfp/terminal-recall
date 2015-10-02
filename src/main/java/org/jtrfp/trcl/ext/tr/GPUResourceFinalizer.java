/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ext.tr;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.ext.Extension;
import org.jtrfp.trcl.gpu.GPU;

public class GPUResourceFinalizer implements Extension<GPU> {
    private ExecutorService      finalizationExecutor;
    private LinkedBlockingQueue<Future<Void>>    finalizationFutures = new LinkedBlockingQueue<Future<Void>>();
    
    private final Thread finalizationFutureCheckerThread = new Thread(){
	@Override
	public void run(){
	    try{
		while(true)
		    {finalizationFutures.take().get();}
	    }//end try{}
	    catch(ExecutionException e){e.printStackTrace();}
	    catch(InterruptedException e){}
	}//end run()
    };

    @Override
    public void init(GPU extended) {
    }

    @Override
    public void apply(GPU extended) {
	finalizationFutureCheckerThread.start();
	finalizationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*3, new GPURFThreadFactory());
    }//end apply(...)

    @Override
    public void remove(GPU extended) {
	finalizationExecutor = null;
	finalizationFutureCheckerThread.interrupt();//UNTESTED
    }

    @Override
    public Class<GPU> getExtendedClass() {
	return GPU.class;
    }

    @Override
    public String getHumanReadableName() {
	return "GPU Resource Finalizer.";
    }

    @Override
    public String getDescription() {
	return "Handles the release of GPU resources while decoupled from the GC's finalizer thread.";
    }
    
    public void submitFinalizationAction(Callable<Void> c){
	finalizationFutures.add(finalizationExecutor.submit(c));
    }//end submitFinalizationAction(...)
    
    private final AtomicInteger threadID = new AtomicInteger();
    
    private final class GPURFThreadFactory implements ThreadFactory{
	@Override
	public Thread newThread(final Runnable r) {
	    final Thread result = new Thread(null,r,"GPUResourceFinalizer-"+threadID.getAndIncrement(),0);
	    return result;
	}
    }//end GPURFThreadFactory

}//end GPUResourceFinalizer
