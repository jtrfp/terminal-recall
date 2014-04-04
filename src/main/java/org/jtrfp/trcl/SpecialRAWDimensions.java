/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.Dimension;

public class SpecialRAWDimensions {
    /**
     * 
     * @param raw
     * @return The dimensions of the image, or null if it is a standard square
     *         power of two.
     * @since May 10, 2013
     */
    public static Dimension getSpecialDimensions(int len) {
	int xLen = (int) Math.sqrt(len), yLen = (int) Math.sqrt(len);
	if ((xLen & (xLen - 1)) != 0) {// Non power of two
	    if (len == 20480) {
		xLen = 320;
		yLen = 64;
	    }
	    if (len == 190512) {
		xLen = 320;
		yLen = 200;
	    }
	}
	return new Dimension((int) xLen, (int) yLen);
    }// end getSpecialDimensions

    public static boolean hasIntegralSquareRoot(int val) {
	double sqt = Math.sqrt(val);
	return Math.abs(sqt - Math.round(sqt)) < .000001;
    }

    public static boolean isPowerOfTwo(int val) {
	return (val & (val - 1)) == 0;
    }
}
