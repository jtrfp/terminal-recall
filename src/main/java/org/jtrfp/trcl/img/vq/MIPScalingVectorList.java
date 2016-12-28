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

import org.apache.commons.math3.analysis.function.Sigmoid;

public class MIPScalingVectorList implements VectorListND {
    private final VectorListND delegate, rgbaReference, esTuTvReference;
    private Sigmoid sigmoid = new Sigmoid();
    private final int numVectors;
    
    public MIPScalingVectorList(
	    VectorListND delegate, 
	    VectorListND rgbaReference, 
	    VectorListND esTuTvReference){
	this.delegate        = delegate;
	this.rgbaReference   = rgbaReference;
	this.esTuTvReference = esTuTvReference;
	numVectors           = delegate.getNumVectors() / (int)Math.pow(2, delegate.getDimensions().length);
    }

    @Override
    public int getNumVectors() {
	return numVectors;
    }

    @Override
    public int getNumComponentsPerVector() {
	return delegate.getNumComponentsPerVector();
    }

    @Override
    public double componentAt(int [] coordinates, int componentIndex) {
	final int dims = coordinates.length;
	final int [] coordBuffer = new int[dims];
	for( int i = 0; i < dims; i++)
	    coordBuffer[i] = coordinates[i] * 2;
	final ResultBuffer resultBuffer = new ResultBuffer();
	recursiveComponentAt(resultBuffer, coordBuffer, 0, componentIndex);
	return resultBuffer.accumulator / resultBuffer.weightAccumulator;
    }
    
    private void recursiveComponentAt(ResultBuffer dest, int [] coordBuffer, int coordIndex, int componentIndex){
	double weight;
	if(coordIndex >= coordBuffer.length)
	    return;
	
	weight = weightAt(coordBuffer, componentIndex);
	dest.accumulator       += delegate.componentAt(coordBuffer, componentIndex) * weight;
	dest.weightAccumulator += weight;
	final int nextCoord = coordIndex+1;
	recursiveComponentAt(dest, coordBuffer, nextCoord, componentIndex);
	
	coordBuffer[coordIndex]++;
	weight = weightAt(coordBuffer, componentIndex);
	dest.accumulator       += delegate.componentAt(coordBuffer, componentIndex) * weight;
	dest.weightAccumulator += weight;
	recursiveComponentAt(dest, coordBuffer, nextCoord, componentIndex);
	coordBuffer[coordIndex]--;//Clean up after ourselves
    }//end recursiveComponentAt(...)
    
    private double weightAt(int [] coordBuffer, int vectorIndex){
	double result = 1;
	if(rgbaReference != null)
	    result *= sigmoid.value(rgbaReference  .componentAt(coordBuffer, 3));
	//if(esTuTvReference != null)
	//    result += esTuTvReference.componentAt(coordBuffer, 0);
	return result;
    }//end weightAt(...)

    @Override
    public int[] getDimensions() {
	final int [] oldDims = delegate.getDimensions();
	final int len = oldDims.length;
	final int [] newDims = new int[len];
	for(int i = 0; i < len; i++)
	    newDims[i] = oldDims[i] / 2;
	return newDims;
    }

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	throw new UnsupportedOperationException("MIPScalingVectorList is read-only.");
    }
    
    private static class ResultBuffer {
	public double accumulator        =0;
	public double weightAccumulator  =0;
    }

}//end MIPScalingVectorList
