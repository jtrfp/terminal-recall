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

public final class VectorListRasterizer implements VectorListND {
    private final VectorList intrinsic;
    private final int [] dimensions;
    
    public VectorListRasterizer(VectorList intrinsic, int [] dimensions){
	this.intrinsic = intrinsic;
	this.dimensions = Arrays.copyOf(dimensions, dimensions.length);
    }

    @Override
    public int getNumVectors() {
	return intrinsic.getNumVectors();
    }

    @Override
    public int getNumComponentsPerVector() {
	return intrinsic.getNumComponentsPerVector();
    }

    @Override
    public int[] getDimensions() {
	return dimensions;
    }

    @Override
    public double componentAt(int[] coordinates, int componentIndex) {
	return intrinsic.componentAt(coord1D(coordinates), componentIndex);
    }//end componentAt(...)

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex, double value) {
	intrinsic.setComponentAt(coord1D(coordinates), componentIndex, value);
    }//setComponentAt(...)
    
    private int coord1D(int[] coordinates){
	int coord1D=0;
	int infinity=1;
	for(int d=0; d<dimensions.length; d++){
	    coord1D+=coordinates[d]*infinity;
	    infinity*=dimensions[d];
	}
	return coord1D;
    }//end coord1D(...)
}//end VectorListRasterizer
