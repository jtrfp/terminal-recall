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

import java.util.Arrays;

public final class RasterizedBlockVectorList implements VectorListND {
    private final VectorListND 	delegate;
    private final int 		rasterWidthInVectors, 
    /*			*/	blockWidthInVectors,
    /*			*/	vectorsPerBlock,
    /*			*/	blocksPerRow;
    private final int[]		dimensions;

    public RasterizedBlockVectorList(VectorListND rasterizedVectorList,int blockWidthInVectors) {//TODO: Arbitrary sizes
	this.rasterWidthInVectors 	= rasterizedVectorList.getDimensions()[0];
	this.blockWidthInVectors 	= blockWidthInVectors;
	this.delegate 			= rasterizedVectorList;
	this.vectorsPerBlock 		= blockWidthInVectors * blockWidthInVectors;
	this.blocksPerRow 		= rasterWidthInVectors / blockWidthInVectors;
	dimensions = Arrays.copyOf(delegate.getDimensions(),delegate.getDimensions().length);
	for(int i=0; i<dimensions.length;i++)
	    dimensions[i]/=blockWidthInVectors;
    }// end constructor

    @Override
    public int getNumVectors() {
	return delegate.getNumVectors() / vectorsPerBlock;
    }

    @Override
    public int getNumComponentsPerVector() {
	return delegate.getNumComponentsPerVector()
		* vectorsPerBlock;
    }
/*
    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	//System.out.println("RasterizedBlockVectorList.componentAt() vectorIndex="+vectorIndex+" componentIndex="+componentIndex);
	final int [] trans = transformIndex(vectorIndex,componentIndex);
	return delegate.componentAt(trans[0],trans[1]);
    }// end componentAt(...)

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	final int [] trans = transformIndex(vectorIndex,componentIndex);
	delegate.setComponentAt(trans[0],trans[1], value);
    }// end setComponentAt(...)
    
    private int[] transformIndex(int vectorIndex, int componentIndex){
	final int sourceComponentsPerVector = delegate.getNumComponentsPerVector();
	final int blockID = vectorIndex;
	
	final int blockCol = blockID % blocksPerRow;
	final int blockRow = blockID / blocksPerRow;
	
	final int subComponentIndex = componentIndex % sourceComponentsPerVector;
	final int subVectorIndex = componentIndex/sourceComponentsPerVector;
	
	final int intraBlockX = subVectorIndex % blockWidthInVectors;
	final int intraBlockY = subVectorIndex / blockWidthInVectors;
	
	final int sourceX = blockCol * blockWidthInVectors + intraBlockX; 
	final int sourceY = (blockRow * blockWidthInVectors) + intraBlockY;
	
	final int sourceOffset = sourceX + sourceY * rasterWidthInVectors;
	
	return new int[] {sourceOffset,subComponentIndex};
	//return srcBlockOffset + intraBlockX + intraBlockY * rasterWidthInVectors;
    }//end transformIndex(...)
    */

    @Override
    public int[] getDimensions() {
	return dimensions;
    }

    @Override
    public double componentAt(int[] coord, int componentIndex) {
	coord = Arrays.copyOf(coord, coord.length);
	int cubeDecumulator = componentIndex / delegate.getNumComponentsPerVector();
	for(int dim=0; dim<coord.length; dim++){
	 coord[dim]=coord[dim]*blockWidthInVectors+cubeDecumulator%blockWidthInVectors;
	 cubeDecumulator/=blockWidthInVectors;
	 }
	componentIndex %= delegate.getNumComponentsPerVector();
	return delegate.componentAt(coord, componentIndex);
    }//end componentAt

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	throw new RuntimeException("Not implemented.");
    }
}// end RasterizedBlockVectorList
