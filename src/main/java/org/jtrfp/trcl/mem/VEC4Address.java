/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import org.jtrfp.trcl.gpu.GPU;

public class VEC4Address extends ModulusAddress {
    /**
     * 
     */
    private static final long serialVersionUID = 98913906815423207L;
    
    public static final VEC4Address ZERO = new VEC4Address(0);
    
    private final long addressInVEC4s;
    
    public VEC4Address(ByteAddress source){
	this(source.longValue()/GPU.BYTES_PER_VEC4);
	setModulus((int)(source.longValue()%GPU.BYTES_PER_VEC4));
    }
    
    public VEC4Address(long addressInVEC4s){
	super();
	this.addressInVEC4s=addressInVEC4s;
    }

    @Override
    public double doubleValue() {
	return (double)addressInVEC4s + (double)getModulus() / (double)PagedByteBuffer.PAGE_SIZE_BYTES;
    }

    @Override
    public float floatValue() {
	return (float)((double)addressInVEC4s + (double)getModulus() / (double)PagedByteBuffer.PAGE_SIZE_BYTES);
    }

    @Override
    public int intValue() {
	return (int)addressInVEC4s;
    }

    @Override
    public long longValue() {
	return addressInVEC4s;
    }

    @Override
    public ByteAddress asByteAddress() {
	return new ByteAddress(addressInVEC4s * GPU.BYTES_PER_VEC4 + getModulus());
    }

}//end VEC4Address
