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
package org.jtrfp.trcl.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.trcl.SpecialRAWDimensions;

public class RAWFile extends SelfParsingFile {
    byte[] 			rawBytes;
    private BufferedImage[] 	segImg;
    private double 		scaleWidth = 1, scaleHeight = 1;
    private Dimension 		dims;
    private int 		segWidth, segHeight, numSegs = -1;
    private Color[] 		palette;

    SegmentDirection 		dir;

    private enum SegmentDirection {
	VERTICAL, HORIZONTAL
    }

    public RAWFile(InputStream inputStream) throws IllegalAccessException,
	    IOException {
	super(inputStream);
    }

    @Override
    public void describeFormat(Parser p) throws UnrecognizedFormatException {
	p.bytesEndingWith(null, p.property("rawBytes", byte[].class), false);
    }

    /**
     * @return Raw bytes representing this RAW file, 256x256x1B rasterized.
     */
    public byte[] getRawBytes() {
	return rawBytes;
    }

    /**
     * @param rawBytes
     *            Raw bytes representing this RAW file, 256x256x1B rasterized.
     */
    public void setRawBytes(byte[] rawBytes) {
	this.rawBytes = rawBytes;
    }

    public int getSideLength() {
	return (int) Math.sqrt(rawBytes.length);
    }

    public int valueAt(int x, int y) {
	return (int) rawBytes[x + (y * getSideLength())] & 0xFF;
    }

    public void setPalette(Color[] palette) {
	if (palette == null)
	    throw new NullPointerException("Palette must be non-null.");
	this.palette = palette;
    }

    public BufferedImage[] asSegments(int upScalePowerOfTwo) {
	if (palette == null)
	    throw new RuntimeException(
		    "Palette property must be set before extracting Image.");
	if (segImg == null) {
	    System.out.println("Raw len: " + getRawBytes().length);
	    dims = SpecialRAWDimensions
		    .getSpecialDimensions(getRawBytes().length);
	    System.out.println("Special dims: " + dims);
	    int lesserDim, greaterDim;
	    if (dims.getHeight() > dims.getWidth()) {
		dir = SegmentDirection.VERTICAL;
		lesserDim = (int) dims.getWidth();
		greaterDim = (int) dims.getHeight();
	    } else {
		dir = SegmentDirection.HORIZONTAL;
		lesserDim = (int) dims.getHeight();
		greaterDim = (int) dims.getWidth();
	    }
	    System.out.println("Raw dims: "+dims.getWidth()+"x"+dims.getHeight());
	    int newLDim = lesserDim, newGDim = greaterDim;
	    // If non-square
	    if (lesserDim != greaterDim) {
		System.out.println("Detected as non-square.");
		if (!SpecialRAWDimensions.isPowerOfTwo(lesserDim)) {
		    newLDim = nextPowerOfTwo(lesserDim);
		    System.out.println("Lesser dim is non-power-of-two. lesserDim="+lesserDim+" newLDim="+newLDim);
		} else {
		    System.out.println("Lesser dim is power-of-two.");
		}
		System.out.println("Before nextMultiple, greaterDim="+greaterDim+" newLDim="+newLDim);
		newGDim = nextMultiple(greaterDim, newLDim);
		System.out.println("After nextMultiple, greaterDim="+newGDim);
	    }// end if(non-square)
	    else {// Square, make sure they are a power-of-two
		System.out.println("Detected as square.");
		if (!SpecialRAWDimensions.isPowerOfTwo(lesserDim)) {
		    newGDim = nextPowerOfTwo(greaterDim);
		    newLDim = nextPowerOfTwo(lesserDim);
		    System.out.println("Non-power-of-two square");
		} else {
		    System.out.println("Power-of-two square.");
		}
	    }// end if(square)
	    System.out.println("Before upscale newGDim="+newGDim+" newLDim="+newLDim);
	    newGDim *= Math.pow(2, upScalePowerOfTwo);
	    newLDim *= Math.pow(2, upScalePowerOfTwo);
	    System.out.println("After upscale newGDim="+newGDim+" newLDim="+newLDim);
	    int newWidth = 0, newHeight = 0;
	    if (dir == SegmentDirection.VERTICAL) {
		newWidth = newLDim;
		newHeight = newGDim;
	    } else {
		newWidth = newGDim;
		newHeight = newLDim;
	    }// Horizontal
	    scaleWidth = (double) newWidth / dims.getWidth();
	    scaleHeight = (double) newHeight / dims.getHeight();
	    System.out.println("newWidth=" + newWidth + " newHeight="
		    + newHeight);
	    System.out.println("scaleWidth=" + scaleWidth + " scaleHeight="
		    + scaleHeight);
	    // Break into segments
	    numSegs = newGDim / newLDim;
	    // Square so height and width are the same.
	    segHeight = newLDim;
	    segWidth = newLDim;
	    System.out.println("Segwidth=" + segWidth + " segHeight="
		    + segHeight);
	    segImg = new BufferedImage[numSegs];
	    for (int seg = 0; seg < numSegs; seg++) {
		BufferedImage segBuf = new BufferedImage(segWidth, segHeight,
			BufferedImage.TYPE_INT_ARGB);
		// System.out.println("Created new seg buffer of size "+segWidth+"x"+segHeight);
		segImg[seg] = segBuf;
		// Generate the bitmap
		for (int y = 0; y < segHeight; y++) {
		    for (int x = 0; x < segWidth; x++) {
			Color c = getFilteredSegmentedPixelAt(x, y, seg);
			segBuf.setRGB(x, y, c.getRGB());
		    }// end for(x)
		}// end for(y)
	    }// end (create rgb888)
	}// end if(segImg==null)
	System.out.println("Num segs="+numSegs);
	return segImg;
    }

