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

import org.jtrfp.trcl.gpu.SubTextureWindow;

public final class SubtextureVL implements VectorList {
    private final SubTextureWindow stw;
    private final int objectIndex;
    
    public SubtextureVL(final SubTextureWindow stw, final int objectIndex){
	this.stw=stw;
	this.objectIndex=objectIndex;
    }//end constructor
    @Override
    public int getNumVectors() {
	return SubTextureWindow.BYTES_PER_SUBTEXTURE;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 1;
    }
    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	throw new RuntimeException("Not implemented.");
    }
    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	stw.codeIDs.setAt(objectIndex, vectorIndex, (byte)value);
    }
}//end SubtextureVL
