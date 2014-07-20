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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class RotateAroundObject extends Behavior {
    private WorldObject target;
    private double distance=TR.mapSquareSize;
    private double angularVelocityRPS = .25;
    final double [] delta = new double[3];
    private double []offset = new double[]{0,0,0};
    @Override
    public void _tick(long tickTimeMillis){
	if(target!=null){
	    final WorldObject parent	= getParent();
	    final double [] tPos	= target.getPosition();
	    final double [] pPos	= parent.getPosition();
	    //Theta = [0,2]pi
	    final double theta = (((angularVelocityRPS*tickTimeMillis) / 1000.)%1.)*2*Math.PI;
	    delta[0]=Math.sin(theta);	//X
	    delta[2]=Math.cos(theta);	//Z
	    delta[1]=0;			//Y
	    Vect3D.scalarMultiply(delta, distance, delta);
	    Vect3D.add(delta, offset, delta);
	    Vect3D.add(tPos, delta, pPos);
	    parent.setPosition(pPos[0],pPos[1],pPos[2]);
	    parent.notifyPositionChange();//TODO: Remove and see if it still works
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
    public RotateAroundObject setTarget(WorldObject target) {
        this.target = target;
        return this;
    }
    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }
    /**
     * @param distance the distance to set
     */
    public RotateAroundObject setDistance(double distance) {
        this.distance = distance;
        return this;
    }
    /**
     * @return the angularVelocityRPS
     */
    public double getAngularVelocityRPS() {
        return angularVelocityRPS;
    }
    /**
     * @param angularVelocityRPS the angularVelocityRPS to set
     */
    public RotateAroundObject setAngularVelocityRPS(double angularVelocityRPS) {
        this.angularVelocityRPS = angularVelocityRPS;
        return this;
    }
    public void setOffset(double[] ds) {
	offset = ds;
    }
}//end RotateAroundObject
