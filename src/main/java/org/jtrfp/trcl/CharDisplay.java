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
import org.jtrfp.trcl.core.DummyTRFutureTask;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject2D;

public class CharDisplay extends WorldObject2D implements RelevantEverywhere {
    private final SelectableTexture tex;

    public CharDisplay(TR tr, RenderableSpacePartitioningGrid grid,
	    double glSize, GLFont font) {
	super(tr);
	final Model model = new Model(false, tr);
	tex = new SelectableTexture(font.getTextures());
	Triangle[] tris = Triangle.quad2Triangles(
		new double[] { 0., glSize, glSize, 0. },// x
		new double[] { 0., 0., glSize, glSize }, new double[] {
			0, 0, 0, 0 },

		new double[] { 0, 1, 1, 0 },// u
		new double[] { 0, 0, 1, 1 }, tex, RenderMode.DYNAMIC,
		Vector3D.ZERO, "CharDisplay");
	tris[0].setAlphaBlended(true);
	tris[1].setAlphaBlended(true);
	model.addTriangles(tris);
	setModel(model);
    }// end constructor()

    public void setChar(char c) {
	tex.setFrame(c);
	if(c==0)setVisible(false);
	else setVisible(true);
    }

    public SelectableTexture getSelectableTexture() {
	return tex;
    }

    public void setFontSize(double glSize) {
	// TODO Auto-generated method stub
	throw new RuntimeException("Not implemented.");
    }
}// end CharDisplay
