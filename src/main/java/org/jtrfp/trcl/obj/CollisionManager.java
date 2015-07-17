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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;

public class CollisionManager {
    public static final double MAX_CONSIDERATION_DISTANCE = TR.mapSquareSize * 15;
    private final TR tr;
    private final ArrayList<Positionable>[] collisionList = new ArrayList[2];//Collideable objects are O(.5n^2-.5n)  !!!
    public static final int SHIP_COLLISION_DISTANCE = 15000;
    private volatile boolean flip = false;
    private final List<Positionable> inputRelevanceList = new ArrayList<Positionable>();
    //private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CollisionManager(TR tr) {
	this.tr = tr;
	collisionList[0] = new ArrayList<Positionable>(1024);
	collisionList[1] = new ArrayList<Positionable>(1024);
    }
    private final Positionable [] positionableWorkArray = new Positionable[4096];
    private volatile int numPositionables =0;
    private int numPositionablesToClear=0;

    public void performCollisionTests() {
	try{World.relevanceExecutor.submit(new Callable<List<Positionable>>(){
	    @Override
	    public List<Positionable> call() {
		List<Positionable> list = inputRelevanceList;
		final int prevNumPositionables = numPositionables;
		numPositionables = list.size();
		numPositionablesToClear = Math.max(0, prevNumPositionables-numPositionables);
		if(numPositionables<=positionableWorkArray.length)
		 list.toArray(positionableWorkArray);
		return list;
	    }}).get();}catch(Exception e){throw new RuntimeException(e);}
	    
		for (int i = 0; i < numPositionables; i++) {
		  //Lock occurs inside one loop-level to reduce render pauses.
		    synchronized(tr.getThreadManager().gameStateLock){
		    final WorldObject left = (WorldObject)positionableWorkArray[i];
		    for (int j = i + 1; j < numPositionables; j++) {
			final WorldObject right = (WorldObject)positionableWorkArray[j];
			if (left!=null && right!=null){
			 if (left.isActive()&& right.isActive()){
			  if(TR.sloppyTwosComplimentTaxicabDistanceXZ(left.getPosition(),
			   right.getPosition()) < MAX_CONSIDERATION_DISTANCE) {
			     left.proposeCollision(right);
			     right.proposeCollision(left);
			     }//end if(distance<MAX_CONSIDERATION)
			 }//end if(both are active)
			}//end if(left/right !=null)
		    }// end for(j)
		}// end sync(gameStateLock)
		}// end for(i)
		//Erase removed items
		final int endPoint = numPositionables+numPositionablesToClear;
		for(int i=numPositionables; i<endPoint; i++)
		    positionableWorkArray[i]=null;
    }//end performCollisionTests

    public void remove(WorldObject worldObject) {
	List<Positionable> cL = this.getCurrentlyActiveCollisionList();
	synchronized(cL){
	    getCurrentlyActiveCollisionList().remove(worldObject);}
    }//end remove(...)

    public List<Positionable> getCurrentlyActiveCollisionList() {
	return collisionList[flip ? 1 : 0];
    }

    private List<Positionable> getWriteCollisionList() {
	return collisionList[flip ? 0 : 1];
    }

    /**
     * @return the inputRelevanceList
     */
    public Collection<Positionable> getInputRelevanceCollection() {
        return inputRelevanceList;
    }
}// end CollisionManager
