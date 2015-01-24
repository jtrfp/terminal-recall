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
package org.jtrfp.trcl.math;

public interface Radians {
    /**
     * Represents this object in radians
     * @return
     * @since Jan 24, 2015
     */
    public double toRadians();
    
    public class View extends AbstractNormalizedObject implements Radians{
	private final Normalized delegate;
	private static final double NORM_RADIANS_SCALAR = Math.PI/(double)Integer.MAX_VALUE;
	
	public View(Normalized delegate){
	    this.delegate=delegate;
	}

	@Override
	public int toNormalized() {
	    return delegate.toNormalized();
	}

	@Override
	public double toRadians() {
	    return ((double)delegate.toNormalized()) * NORM_RADIANS_SCALAR;
	}
	
    }//end Radians.View
    
    public class Source extends AbstractNormalizedObject implements Radians{
	private volatile int normalized=0;
	private static final double NORM_RADIANS_SCALAR = (double)Integer.MAX_VALUE/Math.PI;

	@Override
	public int toNormalized() {
	    return normalized;
	}

	@Override
	public double toRadians() {
	    return ((double)toNormalized()) * NORM_RADIANS_SCALAR;
	}
	
	public void setRadians(double radians){
	    int newNormalized = (int)(radians*NORM_RADIANS_SCALAR);
	    pcs.firePropertyChange(NORMALIZED, normalized, newNormalized);
	    this.normalized=newNormalized;
	}
	
    }//end Source
}//end Radians
