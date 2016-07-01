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

import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class TriggersVisCalcWithMovement extends Behavior {
    private volatile double [] 	positionOfLastVisCalc = new double[]{//Init to The Land Of Wind And Ghosts
	    Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
    private double		distanceThreshold = TRFactory.mapSquareSize*10;
    
    @Override
    public void tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final double [] pPos = parent.getPosition();
	final double dist = Vect3D.taxicabDistance(pPos, positionOfLastVisCalc);
	if(dist>distanceThreshold){
	    System.out.println("TriggersVisCalcWithMovement triggered.");
	    positionOfLastVisCalc[0]=pPos[0];
	    positionOfLastVisCalc[1]=pPos[1];
	    positionOfLastVisCalc[2]=pPos[2];
	}
    }//end _tick(...)

    /**
     * @return the distanceThreshold
     */
    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    /**
     * @param distanceThreshold the distanceThreshold to set
     */
    public void setDistanceThreshold(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    /**
     * @return the positionOfLastVisCalc
     */
    public double[] getPositionOfLastVisCalc() {
        return positionOfLastVisCalc;
    }

}//end TriggersVisCalcWithMovement