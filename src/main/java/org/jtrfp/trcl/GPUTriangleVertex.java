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
package org.jtrfp.trcl;

import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class GPUTriangleVertex implements GPUVec4Element
	{
	public static final int BYTES_PER_VERTEX=GLTextureBuffer.BYTES_PER_VEC4; //16 bytes in a vec4, 1 vec4 per Vertex
	public static final int VERTICES_PER_BLOCK=96;
	
	public final ElementAttrib<Short>x;
	public final ElementAttrib<Short>y;
	public final ElementAttrib<Short>z;
	/**
	 * Scale texture coordinate up by 4096 prior to setting.
	 */
	public final ElementAttrib<Short>u;
	/**
	 * Scale texture coordinate up by 4096 prior to setting.
	 */
	public final ElementAttrib<Short>v;
	
	private final int byteOffset;
	
	private static final IndirectObject<Integer> arrayOffset = new IndirectObject<Integer>();
	private static final AtomicInteger numVertices = new AtomicInteger();
	private static boolean finalized=false;
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(GPUTriangleVertex.class);}
	
	public static void finalizeAllocation()
		{
		int bytesToAllocate = numVertices.get()*GPUTriangleVertex.BYTES_PER_VERTEX;
		arrayOffset.set(GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate));
		System.out.println("Triangle Vertices: Allocating "+bytesToAllocate+" bytes of GPU resident RAM, starting at offset "+arrayOffset.get());
		finalized=true;
		}
	
	public static GPUTriangleVertex [] createVertexBlock(int numVertices)
		{
		if(finalized)throw new RuntimeException("Can't create a vertex block after their allocation has already been finalized.");
		int startIndex = GPUTriangleVertex.numVertices.getAndAdd(numVertices)*BYTES_PER_VERTEX;//16 bytes per uint vec4
		GPUTriangleVertex [] result = new GPUTriangleVertex[numVertices];
		for(int i=0; i<numVertices;i++)
			{result[i]=new GPUTriangleVertex(startIndex+i*BYTES_PER_VERTEX);}
		return result;
		}
	
	public static GPUTriangleVertex create()
		{
		return new GPUTriangleVertex(numVertices.getAndIncrement()*BYTES_PER_VERTEX);
		}
	
	private GPUTriangleVertex(int byteOffset)
		{
		this.byteOffset=byteOffset;
		x=ElementAttrib.create(arrayOffset, byteOffset, Short.class);
		y=ElementAttrib.create(arrayOffset, byteOffset+2, Short.class);
		z=ElementAttrib.create(arrayOffset, byteOffset+4, Short.class);
		// 6 is unused
		u=ElementAttrib.create(arrayOffset, byteOffset+8, Short.class);
		v=ElementAttrib.create(arrayOffset, byteOffset+10, Short.class);
		// 12, 14 are unused
		}
	/*
	 * Wow, you've gone quite deep... way down here in the GPUTriangleVertex class.
	 * You must be up to something very heavy to dare tread in a foresaken place such as this;
	 * surely there are other things you wish you could be doing than sifting through this code...
	 * Remember when you were a kid: what did you want to be when you grow up? Doing this?
	 * Did you really think "I want to read through thousands of lines of GPU glue code"
	 * Did you even know what GPU glue code is? Probably not. But I bet you wanted to do
	 * something which has lead you to doing this. And that's what adulthood really is: The use of 
	 * wisdom to reconcile reality with your earlier dreams in hopes that a win-win situation will be found.
	 * 
	 * That's probably why you are here. 
	 * 
	 * Or, perhaps you just want to suffer. 
	 * That can be accommodated too. (:
	 * 
	 */
	
	
	public final int getAddressInBytes()
		{
		return arrayOffset.get()+byteOffset;
		}
	}//end Vertex
