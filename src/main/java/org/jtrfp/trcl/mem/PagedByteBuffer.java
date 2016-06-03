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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.OutOfIndicesException;

public final class PagedByteBuffer  implements IByteBuffer, Resizeable{
    private final 	ByteBuffer [] 	intrinsic;//Should be size=1. Serves as an indirect reference.
    public static final int 		PAGE_SIZE_BYTES=GPU.BYTES_PER_VEC4*GPU.GPU_VERTICES_PER_BLOCK*1;//Anchored to number of triangle vertices per block
    private final       ListActionDispatcher<Integer>      pageTable = new ListActionDispatcher<Integer>(new ArrayList<Integer>());
    private final 	IndexPool 	pageIndexPool;
    private final 	String		debugName;
    private final       CollectionActionDispatcher<Integer>stalePages = new CollectionActionDispatcher<Integer>(new HashSet<Integer>());
    private final	GPU		gpu;
    private final	WeakReference<PagedByteBuffer> weakThis;
    
    PagedByteBuffer(GPU gpu, ByteBuffer [] intrinsic, IndexPool pageIndexPool, int initialSizeInBytes, String debugName){
	this.intrinsic=intrinsic;
	this.pageIndexPool=pageIndexPool;
	this.debugName=debugName;
	final int sizeInPages = sizeInPages(initialSizeInBytes);
	/*for(int i=0; i<sizeInPages; i++){
	    final int pIndex = pageIndexPool.pop();
	    pageTable.add(pIndex);
	}*/
	pageIndexPool.pop(pageTable,sizeInPages);
	this.gpu=gpu;
	weakThis = new WeakReference<PagedByteBuffer>(this);
	gpu.memoryManager.get().registerPagedByteBuffer(weakThis);
    }//end constructor
    
    public int sizeInPages(){
	return pageTable.size();
    }
    public int logicalPage2PhysicalPage(int logicalPage){
	return pageTable.get(logicalPage);
    }
    
    private static int sizeInPages(int sizeInBytes){
	return index2Page(sizeInBytes)+1;
    }
    private static int index2Page(int indexInBytes){
	return indexInBytes/PAGE_SIZE_BYTES;
    }
    private static int pageModulus(int indexInBytes){
	return indexInBytes%PAGE_SIZE_BYTES;
    }
    private int logicalIndex2PhysicalIndex(int logicalIndexInBytes){
	return (PAGE_SIZE_BYTES*pageTable.get(index2Page(logicalIndexInBytes)))+pageModulus(logicalIndexInBytes);
    }
    
    private AtomicLong lastRootBufferNuclearGCMillis = new AtomicLong(0);

    @Override
    public void resize(int newSizeInBytes) {
	final int newNumPages=sizeInPages(newSizeInBytes);
	final int pageNumDelta=newNumPages-pageTable.size();
	if(pageNumDelta==0)return;
	if(pageNumDelta>0){	//GROW
	    //final int newSize    = newNumPages-pageTable.size();
	    try{
		/*for(int i=0; i<pageNumDelta; i++){
		    final int idx = pageIndexPool.popOrException();
		    pageTable.add(idx);
		}*/
		pageIndexPool.popOrException(pageTable,pageNumDelta);
		}
	    catch(OutOfIndicesException e){
		System.err.println("Out of root pages. Performing Nuclear GC and trying again.");
		TR.nuclearGC();
		try{Thread.sleep(2000);}catch(InterruptedException ee){}
		gpu.compactRootBuffer();
		if(System.currentTimeMillis()-lastRootBufferNuclearGCMillis.get()<10000)
			 gpu.memoryManager.get().dumpAllocationTable();
		lastRootBufferNuclearGCMillis.set(System.currentTimeMillis());
		resize(newSizeInBytes);
	    }
	}else{			//SHRINK
	    /*for(int i=newNumPages; i<pageTable.size(); i++){
		pageIndexPool.free(pageTable.get(i));
	    }*/
	    pageIndexPool.free(pageTable.subList(newNumPages,pageTable.size()));//This had the -1 offset originally
	    //final int numPagesToRemove = pageTable.size()-newNumPages;
	    pageTable.subList(newNumPages, pageTable.size()).clear();
	    /*for(int i=0; i<numPagesToRemove; i++){
		pageTable.remove(pageTable.size()-1);
	    }*/
	}//end if(pageNumDelta...)
    }//end resize()
    
    private void deallocate(){
	pageIndexPool.free(pageTable);
    }//end deallocate()
    
    private void markPageStale(int indexInBytes){
	synchronized(stalePages){
	 stalePages.add(new Integer(indexInBytes/PagedByteBuffer.PAGE_SIZE_BYTES));}
    }
    
