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

import java.util.concurrent.Future;

import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.CLRFile;

public class RawTextureMeshWrapper implements TextureMesh {
    private static final int WIDTH = 256;
    CLRFile file;
    Future<TextureDescription>[] palette;

    // Texture [][] textures = new Texture[WIDTH][WIDTH];

    public RawTextureMeshWrapper(CLRFile f,
	    Future<TextureDescription>[] texturePalette) {
	file = f;
	palette = texturePalette;
    }

    @Override
    public Future<TextureDescription> textureAt(double x, double z) {
	if (x < 0)
	    x += WIDTH;
	if (z < 0)
	    z += WIDTH;
	try {
	    return palette[file.valueAt((int) z % WIDTH, (int) x % WIDTH)];
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

}
