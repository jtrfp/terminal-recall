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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject2DVisibleEverywhere;

public class Dashboard extends WorldObject2DVisibleEverywhere {
    private static final double Z = .5;

    public Dashboard(TR tr) {
	super(tr);
	setImmuneToOpaqueDepthTest(true);
	try{
	// Dashboard
	TextureDescription[] dashTexture = tr.getResourceManager()
		.getSpecialRAWAsTextures("STATBAR.RAW", tr.getGlobalPalette(),
			tr.gpu.get().getGl(), 2,false);
	Model dashModel = new Model(false, tr);
	for (int seg = 0; seg < 5; seg++) {
	    final double segWidth = 2. / 5.;
	    final double x = -1 + segWidth * seg;

	    Triangle[] tris = Triangle.quad2Triangles(new double[] { x,
		    x + segWidth, x + segWidth, x }, new double[] { .36, .36,
		    1, 1 }, new double[] { Z, Z, Z, Z }, new double[] { 0, 1,
		    1, 0 }, new double[] { 0, 0, 1, 1 }, dashTexture[seg],
		    RenderMode.DYNAMIC, Vector3D.ZERO,"Dashboard");
	    tris[0].setAlphaBlended(true);
	    tris[1].setAlphaBlended(true);
	    dashModel.addTriangles(tris);
	}// end for(segs)
	setModel(dashModel.finalizeModel());
	}catch(Exception e){tr.showStopper(e);}
    }
}// end Dashboard
