/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
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

package org.jtrfp.trcl.img.vq;

public final class CodebookTileTableVL implements VectorList {
    private final int [] tileTable;
    
    public CodebookTileTableVL(int [] tileTable){
	this.tileTable =tileTable;
    }

    @Override
    public int getNumVectors() {
	return tileTable.length*256;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 1;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return tileTable[vectorIndex/256]
		+ vectorIndex%256;
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	throw new RuntimeException("CodebookTileTableVL is immutable.");
    }

    public int[] getTileTable() {
        return tileTable;
    }

}//end CodebookTileTableVL
