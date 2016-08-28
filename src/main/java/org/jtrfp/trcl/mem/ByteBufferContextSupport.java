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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.primitives.ByteList;
import org.apache.commons.collections.primitives.FloatList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.ShortList;

public class ByteBufferContextSupport {
    private final ArrayList<FloatList> floatsToSet  = new ArrayList<FloatList>();
    private final ArrayList<IntList>   floatIndices = new ArrayList<IntList>();
    
    private final ArrayList<ShortList> shortsToSet  = new ArrayList<ShortList>();
    private final ArrayList<IntList>   shortIndices = new ArrayList<IntList>();
    
    private final ArrayList<IntList>   intsToSet  = new ArrayList<IntList>();
    private final ArrayList<IntList>   intIndices = new ArrayList<IntList>();
    
    private final ArrayList<ByteList>  bytesToSet  = new ArrayList<ByteList>();
    private final ArrayList<IntList>   byteIndices = new ArrayList<IntList>();
    
    private final ArrayList<BitSet>    stalePages  = new ArrayList<BitSet>();
    
    private final Lock flushLock = new ReentrantLock();
    
 public void submitFlush(
	    FloatList floatsToSet, IntList floatIndices,
	    ShortList shortsToSet, IntList shortIndices, 
	    IntList intsToSet, IntList intIndices, 
	    ByteList bytesToSet, IntList byteIndices,
	    BitSet stalePages){
     flushLock.lock();
     this.floatsToSet .add(floatsToSet);
     this.floatIndices.add(floatIndices);
     this.shortsToSet .add(shortsToSet);
     this.shortIndices.add(shortIndices);
     this.intsToSet   .add(intsToSet);
     this.intIndices  .add(intIndices);
     this.bytesToSet  .add(bytesToSet);
     this.byteIndices .add(byteIndices);
     this.stalePages  .add(stalePages);
     flushLock.unlock();
 }//end submitFlush()
 
 /**
  * Applies the stored modifications to the supplied destination and
  * clears the modification lists.
  * @param destination
  * @since Aug 28, 2016
  */
 public BitSet apply(ByteBuffer destination){
     flushLock.lock();
     
     {//FLOATS
	 final int flushListSize = floatsToSet.size();
	 for(int flushIndex = 0; flushIndex < flushListSize; flushIndex++){
	     FloatList floatData = floatsToSet .get(flushIndex);
	     IntList   indexData = floatIndices.get(flushIndex);
	     final int dataSize  = floatsToSet.size();
	     for(int listIndex = 0; listIndex < dataSize; listIndex++)
		 destination.putFloat(indexData.get(listIndex), floatData.get(listIndex));
	 }//end for(flushIndex)
     }
     
     {//INTS
	 final int flushListSize = intsToSet.size();
	 for(int flushIndex = 0; flushIndex < flushListSize; flushIndex++){
	     IntList   intData   = intsToSet .get(flushIndex);
	     IntList   indexData = intIndices.get(flushIndex);
	     final int dataSize  = intsToSet.size();
	     for(int listIndex = 0; listIndex < dataSize; listIndex++)
		 destination.putInt(indexData.get(listIndex), intData.get(listIndex));
	 }//end for(flushIndex)
     }
     
     {//SHORTS
	 final int flushListSize = shortsToSet.size();
	 for(int flushIndex = 0; flushIndex < flushListSize; flushIndex++){
	     ShortList shortData = shortsToSet .get(flushIndex);
	     IntList   indexData = shortIndices.get(flushIndex);
	     final int dataSize  = shortsToSet.size();
	     for(int listIndex = 0; listIndex < dataSize; listIndex++)
		 destination.putShort(indexData.get(listIndex), shortData.get(listIndex));
	 }//end for(flushIndex)
     }
     
     {//BYTES
	 final int flushListSize = bytesToSet.size();
	 for(int flushIndex = 0; flushIndex < flushListSize; flushIndex++){
	     ByteList   byteData = bytesToSet .get(flushIndex);
	     IntList   indexData = byteIndices.get(flushIndex);
	     final int dataSize  = bytesToSet.size();
	     for(int listIndex = 0; listIndex < dataSize; listIndex++)
		 destination.put(indexData.get(listIndex), byteData.get(listIndex));
	 }//end for(flushIndex)
     }
     
     //STALE PAGES
     final BitSet stalePages = new BitSet();
     final List<BitSet> stalePageBlockList = this.stalePages;
     final int stalePageBlockSize          = stalePageBlockList.size();
     
     for(int stalePageBlockIndex = 0; stalePageBlockIndex < stalePageBlockSize; stalePageBlockIndex++)
	 stalePages.or(stalePageBlockList.get(stalePageBlockIndex));
     
     flushLock.unlock();
     return stalePages;
 }//end apply(...)
}//end ByteBufferContextSupport