    /**
     * Must notify the page IndexPool that this buffer is being forgotten such that its pages may be freed.
     */
    @Override
    public void finalize() throws Throwable{
	deallocate();
	gpu.memoryManager.get().deRegisterPagedByteBuffer(weakThis);
	super.finalize();
    }//end finalize()

    @Override
    public IByteBuffer putShort(int indexInBytes, short val) {
	markPageStale(indexInBytes);
	int index=logicalIndex2PhysicalIndex(indexInBytes);
	intrinsic[0].putShort(index, val);
	return this;
    }

    @Override
    public IByteBuffer put(int indexInBytes, byte val) {
	markPageStale(indexInBytes);
	intrinsic[0].put(logicalIndex2PhysicalIndex(indexInBytes), val);
	return this;
    }

    @Override
    public byte get(int indexInBytes) {
	return intrinsic[0].get(logicalIndex2PhysicalIndex(indexInBytes));
    }

    @Override
    public short getShort(int indexInBytes) {
	return intrinsic[0].getShort(logicalIndex2PhysicalIndex(indexInBytes));
    }

    @Override
    public IByteBuffer putFloat(int indexInBytes, float val) {
	markPageStale(indexInBytes);
	intrinsic[0].putFloat(logicalIndex2PhysicalIndex(indexInBytes), val);
	return this;
    }

    @Override
    public IByteBuffer put(int startIndexInBytes, ByteBuffer src) {
	final int bytesProtrudingIntoPage = startIndexInBytes % PagedByteBuffer.PAGE_SIZE_BYTES;
	final int bytesRemainingInPage = PagedByteBuffer.PAGE_SIZE_BYTES - bytesProtrudingIntoPage;
	final int remaining = src.remaining();
	if(remaining>bytesRemainingInPage){
	    final int oldLimit = src.limit();
	    //Split to pages
	    src.limit(src.position()+bytesRemainingInPage);
	    put(startIndexInBytes, src);
	    src.limit(oldLimit);
	    put(startIndexInBytes+bytesRemainingInPage,src);
	}else{//Do it
	    markPageStale(startIndexInBytes);
	    final ByteBuffer bb = intrinsic[0].duplicate();
	    bb.position(logicalIndex2PhysicalIndex(startIndexInBytes));
	    bb.put(src);
	}
	return this;
    }//end put(...)
    
    private final ArrayList<Integer> spBuffer = new ArrayList<Integer>(1024);
    
    void flushStalePages(){
	synchronized(stalePages){
	 spBuffer.addAll(stalePages);
	 stalePages.clear();
	}//end sync()
	 for(int value:spBuffer)
	    gpu.memoryManager.get().flushRange(pageTable.get(value)*PagedByteBuffer.PAGE_SIZE_BYTES, PagedByteBuffer.PAGE_SIZE_BYTES);
	 spBuffer.clear();
    }//end flushStalePages()

    @Override
    public IByteBuffer putInt(int indexInBytes, int val) {
	markPageStale(indexInBytes);
	intrinsic[0].putInt(logicalIndex2PhysicalIndex(indexInBytes),val);
	return this;
    }

    public int logical2PhysicalAddressBytes(int logicalAddressInBytes) {
	return logicalIndex2PhysicalIndex(logicalAddressInBytes);
    }

    @Override
    public double getFloat(int posInBytes) {
	return intrinsic[0].getFloat(logicalIndex2PhysicalIndex(posInBytes));
    }

    @Override
    public Integer getInt(int posInBytes) {
	return intrinsic[0].getInt(logicalIndex2PhysicalIndex(posInBytes));
    }
    
    @Override
    public String toString(){
	return "PagedByteBuffer '"+debugName+"' "+" hash="+hashCode();
    }

    @Override
    public IByteBuffer putInts(int indexInBytes, int[] vals) {//TODO: Optimize, respecting page boundaries.
	for(int i=0; i<vals.length; i++)
	    putInt(4*i+indexInBytes, vals[i]);
	return this;
    }

    public IByteBuffer putInts(int indexInBytes, Collection<? extends Number> c) {//TODO: Optimize, respecting page boundaries.
	for(Number i:c)
	    {putInt(indexInBytes,i.intValue());indexInBytes+=4;}
	return this;
    }

    /**
     * @return the pageTable
     */
    public ListActionDispatcher<Integer> getPageTable() {
        return pageTable;
    }

    IndexPool getPageIndexPool() {
        return pageIndexPool;
    }

    public String getDebugName() {
        return debugName;
    }
}//end PageByteBuffer
