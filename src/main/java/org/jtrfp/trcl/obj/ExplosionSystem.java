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

import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Smoke.SmokeType;

public class ExplosionSystem extends RenderableSpacePartitioningGrid {
    	private final TR     tr;
    	private final int    MAX_EXPLOSIONS_PER_POOL=20;
    	private final Explosion[][] allExplosions = new Explosion[ExplosionType.values().length][];
    	private final int [] indices = new int[ExplosionType.values().length];
    	
	public ExplosionSystem(TR tr, String debugName){
	    super();
	    this.tr=tr;
	    int i;
	    for(ExplosionType t:ExplosionType.values()){
		final int ordinal = t.ordinal();
		allExplosions[ordinal]=new Explosion[MAX_EXPLOSIONS_PER_POOL];
		for(i=0; i<MAX_EXPLOSIONS_PER_POOL; i++)
			allExplosions[ordinal][i]=new Explosion(t,"ExplosionSystem."+debugName);
	    }//end for(explosionTypes)
	}//end constructor()
	
	public Explosion triggerExplosion(Vector3D loc, ExplosionType type) {
	    if(!isNewExplosionFeasible(loc,type))
		return null;
	    final int ordinal = type.ordinal();
	    indices[ordinal]++;indices[ordinal]%=MAX_EXPLOSIONS_PER_POOL;
	    Explosion result = allExplosions[ordinal][indices[ordinal]];
	    result.reset();
	    result.setPosition(loc.getX(), loc.getY(), loc.getZ());
	    result.notifyPositionChange();
	    final SmokeSystem sf = tr.getResourceManager().getSmokeSystem();
	    final int NUM_PUFFS=1;
	    
	    for(int i=0; i<NUM_PUFFS; i++){
		sf.triggerSmoke(loc.add(new Vector3D(
			Math.random()*10000-5000,
			Math.random()*10000-5000,
			Math.random()*10000-5000)),
			SmokeType.Puff);
	    }//end for(i)
	    
	    add(result);
	    return result;
	}//end TriggerExplosion
	
	private boolean isNewExplosionFeasible(final Vector3D loc, ExplosionType type){
	    final TreeSet<Explosion> proximalExplosions = new TreeSet<Explosion>(new Comparator<Explosion>(){
		@Override
		public int compare(Explosion o1, Explosion o2) {
		    return Misc.satCastInt(o1.getTimeOfLastReset()-o2.getTimeOfLastReset());
		}});
	for (int explosionTypeIndex = 0; explosionTypeIndex < allExplosions.length; explosionTypeIndex++) {
	    Explosion[] explosionsOfThisType = allExplosions[explosionTypeIndex];
	    for (Explosion thisExplosion : explosionsOfThisType) {
		if (thisExplosion.isActive()) {
		    final double distance = new Vector3D(
			    thisExplosion.getPosition()).distance(loc);
		    if (distance < 1000)
			return false;
		    if (distance < OneShotBillboardEvent.PROXIMITY_TEST_DIST)
			proximalExplosions.add(thisExplosion);
		}// end if(isActive)
	    }// end for(explosionsOfThisType)
	}// end for(explosions)
	    if(proximalExplosions.size()+1>OneShotBillboardEvent.MAX_PROXIMAL_EVENTS)
		proximalExplosions.first().destroy();//Destroy oldest
	    return true;
	}//end isNewExplosionFeasible(...)
}//end ExplosionFactory
