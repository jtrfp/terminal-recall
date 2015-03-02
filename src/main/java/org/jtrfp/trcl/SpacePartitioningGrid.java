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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.Positionable;

import com.ochafik.util.Adapter;

public abstract class SpacePartitioningGrid<E extends Positionable>{
	private double 				squareSize, viewingRadius;
	private int 				squaresX, squaresY, squaresZ;
	private final List<E> 			alwaysVisible = new ArrayList<E>(300);
	private WeakReference<SpacePartitioningGrid<E>> 	
						parentGrid = null;
	private Map<SpacePartitioningGrid<E>,String>
						branchGrids = 
	   Collections.synchronizedMap(new WeakHashMap<SpacePartitioningGrid<E>,String>());
	
	private  List<E> []     elements;
	private double 		radiusInWorldUnits;
	private int 		rolloverPoint,
				rawDia,
				rawDiaX,rawDiaY,rawDiaZ,
				xProgression,yProgression,zProgression;
	private final Adapter<Vector3D,Integer> cubeSpaceRasterizer = new Adapter<Vector3D,Integer>(){
	    @Override
	    public Integer adapt(Vector3D value) {
		return (int)(
			value.getX()+
			value.getY()*squaresX+
			value.getZ()*squaresX*squaresY);
	    }//end adapt()

	    @Override
	    public Vector3D reAdapt(Integer value) {
		// TODO Auto-generated method stub
		return null;
	    }};

	    private final Adapter<Vector3D,Integer> worldSpaceRasterizer = new Adapter<Vector3D,Integer>(){
		@Override
		public Integer adapt(Vector3D value) {
		    return (int)(
			    (int)absMod(Math.round(value.getX()/getSquareSize()),squaresX)+
			    (int)absMod(Math.round(value.getY()/getSquareSize()),squaresY)*squaresX+
			    (int)absMod(Math.round(value.getZ()/getSquareSize()),squaresZ)*squaresX*squaresY);
		}//end adapt()

		@Override
		public Vector3D reAdapt(Integer value) {
		    // TODO Auto-generated method stub
		    return null;
		}};
	
	public SpacePartitioningGrid(SpacePartitioningGrid<E> parentGrid)
		{setParentGrid(parentGrid);}
	
    public void activate() {
	if (parentGrid != null) {
	    final SpacePartitioningGrid g = parentGrid.get();
	    if (g != null)
		g.addBranch(this);
	}
    }//end activate()
    
    public Adapter<Vector3D,Integer> getCubeSpaceRasterizer(){
	return cubeSpaceRasterizer;
    }
    
    public Adapter<Vector3D,Integer> getWorldSpaceRasterizer(){
	return worldSpaceRasterizer;
    }

    public void deactivate() {
	if (parentGrid != null) {
	    final SpacePartitioningGrid g = parentGrid.get();
	    if (g != null)
		g.removeBranch(this);
	}
    }//end deactivate()
    
    public void notifyBranchAdded(SpacePartitioningGrid b){
	final SpacePartitioningGrid<E> g = parentGrid.get();
	    if (g != null)
		g.notifyBranchAdded(b);
    }//end notifyBranchAdded(...)
    
    public void notifyBranchRemoved(SpacePartitioningGrid b){
	final SpacePartitioningGrid<E> g = parentGrid.get();
	    if (g != null)
		g.notifyBranchRemoved(b);
    }//end notifyBranchRemoved(...)

	private void addBranch(SpacePartitioningGrid<E> branchToAdd)
		{if(!branchGrids.containsKey(branchToAdd)){
		    branchGrids.put(branchToAdd,"");
		    if(parentGrid==null)return;
		    final SpacePartitioningGrid<E> g = parentGrid.get();
		    if (g != null)
			g.notifyBranchAdded(branchToAdd);
		    }//end if(!contains)
		}//end addBranch(...)
	private void removeBranch(SpacePartitioningGrid<E> branchToRemove)
		{if(branchGrids.remove(branchToRemove)!=null){
		    if(parentGrid==null)return;
		    final SpacePartitioningGrid<E> g = parentGrid.get();
		    if (g != null)
			g.notifyBranchRemoved(branchToRemove);
		    }//end if(!contains)
		}//end removeBranch(...)
	
	private void setParentGrid(SpacePartitioningGrid<E> parentGrid){
		this.parentGrid=new WeakReference<SpacePartitioningGrid<E>>(parentGrid);
		setSquareSize(parentGrid.getSquareSize());
		setSquaresX(parentGrid.getSquaresX());
		setSquaresY(parentGrid.getSquaresY());
		setSquaresZ(parentGrid.getSquaresZ());
		setViewingRadius(parentGrid.getViewingRadius());
		
		allocateSquares();
		}//end setParentGrid(...)

	public SpacePartitioningGrid(Vector3D size, double squareSize, double viewingRadius){
		setSquareSize(squareSize);
		setSquaresX((int)(size.getX()/squareSize));
		setSquaresY((int)(size.getY()/squareSize));
		setSquaresZ((int)(size.getZ()/squareSize));
		setViewingRadius(viewingRadius);
		
		allocateSquares();
		}//end constructor
	
