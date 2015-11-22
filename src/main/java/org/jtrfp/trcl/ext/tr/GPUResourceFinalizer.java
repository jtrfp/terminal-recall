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

import org.jtrfp.trcl.gpu.GPU;

public class GPUResourceFinalizer {
    private ExecutorService      finalizationExecutor;
    private LinkedBlockingQueue<Future<Void>>    finalizationFutures = new LinkedBlockingQueue<Future<Void>>();
    private GPU gpu;
    private int rootBufferCompactionInterval = 1024;
    
    public GPUResourceFinalizer(GPU gpu){
	apply(gpu);
    }
    
    private final Thread finalizationFutureCheckerThread = new Thread(){
	private int finalizationCount = 0;
	@Override
	public void run(){
	    try{while(true){
		    finalizationFutures.take().get();
		    //Every once in a while, compact he Root Buffer to keep things tight.
		    if(finalizationCount++%getRootBufferCompactionInterval() == 0)
			gpu.compactRootBuffer();
		    }
	    }//end try{}
	    catch(ExecutionException e){e.printStackTrace();}
	    catch(InterruptedException e){}
	}//end run()
    };

    public void apply(GPU extended) {
	finalizationFutureCheckerThread.start();
	finalizationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*3, new GPURFThreadFactory());
	gpu = extended;
    }//end apply(...)

    public void remove(GPU extended) {
	finalizationExecutor = null;
	finalizationFutureCheckerThread.interrupt();//UNTESTED
	gpu = null;
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

    /**
     * @return the rootBufferCompactionInterval
     */
    public int getRootBufferCompactionInterval() {
        return rootBufferCompactionInterval;
    }

    /**
     * @param rootBufferCompactionInterval the rootBufferCompactionInterval to set
     */
    public void setRootBufferCompactionInterval(int rootBufferCompactionInterval) {
        this.rootBufferCompactionInterval = rootBufferCompactionInterval;
    }

}//end GPUResourceFinalizer
