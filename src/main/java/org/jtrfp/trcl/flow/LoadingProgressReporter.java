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

package org.jtrfp.trcl.flow;

import java.lang.ref.WeakReference;

public interface LoadingProgressReporter {
    public LoadingProgressReporter [] 	generateSubReporters(int numSubReporters);
    public void 			complete();
    
    public static class Impl implements StemReporter{
	private final		WeakReference<StemReporter> parent;
	private int 		totalSubReporters=1;
	private int 		reportCount	 =0;
	private boolean 	complete	 =false;
	private UpdateHandler	updateHandler;
	
	public static StemReporter createRoot(UpdateHandler updateHandler){
	    return new Impl(null).setUpdateHandler(updateHandler);
	}
	
	Impl(WeakReference<StemReporter> parent){
	    this.parent=parent;
	}//end Impl()
	
	@Override
	public LoadingProgressReporter[] generateSubReporters(
		final int numSubReporters) {
	    final LoadingProgressReporter [] result = new LoadingProgressReporter[numSubReporters];
	    for(int i=0; i<numSubReporters; i++){
		result[i]=new Impl(new WeakReference<StemReporter>(this));
	    }//end for(reporters)
	    totalSubReporters+=numSubReporters;
	    if(parent!=null){
		final StemReporter r = parent.get();
		if(r!=null)r.addSubReporters(numSubReporters);
	    }
	    return result;
	}//end constructor

	@Override
	public void complete() {
	    if(!complete){
		complete=true;
		final int remainingReportCounts = totalSubReporters - reportCount;
		for(int i=0; i<remainingReportCounts; i++)
		    increment();
	    }//end if(!complete)
	}//end complete()

	@Override
	public void increment() {
	    if(reportCount<totalSubReporters){
		reportCount++;
	    	if(parent!=null){
	    	    final StemReporter r = parent.get();
	    	    if(r!=null)	r.increment();
	    	}//end if(!null)
	    	if(updateHandler!=null)
	    	    updateHandler.update((double)reportCount/(double)totalSubReporters);
	    }//end if(in range)
	}//end increment()

	@Override
	public void addSubReporters(int delta) {
	    totalSubReporters+=delta;
	}//end addSubReporteres(...)

	@Override
	public StemReporter setUpdateHandler(UpdateHandler updateHandler) {
	    if(updateHandler==null)return this;
	    this.updateHandler=updateHandler;
	    updateHandler.update(0);
	    return this;
	}
    }//end Impl
    
    public static interface UpdateHandler{
	public void update(double unitProgress);
    }//end UpdateHandler
    
    static interface StemReporter extends LoadingProgressReporter{
	public void increment();
	public void addSubReporters(int delta);
	public StemReporter setUpdateHandler(UpdateHandler updateHandler);
    }//end StemReporter
}//end LoadingProgrsesReporter
