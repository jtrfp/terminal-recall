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
package org.jtrfp.trcl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class ScalingAltitudeMap implements AltitudeMap {
    private final AltitudeMap delegate;
    private final Vector3D    scalar;
    public ScalingAltitudeMap(AltitudeMap delegate, Vector3D scalar){
	this.delegate=delegate;
	this.scalar  =scalar;
    }
    @Override
    public double heightAt(double x, double z) {
	return delegate.heightAt(x/scalar.getX(), z/scalar.getZ())*scalar.getY();
    }
    @Override
    public double getWidth() {
	return delegate.getWidth();
    }
    @Override
    public double getHeight() {
	return delegate.getHeight();
    }
}//end HeightScalingAltitudeMap
