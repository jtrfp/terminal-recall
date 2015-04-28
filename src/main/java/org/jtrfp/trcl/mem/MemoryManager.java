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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GL3;

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
    private final BlockingQueue<WeakReference<PagedByteBuffer>>		
    						newPagedByteBuffers             = new ArrayBlockingQueue<WeakReference<PagedByteBuffer>>(1024),
    						newPagedByteBuffersOverflow     = new LinkedBlockingQueue<WeakReference<PagedByteBuffer>>(1024),
    						deletedPagedByteBuffers         = new ArrayBlockingQueue<WeakReference<PagedByteBuffer>>(1024),
    						deletedPagedByteBuffersOverflow = new LinkedBlockingQueue<WeakReference<PagedByteBuffer>>(1024);
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
		tb.setUsageHint(MemoryUsageHint.DymamicDraw);
		return tb;
	    }}).get();
	}catch(Exception e){throw new RuntimeException(e);}
	
	pageIndexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(final int previousMaxCapacity) {
		final int newMaxCapacity = previousMaxCapacity!=0?previousMaxCapacity*2:1;
		final TRFuture<Integer> ft = MemoryManager.this.gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
		    @Override
		    public Integer call(){
			glPhysicalMemory.reallocate(newMaxCapacity*PagedByteBuffer.PAGE_SIZE_BYTES);
			physicalMemory[0] = glPhysicalMemory.map();
			return newMaxCapacity;
		    }//end call()
		});
		try{return ft.get();}catch(Exception e){e.printStackTrace();}
		return previousMaxCapacity;//Fail by maintaining original size
	    }//end grow(...)
	    @Override
	    public int shrink(final int minDesiredMaxCapacity){
		final int currentMaxCapacity = pageIndexPool.getMaxCapacity();
		final int proposedMaxCapacity = currentMaxCapacity/2;//TODO: This adjusts by a single power of 2, take arbitrary power of 2 instead
		if(proposedMaxCapacity >= minDesiredMaxCapacity){
		    final TRFuture<Integer> ft = MemoryManager.this.gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
			@Override
			public Integer call(){
			    glPhysicalMemory.reallocate(proposedMaxCapacity*PagedByteBuffer.PAGE_SIZE_BYTES);
			    physicalMemory[0] = glPhysicalMemory.map();
			    return proposedMaxCapacity;
			}//end call()
		    });
		    try{return ft.get();}catch(Exception e){e.printStackTrace();return currentMaxCapacity;}
		}else return currentMaxCapacity;
	    }//end shrink
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
	try{newPagedByteBuffers.add(b);}
	catch(IllegalStateException e){
	    newPagedByteBuffersOverflow.offer(b);
	}
    }
    
    void deRegisterPagedByteBuffer(WeakReference<PagedByteBuffer> b){
	try{deletedPagedByteBuffers.add(b);}
	catch(IllegalStateException e){
	    deletedPagedByteBuffersOverflow.offer(b);
	}
    }
    
    private final ArrayList<WeakReference<PagedByteBuffer>> toRemove = new ArrayList<WeakReference<PagedByteBuffer>>();
    
    public void flushStalePages(){
	if(!glPhysicalMemory.isMapped())
	    return;
	
	deletedPagedByteBuffers.drainTo(toRemove);
	deletedPagedByteBuffersOverflow.drainTo(toRemove);
	
	pagedByteBuffers.removeAll(toRemove);
	toRemove.clear();
	
	newPagedByteBuffers.drainTo(pagedByteBuffers);
	newPagedByteBuffersOverflow.drainTo(pagedByteBuffers);
	
	final Iterator<WeakReference<PagedByteBuffer>> it = pagedByteBuffers.iterator();
	while(it.hasNext()){
	    final WeakReference<PagedByteBuffer> r = it.next();
	    if(r.get()==null)
		it.remove();
	    else{
		r.get().flushStalePages();
	    }//end else{}
	}//end while(hasNext)
    }//end flushStalePages()
    
    public void bindToUniform(int textureUnit, GLProgram shaderProgram, GLUniform uniform) {
	glPhysicalMemory.bindToUniform(textureUnit, shaderProgram, uniform);
    }

    public void dumpAllGPUMemTo(final ByteBuffer dest) throws IOException{
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		final GL3 gl = gpu.getGl();
		unmap();
		glPhysicalMemory.bind();
		ByteBuffer bb = gl.glMapBuffer(GL3.GL_TEXTURE_BUFFER, GL3.GL_READ_ONLY);
		bb.clear();
		dest.clear();
		bb.limit(dest.limit());//Avoid overflow
		dest.put(bb);
		gl.glUnmapBuffer(GL3.GL_TEXTURE_BUFFER);
		map();
		glPhysicalMemory.unbind();
		return null;
	    }}).get();
	dest.clear();
    }//end dumpAllGPUMemTo(...)
}//end MemmoryManager
