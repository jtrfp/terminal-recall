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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.mem.MemoryWindow;

public final class MatrixWindow extends MemoryWindow {
    public static final int BYTES_PER_MATRIX = 4 * 16; // 16 floats
    public final Double2FloatArrayVariable matrix = new Double2FloatArrayVariable(
	    16);

    public MatrixWindow(TR tr) {
	init(tr,"MatrixWindow");
    }
    
    private final double [] newVals = new double[16];
    public final void setTransposed(double[] vals, int id) {
	//double[] newVals = new double[16];
	for (int index = 0; index < 16; index++) {
	    newVals[index] = (float) vals[(index / 4) + (index % 4) * 4];
	}// end for(16)
	matrix.set(id, newVals);
    }
}// end Matrix
