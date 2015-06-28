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


public class InterpolatingAltitudeMap implements AltitudeMap {
    private final AltitudeMap toWrap;
    public InterpolatingAltitudeMap(AltitudeMap toWrap){
	this.toWrap=toWrap;
    }
    @Override
    public double heightAt(double x, double z) {
	final int xLow=(int)Math.floor(x);
	final int xHi=(int)Math.ceil(x);
	final int zLow=(int)Math.floor(z);
	final int zHi=(int)Math.ceil(z);
	final double topLeft=toWrap.heightAt(xLow, zLow);
	final double topRight=toWrap.heightAt(xHi, zLow);
	final double bottomLeft=toWrap.heightAt(xLow, zHi);
	final double bottomRight=toWrap.heightAt(xHi, zHi);
	final double wRight=x-xLow;
	final double wLeft=1.-wRight;
	final double wBottom=z-zLow;
	final double wTop=1.-wBottom;
	double accumulator=0;
	accumulator+=topLeft*wTop*wLeft;
	accumulator+=topRight*wTop*wRight;
	accumulator+=bottomLeft*wBottom*wLeft;
	accumulator+=bottomRight*wBottom*wRight;
	assert accumulator != Double.NaN;
	assert accumulator != Double.POSITIVE_INFINITY;
	return accumulator;
    }

    @Override
    public double getWidth() {
	return toWrap.getWidth();
    }

    @Override
    public double getHeight() {
	return toWrap.getHeight();
    }
}//end InterpolatingAltitudeMap
