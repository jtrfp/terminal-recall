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
package org.jtrfp.trcl;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionPacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.PropertyBasedTagger;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.RelevantEverywhere;

import com.ochafik.util.listenable.Pair;

public class SpacePartitioningGrid<E extends Positionable>{
	private double 				squareSize, viewingRadius;
	private int 				squaresX, squaresY, squaresZ;
	private final HashSet<E>		localTaggerSet = new HashSet<E>();
	private Map<SpacePartitioningGrid<E>,String>
						branchGrids = 
	   Collections.synchronizedMap(new WeakHashMap<SpacePartitioningGrid<E>,String>());
	private static final com.ochafik.util.listenable.Adapter<PropertyChangeEvent,Vector3D>cubeSpaceQuantizingAdapter = new com.ochafik.util.listenable.Adapter<PropertyChangeEvent,Vector3D>(){
	    @Override
	    public Vector3D adapt(PropertyChangeEvent evt) {
		final int granularity = World.CUBE_GRANULARITY;
		if(evt.getSource() instanceof RelevantEverywhere)
		    return World.VISIBLE_EVERYWHERE;
		final double[] newPos = (double[])evt.getNewValue();
		final Vector3D newCenterCube = new Vector3D(
			Math.rint(newPos[0]/granularity),
			Math.rint(newPos[1]/granularity),
			Math.rint(newPos[2]/granularity));
		return newCenterCube;
	    }
	};
	private final CollectionActionDispatcher<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> packedObjectsDispatcher =
		new CollectionActionDispatcher<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>(new ArrayList<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>());
	/*private final PredicatedORCollectionActionFilter<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> packedObjectValve =
		new PredicatedORCollectionActionFilter<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>(packedObjectsDispatcher);*/
	private final CollectionActionPacker<Positionable,Vector3D> objectPacker = new CollectionActionPacker<Positionable,Vector3D>(packedObjectsDispatcher);
	private final Collection<Positionable> localTagger
	 = new CollectionThreadDecoupler<Positionable>(new PropertyBasedTagger<Positionable, Vector3D, Vector3D>(objectPacker, cubeSpaceQuantizingAdapter, Positionable.POSITION,World.relevanceExecutor),World.relevanceExecutor);
	
	private  List<E> []     elements;
		
	public SpacePartitioningGrid(){
	    //packedObjectValve.add(TruePredicate.INSTANCE);
	}
    public Future<?> nonBlockingAddBranch(final SpacePartitioningGrid<E> branchToAdd){
	return World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		addBranch(branchToAdd);
	    }});
    }//end nonBlockingAddBranch
    
    public void blockingAddBranch(SpacePartitioningGrid<E> branchToAdd){
	try{nonBlockingAddBranch(branchToAdd).get();}catch(Exception e){}
    }
    
    public Future<?> nonBlockingRemoveBranch(final SpacePartitioningGrid<E> branchToRemove){
	return World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		removeBranch(branchToRemove);
	    }});
    }//end nonBlockingAddBranch
    
    public void blockingRemoveBranch(SpacePartitioningGrid<E> branchToRemove){
	try{nonBlockingRemoveBranch(branchToRemove).get();}catch(Exception e){}
    }

	public SpacePartitioningGrid(Vector3D size, double squareSize, double viewingRadius){
	    this();
	}//end constructor
	
	public synchronized void add(E objectToAdd){//TODO: Enforce set instead?
	    if(!localTaggerSet.add(objectToAdd))
		return;
	    localTagger.add(objectToAdd);
	    objectToAdd.setContainingGrid(this);
	}
	
	public synchronized void remove(E objectToRemove){
	    if(!localTaggerSet.remove(objectToRemove))
		return;
	    localTagger.remove(objectToRemove);
	}
	
	public synchronized boolean containsBranch(SpacePartitioningGrid<E> toFind){
	    return branchGrids.containsKey(toFind);
	}
	
	public synchronized void addBranch(SpacePartitioningGrid<E> toAdd){
	    toAdd.getPackedObjectsDispatcher().addTarget(packedObjectsDispatcher, true);
	    branchGrids.put(toAdd, null);
	}
	
	public synchronized void removeBranch(SpacePartitioningGrid<E> toRemove){
	    toRemove.getPackedObjectsDispatcher().removeTarget(packedObjectsDispatcher, true);
	    branchGrids.remove(toRemove);
	}
	
	public CollectionActionDispatcher<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> getPackedObjectsDispatcher(){
	    return packedObjectsDispatcher;
	}
	
	/**
	 * @return the squareSize
	 */
	public double getSquareSize(){
		return squareSize;
		}
	/**
	 * @param squareSize the squareSize to set
	 */
	public void setSquareSize(double squareSize){
		this.squareSize = squareSize;
		}
	
	/**
	 * @return the squaresX
	 */
	public int getSquaresX(){
		return squaresX;
		}
	/**
	 * @param squaresX the squaresX to set
	 */
	public void setSquaresX(int squaresX){
		if(squaresX<=0)throw new RuntimeException("Invalid size: "+squaresX);
		this.squaresX = squaresX;
		}
	/**
	 * @return the squaresY
	 */
	public int getSquaresY(){
		return squaresY;
		}
	/**
	 * @param squaresY the squaresY to set
	 */
	public void setSquaresY(int squaresY){
		if(squaresY<=0)throw new RuntimeException("Invalid size: "+squaresY);
		this.squaresY = squaresY;
		}
	/**
	 * @return the squaresZ
	 */
	public int getSquaresZ(){
		return squaresZ;
		}
	/**
	 * @param squaresZ the squaresZ to set
	 */
	public void setSquaresZ(int squaresZ){
		if(squaresZ<=0)throw new RuntimeException("Invalid size: "+squaresZ);
		this.squaresZ = squaresZ;
		}

	/**
	 * @return the viewingRadius
	 */
	public double getViewingRadius(){
		return viewingRadius;
		}

	/**
	 * @param viewingRadius the viewingRadius to set
	 */
	public void setViewingRadius(double viewingRadius){
		this.viewingRadius = viewingRadius;
		}

	public void removeDirect(int flatPos, E objectWithPosition) {
	    List<E> list = elements[flatPos];
	    if(list==null)
		return;
	    synchronized(list){
	     if(list.remove(objectWithPosition))
		objectWithPosition.setContainingGrid(null);
	     elements[flatPos].remove(objectWithPosition);
	     }//end sync(list)
	}//end removeDirect(...)

	public void addDirect(int flatPos, E objectWithPosition) {
	    List<E> list = elements[flatPos];
	    if(list==null)
		elements[flatPos] = list = new ArrayList<E>(8);
	    synchronized(list){
	     list.add(objectWithPosition);}
	}//end addDirect(...)
	
	public Future<?> nonBlockingRemoveAll(){
		return World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			removeAll();
		    }});
	    }//end nonBlockingAddBranch
	    
	    public void blockingRemoveAll(){
		try{nonBlockingRemoveAll().get();}catch(Exception e){}
	    }
	
	public void removeAll(){
		final ArrayList<SpacePartitioningGrid<E>> branches = new ArrayList<SpacePartitioningGrid<E>>();
		for(SpacePartitioningGrid<E> g:branchGrids.keySet())
		    branches.add(g);
		for(SpacePartitioningGrid<E> g:branches)
		    removeBranch(g);
		localTagger.clear();
		return;
	}//end removeAll()
}//end SpacePartitionGrid
