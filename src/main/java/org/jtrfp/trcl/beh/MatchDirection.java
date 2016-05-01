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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
    private PropertyChangeListener directionListener;
    /**
     * @return the target
     */
    public WorldObject getTarget() {
        return target;
    }
    
    private class DirectionListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    if(target!=null){
		if(lookAtMatrix4x4 != null){
		    double [] hdg = target.getHeadingArray();
		    final RealVector newHeading = lookAtMatrix4x4.operate(new ArrayRealVector(new double [] {hdg[0],hdg[1],hdg[2],1.}));
		    hdg = newHeading.toArray();
		    getParent().setHeading(new Vector3D(hdg[0], hdg[1], hdg[2]).normalize());
		}//end lookAt
		if(topMatrix4x4 != null){
		    double [] top = target.getTopArray();
		    final RealVector newTop = topMatrix4x4.operate(new ArrayRealVector(new double [] {top[0],top[1],top[2],1.}));
		    top = newTop.toArray();
		    getParent().setTop(new Vector3D(top[0], top[1], top[2]).normalize());
		}//end top
	    }//end if(!null)
	}//end propertyChange(...)
    }//end DirectionListener
    
    /**
     * @param target the target to set
     */
    public void setTarget(WorldObject target) {
	final PropertyChangeListener directionListener = getDirectionListener();
	if(this.target != null)
	    this.target.removePropertyChangeListener(directionListener);
        this.target = target;
        if(this.target != null)
         this.target.addPropertyChangeListener(directionListener);
    }
    public RealMatrix getLookAtMatrix4x4() {
        return lookAtMatrix4x4;
    }
    public MatchDirection setLookAtMatrix4x4(RealMatrix lookAtMatrix4x4) {
        this.lookAtMatrix4x4 = lookAtMatrix4x4;
        return this;
    }
    public RealMatrix getTopMatrix4x4() {
        return topMatrix4x4;
    }
    public MatchDirection setTopMatrix4x4(RealMatrix topMatrix4x4) {
        this.topMatrix4x4 = topMatrix4x4;
        return this;
    }
    protected PropertyChangeListener getDirectionListener() {
	if(directionListener == null)
	    directionListener = new DirectionListener();
        return directionListener;
    }
    protected void setDirectionListener(PropertyChangeListener directionListener) {
        this.directionListener = directionListener;
    }
    
}//end MatchDirection
