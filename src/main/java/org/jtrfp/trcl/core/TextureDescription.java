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
package org.jtrfp.trcl.core;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.TextureBehavior;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.TriangleList;

public interface TextureDescription {
    public Color getAverageColor();

    public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames, Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw);
    public void addBehavior(TextureBehavior beh);
    public void removeBehavior(TextureBehavior beh);
}//end TextureDescription
