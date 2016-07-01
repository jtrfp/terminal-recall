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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.pool.ObjectPool;

public class DebrisSystem extends RenderableSpacePartitioningGrid{
    private final int DEBRIS_POOL_SIZE=80;
    private final TR tr;
    private final ObjectPool<Debris> pool;
    public DebrisSystem(TR tr){
	super();
	this.tr=tr;
	this.pool=new ObjectPool<Debris>(
		new ObjectPool.RoundRobin<Debris>(DEBRIS_POOL_SIZE), 
		preparationMethod, 
		generativeMethod);
    }//end constructor
    
    private final ObjectPool.PreparationMethod<Debris> preparationMethod = new ObjectPool.PreparationMethod<Debris>() {
	@Override
	public Debris deactivate(Debris obj) {
	    obj.destroy();
	    return obj;
	}
	@Override
	public Debris reactivate(Debris obj) {
	    return deactivate(obj);
	}
    };
    
    private final ObjectPool.GenerativeMethod<Debris> generativeMethod = new ObjectPool.GenerativeMethod<Debris>() {
	
	@Override
	public int getAtomicBlockSize() {
	    return 1;
	}

	@Override
	public Submitter<Debris> generateConsecutive(int numBlocks,
		Submitter<Debris> populationTarget) {
	    for(int i=0; i<numBlocks; i++){
		populationTarget.submit(new Debris(tr));
	    }
	    return populationTarget;
	}//end generateConsective(...)
    };
    
    public Debris spawn(Vector3D pos, Vector3D velocity) {
	final Debris result = pool.pop();
	result.reset(pos, velocity);
	add(result);
	return result;
    }//end fire(...)
}
