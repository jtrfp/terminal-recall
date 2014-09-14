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

package org.jtrfp.trcl.mem;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

import org.jtrfp.trcl.gpu.GPU;

public class GPUDomainGarbageCollector {
    private final GPU gpu;
    private final HashSet<HashTransparentWeakReference<GPUResource>> registry 
    	= new HashSet<HashTransparentWeakReference<GPUResource>>();
    private final HashSet<HashTransparentWeakReference<GPUResource>> accounting 
	= new HashSet<HashTransparentWeakReference<GPUResource>>();
    
    public GPUDomainGarbageCollector(GPU gpu){
	this.gpu=gpu;
    }
    
    public synchronized void gc(){
	resetObjectAccounting();
	cleanRegistry();
	makeAccountOfObjects();
	prefinalizeUnaccountedResources();
    }//end gc()
    
    private void makeAccountOfObjects(){
	System.out.println("GPU GC: Making account of objects...");//TODO: debug printout
	Iterator<Object> itr = new ReferenceTraversalIterator(gpu.getTr().getGame());
	while(itr.hasNext()){
	   final Object obj = itr.next();
	   if(obj instanceof GPUResource)
	       makeAccountOfObject((GPUResource)obj);
	}//end (while(itr.hasNext()))
    }//end makeAccountOfObjects()
    
    private void prefinalizeUnaccountedResources(){
	System.out.println("GPU GC: Prefinalizing unaccounted resources...");//TODO: debug printout
	final Iterator<HashTransparentWeakReference<GPUResource>> 
		wRcItr = accounting.iterator();
	while(wRcItr.hasNext()){
	    final HashTransparentWeakReference<GPUResource> wRc = wRcItr.next();
	    final GPUResource rc = wRc.get();
	    registry.remove(wRc);
	    if(rc!=null)
		rc.prefinalize();
	}//end while(hasNext)
    }//end prefinalizeUnaccountedResources()
    
    private void makeAccountOfObject(GPUResource obj){
	accounting.remove(obj);
    }//end makeAccountOfObject(...)
    
    /**
     * Remove stale objects already picked up by java's GC.
     * 
     * @since Sep 14, 2014
     */
    private void cleanRegistry(){
	System.out.println("GPU GC: Cleaning registry...");//TODO: debug printout
	Iterator<HashTransparentWeakReference<GPUResource>> itr = registry.iterator();
	while(itr.hasNext()){
	    final WeakReference<GPUResource> obj = itr.next();
	    if(obj.get()==null)itr.remove();
	}
    }//end cleanRegistry()
    
    private void resetObjectAccounting(){
	System.out.println("GPU GC: Resetting object accounting...");//TODO: debug printout
	accounting.clear();
	accounting.addAll(registry);
    }//end resetObjectAccounting()
    
    public synchronized void registerGPUResource(GPUResource rc){
	registry.add(new HashTransparentWeakReference<GPUResource>(rc));
    }//end registerGPUResource(...)
}//end GPUDomainGarbageCollector
