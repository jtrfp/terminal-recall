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


public class ByteAddress extends Address {
    /**
     * 
     */
    private static final long serialVersionUID = 8005927090195192255L;
    private final long addressInBytes;
    
    public ByteAddress(long addressInBytes){
	super();
	this.addressInBytes=addressInBytes;
    }

    @Override
    public double doubleValue() {
	return addressInBytes;
    }

    @Override
    public float floatValue() {
	return addressInBytes;
    }

    @Override
    public int intValue() {
	return (int)addressInBytes;
    }

    @Override
    public long longValue() {
	return addressInBytes;
    }

    @Override
    public ByteAddress asByteAddress() {
	return this;
    }
 
}
