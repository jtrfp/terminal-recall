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
package org.jtrfp.trcl;

import java.awt.Dimension;

public class SpecialRAWDimensions {
    /**
     * 
     * @param len Un-rasterized length of the image in pixels as if they were one long string.
     * @return The dimensions of the image.
     * @since May 10, 2013
     */
    public static Dimension getSpecialDimensions(int len) {
	int xLen = (int) Math.sqrt(len), yLen = (int) Math.sqrt(len);
	if (!isPowerOfTwo(xLen)) {// Non power of two
	    for(SpecialDimensions d:SpecialDimensions.values()){
		final Dimension result = d.dims(len);
		if(result!=null)return result;
	    }
	}//end if(non power of two)
	return new Dimension((int) xLen, (int) yLen);
    }// end getSpecialDimensions
    
    private enum SpecialDimensions{
	topBar(320,64),
	fullScreen(320,200);
	
	private final int len;
	private final Dimension dims;
	SpecialDimensions(int w, int h){
	    len=w*h;
	    dims=new Dimension(w,h);
	}
	public Dimension dims(int unrasterizedLength){
	    return unrasterizedLength==len?dims:null;
	}
    }//end SpecialDimensions

    public static boolean hasIntegralSquareRoot(int val) {
	double sqt = Math.sqrt(val);
	return Math.abs(sqt - Math.round(sqt)) < .000001;
    }

    public static boolean isPowerOfTwo(int val) {
	return (val & (val - 1)) == 0;
    }
}//end SpecialRAWDimensions
