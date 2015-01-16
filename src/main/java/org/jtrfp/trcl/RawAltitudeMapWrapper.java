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

import org.jtrfp.trcl.file.RAWFile;

/**
 * Wraps a Terminal Reality / mtmX RAW data file as an abstract altitude map.
 * @author Chuck Ritola
 *
 */
public class RawAltitudeMapWrapper implements AltitudeMap {
    private int width, height;
    private RAWFile file;

    public RawAltitudeMapWrapper(RAWFile dat) {
	file = dat;
	width = dat.getSideLength();
	height = dat.getSideLength();
    }// end RawAltitudeMapWrapper

    @Override
    public double heightAt(double x, double z) {
	int ix = (int)Math.floor(x);
	int iz = (int)Math.floor(z);
	if (ix < 0)
	    ix = (width)-(-ix % (width));
	if (iz < 0)
	    iz = (height)-(-iz % (height));
	return file.valueAt(iz % width, ix % height) / 255.;
    }//end heightAt(...)

    @Override
    public double getWidth() {
	return width;
    }

    @Override
    public double getHeight() {
	return height;
    }
}// end RawAltitudeMapWrapper
