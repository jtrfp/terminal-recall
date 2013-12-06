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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.linear.RealMatrix;

public final class Matrix
	{
	public static final int BYTES_PER_MATRIX=4*16; // 16 floats
	private static int arrayOffset=Integer.MIN_VALUE;
	private static final AtomicInteger numMatrices = new AtomicInteger();
	
	private int byteOffset=-1;
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(Matrix.class);}
	
	public static Matrix create4x4()
		{
		return new Matrix(numMatrices.getAndIncrement()*BYTES_PER_MATRIX);
		}
	
	public static void finalizeAllocation()
		{
		int bytesToAllocate=numMatrices.get()*BYTES_PER_MATRIX;
		System.out.println("Matrices: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		arrayOffset=GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate);
		}
	
	private Matrix(int byteOffset)
		{
		this.byteOffset=byteOffset;
		}
	
	public void set(RealMatrix m)//406801
		{
		final ByteBuffer bb = GlobalDynamicTextureBuffer.getByteBuffer();
		bb.position(arrayOffset+byteOffset);
		FloatBuffer putter=bb.asFloatBuffer();
		/*
		if(bb.position()==406801*16)
			{
			//System.out.println("FOUND MAT: "+m.getEntry(0, 0)+" float[]="+valuesToPut[0]+" c="+c);
			m.setEntry(0, 0, 1);//0,0
			m.setEntry(1, 0, 2);//0,1
			m.setEntry(2, 0, 3);
			m.setEntry(3, 0, 4);
			
			m.setEntry(0, 1, 5);
			m.setEntry(1, 1, 6);
			m.setEntry(2, 1, 7);
			m.setEntry(3, 1, 8);//1,3
			
			m.setEntry(0, 2, 9);
			m.setEntry(1, 2, 10);
			m.setEntry(2, 2, 11);
			m.setEntry(3, 2, 12);
			
			m.setEntry(0, 3, 13);
			m.setEntry(1, 3, 14);
			m.setEntry(2, 3, 15);
			m.setEntry(3, 3, 16);
			}*/
		
		for(int c=0; c<4; c++)
			{
			float [] valuesToPut=TR.doubles2Floats(m.getRow(c));
			putter.put(valuesToPut);
			}//end for(row)
		}///end set(...)
	
	public int getAddressInBytes()
		{
		return arrayOffset+byteOffset;
		}
	}//end Matrix
