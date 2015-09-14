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
package org.jtrfp.trcl.obj;

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class SpawnsRandomExplosionsAndDebris extends Behavior {
    private final ExplosionSystem explosions;
    private final DebrisSystem debris;
    public SpawnsRandomExplosionsAndDebris(TR tr){
	this.explosions=tr.getResourceManager().getExplosionFactory();
	this.debris=tr.getResourceManager().getDebrisSystem();
    }
    @Override
    public void tick(long timeMillis){
	if(Math.random()<.05){
	    explosions.triggerExplosion(new Vector3D(getParent().getPosition()), ExplosionType.Blast);}
	if(Math.random()<.05){
	    explosions.triggerExplosion(new Vector3D(getParent().getPosition()), ExplosionType.Billow);}
	if(Math.random()<.1){
	    debris.spawn(new Vector3D(getParent().getPosition()), new Vector3D(
		    Math.random()*50000-25000,
		    Math.random()*50000-25000,
		    Math.random()*50000-25000));}
    }//end _tick(...)
}//end SpawnsRandomExplosionsAndDebris
