/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.img.vq;

import java.awt.image.BufferedImage;

public final class BufferedImageRGBA8888VL implements VectorList {
    private final int width,height;
    private final BufferedImage image;
    
    public BufferedImageRGBA8888VL(BufferedImage image){
	width=image.getWidth();
	height=image.getHeight();
	this.image=image;
    }

    @Override
    public int getNumVectors() {
	return width*height;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 4;
    }
    
    private static final int [] MASKS = new int[]{
	0x00FF0000,
	0x0000FF00,
	0x000000FF,
	0xFF000000
    };
    
    private static final int [] SHIFTS = {16,8,0,24};

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	int x = vectorIndex % width;
	int y = vectorIndex / width;
	return ((image.getRGB(x, y) & MASKS[componentIndex]) >> SHIFTS[componentIndex] & 0xFF)/255.;
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	throw new RuntimeException("Not implemented.");
    }

}
