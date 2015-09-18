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
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;

public class Sprite2D extends WorldObject2DVisibleEverywhere {
    private final TextureDescription texture;

    public Sprite2D(TR tr, double z, double width, double height, TextureDescription tex, boolean useAlpha) {
	super(tr);
	if(tex==null)
	    throw new NullPointerException("Supplied texture intolerably null.");
	this.texture=tex;
	final Model m = new Model(false,tr);
	Triangle [] tris = Triangle.quad2Triangles(
		new double[]{-width/2,width/2,width/2,-width/2}, 
		new double[]{-height/2,-height/2,height/2,height/2}, 
		new double[]{z,z,z,z},
		new double[]{0,1,1,0},
		new double[]{0,0,1,1}, tex, RenderMode.DYNAMIC, useAlpha,Vector3D.ZERO,"Sprite2D non-segmented");
	m.addTriangles(tris);
	setModel(m);
	setTop(Vector3D.PLUS_J);
	setActive(true);
	setVisible(true);
    }//end constructor
    
    public Sprite2D(TR tr, double z, double width, double height, TextureDescription [] tex, boolean useAlpha){
	super(tr);
	if(tex==null)
	    throw new NullPointerException("Supplied texture intolerably null.");
	this.texture=tex[0];
	final Model m = new Model(false,tr);
	final int numSegs = tex.length;
	for (int seg = 0; seg < numSegs; seg++) {
	    final double segWidth = width / numSegs;
	    final double x = (-width/2) + segWidth * seg;
	    
	    Triangle[] tris = Triangle.quad2Triangles(new double[] { x,
		    x + segWidth, x + segWidth, x }, new double[] { -height/2, -height/2,
		    height/2, height/2 }, new double[] { z, z, z, z }, new double[] { 0, 1,
		    1, 0 }, new double[] { 0, 0, 1, 1 }, tex[seg],
		    RenderMode.DYNAMIC, Vector3D.ZERO,"Sprite2D "+numSegs+" segments");
	    tris[0].setAlphaBlended(true);
	    tris[1].setAlphaBlended(true);
	    m.addTriangles(tris);
	}// end for(segs)
	setModel(m);
	setTop(Vector3D.PLUS_J);
	setHeading(Vector3D.PLUS_K);
	setActive(true);
	setVisible(true);
    }//end constructor

    /**
     * @return the texture
     */
    public TextureDescription getTexture() {
        return texture;
    }
}//end Sprite2D
