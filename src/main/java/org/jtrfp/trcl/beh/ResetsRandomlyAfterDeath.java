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

import java.util.concurrent.Callable;

import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.obj.WorldObject;


public class ResetsRandomlyAfterDeath extends Behavior implements DeathListener {
private double minWaitMillis=100,maxWaitMillis=1000;
private Runnable runOnReset;

@Override
public void notifyDeath() {
   //Reset state
    final WorldObject thisObject = getParent();
    final Runnable _runOnReset = runOnReset;
    final long waitTime = (long)(minWaitMillis+Math.random()*(maxWaitMillis-minWaitMillis));
    final ThreadManager threadManager = thisObject.getTr().getThreadManager();
    threadManager.submitToThreadPool(new Callable<Void>(){
	@Override
	public Void call() throws Exception {
	    try{Thread.currentThread().sleep(waitTime);}
	    catch(InterruptedException e){e.printStackTrace();}
		    unDamage();
		    reset();
		    reIntroduce();
		    runOnReset();
		    return null;
		}//end call()
		private void unDamage(){
		    try{thisObject.probeForBehavior(DamageableBehavior.class).unDamage();}
		    catch(SupplyNotNeededException e){e.printStackTrace();}//?!?!
		}
		
		private void reset(){
		    SpacePartitioningGrid grid = thisObject.probeForBehavior(DeathBehavior.class).getGridOfLastDeath();
		    thisObject.probeForBehavior(DeathBehavior.class).reset();
		    if(grid!=null)grid.add(thisObject);
		    else throw new NullPointerException();
		}
		private void reIntroduce(){
		    thisObject.setActive(true);
		    thisObject.setVisible(true);
		}
		private void runOnReset(){
		    _runOnReset.run();
		}}
	);
 }//end notifyDeath

/**
 * @return the minWaitMillis
 */
public double getMinWaitMillis() {
    return minWaitMillis;
}

/**
 * @param minWaitMillis the minWaitMillis to set
 */
public ResetsRandomlyAfterDeath setMinWaitMillis(double minWaitMillis) {
    this.minWaitMillis = minWaitMillis;
    return this;
}

/**
 * @return the maxWaitMillis
 */
public double getMaxWaitMillis() {
    return maxWaitMillis;
}

/**
 * @param maxWaitMillis the maxWaitMillis to set
 */
public ResetsRandomlyAfterDeath setMaxWaitMillis(double maxWaitMillis) {
    this.maxWaitMillis = maxWaitMillis;
    return this;
}

/**
 * @return the runOnReset
 */
public Runnable getRunOnReset() {
    return runOnReset;
}

/**
 * @param runOnReset the runOnReset to set
 */
public ResetsRandomlyAfterDeath setRunOnReset(Runnable runOnReset) {
    this.runOnReset = runOnReset;
    return this;
}

}//end ResetsRandomlyAfterDeath
