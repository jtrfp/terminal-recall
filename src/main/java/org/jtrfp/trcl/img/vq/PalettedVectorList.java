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

public final class PalettedVectorList implements VectorList {
    private final int numVectors;
    private final VectorList palette;
    private final VectorList source;

    public PalettedVectorList(VectorList source,
	    final VectorList palette) {
	this.numVectors=source.getNumVectors();
	this.palette=palette;
	this.source=source;
    }

    @Override
    public int getNumVectors() {
	return numVectors;
    }

    @Override
    public int getNumComponentsPerVector() {
	return palette.getNumComponentsPerVector();
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return palette.componentAt((int)source.componentAt(vectorIndex, 0),componentIndex);
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	throw new RuntimeException("Not Implemented.");
    }
}//end PalettedVectorList
