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

import java.lang.ref.WeakReference;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.WorldObject;

public class DeathBehavior extends Behavior {
    private volatile boolean dead=false;
    private Vector3D locationOfDeath;
    private WeakReference<SpacePartitioningGrid<? extends Positionable>> spgOfLastDeath =
	     new WeakReference<SpacePartitioningGrid<? extends Positionable>>(null);
    public synchronized void die(){
	if(dead)return;
	dead=true;//Only die once until reset
	WorldObject wo = getParent();
	locationOfDeath= new Vector3D(wo.getPositionWithOffset());
	spgOfLastDeath = new WeakReference<SpacePartitioningGrid<? extends Positionable>>(wo.getContainingGrid());
	wo.destroy();
	wo.probeForBehaviors(sub,DeathListener.class);
    }
    private final Submitter<DeathListener> sub = new Submitter<DeathListener>(){

	@Override
	public void submit(DeathListener item) {
	    item.notifyDeath();
	}

	@Override
	public void submit(Collection<DeathListener> items) {
	   for(DeathListener l:items){submit(l);}
	}
    };//end submitter

    public void reset(){
	 dead=false;
	}

    /**
     * @return the locationOfDeath
     */
    public Vector3D getLocationOfLastDeath() {
        return locationOfDeath;
    }

    /**
     * @return the spgOfLastDeath
     */
    public SpacePartitioningGrid<? extends Positionable> getGridOfLastDeath() {
        return spgOfLastDeath.get();
    }
}//end DeathBehavior
