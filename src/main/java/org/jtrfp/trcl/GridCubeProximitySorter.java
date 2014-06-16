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

import java.util.Comparator;
import java.util.TreeSet;

import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class GridCubeProximitySorter extends AbstractSubmitter<SpacePartitioningGrid<PositionedRenderable>.GridCube> {
    private double [] center = new double[]{0,0,0};
    
    public GridCubeProximitySorter setCenter(double [] center){
	this.center=center;
	return this;
    }
    
    private final TreeSet<SpacePartitioningGrid<PositionedRenderable>.GridCube> sortedSet = new TreeSet<SpacePartitioningGrid<PositionedRenderable>.GridCube>(new Comparator<SpacePartitioningGrid<PositionedRenderable>.GridCube>(){

	@Override
	public int compare(SpacePartitioningGrid<PositionedRenderable>.GridCube _left, SpacePartitioningGrid<PositionedRenderable>.GridCube _right) {
	    final double [] left=_left.getTopLeftPosition();
	    final double [] right=_right.getTopLeftPosition();
	    if((left==right)&&left==null)return Integer.MAX_VALUE;
	    if(left==null)return Integer.MIN_VALUE;
	    if(right==null)return Integer.MAX_VALUE;
	    final int diff = (int)(Vect3D.taxicabDistance(center,left)-Vect3D.taxicabDistance(center,right));
	    return diff!=0?diff:1;
	}
	@Override
	public boolean equals(Object o){
	    return o.getClass()==this.getClass();
	}
	
    });
    @Override
    public void submit(SpacePartitioningGrid<PositionedRenderable>.GridCube item) {
	sortedSet.add(item);
    }
    
    public GridCubeProximitySorter reset(){
	sortedSet.clear();
	return this;
    }
    
    public GridCubeProximitySorter dumpPositionedRenderables(Submitter<PositionedRenderable> sub){
	for(SpacePartitioningGrid<PositionedRenderable>.GridCube gc:sortedSet){
	    sub.submit(gc.getElements());
	}//end for(...)
	return this;
    }//end dumpPositionedRenderables(...)

}//end GridCubeProximitySorter
