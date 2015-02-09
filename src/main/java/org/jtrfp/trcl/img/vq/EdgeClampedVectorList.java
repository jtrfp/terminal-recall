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

package org.jtrfp.trcl.img.vq;

import java.util.Arrays;

public class EdgeClampedVectorList implements VectorListND {
    private final VectorListND delegate;
    final int [] virtualDimensions;
    final int numVectors;
    
    public EdgeClampedVectorList(VectorListND delegate, int [] virtualDimensions){
	this.delegate         =delegate;
	this.virtualDimensions=virtualDimensions;
	int accumulator=1;
	for(int d:getDimensions())
	    accumulator*=d;
	numVectors=accumulator;
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
    public int[] getDimensions() {
	return virtualDimensions;
    }

    @Override
    public double componentAt(int[] coordinates, int componentIndex) {
	final int [] newCoords = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<newCoords.length; i++)
	    newCoords[i]=Math.max(newCoords[i], virtualDimensions[i]-1);
	return delegate.componentAt(newCoords, componentIndex);
    }

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	final int [] newCoords = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<newCoords.length; i++)
	    newCoords[i]=Math.max(newCoords[i], virtualDimensions[i]-1);
	delegate.setComponentAt(newCoords, componentIndex,value);
    }//end setComponentAt(...)

}//End EdgeClampedVectorList
