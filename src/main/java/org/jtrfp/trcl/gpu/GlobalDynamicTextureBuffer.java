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

import javax.media.opengl.GL2;

import org.jtrfp.trcl.core.TR;


public final class GlobalDynamicTextureBuffer extends GLTextureBuffer
	{
	private static final AtomicInteger sizeInBytes = new AtomicInteger();
	private static ReallocatableGLMemory buffer;
	private static final ArrayList<Class<?>>finalizationList = new ArrayList<Class<?>>(10);
	
	private GlobalDynamicTextureBuffer(int sizeInBytes, GPU gpu)
		{super(sizeInBytes,gpu);}
	
	public static ByteBuffer getByteBuffer()
		{return buffer.getDuplicateReferenceOfUnderlyingBuffer();}
	
	public static void finalizeAllocation(GPU gpu, TR tr)
		{//Finalize dependent allocations
		for(Class<?> c:finalizationList)
			{try{
			c.getMethod("finalizeAllocation", new Class<?>[]{TR.class}).invoke(null, (Object[])new Object[]{tr});
			}catch(Exception e){e.printStackTrace();}}
		finalizationList.clear();
		buffer=gpu.newEmptyGLMemory();
		buffer.reallocate(sizeInBytes.get());
		}
	
	@Override
	protected int getReadWriteParameter()
		{return GL2.GL_DYNAMIC_DRAW;}
	
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

	/**
	 * @return the buffer
	 */
	public static GLMemory getTextureBuffer()
		{return buffer;}

	public static void putShort(int byteOffset, short val) {
	    buffer.putShort(byteOffset,val);
	}

	public static void putFloat(int byteOffset, float val) {
	    buffer.putFloat(byteOffset,val);
	}
}//end GlobalTextureBuffer
