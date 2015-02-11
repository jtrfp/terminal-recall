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


public class ConstantVectorList implements VectorList {
    private final double constant;
    private final int    numVectors;
    private final int    numComponents;
    
    public ConstantVectorList(double constant, int numVectors, int numComponents){
	this.constant     =constant;
	this.numVectors   =numVectors;
	this.numComponents=numComponents;
    }
    
    public ConstantVectorList(double constant, VectorList copyTraitsFrom){
	this.numVectors   =copyTraitsFrom.getNumVectors();
	this.numComponents=copyTraitsFrom.getNumComponentsPerVector();
	this.constant     =constant;
    }

    @Override
    public int getNumVectors() {
	return numVectors;
    }

    @Override
    public int getNumComponentsPerVector() {
	return numComponents;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return constant;
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	throw new RuntimeException("Not Implemented.");
    }

}//end ConstantVectorList
