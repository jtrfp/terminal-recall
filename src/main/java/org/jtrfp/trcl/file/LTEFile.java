/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.file;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class LTEFile implements ThirdPartyParseable {
    private byte [] lteData;
    @Override
    public void describeFormat(Parser parser)
	    throws UnrecognizedFormatException {
	parser.bytesOfCount(4096, parser.property("lteData", byte[].class));
    }//end describeFormat(...)
    /**
     * @return the lteData
     */
    public byte[] getLteData() {
        return lteData;
    }
    /**
     * @param lteData the lteData to set
     */
    public void setLteData(byte[] lteData) {
        this.lteData = lteData;
    }
    
    public int gradientIndex(int colorIndex, int lightLevel){
	if(lightLevel<0 || lightLevel>15)
	    throw new IllegalArgumentException("Lightlevel should be in range [0,15]. Got "+lightLevel);
	if(colorIndex<0 || colorIndex>255){
	    throw new IllegalArgumentException("ColorIndex should be in range [0,255]. Got "+colorIndex);
	}
	return lteData[colorIndex*16+lightLevel]&0xFF;
    }//end gradientIndex(...)
    /**
     * Reverse-compatible form of the emissive components in ESTuTv form where:<br>
     * R = E<br>
     * G = S<br>
     * B = Tu<br>
     * A = Tv<br>
     * @return
     * @since Feb 5, 2015
     */
    public Color[] toColors(Color [] referencePalette) {
	Color[] result = new Color[256];
	for(int i=0; i<256; i++){
	    final Color rColor = referencePalette[i];
	    final Color loColor = referencePalette[gradientIndex(i,0)];
	    final Color hiColor = referencePalette[gradientIndex(i,15)];
	    final double loIntensity = new Vector3D(loColor.getRed(),loColor.getGreen(),loColor.getBlue()).getNorm();
	    //Brightest
	    final double hiIntensity = new Vector3D(hiColor.getRed(),hiColor.getGreen(),hiColor.getBlue()).getNorm();
	    
	    final float []hsbVals = new float[3];
	    Color.RGBtoHSB(rColor.getRed(), rColor.getGreen(), rColor.getBlue(), hsbVals);
	    final double saturation = hsbVals[1];
	    double dI = Math.pow(1-(Math.abs(hiIntensity-loIntensity)/221.),1);
	    //dI = nSaturation*128;
	    if(dI>255)dI=255;
	    else if(dI<0)dI=0;
	    result[i]=new Color((int)(dI*saturation*255.),0,0,0);
	    }
	return result;
    }//end toColors()
}//end LTEFile
