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

package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jtrfp.trcl.obj.WorldObject;

public class MatchDirection extends Behavior {
    private WorldObject target;
    private RealMatrix lookAtMatrix4x4 = MatrixUtils.createRealIdentityMatrix(4);
    private RealMatrix topMatrix4x4    = MatrixUtils.createRealIdentityMatrix(4);
    @Override
    public void tick(long tickTimeMillis){
	if(target!=null){
	    double [] hdg = target.getHeadingArray();
	    final RealVector newHeading = lookAtMatrix4x4.operate(new ArrayRealVector(new double [] {hdg[0],hdg[1],hdg[2],1.}));
	    hdg = newHeading.toArray();
	    getParent().setHeading(new Vector3D(hdg[0], hdg[1], hdg[2]).normalize());
	    double [] top = target.getTopArray();
	    final RealVector newTop = topMatrix4x4.operate(new ArrayRealVector(new double [] {top[0],top[1],top[2],1.}));
	    top = newTop.toArray();
	    getParent().setTop(new Vector3D(top[0], top[1], top[2]).normalize());
	}//end if(!null)
    }//end _tick(...)
    /**
     * @return the target
     */
    public WorldObject getTarget() {
        return target;
    }
    /**
     * @param target the target to set
     */
    public void setTarget(WorldObject target) {
        this.target = target;
    }
    public RealMatrix getLookAtMatrix4x4() {
        return lookAtMatrix4x4;
    }
    public void setLookAtMatrix4x4(RealMatrix lookAtMatrix4x4) {
        this.lookAtMatrix4x4 = lookAtMatrix4x4;
    }
    public RealMatrix getTopMatrix4x4() {
        return topMatrix4x4;
    }
    public void setTopMatrix4x4(RealMatrix topMatrix4x4) {
        this.topMatrix4x4 = topMatrix4x4;
    }
    
}//end MatchDirection
