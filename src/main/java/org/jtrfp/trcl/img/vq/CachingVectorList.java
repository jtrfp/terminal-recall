/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.util.BitSet;

public class CachingVectorList implements VectorList {
    private final VectorList delegate;
    private final int numVectors;
    private final int numComponentsPerVector;
    private final BitSet populatedBitSet;
    private final double [] cache;
    
    public CachingVectorList(VectorList delegate){
	this.delegate = delegate;
	this.numVectors = delegate.getNumVectors();
	this.numComponentsPerVector = delegate.getNumComponentsPerVector();
	populatedBitSet = new BitSet(delegate.getNumVectors());//Cached on a per-vector basis
	cache = new double[delegate.getNumVectors()*delegate.getNumComponentsPerVector()];
    }
    
    protected void populateCache(int vectorIndex){
	populatedBitSet.set(vectorIndex);
	final int numComponents = getNumComponentsPerVector();
	final int startIdx      = vectorIndex * numComponents;
	final int endIdx        = (vectorIndex+1) * numComponents;
	int componentIndex = 0;
	for(int i = startIdx; i < endIdx; i++)
	    cache[i] = delegate.componentAt(vectorIndex, componentIndex++);
    }
    
    protected boolean isCached(int vectorIndex){
	return populatedBitSet.get(vectorIndex);
    }

    @Override
    public int getNumVectors() {
	return numVectors;
    }

    @Override
    public int getNumComponentsPerVector() {
	return numComponentsPerVector;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	if(!isCached(vectorIndex))
	    populateCache(vectorIndex);
	final int index = vectorIndex * numComponentsPerVector + componentIndex;
	return cache[index];
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	delegate.setComponentAt(vectorIndex, componentIndex, value);//Can't cache due to vector-level granularity
    }

}//end CachingVectorList
