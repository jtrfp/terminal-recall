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
import org.jtrfp.trcl.objects.PositionedRenderable;

public abstract class RenderableSpacePartitioningGrid extends SpacePartitioningGrid<PositionedRenderable>
	{
	
	public RenderableSpacePartitioningGrid(double sizeX, double sizeY, double sizeZ, double gridBlockSize, double viewDepth)
		{
		super(new Vector3D(sizeX,sizeY,sizeZ),gridBlockSize,viewDepth*1.2);
		activate();
		}
	
	public RenderableSpacePartitioningGrid(SpacePartitioningGrid<PositionedRenderable>parent)
		{
		super(parent);
		}
	}
