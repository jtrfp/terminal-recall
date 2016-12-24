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

public class MIPScalingVectorList implements VectorList {
    private final VectorList delegate;
    private final int sideLength;
    
    public MIPScalingVectorList(VectorList delegate, int originalSideLength){
	this.delegate   = delegate;
	this.sideLength = originalSideLength;
    }

    @Override
    public int getNumVectors() {
	return delegate.getNumVectors() / 4;
    }

    @Override
    public int getNumComponentsPerVector() {
	return delegate.getNumComponentsPerVector();
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	final int newSideLength = sideLength / 2;
	final int x = vectorIndex % newSideLength * 2;
	final int y = vectorIndex / newSideLength * 2;
	final int newIndex = sideLength * y + x;
	double accumulator = 0;
	double weightAccum = 0, weight;
	
	weight = delegate.componentAt(newIndex, 3);//ALPHA
	weightAccum += weight;
	accumulator += delegate.componentAt(newIndex, componentIndex) * weight;
	
	weight = delegate.componentAt(newIndex+1, 3);//ALPHA
	weightAccum += weight;
	accumulator += delegate.componentAt(newIndex+1, componentIndex) * weight;
	
	weight = delegate.componentAt(newIndex+sideLength, 3);//ALPHA
	weightAccum += weight;
	accumulator += delegate.componentAt(newIndex+sideLength, componentIndex) * weight;
	
	weight = delegate.componentAt(newIndex+sideLength+1, 3);//ALPHA
	weightAccum += weight;
	accumulator += delegate.componentAt(newIndex+sideLength+1, componentIndex) * weight;
	accumulator /= weightAccum;
	return accumulator;
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	throw new UnsupportedOperationException("MIPScalingVectorList is read-only.");
    }

}//end MIPScalingVectorList
