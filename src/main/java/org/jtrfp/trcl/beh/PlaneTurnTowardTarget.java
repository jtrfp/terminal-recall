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

package org.jtrfp.trcl.beh;

import java.lang.ref.WeakReference;

import org.apache.commons.math3.analysis.function.Sigmoid;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.tools.Util;

public class PlaneTurnTowardTarget extends Behavior {
    private PlaneTurnBehavior planeTurnBehavior;
    private double            turnFactor = 0;
    private WeakReference<WorldObject> target;
    private Vector3D desiredRelativeVector = Vector3D.PLUS_K;
    private final Sigmoid sigmoid = new Sigmoid(-1,1);
    private boolean symmetricalRelativeVector = false;
    private final double [] workVector = new double[3];
    private final double [] deltaVector = new double[3];
    
    @Override
    public void tick(long tickTimeMillis){
	final PlaneTurnBehavior planeTurnBehavior = getPlaneTurnBehavior();
	final WorldObject parent = getParent();
	final WorldObject target = getTarget();
	final double [] parentPos = parent.getPosition();
	final double [] parentHeading = parent.getHeadingArray();
	final double [] targetPos = target.getPosition();
	    if(target.probeForBehavior(Cloakable.class).isCloaked())return;
	    final double [] deltaVector = this.deltaVector, workVector = this.workVector;
	    Util.relativeHeadingVector(parentPos, parentHeading, targetPos, deltaVector);
	    System.arraycopy(deltaVector, 0, workVector, 0, 3);
	    double turnFactor = vector2TurnFactor(workVector);
	    if(isSymmetricalRelativeVector()){
		System.arraycopy(deltaVector, 0, workVector, 0, 3);
		Vect3D.negate(workVector);
		final double symmetricalTurnFactor = vector2TurnFactor(workVector);
		if(Math.abs(symmetricalTurnFactor) < Math.abs(turnFactor))
		    turnFactor = symmetricalTurnFactor;
	    }
	    planeTurnBehavior.setTurnFactor(turnFactor);
    }//end tick(...)
    
    private double vector2TurnFactor(double [] deltaVector){
	simpleRotation(getDesiredRelativeVector().toArray(), deltaVector);
	double turnFactor = sigmoid.value(deltaVector[0]*3);
	if(deltaVector[2]<0)
	    turnFactor = Math.signum(deltaVector[0]);
	turnFactor *= getTurnFactor();
	return turnFactor;
    }
    
    private void simpleRotation(double [] rotationVector, double [] targetVector){
	final double zAxisX = rotationVector[0];
	final double zAxisZ = rotationVector[2];
	
	final double xAxisX = rotationVector[2];
	final double xAxisZ = -rotationVector[0];
	
	final double resultX = targetVector[0]*xAxisX + targetVector[2]*xAxisZ;
	final double resultZ = targetVector[0]*zAxisX + targetVector[2]*zAxisZ;
	targetVector[0]=resultX; targetVector[2]=resultZ;
    }
    
    public WorldObject getTarget(){
	return target.get();
    }
    
    public void setTarget(WorldObject target){
	this.target = new WeakReference<WorldObject>(target);
    }

    public PlaneTurnBehavior getPlaneTurnBehavior() {
	if( planeTurnBehavior == null)
	    return planeTurnBehavior = getParent().probeForBehavior(PlaneTurnBehavior.class);
        return planeTurnBehavior;
    }

    public void setPlaneTurnBehavior(PlaneTurnBehavior planeTurnBehavior) {
        this.planeTurnBehavior = planeTurnBehavior;
    }

    public double getTurnFactor() {
        return turnFactor;
    }

    public void setTurnFactor(double turnFactor) {
        this.turnFactor = turnFactor;
    }

    public Vector3D getDesiredRelativeVector() {
        return desiredRelativeVector;
    }

    public void setDesiredRelativeVector(Vector3D desiredRelativeVector) {
        this.desiredRelativeVector = desiredRelativeVector;
    }

    public boolean isSymmetricalRelativeVector() {
        return symmetricalRelativeVector;
    }

    public void setSymmetricalRelativeVector(boolean symmetricalRelativeVector) {
        this.symmetricalRelativeVector = symmetricalRelativeVector;
    }
}//end PlaneTurnTowardPlayer
