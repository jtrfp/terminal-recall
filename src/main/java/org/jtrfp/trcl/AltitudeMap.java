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

public interface AltitudeMap {
    /**
     * Returns a height between 0 and 1
     * 
     * @param x
     *            The x-cell coordinate. Decimals are supported for interpolated
     *            values
     * @param z
     *            The z-cell coordinate. Decimals are supported for interpolated
     *            values
     * @return
     * @since Oct 3, 2012
     */
    public double heightAt(double x, double z);
    
    /**
     * Returns width in height cells.
     * @return
     * @since Sep 23, 2014
     */
    public double getWidth();

    /**
     * Returns map height in height cells.
     * @return
     * @since Sep 23, 2014
     */
    public double getHeight();
}//end AltitudeMap
