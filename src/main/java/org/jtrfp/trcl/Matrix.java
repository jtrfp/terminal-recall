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

import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public final class Matrix
	{
	public static final int BYTES_PER_MATRIX=4*16; // 16 floats
	private static int arrayOffset=Integer.MIN_VALUE;
	private static final AtomicInteger numMatrices = new AtomicInteger();
	
	private int byteOffset=-1;
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(Matrix.class);}
	
	public static Matrix create4x4(){
		return new Matrix(numMatrices.getAndIncrement()*BYTES_PER_MATRIX);
		}
	
	public static void finalizeAllocation(){
		int bytesToAllocate=numMatrices.get()*BYTES_PER_MATRIX;
		System.out.println("Matrices: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		arrayOffset=GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate);
		}
	
	private Matrix(int byteOffset){
		this.byteOffset=byteOffset;
		}
	
	public void set(RealMatrix m){
		final int firstOffset=arrayOffset+byteOffset;
		for(int index=0; index<16; index++){
		    GlobalDynamicTextureBuffer.putFloat(firstOffset+index*4,(float)m.getEntry(index/4, index%4));
		 }//end for(16)
		}///end set(...)
	
	public int getAddressInBytes(){
		return arrayOffset+byteOffset;
		}
	}//end Matrix
