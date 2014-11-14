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

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Smoke.SmokeType;

public class ExplosionSystem extends RenderableSpacePartitioningGrid {
    	private final TR     tr;
    	private final int    MAX_EXPLOSIONS_PER_POOL=20;
    	private final Explosion[][] allExplosions = new Explosion[ExplosionType.values().length][];
    	private final int [] indices = new int[ExplosionType.values().length];
	public ExplosionSystem(TR tr){
	    super(tr.getWorld());
	    this.tr=tr;
	    int i;
	    for(ExplosionType t:ExplosionType.values()){
		allExplosions[t.ordinal()]=new Explosion[MAX_EXPLOSIONS_PER_POOL];
		for(i=0; i<MAX_EXPLOSIONS_PER_POOL; i++)
			allExplosions[t.ordinal()][i]=new Explosion(tr,t);
	    }//end for(explosionTypes)
	}//end constructor()
	
	public Explosion triggerExplosion(double [] position, ExplosionType type) {
	    indices[type.ordinal()]++;indices[type.ordinal()]%=MAX_EXPLOSIONS_PER_POOL;
	    Explosion result = allExplosions[type.ordinal()][indices[type.ordinal()]];
	    result.destroy();
	    result.resetExplosion();
	    final double [] pos = result.getPosition();
	    System.arraycopy(position, 0, pos, 0, 3);
	    result.notifyPositionChange();
	    final SmokeSystem sf = tr.getResourceManager().getSmokeSystem();
	    final int NUM_PUFFS=1;
	    for(int i=0; i<NUM_PUFFS; i++){
		final double [] work =
			new double[]{
			Math.random()*10000,
			Math.random()*10000,
			Math.random()*10000};
		sf.triggerSmoke(Vect3D.add(
			position,
			work,
			work),
			SmokeType.Puff);
	    }//end for(i)
	    add(result);
	    return result;
	}//end TriggerExplosion
}//end ExplosionFactory
