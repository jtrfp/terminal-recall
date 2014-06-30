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

import java.awt.Color;


public final class ColorPaletteVectorList implements VectorList {
    private final double [][] palette;
    
    public ColorPaletteVectorList(Color [] colors){
	palette 	 = new double[colors.length][];
	for(int i=0; i<colors.length; i++){
	    palette[i]	 =new double[4];
	    palette[i][0]=(double)colors[i].getRed()/255.;
	    palette[i][1]=(double)colors[i].getGreen()/255.;
	    palette[i][2]=(double)colors[i].getBlue()/255.;
	    palette[i][3]=(double)colors[i].getAlpha()/255.;
	}//end for(colors)
    }//end constructor

    @Override
    public int getNumVectors() {
	return palette.length;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 4;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return palette[vectorIndex][componentIndex];
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	palette[vectorIndex][componentIndex]=value;
    }

}//end ColorPaletteVectorList
