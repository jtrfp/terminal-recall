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
package org.jtrfp.trcl.math;

import java.nio.ByteBuffer;

public class Img {
    public static final double WEIGHT_RED=0.299;
    public static final double WEIGHT_BLUE=0.114;
    public static final double WEIGHT_GREEN=0.587;
    public static final double U_MAX=.436;
    public static final double V_MAX=.615;
    
    /**
     * https://en.wikipedia.org/wiki/YUV#Conversion_to.2Ffrom_RGB
     * @param rgba8888in
     * @param yOut
     * @param uOut
     * @param vOut
     * @param aOut
     * @since Mar 20, 2014
     */
    public static void rgba8888toyuva8(ByteBuffer rgba8888in, byte [] yOut, byte [] uOut, byte [] vOut, byte [] aOut){
	rgba8888in.clear();
	int pixIndex=0;
	while(rgba8888in.hasRemaining()){
	    final double r = (rgba8888in.get()&0xFF)-127;
	    final double g = (rgba8888in.get()&0xFF)-127;
	    final double b = (rgba8888in.get()&0xFF)-127;
	    final double a = (rgba8888in.get()&0xFF)-127;
	    
	    final double y = WEIGHT_RED*r + WEIGHT_GREEN*g + WEIGHT_BLUE*b;
	    final double u = .492*(b-y);
	    final double v = .877*(r-y);
	    
	    yOut[pixIndex]=(byte)Math.max(Math.min(Math.round(y),127),-127);
	    uOut[pixIndex]=(byte)Math.max(Math.min(Math.round(u),127),-127);
	    vOut[pixIndex]=(byte)Math.max(Math.min(Math.round(v),127),-127);
	    aOut[pixIndex]=(byte)Math.max(Math.min(Math.round(a),127),-127);
	    
	    pixIndex++;
	}//end for(pixelData)
    }//end rgba8888toyuva8888
    
    public static void yuva8torgba8888(ByteBuffer rgba8888out, byte [] yIn, byte [] uIn, byte [] vIn, byte [] aIn){
	rgba8888out.clear();
	int pixIndex=0;
	while(rgba8888out.hasRemaining()){
	    final double y = (yIn[pixIndex]);
	    final double u = (uIn[pixIndex]);
	    final double v = (vIn[pixIndex]);
	    
	    rgba8888out.put((byte)(y+(v/.877)+127));
	    rgba8888out.put((byte)(y-.395*u-.581*v+127));
	    rgba8888out.put((byte)(y+(u/.492)+127));
	    rgba8888out.put((byte)(aIn[pixIndex]+127));
	    
	    pixIndex++;
	}//end for(pixelData)
    }//end rgba8888toyuva8888
    
    public static void readRasterized8x8ByteBlockOf(byte [] buf, int offX, int offY, int rasterWidth, float [] dest){
	final int offXY = offX+offY*rasterWidth;
	for(int i=0; i< 64; i++){
	    dest[i]=(buf[offXY+i%8+(i/8)*rasterWidth]&0xFF)-127f;
	}//end for(64)
    }//end rasterized8x8ByteBlockOf(...)
    
    public static void writeRasterized8x8ByteBlockOf(byte [] buf, int offX, int offY, int rasterWidth, float [] src){
	final int offXY = offX+offY*rasterWidth;
	for(int i=0; i< 64; i++){
		buf[offXY+i%8+(i/8)*rasterWidth]=(byte)(src[i]+127);
	}//end for(64)
    }//end rasterized8x8ByteBlockOf(...)
}//end Img
