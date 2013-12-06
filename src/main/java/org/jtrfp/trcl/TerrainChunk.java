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

import org.jtrfp.trcl.objects.WorldObject;


/**
 * 3-dimensional chunk of unchanging triangles.
 * @author Chuck Ritola
 *
 */
public class TerrainChunk extends WorldObject
	{
	public TerrainChunk(World w, Model m)
		{
		super(w,m);
		m.setDebugName("TerrainChunk");
		}
	}//end RenderingCube
