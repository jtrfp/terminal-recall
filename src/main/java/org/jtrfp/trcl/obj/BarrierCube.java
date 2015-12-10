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
package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;

public class BarrierCube extends WorldObject {
    final double fluffX;
    final double [] dims;
    final double [] origin;
    protected BarrierCube(TR tr, double w, double h, double d, double []origin){
	super(tr);
	fluffX=d;
	this.dims=new double[]{w,h,d};
	this.origin=origin;
    }
    public BarrierCube(TR tr,double w, double h, double d, TextureDescription tex, double [] origin, boolean hasAlpha) {
	this(tr,w,h,d,origin);
	Model m = Model.buildCube(w, h, d, tex, origin,hasAlpha,tr);
	setModel(m);
    }// end constructor
    public BarrierCube(TR tr,double w, double h, double d, TextureDescription tex, double [] origin, double u0, double v0, double u1, double v1, boolean hasAlpha) {
	this(tr,w,h,d,origin);
	Model m = Model.buildCube(w, h, d, tex, origin,u0,v0,u1,v1,hasAlpha,tr);
	setModel(m);
    }// end constructor
    /**
     * @return the fluffX
     */
    public double getFluffX() {
        return fluffX;
    }
    /**
     * @return the dims
     */
    public double[] getDims() {
        return dims;
    }
    /**
     * @return the origin
     */
    public double[] getOrigin() {
        return origin;
    }

}//end BarrierCube
