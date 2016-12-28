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

public class CachingVectorListND implements VectorListND {
    private final VectorListND delegate;
    final int []               dimensions;
    private final BitSet       populatedBitSet;
    private final double []    cache;
    private final int          numVectors, numComponentsPerVector;
    
    public CachingVectorListND(VectorListND delegate){
	this.delegate   = delegate;
	this.dimensions = delegate.getDimensions();
	this.numVectors = delegate.getNumVectors();
	this.numComponentsPerVector = delegate.getNumComponentsPerVector();
	populatedBitSet             = new BitSet(delegate.getNumVectors());//Cached on a per-vector basis
	cache = new double[numVectors*numComponentsPerVector];
    }

    @Override
    public int[] getDimensions() {
	return dimensions;
    }

    @Override
    public double componentAt(int[] coordinates, int componentIndex) {
	final int coord1D = coord1D(coordinates);
	if(!isCached(coord1D))
	    populateCache(coord1D, coordinates);
	return cache[coord1D*getNumComponentsPerVector()+componentIndex];
    }//end componentAt(...)
    
    protected void populateCache(int coord1D, int [] coords){
	populatedBitSet.set(coord1D);
	final int numComponents = getNumComponentsPerVector();
	final int startIdx      = coord1D * numComponents;
	final int endIdx        = (coord1D+1) * numComponents;
	int componentIndex = 0;
	for(int i = startIdx; i < endIdx; i++)
	    cache[i] = delegate.componentAt(coords, componentIndex++);
    }//end populateCache(...)
    
    protected boolean isCached(int vectorIndex){
	return populatedBitSet.get(vectorIndex);
    }
    
    private int coord1D(int[] coordinates){
	int coord1D =0;
	int infinity=1;
	final int [] dimensions = this.dimensions;
	final int numDims = dimensions.length;
	for(int d=0; d<numDims; d++){
	    coord1D+=coordinates[d]*infinity;
	    infinity*=dimensions[d];
	}
	return coord1D;
    }//end coord1D

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	delegate.setComponentAt(coordinates, componentIndex, value);
    }

    @Override
    public int getNumVectors() {
	return numVectors;
    }

    @Override
    public int getNumComponentsPerVector() {
	return numComponentsPerVector;
    }

}//end CachingVectorList
