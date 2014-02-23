/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.mem.IByteBuffer;
import org.jtrfp.trcl.mem.PagedByteBuffer;


public final class GlobalDynamicTextureBuffer
	{
	private static final AtomicInteger sizeInBytes = new AtomicInteger();
	private static final ArrayList<Class<?>>finalizationList = new ArrayList<Class<?>>(10);
	private static PagedByteBuffer logicalMemory;
	public static void finalizeAllocation(GPU gpu, TR tr)
		{//Finalize dependent allocations
	    	logicalMemory = tr.getGPU().getMemoryManager().createPagedByteBuffer(4, "Legacy Buffer");
		for(Class<?> c:finalizationList)
			{try{
			System.out.println("Finalizing allocation: "+c);
			c.getMethod("finalizeAllocation", new Class<?>[]{TR.class}).invoke(null, (Object[])new Object[]{tr});
			}catch(Exception e){e.printStackTrace();}}
		finalizationList.clear();
		logicalMemory.resize(sizeInBytes.get());
		}
	
	public static void addAllocationToFinalize(Class<?> clazz)
		{finalizationList.add(clazz);}
	
	/**
	 * 
	 * @param sizeInBytes
	 * @return byte offset within this buffer at which this allocation begins.
	 * @since Nov 30, 2012
	 */
	public static int requestAllocation(int sizeInBytes)
		{return GlobalDynamicTextureBuffer.sizeInBytes.getAndAdd(sizeInBytes);}

	/**
	 * @return the sizeinbytes
	 */
	public static int getSizeinbytes()
		{return sizeInBytes.get();}
	
	public static void putShort(int byteOffset, short val) {
	    logicalMemory.putShort(byteOffset,val);
	}
	public static void putFloat(int byteOffset, float val) {
	    logicalMemory.putFloat(byteOffset,val);
	}
	public static void put(int byteOffset, ByteBuffer bytes){
	    logicalMemory.put(byteOffset, bytes);
	}
	public static void put(int byteOffset, byte val){
	    logicalMemory.put(byteOffset, val);
	}
	public static void putInt(int byteOffset, int val) {
	    logicalMemory.putInt(byteOffset, val);
	}
	public static IByteBuffer getLogicalMemory(){
	    return logicalMemory;
	}
}//end GlobalTextureBuffer
