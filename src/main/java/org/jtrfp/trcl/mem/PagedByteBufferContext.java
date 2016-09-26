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

public class PagedByteBufferContext implements IByteBuffer, Flushable {
    private PagedByteBuffer pagedByteBuffer;
    
    private FloatList floatsToSet  = new ArrayFloatList();
    private IntList   floatIndices = new ArrayIntList();
    
    private ShortList shortsToSet  = new ArrayShortList();
    private IntList   shortIndices = new ArrayIntList();
    
    private IntList   intsToSet    = new ArrayIntList();
    private IntList   intIndices   = new ArrayIntList();;
    
    private ByteList  bytesToSet   = new ArrayByteList();
    private IntList   byteIndices  = new ArrayIntList();
    
    private BitSet    stalePages   = new BitSet();

    @Override
    public void flush() throws IOException {
	getPagedByteBuffer().flush( 
		floatsToSet,
		floatIndices,
		shortsToSet,
		shortIndices,
		intsToSet,
		intIndices,
		bytesToSet,
		byteIndices,
		stalePages);

	floatsToSet  = new ArrayFloatList();
	floatIndices = new ArrayIntList();

	shortsToSet  = new ArrayShortList();
	shortIndices = new ArrayIntList();

	intsToSet    = new ArrayIntList();
	intIndices   = new ArrayIntList();;

	bytesToSet   = new ArrayByteList();
	byteIndices  = new ArrayIntList();
	
	stalePages   = new BitSet();
    }//end flush()
    
    int getNumStalePages(){
	return stalePages.cardinality();
    }
    
    int getNumShortsToSet(){
	return shortsToSet.size();
    }
    
    int getNumIntsToSet(){
	return intsToSet.size();
    }
    
    int getBytesToSet(){
	return bytesToSet.size();
    }
    
    int getNumFloatsToSet(){
	return floatsToSet.size();
    }
    
    @Override
    public IByteBuffer putInt(int indexInBytes, int val) {
	markPageStale(indexInBytes);
	intIndices.add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	intsToSet.add(val);
	//intrinsic[0].putInt(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes),val);
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
	shortIndices.add(index);
	shortsToSet.add(val);
	//intrinsic[0].putShort(index, val);
	return this;
    }

    @Override
    public IByteBuffer put(int indexInBytes, byte val) {
	markPageStale(indexInBytes);
	byteIndices.add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	bytesToSet.add(val);
	//intrinsic[0].put(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes), val);
	return this;
    }
    
    @Override
    public IByteBuffer putFloat(int indexInBytes, float val) {
	markPageStale(indexInBytes);
	floatIndices.add(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes));
	floatsToSet.add(val);
	//intrinsic[0].putFloat(getPagedByteBuffer().logicalIndex2PhysicalIndex(indexInBytes), val);
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
	    /*
	    final ByteBuffer bb = intrinsic[0].duplicate();
	    bb.position(getPagedByteBuffer().logicalIndex2PhysicalIndex(startIndexInBytes));
	    bb.put(src);
	    */
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
	stalePages.set(indexInBytes/PagedByteBuffer.PAGE_SIZE_BYTES);
    }

}//end PagedByteBufferContext
