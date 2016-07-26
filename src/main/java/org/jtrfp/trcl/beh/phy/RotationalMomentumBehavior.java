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
package org.jtrfp.trcl.beh.phy;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.WorldObject;

public class RotationalMomentumBehavior extends Behavior {
    private double equatorialMomentum=0;//Axis is getTop()
    private double polarMomentum     =0;//Axis is getHeading().crossProduct(getTop())
    private double lateralMomentum   =0;//Axis is getHeading()
    @Override
    public void tick(long tickTimeInMillis){
	final WorldObject p = getParent();
	
	final double lateralMomentum    = getLateralMomentum();
	final double equatorialMomentum = getEquatorialMomentum();
	final double polarMomentum      = getPolarMomentum();
	try{Rotation rot;
	    rot = new Rotation(p.getHeading(),lateralMomentum);
	    p.setTop(rot.applyTo(p.getTop()));
	    rot = new Rotation(p.getHeading().crossProduct(p.getTop()),polarMomentum);
	    p.setHeading(rot.applyTo(p.getHeading()));
	    p.setTop(rot.applyTo(p.getTop()));
	    rot = new Rotation(p.getTop(),equatorialMomentum);
	    p.setHeading(rot.applyTo(p.getHeading()));
	    p.setTop(rot.applyTo(p.getTop()));
	}catch(MathIllegalArgumentException e){}
    }//end _tick(....)
    /**
     * @return the equatorialMomentum
     */
    public double getEquatorialMomentum() {
        return equatorialMomentum;
    }
    /**
     * @param equatorialMomentum the equatorialMomentum to set
     */
    public RotationalMomentumBehavior setEquatorialMomentum(double equatorialMomentum) {
        this.equatorialMomentum = equatorialMomentum; return this;
    }
    
    public RotationalMomentumBehavior accellerateEquatorialMomentum(double delta){equatorialMomentum+=delta;return this;}
    
    /**
     * @return the polarMomentum
     */
    public double getPolarMomentum() {
        return polarMomentum;
    }
    
//    public void accelleratePolarMomentum(double delta){pMomentum+=delta;}
    /**
     * @param polarMomentum the polarMomentum to set
     */
    public RotationalMomentumBehavior setPolarMomentum(double polarMomentum) {
        this.polarMomentum = polarMomentum; return this;
    }
    
    public RotationalMomentumBehavior accelleratePolarMomentum(double delta){polarMomentum+=delta;return this;}
    /**
     * @return the lateralMomentum
     */
    public double getLateralMomentum() {
        return lateralMomentum;
    }
    
    public RotationalMomentumBehavior accellerateLateralMomentum(double delta){lateralMomentum+=delta;return this;}
    /**
     * @param lateralMomentum the lateralMomentum to set
     */
    public RotationalMomentumBehavior setLateralMomentum(double lateralMomentum) {
        this.lateralMomentum = lateralMomentum; return this;
    }
}
