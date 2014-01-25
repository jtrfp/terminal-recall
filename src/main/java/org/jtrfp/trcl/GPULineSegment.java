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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class GPULineSegment implements GPUVec4Element{
	public static final int BYTES_PER_SEGMENT=GLTextureBuffer.BYTES_PER_VEC4; //16 bytes in a vec4, 1 vec4 per LineSegment
	public static final int SEGMENTS_PER_BLOCK=96/6;
	
	public final ElementAttrib<Short>x1;
	public final ElementAttrib<Short>y1;
	public final ElementAttrib<Short>z1;
	
	public final ElementAttrib<Short>x2;
	public final ElementAttrib<Short>y2;
	public final ElementAttrib<Short>z2;
	
	public final ElementAttrib<Byte>red;
	public final ElementAttrib<Byte>green;
	public final ElementAttrib<Byte>blue;
	
	public final ElementAttrib<Byte>thickness;
	
	private final int byteOffset;
	
	private static final IndirectObject<Integer> arrayOffset = new IndirectObject<Integer>();
	private static final AtomicInteger numSegments = new AtomicInteger();
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(GPULineSegment.class);}
	
	public static void finalizeAllocation(TR tr){
		int bytesToAllocate = numSegments.get()*GPULineSegment.BYTES_PER_SEGMENT;
		System.out.println("LineSegments: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		arrayOffset.set(GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate));
		}
	
	public static GPULineSegment [] createLineSegmentBlock(int numSegments){
		int startIndex = GPULineSegment.numSegments.getAndAdd(numSegments)*BYTES_PER_SEGMENT;//16 bytes per uint vec4
		GPULineSegment [] result = new GPULineSegment[numSegments];
		for(int i=0; i<numSegments;i++)
			{result[i]=new GPULineSegment(startIndex+i*BYTES_PER_SEGMENT);}
		return result;
		}
	
	public static GPULineSegment create()
		{return new GPULineSegment(numSegments.getAndIncrement()*BYTES_PER_SEGMENT);}
	
	private GPULineSegment(int byteOffset){
		this.byteOffset=byteOffset;
		x1=ElementAttrib.create(arrayOffset, byteOffset, Short.class);
		y1=ElementAttrib.create(arrayOffset, byteOffset+2, Short.class);
		z1=ElementAttrib.create(arrayOffset, byteOffset+4, Short.class);
		
		x2=ElementAttrib.create(arrayOffset, byteOffset+6, Short.class);
		y2=ElementAttrib.create(arrayOffset, byteOffset+8, Short.class);
		z2=ElementAttrib.create(arrayOffset, byteOffset+10, Short.class);
		
		thickness=ElementAttrib.create(arrayOffset, byteOffset+12, Byte.class);
		
		red=ElementAttrib.create(arrayOffset, byteOffset+13, Byte.class);
		green=ElementAttrib.create(arrayOffset, byteOffset+14, Byte.class);
		blue=ElementAttrib.create(arrayOffset, byteOffset+15, Byte.class);
		}
	
	public final int getAddressInBytes()
		{return arrayOffset.get()+byteOffset;}
	}//end LineSegment
