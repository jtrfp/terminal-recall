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

public final class RasterizedBlockVectorList implements VectorList {
    private final VectorList 	rasterizedVectorList;
    private final int 		rasterWidthInVectors, 
    /*			*/	blockWidthInVectors,
    /*			*/	vectorsPerBlock,
    /*			*/	blocksPerRow;

    public RasterizedBlockVectorList(VectorList rasterizedVectorList,
	    int rasterWidthInVectors, int blockWidthInVectors) {
	this.rasterWidthInVectors 	= rasterWidthInVectors;
	this.blockWidthInVectors 	= blockWidthInVectors;
	this.rasterizedVectorList 	= rasterizedVectorList;
	this.vectorsPerBlock 		= blockWidthInVectors * blockWidthInVectors;
	this.blocksPerRow 		= rasterWidthInVectors / blockWidthInVectors;
    }// end constructor

    @Override
    public int getNumVectors() {
	return rasterizedVectorList.getNumVectors() / vectorsPerBlock;
    }

    @Override
    public int getNumComponentsPerVector() {
	return rasterizedVectorList.getNumComponentsPerVector()
		* vectorsPerBlock;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	//System.out.println("RasterizedBlockVectorList.componentAt() vectorIndex="+vectorIndex+" componentIndex="+componentIndex);
	final int [] trans = transformIndex(vectorIndex,componentIndex);
	return rasterizedVectorList.componentAt(trans[0],
		trans[1]);
    }// end componentAt(...)

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	final int [] trans = transformIndex(vectorIndex,componentIndex);
	rasterizedVectorList.setComponentAt(trans[0],
		trans[1], value);
    }// end setComponentAt(...)
    
    private int[] transformIndex(int vectorIndex, int componentIndex){
	final int sourceComponentsPerVector = rasterizedVectorList.getNumComponentsPerVector();
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
    
}// end RasterizedBlockVectorList
