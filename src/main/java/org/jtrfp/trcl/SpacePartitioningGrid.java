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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.PositionListenable;
import org.jtrfp.trcl.obj.PositionListener;
import org.jtrfp.trcl.obj.VisibleEverywhere;

public abstract class SpacePartitioningGrid<E extends PositionListenable>{
	private double 				squareSize, viewingRadius;
	private Object [] 			gridSquares;
	private int 				squaresX, squaresY, squaresZ;
	private final GridCube 			alwaysVisible = new GridCube(null);
	private WeakReference<SpacePartitioningGrid<E>> 	
						parentGrid = null;
	private List<SpacePartitioningGrid<E>> 	branchGrids = Collections
		.synchronizedList(new ArrayList<SpacePartitioningGrid<E>>());
	
	private double 		radiusInWorldUnits;
	private int 		rolloverPoint,
				rawDia,
				rawDiaX,rawDiaY,rawDiaZ,
				xProgression,yProgression,zProgression;
	
	public SpacePartitioningGrid(SpacePartitioningGrid<E> parentGrid)
		{setParentGrid(parentGrid);}
	
    public void activate() {
	if (parentGrid != null) {
	    final SpacePartitioningGrid g = parentGrid.get();
	    if (g != null)
		g.addBranch(this);
	}
    }//end activate()

    public void deactivate() {
	if (parentGrid != null) {
	    final SpacePartitioningGrid g = parentGrid.get();
	    if (g != null)
		g.removeBranch(this);
	}
    }//end deactivate()

	private void addBranch(SpacePartitioningGrid<E> branchToAdd)
		{if(!branchGrids.contains(branchToAdd))branchGrids.add(branchToAdd);}
	private void removeBranch(SpacePartitioningGrid<E> branchToRemove)
		{branchGrids.remove(branchToRemove);}
	
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
		Object []squares = new Object[squaresX*squaresY*squaresZ];
		int squaresZMult=squaresX*squaresY;
		setGridSquares(squares);
		for(int z=0; z<squaresZ; z++)
			{for(int y=0; y<squaresY; y++)
				{for(int x=0; x<squaresX; x++)
					{squares[x+y*squaresX+z*squaresZMult]=(new GridCube(square2World(new double[]{x,y,z})));}
				}//end for(squaresY)
			}//end for(squaresZ)
		
