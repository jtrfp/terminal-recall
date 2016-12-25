/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola and contributors.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gpu;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.TextureBehavior;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.TriangleList;
import org.jtrfp.trcl.core.TriangleVertexWindow;

public abstract class DynamicTexture implements Texture {
    private final TextureBehavior.Support tbs = new TextureBehavior.Support();

    @Override
    @Deprecated
    public Color getAverageColor() {
	throw new UnsupportedOperationException();
    }

    @Override
    /**
     * @param triangleList
     * @param gpuTVIndex
     * @param numFrames
     * @param thisTriangle
     * @param pos
     * @param vw
     * @see org.jtrfp.trcl.TextureBehavior.Support#apply(org.jtrfp.trcl.TriangleList, int, int, org.jtrfp.trcl.Triangle, org.apache.commons.math3.geometry.euclidean.threed.Vector3D, org.jtrfp.trcl.core.TriangleVertexWindow)
     */
    public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames,
	    Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw) {
	tbs.apply(triangleList, gpuTVIndex, numFrames, thisTriangle, pos, vw);
    }

    @Override
    public void addBehavior(TextureBehavior beh) {
	tbs.addBehavior(beh);
    }

    @Override
    public void removeBehavior(TextureBehavior beh) {
	tbs.removeBehavior(beh);
    }
    
    //public abstract int getCurrentTexturePage();

    public abstract Texture getCurrentTexture();
    
}//end DynamicTexture
