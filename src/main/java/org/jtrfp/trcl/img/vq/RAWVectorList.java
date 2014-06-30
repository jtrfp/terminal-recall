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

package org.jtrfp.trcl.img.vq;

import org.jtrfp.trcl.file.RAWFile;

public final class RAWVectorList implements VectorList {
    private final RAWFile raw;
    private final byte [] rawBytes;
    
    public RAWVectorList(RAWFile raw){
	this.raw	=raw;
	this.rawBytes	=raw.getRawBytes();
    }

    @Override
    public int getNumVectors() {
	return raw.getRawBytes().length;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 1;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return (rawBytes[vectorIndex]&0xFF);
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	rawBytes[vectorIndex]=(byte)(value*255.);
    }

}//end RAWVectorList