		//Fudge factor to fix suddenly appearing terrain at distance
		radiusInWorldUnits	=getViewingRadius()*1.25;
		rolloverPoint		=gridSquares.length;
		rawDia			=(int)((radiusInWorldUnits*2)/getSquareSize());
		rawDiaX			=rawDia<getSquaresX()?rawDia:getSquaresX();
		rawDiaY			=rawDia<getSquaresY()?rawDia:getSquaresY();
		rawDiaZ			=rawDia<getSquaresZ()?rawDia:getSquaresZ();
		zProgression		=getSquaresX()*getSquaresY()-rawDiaY*getSquaresX();
		yProgression		=getSquaresX()-rawDiaX;
		xProgression=1;
		}//end allocateSquares()
	
	private int space2Flat(Vector3D space)
		{return (int)(space.getX()+space.getY()*squaresX+space.getZ()*squaresX*squaresY);}
	public synchronized void add(E objectWithPosition)
		{//Figure out where it goes
	    	if(objectWithPosition==null)throw new NullPointerException("Passed objectWithPosition is intolerably null.");
	    	objectWithPosition.setContainingGrid(this);
	    	if(objectWithPosition instanceof VisibleEverywhere){
		    addAlwaysVisible(objectWithPosition);return;}
		final GridCube dest = squareAtWorldCoord(objectWithPosition.getPosition());
		dest.add(objectWithPosition);
		}
	public synchronized void remove(E objectWithPosition){
	    if(objectWithPosition instanceof VisibleEverywhere){
		removeAlwaysVisible(objectWithPosition);
	    }else{
		final GridCube dest = squareAtWorldCoord(objectWithPosition.getPosition());
		dest.remove(objectWithPosition);
	    }
		}
	private void addAlwaysVisible(E objectWithPosition)
		{alwaysVisible.add(objectWithPosition);}
	private void removeAlwaysVisible(E objectWithPosition)
		{alwaysVisible.remove(objectWithPosition);}
	
	private static double absMod(double value, double mod){
		if(value>=-0.)
			{return value%mod;}
		value*=-1;
		value%=mod;
		if(value==0)return 0;
		return mod-value;
		}//end absMod
	
	private Vector3D world2Square(double[] ds){
		return new Vector3D(
				absMod(Math.round(ds[0]/getSquareSize()),squaresX),
				absMod(Math.round(ds[1]/getSquareSize()),squaresY),
				absMod(Math.round(ds[2]/getSquareSize()),squaresZ));
		}
	private double[] square2World(double[] ds){
		return new double[]{
				ds[0]*getSquareSize(),
				ds[1]*getSquareSize(),
				ds[2]*getSquareSize()};
		}//end square2World(...)
	
	protected GridCube squareAtWorldCoord(double[] ds)
		{return squareAtGridCoord(world2Square(ds));}
	
	@SuppressWarnings("unchecked")
	protected GridCube squareAtGridCoord(Vector3D gridCoord)
		{return (GridCube)(gridSquares[space2Flat(gridCoord)]);}
	
	public void cubesWithinRadiusOf(Vector3D centerInWorldUnits, Submitter<GridCube> submitter){
	    recursiveAlwaysVisibleGridCubeSubmit(submitter);
	    final double [] startPoint=centerInWorldUnits.subtract(new Vector3D(radiusInWorldUnits,radiusInWorldUnits,radiusInWorldUnits)).toArray();
		int startRaw=space2Flat(world2Square(startPoint));
		
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
		int startRaw=space2Flat(world2Square(startPoint));
		
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
	sub.submit(alwaysVisible.getElements());
	synchronized(branchGrids){
	final int size = branchGrids.size();
	for (int index = 0; index < size; index++) {
	    branchGrids.get(index).recursiveAlwaysVisibleSubmit(sub);
	}}
    }// end recursiveAlwaysVisisbleSubmit(...)

    private void recursiveAlwaysVisibleGridCubeSubmit(Submitter<GridCube> sub) {
	sub.submit(alwaysVisible);
	synchronized(branchGrids){
	final int size = branchGrids.size();
	for (int index = 0; index < size; index++) {
	    branchGrids.get(index).recursiveAlwaysVisibleGridCubeSubmit(sub);
	}}
    }// end recursiveAlwaysVisisbleSubmit(...)
	
    private void recursiveBlockSubmit(Submitter<E> sub, int blockID) {
	final List<E> elements = ((GridCube) gridSquares[blockID])
		.getElements();
	{
	    final int size = elements.size();
	    for (int i = 0; i < size; i++) {
		sub.submit(elements.get(i));
	    }
	}// end submit local elements
	synchronized(branchGrids){
	final int size = branchGrids.size();
	for (int index = 0; index < size; index++) {
	    branchGrids.get(index).recursiveBlockSubmit(sub, blockID);
	}}
    }// end recusiveBlockSubmit(...)

    private void recursiveGridCubeSubmit(Submitter<GridCube> sub, int blockID) {
	final GridCube cube = (GridCube)gridSquares[blockID];
	sub.submit(cube);
	synchronized(branchGrids){
	final int size = branchGrids.size();
	for (int index = 0; index < size; index++) {
	    branchGrids.get(index).recursiveGridCubeSubmit(sub, blockID);
	}}//end for(size) sync(branchGrids)
    }// end recusiveGridCubeSubmit(...)

	private Collection<E> getAlwaysVisible()
		{return alwaysVisible.getElements();}

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
	
	public class GridCube implements PositionListener{
		double [] topLeftPosition;
		private List<E> elements = null;
		
		public GridCube(double [] topLeftPosition){
			setTopLeftPosition(topLeftPosition);
			}
		
		public String toString(){
		    return "GridCube topLeftPos=("+topLeftPosition[0]+", "+topLeftPosition[1]+", "+topLeftPosition[2]+")";
		}//end toString()
		
		public void add(E objectToAdd){
			if(getElements().contains(objectToAdd)){
			    /*new Exception("Redundant add!").printStackTrace();
			    List<PositionListener>pcls = ((WorldObject)objectToAdd).getPositionListeners();
				for(PositionListener pl:pcls){
				    System.out.println("PositionListener "+pl);
				}*/
			   }//TODO Comment out 
			else{//Ok to add.
			getElements().add(objectToAdd);
			if(!(objectToAdd instanceof VisibleEverywhere))
			    objectToAdd.addPositionListener(this);}
			}//end add(E)

		@SuppressWarnings("unchecked")
		@Override
		public void positionChanged(PositionListenable objectWithPosition){
		    	synchronized(SpacePartitioningGrid.this){
			//Is it still in the range of this cube
			if(isInRange(objectWithPosition.getPosition()))return;// Then ignore
			//Else remove and re-add so it finds its new cube.
			synchronized(SpacePartitioningGrid.this){synchronized(objectWithPosition){
				remove(objectWithPosition);
				SpacePartitioningGrid.this.add((E)objectWithPosition);
				}}
		    	  }//end sync(SpacePartitioningGrid.this)
			}//end constructor(..)

		private void remove(PositionListenable objectWithPosition){
			boolean result = getElements().remove(objectWithPosition);
			/*if(!result) && objectWithPosition instanceof Player){
			    new Exception("Removal failure.").printStackTrace();
			    System.exit(0);
			}*/
			objectWithPosition.removePositionListener(this);
			}//end remove(...)

		private boolean isInRange(double[] ds){
		    if(topLeftPosition==null)return true;//Always in range.
			return(	ds[0]>topLeftPosition[0] &&							//Top Left
					ds[1]>topLeftPosition[1] &&
					ds[2]>topLeftPosition[2] &&
					ds[0]<topLeftPosition[0]+SpacePartitioningGrid.this.getSquareSize() && 	//Bottom right
					ds[1]<topLeftPosition[1]+SpacePartitioningGrid.this.getSquareSize() &&
					ds[2]<topLeftPosition[2]+SpacePartitioningGrid.this.getSquareSize());
			}//end isInRange()

		/**
		 * @return the topLeftPosition
		 */
		public double [] getTopLeftPosition(){
			return topLeftPosition;
			}

		/**
		 * @param topLeftPosition2 the topLeftPosition to set
		 */
		public void setTopLeftPosition(double[] topLeftPosition2){
			this.topLeftPosition = topLeftPosition2;
			}

		/**
		 * @return the elements
		 */
		public List<E> getElements(){
		    	if(elements==null)elements =  Collections.synchronizedList(new ArrayList<E>(3));
			return elements;
			}

		/**
		 * @param elements the elements to set
		 */
		public void setElements(ArrayList<E> elements){
			this.elements = elements;
			}
		}//end GridSquare

	/**
	 * @return the gridSquares
	 */
	public Object[] getGridSquares(){
		return gridSquares;
		}
	/**
	 * @param squares the gridSquares to set
	 */
	public void setGridSquares(Object[] squares){
		this.gridSquares = squares;
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
	}//end SpacePartitionGrid
