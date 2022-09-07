/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.jtrfp.trcl.gpu.GLExecutor;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.MemoryUsageHint;
import org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;

import com.jogamp.opengl.GL3;

public final class MemoryManager {
    private final IndexPool 			pageIndexPool 	= new IndexPool();
    private final ByteBuffer [] 		physicalMemory 	= new ByteBuffer[1];
    private final ReallocatableGLTextureBuffer 	glPhysicalMemory;
    private final GPU				gpu;
    //WARNING: Don't forget to lock these before access!
    private final Collection<WeakReference<PagedByteBuffer>>
                                                newPagedByteBuffers     = new ArrayList<WeakReference<PagedByteBuffer>>(1024),
                                                deletedPagedByteBuffers = new ArrayList<WeakReference<PagedByteBuffer>>(1024);
    private final ArrayList<WeakReference<PagedByteBuffer>>	
    						pagedByteBuffers = new ArrayList<WeakReference<PagedByteBuffer>>(1024);
    private final GLExecutor<GL3>               glExecutor;
    /**
     * 16MB of zeroes. Don't forget to sync to avoid co-modification of the position.
     */
    public static final ByteBuffer		ZEROES = ByteBuffer.allocate(1024*1024*16);
    
    public MemoryManager(GPU gpu, final GLExecutor<GL3> glExecutor){
	this.gpu=gpu;
	this.glExecutor=glExecutor;
	try{
	glPhysicalMemory = glExecutor.submitToGL(new GLExecutable<ReallocatableGLTextureBuffer,GL3>(){
	    @Override
	    public ReallocatableGLTextureBuffer execute(GL3 gl) throws Exception {
		ReallocatableGLTextureBuffer tb;
		//TODO: Set reporter in thread agnostic manner
		tb=new ReallocatableGLTextureBuffer(MemoryManager.this.gpu);
		tb.reallocate(PagedByteBuffer.PAGE_SIZE_BYTES);
		physicalMemory[0] = tb.map();
		tb.setUsageHint(MemoryUsageHint.DymamicDraw);
		return tb;
	    }}).get();
	}catch(Exception e){throw new RuntimeException(e);}
	
	pageIndexPool.setHardLimit(65535);
	
	pageIndexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(final int previousMaxCapacity) {
		final int newMaxCapacity = previousMaxCapacity!=0?previousMaxCapacity*2:1;
		final Future<Integer> ft = glExecutor.submitToGL(new GLExecutable<Integer,GL3>(){
		    @Override
		    public Integer execute(GL3 gl){
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
		    final Future<Integer> ft = glExecutor.submitToGL(new GLExecutable<Integer, GL3>(){
			@Override
			public Integer execute(GL3 gl){
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
	synchronized(newPagedByteBuffers){
	    newPagedByteBuffers.add(b);
	}
    }//end registerPagedByteBuffer(...)
    
    void deRegisterPagedByteBuffer(WeakReference<PagedByteBuffer> b){
	synchronized(deletedPagedByteBuffers){
	    deletedPagedByteBuffers.add(b);
	}
    }
    
    public void flushStalePages(){
	if(!glPhysicalMemory.isMapped())
	    return;
	
	synchronized(pagedByteBuffers){synchronized(deletedPagedByteBuffers){
    	 
    	 pagedByteBuffers.removeAll(deletedPagedByteBuffers);
    	 deletedPagedByteBuffers.clear();
	}//end sync(deletedPageDByteBuffers)
    	synchronized(newPagedByteBuffers){
    	 pagedByteBuffers.addAll(newPagedByteBuffers);
    	 newPagedByteBuffers.clear();
    	}//end sync(newPagedByteBuffers)
    	 final Iterator<WeakReference<PagedByteBuffer>> it = pagedByteBuffers.iterator();
    	 while(it.hasNext()){
    	    final WeakReference<PagedByteBuffer> r = it.next();
    	    if(r.get()==null)
    		it.remove();
    	    else
    		r.get().flushStalePages();
    	 }//end while(hasNext)
	}//end sync(pagedByteBuffers)
    }//end flushStalePages()
    
    public void bindToUniform(int textureUnit, GLProgram shaderProgram, GLUniform uniform) {
	glPhysicalMemory.bindToUniform(textureUnit, shaderProgram, uniform);
    }

    public void dumpAllGPUMemTo(final ByteBuffer dest) {
	if(dest==null)
	    throw new NullPointerException("Destination intolerably null.");
	try {glExecutor.submitToGL(new GLExecutable<Void, GL3>(){
	    @Override
	    public Void execute(GL3 gl) throws Exception {
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
	} catch(Exception e) {e.printStackTrace();}
	dest.clear();
    }//end dumpAllGPUMemTo(...)

    public void compactRootBuffer() {
	pageIndexPool.compact();
    }
    
    public synchronized void dumpAllocationTable(){
	HashMap<Integer,PageEntry> pageMap = new HashMap<Integer,PageEntry>();
	final ArrayList<WeakReference<PagedByteBuffer>> buffers = new ArrayList<WeakReference<PagedByteBuffer>>(pagedByteBuffers);
	for(WeakReference<PagedByteBuffer> wr:buffers){
	    PagedByteBuffer pbb = wr.get();
	    if(pbb !=null){
		final List<Integer> pageTable = pbb.getPageTable();
		for(int logicalPageIndex=0; logicalPageIndex<pageTable.size(); logicalPageIndex++){
		    final int physicalPage = pageTable.get(logicalPageIndex);
		    PageEntry pe = new PageEntry();
		    pe.pagedByteBuffer = pbb;
		    pe.logicalPage     = logicalPageIndex;
		    pageMap.put(physicalPage, pe);
		}//end for(logicalPageIndices)
	    }//end pbb != null
	}//end for(pagedByteBuffers)
	
	System.out.println("Total allocated pages: "+pageMap.size());
	
	for(int physicalPageIndex=0; physicalPageIndex<65535; physicalPageIndex++){
	    System.out.print("==== PHYSICAL PAGE "+physicalPageIndex);
	    final PageEntry entry = pageMap.get(physicalPageIndex);
	    if(entry!=null){
		System.out.println(" "+entry.pagedByteBuffer.getDebugName());
		//HashMap<Integer,String> indexMap = new HashMap<Integer,String>();
	    }else {
		System.out.print("\t[empty page]");
		if(!pageIndexPool.getFreeIndices().contains(physicalPageIndex))
		    System.out.print("   ***UNACCOUNTED-FOR as free page!!***");
		System.out.print("");
		System.out.println();
		}
	}//end for(physicalPageIndex)
	
	System.out.println("Terminal Recall has aborted due to a problem re-allocating pages.");
	System.exit(0);
    }//end dumpAllocationTable()
    
    class PageEntry{
	PagedByteBuffer pagedByteBuffer;
	int logicalPage;
    }
}//end MemmoryManager
