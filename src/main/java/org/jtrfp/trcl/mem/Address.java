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


public abstract class Address extends Number {

    /**
     * 
     */
    private static final long serialVersionUID = -2924146452695795547L;
    
    public abstract ByteAddress asByteAddress();
    
    protected Address(){}
    
    public VEC4Address asVEC4Address(){
	return new VEC4Address(asByteAddress());
    }
    
    public PageAddress asPageAddress(){
	return new PageAddress(asByteAddress());
    }
    
    @Override
    public int hashCode(){
	return asByteAddress().intValue();
    }

}//end Address
