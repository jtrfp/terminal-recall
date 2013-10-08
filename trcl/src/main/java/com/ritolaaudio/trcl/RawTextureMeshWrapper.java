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
package com.ritolaaudio.trcl;

import com.ritolaaudio.trcl.file.CLRFile;

public class RawTextureMeshWrapper implements TextureMesh
	{
	private static final int WIDTH=256;
	CLRFile file;
	TextureDescription [] palette;
	//Texture [][] textures = new Texture[WIDTH][WIDTH];
	
	public RawTextureMeshWrapper(CLRFile f, TextureDescription[] texturePalette)
		{
		file=f;
		palette=texturePalette;
		}
	@Override
	public TextureDescription textureAt(double x, double z)
		{
		if(x<0)x+=WIDTH;
		if(z<0)z+=WIDTH;
		return palette[file.valueAt((int)z%WIDTH, (int)x%WIDTH)];
		}

	}
