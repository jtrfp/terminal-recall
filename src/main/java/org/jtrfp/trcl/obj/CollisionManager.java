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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.ConsolidatingCollectionActionPacker;
import org.jtrfp.trcl.core.TR;

import com.ochafik.util.listenable.Pair;

public class CollisionManager {
    public static final double MAX_CONSIDERATION_DISTANCE = TR.mapSquareSize * 15;
    private final TR tr;
    private final ArrayList<Positionable>[] collisionList = new ArrayList[2];//Collideable objects are O(.5n^2-.5n)  !!!
    public static final int SHIP_COLLISION_DISTANCE = 15000;
    private volatile boolean flip = false;
    private final List<Positionable> inputRelevanceList = new ArrayList<Positionable>();
    //private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> relevancePairs = new ArrayList<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>();
    private final ConsolidatingCollectionActionPacker<Positionable, Vector3D> inputRelevancePairCollection = 
	     new ConsolidatingCollectionActionPacker<Positionable, Vector3D>(relevancePairs);
    private final ArrayList<Pair<Vector3D,Collection<Positionable>>> cubeBuffer = new ArrayList<Pair<Vector3D,Collection<Positionable>>>();
    
    private final ArrayDeque<Collection<Positionable>> unusedCubes = new ArrayDeque<Collection<Positionable>>();
    
    public CollisionManager(TR tr) {
	this.tr = tr;
	collisionList[0] = new ArrayList<Positionable>(1024);
	collisionList[1] = new ArrayList<Positionable>(1024);
    }
    private final Positionable [] positionableWorkArray = new Positionable[4096];
    private volatile int numPositionables =0;
    private int numPositionablesToClear=0;
    
    public void newPerformCollisionTests(){
	try{World.relevanceExecutor.submit(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		for(Pair<Vector3D,CollectionActionDispatcher<Positionable>> cube:relevancePairs){
		    Collection<Positionable> col = newCube(); col.addAll(cube.getValue());
		    cubeBuffer.add(new Pair<Vector3D,Collection<Positionable>>(cube.getKey(),col));
		}//end for(relevanceCubes)
		return null;
	    }}).get();}catch(Exception e){e.printStackTrace();}
	
	Collection<Positionable> everywhere=null;
	synchronized(tr.getThreadManager().gameStateLock){
	    for(Pair<Vector3D,Collection<Positionable>> cube:cubeBuffer){
		if(!cube.getKey().equals(World.VISIBLE_EVERYWHERE))
		    processCubes(cube.getValue(),cube.getValue());
		else everywhere = cube.getValue();
	    }//end for(relevanceCubes)
	}synchronized(tr.getThreadManager().gameStateLock){
	    for(Pair<Vector3D,Collection<Positionable>> cube:cubeBuffer){
		processCubes(everywhere,cube.getValue());
		processCubes(cube.getValue(),everywhere);
	    }//end for(relevanceCubes) (relevant everywhere)
	}//end sync(gameStateLock)
	for(Pair<Vector3D,Collection<Positionable>> cube:cubeBuffer){
	    Collection<Positionable> col = cube.getValue();
	    col.clear();
	    unusedCubes.add(col);
	}
	cubeBuffer.clear();
    }//end newPerformCollisionTests()
    
    private Collection<Positionable> newCube(){
	Collection<Positionable> result = unusedCubes.poll();
	if(result==null)result = new ArrayList<Positionable>();
	return result;
    }
    
    private void processCubes(Collection<Positionable> leftCube,Collection<Positionable> rightCube){
	for(Positionable left:leftCube){
	    final WorldObject l = (WorldObject)left;
	    for(Positionable right:rightCube){
		if(left!=right){
		 final WorldObject r = (WorldObject)right;
		 if(l.isActive()&&r.isActive())
		  l.proposeCollision(r);
		}//end if(left!=right)
	    }//end for(right)
	}//end for(left)
    }//end processCubes(...)

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

    /**
     * @return the inputRelevanceCubeCollection
     */
    public Collection<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> getInputRelevancePairCollection() {
        return inputRelevancePairCollection;
    }
}// end CollisionManager
