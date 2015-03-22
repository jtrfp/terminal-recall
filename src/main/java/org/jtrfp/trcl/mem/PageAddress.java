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

public class PageAddress extends ModulusAddress {
    /**
     * 
     */
    private static final long serialVersionUID = -2198224518084161286L;
    
    private final long pageAddress;
    
    public PageAddress(ByteAddress a){
	this(a.longValue()/PagedByteBuffer.PAGE_SIZE_BYTES);
	setModulus((int)(a.longValue()%PagedByteBuffer.PAGE_SIZE_BYTES));
    }
    
    public PageAddress(long pageAddress){
	super();
	this.pageAddress=pageAddress;
    }

    @Override
    public ByteAddress asByteAddress() {
	return new ByteAddress(pageAddress * PagedByteBuffer.PAGE_SIZE_BYTES + getModulus());
    }

    @Override
    public double doubleValue() {
	return (double)pageAddress + (double)getModulus() / (double)PagedByteBuffer.PAGE_SIZE_BYTES;
    }

    @Override
    public float floatValue() {
	return (float)((double)pageAddress + (double)getModulus() / (double)PagedByteBuffer.PAGE_SIZE_BYTES);
    }

    @Override
    public int intValue() {
	return (int)pageAddress;
    }

    @Override
    public long longValue() {
	return pageAddress;
    }

}//end PageAddress
