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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.mem.MemoryWindow;
import org.jtrfp.trcl.mem.SubByteBuffer;

public final class MatrixWindow extends MemoryWindow{
    	public static final int BYTES_PER_MATRIX=4*16; // 16 floats
    	public final Double2FloatArrayVariable matrix = new Double2FloatArrayVariable(16);
    	
	public MatrixWindow() {
	init();
	}
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(MatrixWindow.class);}
	
	public static void finalizeAllocation(TR tr){
	    	final MatrixWindow mw=tr.getMatrixWindow();
		int bytesToAllocate=mw.getNumObjects()*BYTES_PER_MATRIX;
		System.out.println("Matrices: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		//mw.setArrayOffset(arrayOffset=GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate));
		mw.setBuffer(new SubByteBuffer(GlobalDynamicTextureBuffer.getLogicalMemory(),GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate)));
		tr.getReporter().report("org.jtrfp.trcl.MatrixWindow.arrayOffsetBytes", String.format("%08X", mw.getBuffer().logical2PhysicalAddressBytes(0)));
		}
	
	public final void setTransposed(double [] vals, int id){
	    double [] newVals = new double[16];
	    for(int index=0; index<16; index++){
		    newVals[index]=(float)vals[(index/4)+(index%4)*4];
		 }//end for(16)
	    matrix.set(id, newVals);
	}
	/*public final void set(double [] vals, int id){
	    final int firstOffset=id*BYTES_PER_MATRIX;
	    for(int index=0; index<16; index++){
		    buffer.putFloat(firstOffset+index*4,(float)vals[index]);
		 }//end for(16)
	}*/
	/*public final void setTransposed(double [] vals, int id){
	    final int firstOffset=id*BYTES_PER_MATRIX;
	    for(int index=0; index<16; index++){
		    getBuffer().putFloat(firstOffset+index*4,(float)vals[(index/4)+(index%4)*4]);
		 }//end for(16)
	}*/
	/*
	public final void set(RealMatrix m, int id){
		final int firstOffset=id*BYTES_PER_MATRIX;
		for(int index=0; index<16; index++){
		    buffer.putFloat(firstOffset+index*4,(float)m.getEntry(index/4, index%4));
		 }//end for(16)
		}///end set(...)
	
	public final void setTransposed(RealMatrix m, int id){
		final int firstOffset=id*BYTES_PER_MATRIX;
		for(int index=0; index<16; index++){
		    buffer.putFloat(firstOffset+index*4,(float)m.getEntry(index%4, index/4));
		 }//end for(16)
		}///end set(...)
	
	public final int getPhysicalAddressInBytes(int id){
	    return buffer.logical2PhysicalAddressBytes(id*BYTES_PER_MATRIX);
	}*/
	}//end Matrix
