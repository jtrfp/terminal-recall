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
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.ConsolidatingCollectionActionPacker;
import org.jtrfp.trcl.core.TR;

import com.ochafik.util.listenable.Pair;

public class CollisionManager {
    public static final double MAX_CONSIDERATION_DISTANCE = TR.mapSquareSize * 15;
    public static final int    SHIP_COLLISION_DISTANCE    = 15000;
    private final TR tr;
    private final ArrayList<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>relevancePairs = new ArrayList<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>();
    private final ConsolidatingCollectionActionPacker<Positionable, Vector3D>       inputRelevancePairCollection = 
	     new ConsolidatingCollectionActionPacker<Positionable, Vector3D>(relevancePairs);
    private final ArrayList<Pair<Vector3D,Collection<Positionable>>>                pairBuffer     = new ArrayList<Pair<Vector3D,Collection<Positionable>>>();
    private final ArrayDeque<Collection<Positionable>>                              collectionPool = new ArrayDeque<Collection<Positionable>>();
    
    public CollisionManager(TR tr) {
	this.tr = tr;
    }
    
    public void newPerformCollisionTests(){
	try{World.relevanceExecutor.submit(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		for(Pair<Vector3D,CollectionActionDispatcher<Positionable>> cube:relevancePairs){
		    Collection<Positionable> col = newCube(); col.addAll(cube.getValue());
		    pairBuffer.add(new Pair<Vector3D,Collection<Positionable>>(cube.getKey(),col));
		}//end for(relevanceCubes)
		return null;
	    }}).get();}catch(Exception e){e.printStackTrace();}
	
	Collection<Positionable> everywhere=null;
	synchronized(tr.getThreadManager().gameStateLock){//Process cubes
	    for(Pair<Vector3D,Collection<Positionable>> cube:pairBuffer){
		if(!cube.getKey().equals(World.VISIBLE_EVERYWHERE))
		    processCubes(cube.getValue(),cube.getValue());
		else everywhere = cube.getValue();
	    }//end for(relevanceCubes)
	}if(everywhere!=null){synchronized(tr.getThreadManager().gameStateLock){//Process "everywhere" items.
	    for(Pair<Vector3D,Collection<Positionable>> cube:pairBuffer){
		processCubes(everywhere,cube.getValue());
		processCubes(cube.getValue(),everywhere);
	    }//end for(relevanceCubes) (relevant everywhere)
	}}//end sync(gameStateLock)
	for(Pair<Vector3D,Collection<Positionable>> cube:pairBuffer){
	    Collection<Positionable> col = cube.getValue();
	    col.clear();
	    collectionPool.add(col);
	}pairBuffer.clear();
    }//end newPerformCollisionTests()
    
    private Collection<Positionable> newCube(){
	Collection<Positionable> result = collectionPool.poll();
	if(result==null)         result = new ArrayList<Positionable>();
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

    /**
     * @return the inputRelevanceCubeCollection
     */
    public Collection<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> getInputRelevancePairCollection() {
        return inputRelevancePairCollection;
    }
}// end CollisionManager
