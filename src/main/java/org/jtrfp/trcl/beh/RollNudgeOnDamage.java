/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.obj.WorldObject;

public class RollNudgeOnDamage extends Behavior implements DamageListener {
    private double nudgeMagnitude = 2*Math.PI*.045;

    @Override
    public void damageEvent(Event ev) {
	final WorldObject parent = getParent();
	parent.probeForBehaviors(nudgeSubmitter, RotationalMomentumBehavior.class);
	/*  //// USE THIS IF NO ROTATIONAL MOMENTUM BEHAVIOR PRESENT
	//Rotate along axis
	Rotation nudgeRot = new Rotation(parent.getHeading(), nudgeMagnitude*(Math.random()-.5)*2);
	parent.setHeading(nudgeRot.applyTo(parent.getHeading()));
	parent.setTop    (nudgeRot.applyTo(parent.getTop()));
	*/
    }//end damageEvent(ev)
    
    private final Submitter<RotationalMomentumBehavior> nudgeSubmitter = new AbstractSubmitter<RotationalMomentumBehavior>(){
	@Override
	public void submit(RotationalMomentumBehavior item) {
	    item.accellerateLateralMomentum(getNudgeMagnitude()*(Math.random()-.5)*2);
	}
    };//end nudgeSubmitter

    public double getNudgeMagnitude() {
        return nudgeMagnitude;
    }

    public void setNudgeMagnitude(double nudgeMagnitude) {
        this.nudgeMagnitude = nudgeMagnitude;
    }

    public Submitter<RotationalMomentumBehavior> getNudgeSubmitter() {
        return nudgeSubmitter;
    }

}//end RollNudgeOnDamage
