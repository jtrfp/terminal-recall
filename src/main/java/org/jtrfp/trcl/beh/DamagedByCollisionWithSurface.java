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
import org.jtrfp.trcl.obj.WorldObject;

public class DamagedByCollisionWithSurface extends Behavior implements SurfaceImpactListener {
    private int collisionDamage=6554;
    private final double MAX_SPEED=70000;
    private final int MIN_FRAGS=6;
    @Override
    public void collidedWithSurface(WorldObject wo, double[] surfaceNormal) {
	if(!isEnabled())return;
	final WorldObject p = getParent();
	p.getBehavior().probeForBehavior(DamageableBehavior.class).shearDamage(collisionDamage);
	for(int i=0; i<MIN_FRAGS+p.getModel().getTriangleList().getMaximumVertexValue()/6000; i++){
	    p.getTr().getResourceManager().getDebrisSystem().spawn(p.getPosition(), 
	    new Vector3D(
		Math.random()*MAX_SPEED-MAX_SPEED/2.,
		Math.random()*MAX_SPEED+30000,
		Math.random()*MAX_SPEED-MAX_SPEED/2.));}
    }//end collidedWithSurface(...)
    /**
     * @return the collisionDamage
     */
    public int getCollisionDamage() {
        return collisionDamage;
    }
    /**
     * @param collisionDamage the collisionDamage to set
     */
    public DamagedByCollisionWithSurface setCollisionDamage(int collisionDamage) {
        this.collisionDamage = collisionDamage;
        return this;
    }
    
}
