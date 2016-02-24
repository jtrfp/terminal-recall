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

import org.jtrfp.trcl.file.CLRFile;
import org.jtrfp.trcl.gpu.TextureDescription;

public class RawTextureMeshWrapper implements TextureMesh {
    private static final int WIDTH = 256;
    CLRFile file;
    TextureDescription[] palette;

    // Texture [][] textures = new Texture[WIDTH][WIDTH];

    public RawTextureMeshWrapper(CLRFile f,
	    TextureDescription[] texturePalette) {
	file = f;
	palette = texturePalette;
    }

    @Override
    public TextureDescription textureAt(double x, double z) {
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
    }//end textureAt(...)

}//end RawTextureMeshWrapper
