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

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject2D;

public class Crosshairs extends WorldObject2D
	{
	private static final double Z = 0;
	public Crosshairs(TR tr)
		{
		super(tr);
		//Crosshairs
		Model crossModel = new Model(false);
		Texture [] greenThrobFrames = new Texture[16];
		for(int f=0; f<8; f++)
			{greenThrobFrames[f]=greenThrobFrames[15-f]=(Texture)Texture.solidColor(new Color(f*22,f*32,f*23,170));}
		AnimatedTexture greenThrob = new AnimatedTexture(new Sequencer(20,greenThrobFrames.length,false), greenThrobFrames);
		final double xhairScale=.80;
		final double xhairThick=.005*xhairScale;
		final double xhairLen=.015*xhairScale;
		crossModel.addTriangles(Triangle.quad2Triangles(//Horiz
				new double[]{-xhairLen,xhairLen,xhairLen,-xhairLen}, new double[]{xhairThick,xhairThick,-xhairThick,-xhairThick}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1}, greenThrob, RenderMode.DYNAMIC));
		crossModel.addTriangles(Triangle.quad2Triangles(//Vert
				new double[]{-xhairThick,xhairThick,xhairThick,-xhairThick}, new double[]{xhairLen,xhairLen,-xhairLen,-xhairLen}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1},greenThrob, RenderMode.DYNAMIC));
		crossModel.addTriangles(Triangle.quad2Triangles(//Top
				new double[]{-xhairThick*3,xhairThick*3,xhairThick*3,-xhairThick*3}, new double[]{xhairLen+xhairThick*6,xhairLen+xhairThick*6,xhairLen,xhairLen}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1},greenThrob, RenderMode.DYNAMIC));
		crossModel.addTriangles(Triangle.quad2Triangles(//Bottom
				new double[]{-xhairThick*3,xhairThick*3,xhairThick*3,-xhairThick*3}, new double[]{-(xhairLen+xhairThick*6),-(xhairLen+xhairThick*6),-(xhairLen),-(xhairLen)}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1},greenThrob, RenderMode.DYNAMIC));
		crossModel.addTriangles(Triangle.quad2Triangles(//Left
				new double[]{-(xhairLen+xhairThick*6),-(xhairLen+xhairThick*6),-(xhairLen),-(xhairLen)}, new double[]{-xhairThick*3,xhairThick*3,xhairThick*3,-xhairThick*3}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1},greenThrob, RenderMode.DYNAMIC));
		crossModel.addTriangles(Triangle.quad2Triangles(//Right
				new double[]{(xhairLen+xhairThick*6),(xhairLen+xhairThick*6),(xhairLen),(xhairLen)}, new double[]{-xhairThick*3,xhairThick*3,xhairThick*3,-xhairThick*3}, new double[]{Z,Z,Z,Z}, 
				new double[]{0,1,1,0}, new double[]{0,0,1,1},greenThrob, RenderMode.DYNAMIC));
		setPosition(Vector3D.ZERO);
		setModel(crossModel.finalizeModel());
		}
	}
