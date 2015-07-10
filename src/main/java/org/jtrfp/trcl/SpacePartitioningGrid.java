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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionPacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.PredicatedORCollectionActionFilter;
import org.jtrfp.trcl.coll.PropertyBasedTagger;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.RelevantEverywhere;

import com.ochafik.util.Adapter;
import com.ochafik.util.listenable.Pair;

public class SpacePartitioningGrid<E extends Positionable>{
	private double 				squareSize, viewingRadius;
	private int 				squaresX, squaresY, squaresZ;
	private final List<E> 			alwaysVisible  = new ArrayList<E>(300);
	private final HashSet<E>		localTaggerSet = new HashSet<E>();
	private Map<SpacePartitioningGrid<E>,String>
						branchGrids = 
	   Collections.synchronizedMap(new WeakHashMap<SpacePartitioningGrid<E>,String>());
	private final com.ochafik.util.listenable.Adapter<PropertyChangeEvent,Vector3D>cubeSpaceQuantizingAdapter = new com.ochafik.util.listenable.Adapter<PropertyChangeEvent,Vector3D>(){
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
	private final PredicatedORCollectionActionFilter<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> packedObjectValve =
		new PredicatedORCollectionActionFilter<Pair<Vector3D,CollectionActionDispatcher<Positionable>>>(packedObjectsDispatcher);
	private final CollectionActionPacker<Positionable,Vector3D> objectPacker = new CollectionActionPacker<Positionable,Vector3D>(packedObjectValve.input);
	private final Collection<Positionable> localTagger
	 = new CollectionThreadDecoupler<Positionable>(new PropertyBasedTagger<Positionable, Vector3D, Vector3D>(objectPacker, cubeSpaceQuantizingAdapter, Positionable.POSITION,World.relevanceExecutor),World.relevanceExecutor);
	
	private  List<E> []     elements;
	private double 		radiusInWorldUnits;
	private int 		rawDia,
				rawDiaX,rawDiaY;
		
	public SpacePartitioningGrid(){
	    packedObjectValve.add(TruePredicate.INSTANCE);
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
	
	public synchronized void addBranch(SpacePartitioningGrid<E> toAdd){
	    toAdd.getPackedObjectsDispatcher().addTarget(packedObjectValve.input, true);
	    branchGrids.put(toAdd, null);
	}
	
	public synchronized void removeBranch(SpacePartitioningGrid<E> toRemove){
	    toRemove.getPackedObjectsDispatcher().removeTarget(packedObjectValve.input, true);
	    branchGrids.remove(toRemove);
	}
	
	public CollectionActionDispatcher<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> getPackedObjectsDispatcher(){
	    return packedObjectsDispatcher;
	}
	private static double absMod(double value, double mod){
	    if(value>=-0.)
	    {return value%mod;}
	    value*=-1;
	    value%=mod;
	    if(value==0)return 0;
	    return mod-value;
	}//end absMod
    private void recursiveAlwaysVisibleSubmit(Submitter<E> sub) {
	sub.submit(alwaysVisible);
	synchronized(branchGrids){
	 for(SpacePartitioningGrid<E> g:branchGrids.keySet())
	     g.recursiveAlwaysVisibleSubmit(sub);
	 }//end sync(branchGrids)
    }// end recursiveAlwaysVisisbleSubmit(...)

    private void recursiveAlwaysVisibleGridCubeSubmit(Submitter<List<E>> sub) {
	sub.submit(alwaysVisible);
	synchronized(branchGrids){
	
	for(SpacePartitioningGrid<E> g:branchGrids.keySet())
	     g.recursiveAlwaysVisibleGridCubeSubmit(sub);
	}//end sync(branchGrids)
    }// end recursiveAlwaysVisisbleSubmit(...)
	
    private void recursiveBlockSubmit(Submitter<E> sub, int blockID) {
	final List<E> elements = this.elements[blockID];
	if (elements != null) {
	    synchronized (elements) {
		final int size = elements.size();
		for (int i = 0; i < size; i++) {
		    sub.submit(elements.get(i));
		}// end for(size)
	    }// end sync(elements)
	}// end if(!null)
	synchronized(branchGrids){
	for(SpacePartitioningGrid<E> g:branchGrids.keySet())
	     g.recursiveBlockSubmit(sub, blockID);
	}//end sync(branchGrids)
    }// end recusiveBlockSubmit(...)

    private void recursiveGridCubeSubmit(Submitter<List<E>> sub, int blockID) {
	sub.submit(elements[blockID]);
	synchronized(branchGrids){
	for(SpacePartitioningGrid<E> g:branchGrids.keySet())
	     g.recursiveGridCubeSubmit(sub, blockID);
	}//end sync(branchGrids)
    }// end recusiveGridCubeSubmit(...)

	private Collection<E> getAlwaysVisible()
		{return alwaysVisible;}

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
	
	public void removeAll(){
		final ArrayList<SpacePartitioningGrid> branches = new ArrayList<SpacePartitioningGrid>();
		for(SpacePartitioningGrid g:branchGrids.keySet())
		    branches.add(g);
		for(SpacePartitioningGrid g:branches)
		    removeBranch(g);
		final ArrayList<E> alwaysVisible = new ArrayList<E>();
		localTagger.clear();
		return;
	}//end removeAll()
}//end SpacePartitionGrid
