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

package org.jtrfp.trcl.math;

import org.jtrfp.trcl.gpu.TranslationMatrixFactory;

public class ColumnMajorTranslationMatrixFactory implements
	TranslationMatrixFactory {

    @Override
    public void applyTranslationMatrix(double[] pos, double[] dest) {
	final double x=pos[0], y=pos[1], z=pos[2];
	final double x3 = dest[3];
	dest[0]+=x3*x;
	dest[1]+=x3*y;
	dest[2]+=x3*z;
	
	final double y3 = dest[7];
	dest[4]+=y3*x;
	dest[5]+=y3*y;
	dest[6]+=y3*z;
	
	final double z3 = dest[11];
	dest[8] +=z3*x;
	dest[9] +=z3*y;
	dest[10]+=z3*z;
	
	final double w3 = dest[15];
	dest[12]+=w3*x;
	dest[13]+=w3*y;
	dest[14]+=w3*z;
    }//end applyTranslationMatrix(...)

}//end ColumnMajorTranslationMatrixFactory
