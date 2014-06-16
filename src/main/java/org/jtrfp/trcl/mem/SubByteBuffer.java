/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
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

public final class SubByteBuffer implements IByteBuffer {
    private int byteOffset;
    private IByteBuffer intrinsic;
    
    public SubByteBuffer(IByteBuffer intrinsic, int byteOffset){
	this.intrinsic=intrinsic;
	this.byteOffset=byteOffset;
    }
    
    @Override
    public IByteBuffer putShort(int indexInBytes, short val) {
	intrinsic.putShort(indexInBytes+byteOffset, val);
	return this;
    }

    @Override
    public IByteBuffer putFloat(int indexInBytes, float val) {
    	intrinsic.putFloat(indexInBytes, val);
	return this;
    }

    @Override
    public IByteBuffer putInt(int indexInBytes, int val) {
	intrinsic.putInt(indexInBytes+byteOffset, val);
	return this;
    }

    @Override
    public IByteBuffer put(int indexInBytes, byte val) {
	intrinsic.put(indexInBytes+byteOffset, val);
	return this;
    }

    @Override
    public IByteBuffer put(int startIndexInBytes, ByteBuffer src) {
	intrinsic.put(startIndexInBytes+byteOffset, src);
	return this;
    }

    @Override
    public byte get(int indexInBytes) {
	return intrinsic.get(indexInBytes+byteOffset);
    }

    @Override
    public short getShort(int indexInBytes) {
	return intrinsic.getShort(indexInBytes+byteOffset);
    }

    /**
     * @return the byteOffset
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @param byteOffset the byteOffset to set
     */
    public IByteBuffer setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
        return this;
    }

    /**
     * @return the intrinsic
     */
    public IByteBuffer getIntrinsic() {
        return intrinsic;
    }

    /**
     * @param intrinsic the intrinsic to set
     */
    public IByteBuffer setIntrinsic(IByteBuffer intrinsic) {
        this.intrinsic = intrinsic;
        return this;
    }

    @Override
    public int logical2PhysicalAddressBytes(int logicalAddressInBytes) {
	return logicalAddressInBytes+byteOffset;
    }

    @Override
    public double getFloat(int posInBytes) {
	return intrinsic.getFloat(posInBytes);
    }

    @Override
    public Integer getInt(int posInBytes) {
	return intrinsic.getInt(posInBytes);
    }

}//end SubByteBuffer
