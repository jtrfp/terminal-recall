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

public class NormalMap implements AltitudeMap{
    private static final double NUDGE=.25;
    private final AltitudeMap delegate;
    public NormalMap(AltitudeMap delegate){
	this.delegate=delegate;
    }

    public Vector3D normalAt(double x, double z){
	/*return new Vector3D(NUDGE,heightAt(x+NUDGE,z)-heightAt(x,z),0).//X
		crossProduct(new Vector3D(0,heightAt(x,z+NUDGE)-heightAt(x,z),NUDGE)).normalize().negate();//Z
	 */
	double origin = heightAt(x,z);
	return new Vector3D(0,heightAt(x,z+NUDGE)-origin,NUDGE).
		crossProduct(new Vector3D(NUDGE,heightAt(x+NUDGE,z)-origin,0)).normalize();
	//final double magX=(heightAt(x-NUDGE,z)-heightAt(x+NUDGE,z))/NUDGE;
	//final double magZ=(heightAt(x,z-NUDGE)-heightAt(x,z+NUDGE))/NUDGE;
	//final double magY=Math.sqrt(Math.max(1-(magX*magX+magZ*magZ),0));
	//return new Vector3D(magX,magY,magZ).normalize();
    }//end normalAt(...)

    /**
     * Same as normalAt(x,z) except using triplet array with no object creation.
     * @since Jun 12, 2015
     */
    public double [] normalAt(double x, double z, double [] dest){
	final Vector3D n = normalAt(x,z);
	dest[0]=n.getX();
	dest[1]=n.getY();
	dest[2]=n.getZ();
	return dest;
	/*
	final double magX=(heightAt(x-NUDGE,z)-heightAt(x+NUDGE,z))/NUDGE;
	final double magZ=(heightAt(x,z-NUDGE)-heightAt(x,z+NUDGE))/NUDGE;
	final double magY=Math.sqrt(Math.max(1-(magX*magX+magZ*magZ),0));
	assert !Double.isNaN(magX);
	assert !Double.isNaN(magY);
	assert !Double.isNaN(magZ);
	dest[0]=magX; dest[1]=magY; dest[2]=magZ;
	assert dest.length==3:"Got "+dest.length;
	assert !Vect3D.isAnyNaN(dest);
	return Vect3D.normalize(dest, dest);
	 */
    }

    @Override
    public double heightAt(double x, double z) {
	return delegate.heightAt(x, z);
    }

    @Override
    public double getWidth() {
	return delegate.getWidth();
    }

    @Override
    public double getHeight() {
	return delegate.getHeight();
    }
}//end NormalMap
