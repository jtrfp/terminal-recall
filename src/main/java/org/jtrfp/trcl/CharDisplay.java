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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.objects.WorldObject2D;

public class CharDisplay extends WorldObject2D
	{
	//private GLFont font;
	private SelectableTexture tex;
	
	public CharDisplay(TR tr, RenderableSpacePartitioningGrid grid, double glSize, GLFont font)
		{
		super(tr);
		//this.font=font;
		final Model model = new Model(false);
		tex=new SelectableTexture(font.getTextures());
		Triangle [] tris = Triangle.quad2Triangles(
				new double []{0.,glSize,glSize,0.},//x
				new double []{0.,0.,glSize,glSize},
				new double []{.000001,.000001,.000001,.000001},
				
				new double []{0,1,1,0},//u
				new double []{0,0,1,1},
				tex, RenderMode.DYNAMIC);//TODO: tex instead of fallback
		tris[0].setAlphaBlended(true);
		tris[1].setAlphaBlended(true);
		model.addTriangles(tris);
		setModel(model.finalizeModel());
		setPosition(Vector3D.ZERO);
		}//end constructor()
	
	public void setChar(char c){tex.setFrame(c);}
	}//end CharDisplay
