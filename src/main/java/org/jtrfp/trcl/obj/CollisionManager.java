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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.ConsolidatingCollectionActionPacker;
import org.jtrfp.trcl.core.CubeCoordinate;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;

import com.ochafik.util.listenable.Pair;

public class CollisionManager {
    public static final double MAX_CONSIDERATION_DISTANCE = TRFactory.mapSquareSize * 15;
    public static final int    SHIP_COLLISION_DISTANCE    = 15000;
    private static final double ROLLOVER_POINT            = World.WORLD_WIDTH_CUBES/2.;
    private final TR tr;
    private final ArrayList<Pair<CubeCoordinate,CollectionActionDispatcher<Positionable>>>relevancePairs = new ArrayList<Pair<CubeCoordinate,CollectionActionDispatcher<Positionable>>>();
    private final ConsolidatingCollectionActionPacker<Positionable, CubeCoordinate>       inputRelevancePairCollection = 
	     new ConsolidatingCollectionActionPacker<Positionable, CubeCoordinate>(relevancePairs);
    private final HashMap<CubeCoordinate,Collection<Positionable>>                        pairBuffer     = new HashMap<CubeCoordinate,Collection<Positionable>>();
    private final ArrayDeque<Collection<Positionable>>                              collectionPool = new ArrayDeque<Collection<Positionable>>();
    
    public CollisionManager(TR tr) {
	this.tr = tr;
    }
    
    public void newPerformCollisionTests(){
	//Obtain a thread-local copy
	try{World.relevanceExecutor.submit(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		for(Pair<CubeCoordinate,CollectionActionDispatcher<Positionable>> relevancePair:relevancePairs){
		    Collection<Positionable> newCube = newCube(); newCube.addAll(relevancePair.getValue());
		    pairBuffer.put(relevancePair.getKey(),newCube);
		}//end for(relevanceCubes)
		return null;
	    }}).get();}catch(Exception e){e.printStackTrace();}
	//Process non-everywhere cubes
	Entry<CubeCoordinate,Collection<Positionable>> everywhere=null;
	synchronized(tr.getThreadManager().gameStateLock){//Process cubes
	    for(Entry<CubeCoordinate,Collection<Positionable>> cube:pairBuffer.entrySet()){
		 CubeCoordinate orig = cube.getKey();
		if(!World.RELEVANT_EVERYWHERE.equals(orig)){
		    final Collection<Positionable> thisCube = cube.getValue();
		    //Intra-cube
		    processCubes(thisCube,thisCube);
		    processNeighbors(orig,thisCube);
		    
		    //orig = new CubeCoordinate(orig.getX(),incLoop(orig.getY()),orig.getZ());
		    //processNeighbors(orig,thisCube);
		    }
		else {//EVERYWHERE
		    if(everywhere==null)
		     everywhere = cube;
		    else
			throw new RuntimeException("Intolerable multiple 'everywhere' cubes. Found "+
				cube.getKey()+" but already had "+
				everywhere.getKey());
		    }//end EVERYWHERE
	    }//end for(relevanceCubes)
	}if(everywhere!=null){synchronized(tr.getThreadManager().gameStateLock){//Process "everywhere" items.
	    final boolean wasPresent = pairBuffer.remove(everywhere.getKey()) != null;
	    assert wasPresent;
	    assert !pairBuffer.containsKey(everywhere.getKey());
	    processCubes(everywhere.getValue(),everywhere.getValue());
	    for(Entry<CubeCoordinate,Collection<Positionable>> cube:pairBuffer.entrySet()){
		processCubes(everywhere.getValue(),cube.getValue());
		processCubes(cube.getValue(),everywhere.getValue());
	    }//end for(relevanceCubes) (relevant everywhere)
	}}//end sync(gameStateLock)
	for(Entry<CubeCoordinate,Collection<Positionable>> cube:pairBuffer.entrySet()){
	    Collection<Positionable> col = cube.getValue();
	    col.clear();
	    collectionPool.add(col);
	}pairBuffer.clear();
    }//end newPerformCollisionTests()
    
    private void processNeighbors(CubeCoordinate orig, Collection<Positionable> thisCube){
	int x=orig.getX(), y=orig.getY(), z=orig.getZ();
	int x1=incLoop(x), y1=incLoop(y), z1=incLoop(z);
	Collection<Positionable> other;
	//X,Z+1
	other = pairBuffer.get(new CubeCoordinate(x,y,z1));
	if(other != null)
	    bidiProcessCubes(thisCube,other);
	//X+1,Z
	other = pairBuffer.get(new CubeCoordinate(x1,y,z));
	if(other != null)
	    bidiProcessCubes(thisCube,other);
	//X+1, Z+1
	other = pairBuffer.get(new CubeCoordinate(x1,y,z1));
	if(other != null)
	    bidiProcessCubes(thisCube,other);

	//// Y+1
	//X,Z+1
	other = pairBuffer.get(new CubeCoordinate(x,y1,z1));
	if(other != null)
	    bidiProcessCubes(thisCube,other);
	//X+1,Z
	other = pairBuffer.get(new CubeCoordinate(x1,y1,z));
	if(other != null)
	    bidiProcessCubes(thisCube,other);
	//X+1, Z+1
	other = pairBuffer.get(new CubeCoordinate(x1,y1,z1));
	if(other != null)
	    bidiProcessCubes(thisCube,other);
    }//end processNeighbors
    
    private int incLoop(int coord){
	coord++;
	while(coord >= ROLLOVER_POINT)
	    coord -= World.WORLD_WIDTH_CUBES;
	return coord;
    }//end incLoop(...)
    
    private Collection<Positionable> newCube(){
	Collection<Positionable> result = collectionPool.poll();
	if(result==null)         result = new ArrayList<Positionable>();
	return result;
    }
    
    private void bidiProcessCubes(Collection<Positionable> leftCube,Collection<Positionable> rightCube){
	processCubes(leftCube,rightCube);
	processCubes(rightCube,leftCube);
    }//end bidiProcessCubes
    
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
    public Collection<Pair<CubeCoordinate,CollectionActionDispatcher<Positionable>>> getInputRelevancePairCollection() {
        return inputRelevancePairCollection;
    }
}// end CollisionManager
