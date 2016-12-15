/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;

/**
 * Sustains a tilt relative to the heading, similar to a quarternion.
 * @author Chuck Ritola
 *
 */

public class RollPitchYawBehavior extends Behavior {
    private static final int PITCH = 0, ROLL = 1, YAW = 2;
    private final double []            desiredThetas = new double[3];
    private double                     thrustFactor  = .001;
    private final double []            currentThetas = new double[3];
    private RotationalMomentumBehavior rotationalMomentumBehavior;

    @Override
    public void tick(long tickTimeMillis){
	final WorldObject parent = getParent();
	final Vector3D heading  = parent.getHeading();
	final Vector3D top      = parent.getTop();
	final Vector3D side     = top.crossProduct(heading);
	//ROLL
	final double rollTheta = Vector3D.angle(Vector3D.PLUS_J, new Rotation(heading, Vector3D.PLUS_K).applyTo(top));
	setCurrentRollTheta (rollTheta);
	//YAW
	final double yawTheta = Vector3D.angle(new Vector3D(heading.getX(),0,heading.getZ()).normalize(), Vector3D.PLUS_K);
	setCurrentYawTheta  (yawTheta);
	//PITCH
	final double pitchTheta = Vector3D.angle(Vector3D.PLUS_K, new Rotation(side, Vector3D.PLUS_I).applyTo(heading));
	setCurrentPitchTheta(pitchTheta);
	
	final double thrustFactor     = getThrustFactor();
	final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
	double desiredThrust;
	
	final double desiredRollTheta = getDesiredRollTheta();
	             desiredThrust    = thrustFactor * (desiredRollTheta - getCurrentRollTheta());
	
	//System.out.println("Current roll: "+getCurrentRollTheta()+" desired: "+desiredRollTheta+" dThrust:"+desiredThrust);
	rotationalMomentumBehavior.accellerateLateralMomentum(desiredThrust);
	
	final double desiredPitchTheta = getDesiredPitchTheta();
                      desiredThrust    = thrustFactor * (desiredPitchTheta - getCurrentPitchTheta());
	rotationalMomentumBehavior.accelleratePolarMomentum(desiredThrust);
	
	final double desiredYawTheta = getDesiredYawTheta();
                      desiredThrust    = thrustFactor * (desiredYawTheta - getCurrentYawTheta());
        rotationalMomentumBehavior.accellerateEquatorialMomentum(desiredThrust);
    }//end tick(...)

    protected RotationalMomentumBehavior getRotationalMomentumBehavior() {
	if(rotationalMomentumBehavior == null)
	    setRotationalMomentumBehavior(getParent().probeForBehavior(RotationalMomentumBehavior.class));
        return rotationalMomentumBehavior;
    }//end getRotationalMomentumBehavior(...)

    protected RollPitchYawBehavior setRotationalMomentumBehavior(
    	RotationalMomentumBehavior rotationalMomentumBehavior) {
        this.rotationalMomentumBehavior = rotationalMomentumBehavior;
        return this;
    }//end setRotationalMomentumBehavior()

    public double getDesiredRollTheta() {
        return desiredThetas[ROLL];
    }

    public RollPitchYawBehavior setDesiredRollTheta(double desiredRollTheta) {
        this.desiredThetas[ROLL] = desiredRollTheta;
        return this;
    }
    
    public double getDesiredPitchTheta() {
        return desiredThetas[PITCH];
    }

    public RollPitchYawBehavior setDesiredPitchTheta(double desiredPitchTheta) {
        this.desiredThetas[PITCH] = desiredPitchTheta;
        return this;
    }
    
    public double getDesiredYawTheta() {
        return desiredThetas[YAW];
    }

    public RollPitchYawBehavior setDesiredYawTheta(double desiredYawTheta) {
        this.desiredThetas[YAW] = desiredYawTheta;
        return this;
    }

    public double getThrustFactor() {
        return thrustFactor;
    }

    public RollPitchYawBehavior setThrustFactor(double thrustFactor) {
        this.thrustFactor = thrustFactor;
        return this;
    }

    public double getCurrentRollTheta() {
        return currentThetas[ROLL];
    }

    protected void setCurrentRollTheta(double currentRollTheta) {
        this.currentThetas[ROLL] = currentRollTheta;
    }
    
    public double getCurrentPitchTheta() {
        return currentThetas[PITCH];
    }

    protected void setCurrentPitchTheta(double currentPitchTheta) {
        this.currentThetas[PITCH] = currentPitchTheta;
    }
    
    public double getCurrentYawTheta() {
        return currentThetas[YAW];
    }

    protected void setCurrentYawTheta(double currentYawTheta) {
        this.currentThetas[YAW] = currentYawTheta;
    }
    
}//end TiltBehavior
