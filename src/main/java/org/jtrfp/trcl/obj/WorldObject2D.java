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
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.core.TR;


/**
 * A WorldObject which exists flat on the screen, immune to camera position or perspective effects.
 * Typically used for GUIs, messages, and HUDs.
 * @author Chuck Ritola
 *
 */
public class WorldObject2D extends WorldObject
	{
	public WorldObject2D(TR tr){super(tr);}
	public WorldObject2D(TR tr, Model m)
		{
		super(tr, m);
		}//end WorldObject2D
	
	@Override
	protected void recalculateTransRotMBuffer()
		{
		final Vector3D tV = position;
		
		RealMatrix tM = new Array2DRowRealMatrix(new double [][] 
					{
					new double[]{1,0,	0,	tV.getX()},
					new double[]{0,1,	0,	tV.getY()},
					new double[]{0,0,	1,	tV.getZ()},
					new double[]{0,0,	0,	1}
					});
		matrix.set(tM.transpose());
		}//end recalculateTransRotMBuffer()
	}//end WorldObject2D
