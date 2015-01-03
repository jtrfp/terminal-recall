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

package org.jtrfp.trcl.prop;

import java.awt.Color;
import java.nio.ByteBuffer;

public class HorizGradientCubeGen implements SkyCubeGen {
    private ByteBuffer top,bottom,east,west,north,south;
    private final Color topColor,bottomColor;

    public HorizGradientCubeGen(Color bottomColor, Color topColor) {
	this.topColor=topColor;
	this.bottomColor=bottomColor;
    }

    @Override
    public ByteBuffer getTop() {
	return top = ensureGrad(top,topColor,topColor);
    }

    @Override
    public ByteBuffer getBottom() {
	return bottom = ensureGrad(bottom,bottomColor,bottomColor);
    }

    @Override
    public ByteBuffer getNorth() {
	return north = ensureGrad(north,bottomColor,topColor);
    }

    @Override
    public ByteBuffer getSouth() {
	return south = ensureGrad(south,bottomColor,topColor);
    }

    @Override
    public ByteBuffer getWest() {
	return west = ensureGrad(west,bottomColor,topColor);
    }

    @Override
    public ByteBuffer getEast() {
	return east = ensureGrad(east,bottomColor,topColor);
    }
    
    private ByteBuffer ensureGrad(ByteBuffer bb, Color low, Color hi){
	if(bb==null){
	    bb = ByteBuffer.allocate(32*32*4);
	    bb.clear();
	    for(int y=0; y<32; y++){
		final float lW = ((float)y)/32f;
		final float hW = 1f-lW;
		final Color c = new Color(
			(low.getRed()*lW+hi.getRed()*hW)/256f,
			(low.getGreen()*lW+hi.getGreen()*hW)/256f,
			(low.getBlue()*lW+hi.getBlue()*hW)/256f,
			(low.getAlpha()*lW+hi.getAlpha()*hW)/256f);
		final byte r = (byte)c.getRed();
		final byte g = (byte)c.getGreen();
		final byte b = (byte)c.getBlue();
		final byte a = (byte)c.getAlpha();
		for(int x=0; x<32; x++)
		    {bb.put(r);bb.put(g);bb.put(b);bb.put((byte)1);}
	    }//end for(y)
	}//end if(null)
	bb.clear();
	return bb;
    }//end ensureGrad(...)
    
    @Override
    public int hashCode(){
	return this.getClass().hashCode()
		+topColor.getRed()*1
		+topColor.getGreen()*256
		+topColor.getBlue()*1024
		+topColor.getAlpha()*65535
		+65535*256*(
		 bottomColor.getRed()*1
		+bottomColor.getGreen()*256
		+bottomColor.getBlue()*1024
		+bottomColor.getAlpha()*65535);
    }
}//end HorizGradientCubeGen