	private void allocateSquares(){
		elements = new List[squaresX*squaresY*squaresZ];
		//Fudge factor to fix suddenly appearing terrain at distance
		radiusInWorldUnits	=getViewingRadius()*1.25;
		rolloverPoint		=elements.length;
		rawDia			=(int)((radiusInWorldUnits*2)/getSquareSize());
		rawDiaX			=rawDia<getSquaresX()?rawDia:getSquaresX();
		rawDiaY			=rawDia<getSquaresY()?rawDia:getSquaresY();
		rawDiaZ			=rawDia<getSquaresZ()?rawDia:getSquaresZ();
		zProgression		=getSquaresX()*getSquaresY()-rawDiaY*getSquaresX();
		yProgression		=getSquaresX()-rawDiaX;
		xProgression=1;
		}//end allocateSquares()
	
	public synchronized void add(E objectWithPosition){
	    	//Figure out where it goes
	    	if(objectWithPosition==null)throw new NullPointerException("Passed objectWithPosition is intolerably null.");
	    	objectWithPosition.setContainingGrid(this);
		}
	public synchronized void remove(E objectWithPosition){
	    objectWithPosition.setContainingGrid(null);
	}//end remove(...)
	private static double absMod(double value, double mod){
		if(value>=-0.)
			{return value%mod;}
		value*=-1;
		value%=mod;
		if(value==0)return 0;
		return mod-value;
		}//end absMod
	
	public void cubesWithinRadiusOf(Vector3D centerInWorldUnits, Submitter<List<E>> submitter){
	    recursiveAlwaysVisibleGridCubeSubmit(submitter);
	    final double [] startPoint=centerInWorldUnits.subtract(new Vector3D(radiusInWorldUnits,radiusInWorldUnits,radiusInWorldUnits)).toArray();
		int startRaw = worldSpaceRasterizer.adapt(new Vector3D(startPoint[0],startPoint[1],startPoint[2]));
		
		final int zEnd=startRaw+getSquaresX()*getSquaresY()*rawDiaZ + (rawDiaY*getSquaresX()) + (rawDiaX);
		for(int point=startRaw; point<zEnd; point+=zProgression){//Z
			final int yEnd=point+getSquaresX()*rawDiaY;
			for(;point<yEnd; point+=yProgression){//Y
				final int xEnd=point+rawDiaX;
				for(;point<xEnd; point+=xProgression){//X
					final int wrappedPoint=point%rolloverPoint;
					recursiveGridCubeSubmit(submitter,wrappedPoint);
					}//end for(X)
				}//end for(Y)
			}//end for(Z)
	}//end cubesWithRadiusOf(...)
	
	@SuppressWarnings("unchecked")
	public void itemsWithinRadiusOf(Vector3D centerInWorldUnits, Submitter<E> submitter){
		recursiveAlwaysVisibleSubmit(submitter);
		
		final double [] startPoint=centerInWorldUnits.subtract(new Vector3D(radiusInWorldUnits,radiusInWorldUnits,radiusInWorldUnits)).toArray();
		int startRaw = worldSpaceRasterizer.adapt(new Vector3D(startPoint[0],startPoint[1],startPoint[2]));
		
		final int zEnd=startRaw+getSquaresX()*getSquaresY()*rawDiaZ + (rawDiaY*getSquaresX()) + (rawDiaX);
		for(int point=startRaw; point<zEnd; point+=zProgression){//Z
			final int yEnd=point+getSquaresX()*rawDiaY;
			for(;point<yEnd; point+=yProgression){//Y
				final int xEnd=point+rawDiaX;
				for(;point<xEnd; point+=xProgression){//X
					final int wrappedPoint=point%rolloverPoint;
					recursiveBlockSubmit(submitter,wrappedPoint);
					}//end for(X)
				}//end for(Y)
			}//end for(Z)
		}//end itemsInRadiusOf(...)
	
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

	public List<E> world2List(double x, double y,
		double z, boolean newListIfNull) {
	    final int pos = worldSpaceRasterizer.adapt(new Vector3D(x,y,z));
	    List<E> result = elements[pos];
	    if(newListIfNull && result==null)
		result = elements[pos] = new ArrayList<E>(8);
	    return result;
	}//end world2List
	
	public List<E> getAlwaysVisibleList(){
	    return alwaysVisible;
	}
	
	public void removeAll(){
	    final ArrayList<SpacePartitioningGrid> branches = new ArrayList<SpacePartitioningGrid>();
	    for(SpacePartitioningGrid g:branchGrids.keySet())
		branches.add(g);
	    for(SpacePartitioningGrid g:branches)
		removeBranch(g);
	    final ArrayList<E> alwaysVisible = new ArrayList<E>();
	    for(E e:getAlwaysVisibleList())
		alwaysVisible.add(e);
	    for(E e:alwaysVisible)
		remove(e);
	    final ArrayList<E> everythingElse = new ArrayList<E>();
	    for(List<E> l:elements)
		if(l!=null)
		 for(E e:l)
		  everythingElse.add(e);
	    for(E e:everythingElse)
		remove(e);
	}//end removeAll()
}//end SpacePartitionGrid