    private int nextMultiple(int originalValue, int multiplicand) {
	return ((int) Math.ceil((double) originalValue / (double) multiplicand))
		* multiplicand;
    }

    private int nextPowerOfTwo(double v) {
	return (int) Math.pow(2, Math.ceil(log2(v)));
    }
    
    private static double log2(double v){
	return Math.log(v)/Math.log(2);
    }

    private Color getFilteredSegmentedPixelAt(double u, double v, int segment) {
	switch (dir) {
	case HORIZONTAL:
	    return getFilteredScaledGlobalPixelAt(u + segment * segWidth, v);
	case VERTICAL:
	    return getFilteredScaledGlobalPixelAt(u, v + segment * segHeight);
	}// end switch(dir)
	throw new RuntimeException("Invalid segment direction: " + dir);
    }// end getFilteredSegmentedPixelAt(...)

    private Color getFilteredScaledGlobalPixelAt(double u, double v) {
	return getFilteredGlobalPixelAt(u / scaleWidth, v / scaleHeight);
    }

    private Color getFilteredGlobalPixelAt(double u, double v) {
	u -= .5;
	v -= .5;
	int x = (int) Math.floor(u);
	int y = (int) Math.floor(v);

	Color tl = getPixelAt(x, y + 1);
	Color tr = getPixelAt(x + 1, y + 1);
	Color bl = getPixelAt(x, y);
	Color br = getPixelAt(x + 1, y);

	// Some experimental attempts at selective blurring/sharpening
	double sharpU = Math.pow(
		Math.pow(
			(tl.getRed() + bl.getRed())
				- (tr.getRed() + br.getRed()), 2.)
			+ Math.pow(
				(tl.getGreen() + bl.getGreen())
					- (tr.getGreen() + br.getGreen()), 2.)
			+ Math.pow(
				(tl.getBlue() + bl.getBlue())
					- (tr.getBlue() + br.getBlue()), 2.)
			+ Math.pow(
				(tl.getAlpha() + bl.getAlpha())
					- (tr.getAlpha() + br.getAlpha()), 2.),
		2);
	double sharpV = Math.pow(
		Math.pow(
			(tl.getRed() + tr.getRed())
				- (bl.getRed() + br.getRed()), 2.)
			+ Math.pow(
				(tl.getGreen() + tr.getGreen())
					- (bl.getGreen() + br.getGreen()), 2.)
			+ Math.pow(
				(tl.getBlue() + tr.getBlue())
					- (bl.getBlue() + br.getBlue()), 2.)
			+ Math.pow(
				(tl.getAlpha() + tr.getAlpha())
					- (bl.getAlpha() + br.getAlpha()), 2.),
		2);
	double uRatio = (u - x);
	double vRatio = (v - y);
	// uRatio=sharp(uRatio,sharpU);
	// vRatio=sharp(vRatio,sharpV);

	double uInverse = 1 - uRatio;
	double vInverse = 1 - vRatio;

	double r = ((double) bl.getRed() * uInverse + (double) br.getRed()
		* uRatio)
		* vInverse
		+ ((double) tl.getRed() * uInverse + (double) tr.getRed()
			* uRatio) * vRatio;
	double g = ((double) bl.getGreen() * uInverse + (double) br.getGreen()
		* uRatio)
		* vInverse
		+ ((double) tl.getGreen() * uInverse + (double) tr.getGreen()
			* uRatio) * vRatio;
	double b = ((double) bl.getBlue() * uInverse + (double) br.getBlue()
		* uRatio)
		* vInverse
		+ ((double) tl.getBlue() * uInverse + (double) tr.getBlue()
			* uRatio) * vRatio;
	double a = ((double) bl.getAlpha() * uInverse + (double) br.getAlpha()
		* uRatio)
		* vInverse
		+ ((double) tl.getAlpha() * uInverse + (double) tr.getAlpha()
			* uRatio) * vRatio;
	// System.out.println("r="+r+" g="+g+" b="+b+" a="+a+" uRatio="+uRatio+" vRatio="+vRatio+" uInverse="+uInverse+" vInverse="+vInverse);
	return new Color((int) r, (int) g, (int) b, (int) a);
    }

    private final Color getPixelAt(int x, int y) {
	if (x < 0)
	    return palette[0];
	if (y < 0)
	    return palette[0];
	if (x >= dims.getWidth())
	    return palette[0];
	if (y >= dims.getHeight())
	    return palette[0];
	return palette[getRawBytes()[x + y * (int) dims.getWidth()] & 0xFF];
    }
}// end RAWFile
