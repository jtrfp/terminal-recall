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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class HorizAimAtPlayerBehavior extends Behavior {
    private WorldObject chaseTarget;
    private double equatorialAccelleration=.004;
    private final double [] vectorToTargetVar = new double[3];
    private final double [] headingVarianceDelta = new double[3];
    private boolean reverse = false;
    private boolean leftHanded = true;
    private double hysteresis=.02;//Prevents gimbal shake.
    public HorizAimAtPlayerBehavior(WorldObject chaseTarget){super();this.chaseTarget=chaseTarget;}
    @Override
    public void _tick(long timeInMillis){
	if(chaseTarget!=null){
	    WorldObject thisObject = getParent();
	    final Player player = thisObject.getTr().getGame().getPlayer();
	    if(player.getBehavior().probeForBehavior(Cloakable.class).isCloaked())return;
	    final RotationalMomentumBehavior rmb = thisObject.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);

	    assert !Vect3D.isAnyEqual(chaseTarget.getPosition(), Double.POSITIVE_INFINITY);
	    assert !Vect3D.isAnyEqual(thisObject.getPosition(), Double.NEGATIVE_INFINITY);
	    TR.twosComplimentSubtract(chaseTarget.getPosition(), thisObject.getPosition(),vectorToTargetVar);
	    assert !Vect3D.isAnyNaN(vectorToTargetVar);
	    assert !Vect3D.isAnyEqual(vectorToTargetVar, Double.POSITIVE_INFINITY);
	    assert !Vect3D.isAnyEqual(vectorToTargetVar, Double.NEGATIVE_INFINITY);
	    Vect3D.normalize(vectorToTargetVar);
	    vectorToTargetVar[1]=0;
	    Vect3D.normalize(vectorToTargetVar);
	    final Vector3D thisHeading=new Vector3D(thisObject.getHeading().getX(),0,thisObject.getHeading().getZ()).normalize();
	    Vect3D.subtract(thisHeading.toArray(), vectorToTargetVar, headingVarianceDelta);	
	    if(Math.sqrt(headingVarianceDelta[2]*headingVarianceDelta[2]+headingVarianceDelta[0]*headingVarianceDelta[0])<hysteresis)return;
	    if(!reverse)Vect3D.negate(vectorToTargetVar);
	    Rotation rot = new Rotation(new Vector3D(vectorToTargetVar),thisHeading);
	    final Vector3D deltaVector=rot.applyTo(Vector3D.PLUS_K);
	    if((deltaVector.getZ()>0||deltaVector.getX()<0)==leftHanded){rmb.accellerateEquatorialMomentum(-equatorialAccelleration);}
	    else{rmb.accellerateEquatorialMomentum(equatorialAccelleration);}
	}//end if(target!null)
    }
    /**
     * @return the reverse
     */
    public boolean isReverse() {
        return reverse;
    }
    /**
     * @param reverse the reverse to set
     * @return 
     */
    public HorizAimAtPlayerBehavior setReverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }
    public HorizAimAtPlayerBehavior setTurnAcceleration(double accelleration) {
	equatorialAccelleration=accelleration;
	return this;
    }
    /**
     * @return the leftHanded
     */
    public boolean isLeftHanded() {
        return leftHanded;
    }
    /**
     * @param leftHanded the leftHanded to set
     */
    public HorizAimAtPlayerBehavior setLeftHanded(boolean leftHanded) {
        this.leftHanded = leftHanded;
        return this;
    }
    /**
     * @return the hysteresis
     */
    public double getHysteresis() {
        return hysteresis;
    }
    /**
     * @param hysteresis the hysteresis to set
     */
    public HorizAimAtPlayerBehavior setHysteresis(double hysteresis) {
        this.hysteresis = hysteresis;
        return this;
    }
}//end ChaseBehavior
