/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.PositionListenable;
import org.jtrfp.trcl.obj.PositionListener;
import org.jtrfp.trcl.obj.VisibleEverywhere;

public abstract class SpacePartitioningGrid<E extends PositionListenable>{
	private double squareSize, viewingRadius;
	private Object [] gridSquares;
	private int squaresX, squaresY, squaresZ;
	private ArrayList<E> alwaysVisible = new ArrayList<E>();
	private SpacePartitioningGrid<E> parentGrid = null;
	private ArrayList<SpacePartitioningGrid<E>> branchGrids = new ArrayList<SpacePartitioningGrid<E>>();
	
	private double radiusInWorldUnits;
	private int rolloverPoint,rawDia,rawDiaX,rawDiaY,rawDiaZ,xProgression,yProgression,zProgression;
	
	public SpacePartitioningGrid(SpacePartitioningGrid<E> parentGrid)
		{setParentGrid(parentGrid);activate();}
	
	public void activate()
		{if(parentGrid!=null)parentGrid.addBranch(this);}
	public void deactivate()
		{if(parentGrid!=null)parentGrid.removeBranch(this);}
	
	private void addBranch(SpacePartitioningGrid<E> branchToAdd)
		{if(!branchGrids.contains(branchToAdd))branchGrids.add(branchToAdd);}
	private void removeBranch(SpacePartitioningGrid<E> branchToRemove)
		{branchGrids.remove(branchToRemove);}
	
	private void setParentGrid(SpacePartitioningGrid<E> parentGrid){
		this.parentGrid=parentGrid;
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
					{squares[x+y*squaresX+z*squaresZMult]=(new GridCube(new double []{x,y,z}));}
				}//end for(squaresY)
			}//end for(squaresZ)
		
		radiusInWorldUnits=getViewingRadius()*1.25;//Fudge factor to fix suddenly appearing terrain at distance
		
		rolloverPoint=gridSquares.length;
		rawDia=(int)((radiusInWorldUnits*2)/getSquareSize());
		rawDiaX=rawDia<getSquaresX()?rawDia:getSquaresX();
		rawDiaY=rawDia<getSquaresY()?rawDia:getSquaresY();
		rawDiaZ=rawDia<getSquaresZ()?rawDia:getSquaresZ();

		zProgression=getSquaresX()*getSquaresY()-rawDiaY*getSquaresX();
		yProgression=getSquaresX()-rawDiaX;
		xProgression=1;
		}//end allocateSquares()
	
	private int space2Flat(Vector3D space)
		{return (int)(space.getX()+space.getY()*squaresX+space.getZ()*squaresX*squaresY);}
	public void add(E objectWithPosition)
		{//Figure out where it goes
	    	objectWithPosition.setContainingGrid(this);
	    	if(objectWithPosition instanceof VisibleEverywhere){
		    addAlwaysVisible(objectWithPosition);return;}
		final GridCube dest = squareAtWorldCoord(objectWithPosition.getPosition());
		dest.add(objectWithPosition);
		}
	public void remove(E objectWithPosition){
		for(Object o:gridSquares)
			{//Blind removal
			GridCube gc = (GridCube)o;
			gc.remove(objectWithPosition);
			}
		}
	private void addAlwaysVisible(E objectWithPosition)
		{alwaysVisible.add(objectWithPosition);}
	
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
	
	protected GridCube squareAtWorldCoord(double[] ds)
		{return squareAtGridCoord(world2Square(ds));}
	
	@SuppressWarnings("unchecked")
	protected GridCube squareAtGridCoord(Vector3D gridCoord)
		{return (GridCube)(gridSquares[space2Flat(gridCoord)]);}
	
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
	
	private void recursiveAlwaysVisibleSubmit(Submitter<E> sub)
		{sub.submit(alwaysVisible);
		final int size=branchGrids.size();
		for(int index=0; index<size; index++){
		    branchGrids.get(index).recursiveAlwaysVisibleSubmit(sub);
		}
		for(SpacePartitioningGrid<E> grid:branchGrids)
			{grid.recursiveAlwaysVisibleSubmit(sub);}
		}//end recursiveAlwaysVisisbleSubmit(...)
	
	private void recursiveBlockSubmit(Submitter<E> sub, int blockID)
		{final ArrayList<E> elements = ((GridCube)gridSquares[blockID]).getElements();
		 {final int size=elements.size();
		 for(int i=0; i<size; i++){
		    sub.submit(elements.get(i));
		 }}//end submit local elements
		final int size=branchGrids.size();
		for(int index=0; index<size; index++){
		    branchGrids.get(index).recursiveBlockSubmit(sub,blockID);
		}
	}//end recusiveBlockSubmit(...)
	
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
	
	class GridCube implements PositionListener{
		double [] topLeftPosition;
		private ArrayList<E> elements = new ArrayList<E>();
		
		public GridCube(double [] topLeftPosition){
			setTopLeftPosition(topLeftPosition);
			}
		
		public void add(E objectToAdd){
			//if(getElements().contains(objectToAdd)){System.err.println("WARNING: Redundant add!");return;}
			getElements().add(objectToAdd);
			objectToAdd.addPositionListener(this);
			}

		@SuppressWarnings("unchecked")
		@Override
		public void positionChanged(PositionListenable objectWithPosition){
			//Is it still in the range of this cube
			if(isInRange(objectWithPosition.getPosition()))return;// Then ignore
			//Else remove and re-add so it finds its new cube.
			synchronized(SpacePartitioningGrid.this){synchronized(objectWithPosition){
				remove(objectWithPosition);
				SpacePartitioningGrid.this.add((E)objectWithPosition);
				}}
			}//end constructor(..)

		private void remove(PositionListenable objectWithPosition){
			getElements().remove(objectWithPosition);
			objectWithPosition.removePositionListener(this);
			}

		private boolean isInRange(double[] ds){
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
		public ArrayList<E> getElements(){
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
