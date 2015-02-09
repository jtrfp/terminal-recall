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

public final class SubVectorList implements VectorListND {
    private final VectorListND delegate;
    private int             [] translate;
    private int		    [] dimensions;
    
    public SubVectorList(VectorListND delegate, int [] translate, int [] dimensions){
	this.delegate  =delegate;
	this.dimensions=dimensions;
	this.translate =translate;
    }

    @Override
    public int getNumVectors() {
	int accumulator = 1;
	for (int d : dimensions)
	    accumulator *= d;
	return accumulator;
    }

    @Override
    public int getNumComponentsPerVector() {
	return delegate.getNumComponentsPerVector();
    }

    @Override
    public int[] getDimensions() {
	return dimensions;
    }

    @Override
    public double componentAt(int[] coordinates, int componentIndex) {
	final int[] translated = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<translated.length;i++)
	    translated[i]+=translate[i];
	return delegate.componentAt(translated, componentIndex);
    }//end componentAt(...)

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	final int[] translated = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<translated.length;i++)
	    translated[i]+=translate[i];
	delegate.setComponentAt(translated, componentIndex,value);
    }//end setComponentAt(...)

}//end CoordTranslatingVectorListND
