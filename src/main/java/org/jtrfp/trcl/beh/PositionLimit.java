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

import org.jtrfp.trcl.obj.WorldObject;

public class PositionLimit extends Behavior {
    private final double [] positionMaxima = new double[]{Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
    private final double [] positionMinima = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    @Override
    public void tick(long timeInMillis){//I'm in the mood to use ASM coding style...
	final 	WorldObject 	thisObject	=	getParent();
	final 	double [] 	thisPos 	=	thisObject.getPosition();
		boolean 	changed		=	false;
	
	if(thisPos[0]>positionMaxima[0]){thisPos[0]=positionMaxima[0];changed=true;}
	if(thisPos[1]>positionMaxima[1]){thisPos[1]=positionMaxima[1];changed=true;}
	if(thisPos[2]>positionMaxima[2]){thisPos[2]=positionMaxima[2];changed=true;}
	
	if(thisPos[0]<positionMinima[0]){thisPos[0]=positionMinima[0];changed=true;}
	if(thisPos[1]<positionMinima[1]){thisPos[1]=positionMinima[1];changed=true;}
	if(thisPos[2]<positionMinima[2]){thisPos[2]=positionMinima[2];changed=true;}
	
	if(changed)thisObject.notifyPositionChange();
    }//end _tick()
    /**
     * @return the positionMinima
     */
    public double[] getPositionMinima() {
        return positionMinima;
    }
    /**
     * @return the positionMaxima
     */
    public double[] getPositionMaxima() {
        return positionMaxima;
    }
}//end PositionLimit
