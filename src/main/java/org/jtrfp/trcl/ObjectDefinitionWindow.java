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
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public final class ObjectDefinitionWindow
	{
	public static final int BYTES_PER_OBJECT_BLOCK = 16; // One vec4 is 16bytes
	private static final AtomicInteger objectBlockCount = new AtomicInteger();
	private static final IndirectObject<Integer> arrayOffset = new IndirectObject<Integer>();
	
	public final ElementAttrib<Integer>matrixOffset;
	public final ElementAttrib<Integer>vertexOffset;
	public final ElementAttrib<Byte>numVertices;
	public final ElementAttrib<Byte>mode;
	public final ElementAttrib<Byte>modelScale;
	
	private final int byteOffset;
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(ObjectDefinitionWindow.class);}
	
	public static ObjectDefinitionWindow create()
		{return new ObjectDefinitionWindow();}
	
	public static void finalizeAllocation(TR tr)
		{
		int bytesToAllocate=objectBlockCount.getAndIncrement()*BYTES_PER_OBJECT_BLOCK;
		System.out.println("Object Definitions: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		arrayOffset.set(GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate));
		tr.getReporter().report("org.jtrfp.trcl.ObjectDefinition.arrayOffsetBytes", String.format("%08X", arrayOffset.get()));
		}
	
	private ObjectDefinitionWindow()
		{
		byteOffset= objectBlockCount.getAndIncrement()*BYTES_PER_OBJECT_BLOCK;
		matrixOffset=ElementAttrib.create(arrayOffset, byteOffset+0, Integer.class);
		vertexOffset=ElementAttrib.create(arrayOffset, byteOffset+4, Integer.class);
		numVertices=ElementAttrib.create(arrayOffset, byteOffset+8, Byte.class);
		mode=ElementAttrib.create(arrayOffset, byteOffset+9, Byte.class);
		modelScale=ElementAttrib.create(arrayOffset, byteOffset+10, Byte.class);
		}
	
	public int getByteOffset()
		{return byteOffset;}
	
	public int getAddressInBytes()
		{return byteOffset+arrayOffset.get();}
	}//end ObjectBlock
