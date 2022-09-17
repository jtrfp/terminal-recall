/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

import org.apache.commons.collections.primitives.ArrayByteList;
import org.apache.commons.collections.primitives.ArrayFloatList;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.ShortList;
import org.jtrfp.trcl.tools.Util;

import lombok.AllArgsConstructor;

public class PagedByteBufferContext implements IByteBuffer, Flushable {
    private PagedByteBuffer pagedByteBuffer;
    
    private final FloatList []floatsToSet  = {new ArrayFloatList()};
    private final IntList   []floatIndices = {new ArrayIntList()};
    
    private final ShortList []shortsToSet  = {new ArrayShortList()};
    private final IntList   []shortIndices = {new ArrayIntList()};
    
    private final IntList   []intsToSet    = {new ArrayIntList()};
    private final IntList   []intIndices   = {new ArrayIntList()};
    
    private final ByteList  []bytesToSet   = {new ArrayByteList()};
    private final IntList   []byteIndices  = {new ArrayIntList()};
    
    private final BitSet    []stalePages   = {new BitSet()};
    
    private final CleaningAction cleaningAction;
    
    public PagedByteBufferContext() {
	cleaningAction = new CleaningAction(floatsToSet,shortsToSet,intsToSet,bytesToSet,stalePages);
	Util.CLEANER.register(this, cleaningAction);
    }
    
    @AllArgsConstructor
    private static class CleaningAction implements Runnable {
	private final FloatList []floatsToSet;
	private final ShortList []shortsToSet;
	private final IntList   []intsToSet;
	private final ByteList  []bytesToSet;
	private final BitSet    []stalePages;

	@Override
	public void run() {
	    System.out.println("PagedByteBufferContext cleaning action...");
	    if(!floatsToSet[0].isEmpty())
		new RuntimeException("FloatsToSet unflushed following finalization!").printStackTrace();
	    if(!intsToSet[0].isEmpty())
		new RuntimeException("intsToSet unflushed following finalization!").printStackTrace();
	    if(!shortsToSet[0].isEmpty())
		new RuntimeException("ShortsToSet unflushed following finalization!").printStackTrace();
	    if(!bytesToSet[0].isEmpty())
		new RuntimeException("BytesToSet unflushed following finalization!").printStackTrace();
	    if(stalePages[0].cardinality() != 0)
		new RuntimeException("Stalepages unflushed following finalization!").printStackTrace();
	}
    }//end CleaningAction

    @Override
    public void flush() throws IOException {
	getPagedByteBuffer().flush( 
		floatsToSet[0],
		floatIndices[0],
		shortsToSet[0],
		shortIndices[0],
		intsToSet[0],
		intIndices[0],
		bytesToSet[0],
		byteIndices[0],
		stalePages[0]);

	floatsToSet[0]  = new ArrayFloatList();
	floatIndices[0] = new ArrayIntList();

	shortsToSet[0]  = new ArrayShortList();
	shortIndices[0] = new ArrayIntList();

	intsToSet[0]    = new ArrayIntList();
	intIndices[0]   = new ArrayIntList();

	bytesToSet[0]   = new ArrayByteList();
	byteIndices[0]  = new ArrayIntList();
	
	stalePages[0]   = new BitSet();
    }//end flush()
    
    int getNumStalePages(){
	return stalePages[0].cardinality();
    }
    
    int getNumShortsToSet(){
	return shortsToSet[0].size();
    }
    
    int getNumIntsToSet(){
	return intsToSet[0].size();
    }
    
    int getBytesToSet(){
	return bytesToSet[0].size();
    }
    
    int getNumFloatsToSet(){
	return floatsToSet[0].size();
    }
    
    @Override
    public IByteBuffer putInt(int indexInBytes, int val) {
	markPageStale(indexInBytes);
	intIndices[0].add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	intsToSet[0].add(val);
	return this;
    }

    public int logical2PhysicalAddressBytes(int logicalAddressInBytes) {
	return getPagedByteBuffer().logicalIndex2PhysicalIndex(logicalAddressInBytes);
    }
    
    @Override
    public IByteBuffer putInts(int indexInBytes, int[] vals) {//TODO: Optimize, respecting page boundaries.
	for(int i=0; i<vals.length; i++)
	    putInt(4*i+indexInBytes, vals[i]);
	return this;
    }
    
    @Override
    public IByteBuffer putShort(int indexInBytes, short val) {
	markPageStale(indexInBytes);
	int index=getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes);
	shortIndices[0].add(index);
	shortsToSet[0].add(val);
	return this;
    }

    @Override
    public IByteBuffer put(int indexInBytes, byte val) {
	markPageStale(indexInBytes);
	byteIndices[0].add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	bytesToSet[0].add(val);
	return this;
    }
    
    @Override
    public IByteBuffer putFloat(int indexInBytes, float val) {
	markPageStale(indexInBytes);
	floatIndices[0].add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	floatsToSet[0].add(val);
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
	    int index = getPagedByteBuffer().logicalIndex2PhysicalIndex(startIndexInBytes);
	    while(src.hasRemaining())
		put(index++,src.get());//TODO: Optimize with batch on flush
	}
	return this;
    }//end put(...)

    @Override
    public byte get(int indexInBytes) {
	return getPagedByteBuffer().get(indexInBytes);
    }

    @Override
    public short getShort(int indexInBytes) {
	return getPagedByteBuffer().getShort(indexInBytes);
    }

    @Override
    public double getFloat(int posInBytes) {
	return getPagedByteBuffer().getFloat(posInBytes);
    }

    @Override
    public Integer getInt(int posInBytes) {
	return getPagedByteBuffer().getInt(posInBytes);
    }

    PagedByteBuffer getPagedByteBuffer() {
        return pagedByteBuffer;
    }

    void setPagedByteBuffer(PagedByteBuffer pagedByteBuffer) {
        this.pagedByteBuffer = pagedByteBuffer;
    }
    
    private void markPageStale(int indexInBytes){
	stalePages[0].set(indexInBytes/PagedByteBuffer.PAGE_SIZE_BYTES);
    }

}//end PagedByteBufferContext
