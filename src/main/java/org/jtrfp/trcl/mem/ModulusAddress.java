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

public abstract class ModulusAddress extends Address {
    protected int modulus;

    /**
     * 
     */
    private static final long serialVersionUID = 2421428816041864744L;
    
    protected ModulusAddress(){
	super();
    }

    protected void setModulus(int modulus){
	this.modulus=modulus;
    }
    public int getModulus(){
	return modulus;
    }
}//end ModulusAddress
