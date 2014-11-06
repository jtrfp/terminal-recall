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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.MemoryUsageHint;
import org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;

public final class MemoryManager {
    private final IndexPool 			pageIndexPool 	= new IndexPool();
    private final ByteBuffer [] 		physicalMemory 	= new ByteBuffer[1];
    private final ReallocatableGLTextureBuffer 	glPhysicalMemory;
    private final GPU				gpu;
    private final ArrayList<WeakReference<PagedByteBuffer>>	
    						pagedByteBuffers = new ArrayList<WeakReference<PagedByteBuffer>>(1024);
    /**
     * 16MB of zeroes. Don't forget to sync to avoid co-modification of the position.
     */
    public static final ByteBuffer		ZEROES = ByteBuffer.allocate(1024*1024*16);
    
    public MemoryManager(GPU gpu){
	this.gpu=gpu;
	try{
	glPhysicalMemory = gpu.getTr().getThreadManager().submitToGL(new Callable<ReallocatableGLTextureBuffer>(){
	    @Override
	    public ReallocatableGLTextureBuffer call() throws Exception {
		ReallocatableGLTextureBuffer tb;
		tb=new ReallocatableGLTextureBuffer(MemoryManager.this.gpu);
		tb.reallocate(PagedByteBuffer.PAGE_SIZE_BYTES);
		physicalMemory[0] = tb.map();
		tb.setUsageHint(MemoryUsageHint.StreamDraw);
		return tb;
	    }}).get();
	}catch(Exception e){throw new RuntimeException(e);}
	
	pageIndexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(final int previousMaxCapacity) {
		final TRFuture<Integer> ft = MemoryManager.this.gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
		    @Override
		    public Integer call(){
			glPhysicalMemory.reallocate(previousMaxCapacity*PagedByteBuffer.PAGE_SIZE_BYTES*2);
			physicalMemory[0] = glPhysicalMemory.map();
			return previousMaxCapacity*2;
		    }//end call()
		});
		try{return ft.get();}catch(Exception e){e.printStackTrace();}
		return previousMaxCapacity;//Fail by maintaining original size
	    }//end grow(...)
	});
    }//end constructor
    
    public int getMaxCapacityInBytes(){
	return PagedByteBuffer.PAGE_SIZE_BYTES*pageIndexPool.getMaxCapacity();
    }
    public void map(){
	if((physicalMemory[0] = glPhysicalMemory.map())==null)throw new NullPointerException("Failed to map GPU memory. (returned null)");
    }
    public void flushRange(int startPositionInBytes, int lengthInBytes){
	glPhysicalMemory.flushRange(startPositionInBytes, lengthInBytes);
    }
    public void unmap(){
	physicalMemory[0]=null;//Make sure we don't start reading/writing somewhere bad.
	glPhysicalMemory.unmap();
	}
    public PagedByteBuffer createPagedByteBuffer(int initialSizeInBytes, String debugName){
	return new PagedByteBuffer(gpu, physicalMemory, pageIndexPool, initialSizeInBytes, debugName);
    }
    
    void registerPagedByteBuffer(WeakReference<PagedByteBuffer> b){
	synchronized(pagedByteBuffers){
	pagedByteBuffers.add(b);}
    }
    
    void deRegisterPagedByteBuffer(WeakReference<PagedByteBuffer> b){
	synchronized(pagedByteBuffers){
	pagedByteBuffers.remove(b);}
    }
    
    public void flushStalePages(){
	if(!glPhysicalMemory.isMapped())
	    return;
	synchronized(pagedByteBuffers){
	final Iterator<WeakReference<PagedByteBuffer>> it = pagedByteBuffers.iterator();
	while(it.hasNext()){
	    final WeakReference<PagedByteBuffer> r = it.next();
	    if(r.get()==null)
		it.remove();
	    else{
		r.get().flushStalePages();
	    }//end else{}
	}//end while(hasNext)
	}//end sync()
    }//end flushStalePages()
    
    public void bindToUniform(int textureUnit, GLProgram shaderProgram, GLUniform uniform) {
	glPhysicalMemory.bindToUniform(textureUnit, shaderProgram, uniform);
    }

    public void dumpAllGPUMemTo(ByteBuffer dest) throws IOException{
	map();
	physicalMemory[0].clear();
	dest.clear();
	physicalMemory[0].limit(dest.limit());//Avoid overflow
	dest.put(physicalMemory[0]);
	physicalMemory[0].clear();
	dest.clear();
    }
}//end MemmoryManager
