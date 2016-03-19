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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combines one or more VectorListNDs as a single VectorListND view by rasterizing the lists 
 * themselves in a left-to-right-then-top-down order. All sublists are assumed to be of the same dimensions.
 * Differently-sized sublists will result in clipping if too large or exception if too small.
 * Differently-dimensioned sublists will result in exceptions.
 * @author Chuck Ritola
 *
 */

public final class CompositeVectorListND implements VectorListND {
    private final List<VectorListND> subLists = new ArrayList<VectorListND>();
    private int [] dimensions;

    @Override
    public int getNumVectors() {
	int accumulator = 1;
	for(int d:getDimensions())
	    accumulator *=d;
	return accumulator;
    }

    @Override
    public int getNumComponentsPerVector() {
	final List<VectorListND> subLists = getSubLists();
	if(subLists.isEmpty())
	    throw new IllegalStateException("Component count cannot be determined because there are no sublists present.");
	return subLists.get(0).getNumComponentsPerVector();
    }
    
    private VectorListND subListAt(int [] coordinates){
	final int [] slSize       = getSubListSize();
	final int [] dimsSublists = getDimsInSublists();
	int index = 0;
	int multiplier = 1;
	final int [] slCoord = coordToSublist(coordinates);
	for(int cI = 0; cI < slSize.length; cI++){
	    index += multiplier*slCoord[cI];
	    multiplier*=dimsSublists[cI];
	}
	return getSubLists().get(index);
    }//end subListAt()
    
    public int [] coordToSublist(int [] coord){
	final int [] sls = getSubListSize();
	final int [] result = Arrays.copyOf(coord, coord.length);
	for(int i=0; i<result.length; i++)
	    result[i] /= sls[i];
	return result;
    }
    
    public int [] getDimsInSublists(){
	final int [] dims   = getDimensions();
	final int [] result = new int[dims.length];
	final int [] sls = getSubListSize();
	for(int i=0; i<dims.length; i++)
	    result[i] = (int)Math.ceil((double)dims[i]/(double)sls[i]);
	return result;
    }

    @Override
    public double componentAt(int[] coordinates, int componentIndex) {
	final int [] slSize       = getSubListSize();
	final VectorListND vl = subListAt(coordinates);
	final int [] newCoords = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<coordinates.length; i++)
	    newCoords[i]%=slSize[i];
	return vl.componentAt(newCoords, componentIndex);
    }

    @Override
    public void setComponentAt(int[] coordinates, int componentIndex,
	    double value) {
	final int [] slSize       = getSubListSize();
	final VectorListND vl = subListAt(coordinates);
	final int [] buffer = Arrays.copyOf(coordinates, coordinates.length);
	for(int i=0; i<coordinates.length; i++)
	    buffer[i]%=slSize[i];
	vl.setComponentAt(buffer, componentIndex, value);
    }
    
    public int [] getSubListSize(){
	final List<VectorListND> subLists = getSubLists();
	if(subLists.isEmpty())
	    throw new IllegalStateException("Component count cannot be determined because there are no sublists present.");
	return subLists.get(0).getDimensions();
    }

    public List<VectorListND> getSubLists() {
        return subLists;
    }

    @Override
    public int[] getDimensions() {
        return dimensions;
    }

    public void setDimensions(int[] totalSize) {
        this.dimensions = totalSize;
    }
}//end CompositeVectorListND
