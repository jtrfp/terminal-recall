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
import java.util.List;
import java.util.TreeSet;

import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.RelevantEverywhere;

public class GridCubeProximitySorter extends AbstractSubmitter<List<PositionedRenderable>> {
    private double [] center = new double[]{0,0,0};
    
    public static Comparator<List<PositionedRenderable>> getComparator(final double [] center){
	return new Comparator<List<PositionedRenderable>>(){
	@Override
	public int compare(List<PositionedRenderable> _left, List<PositionedRenderable> _right) {
	    synchronized(_left){
	    synchronized(_right){
	    if(_left.isEmpty()&&_right.isEmpty())
		return 0;
	    if(_left.isEmpty())
		return -1;
	    if(_right.isEmpty())
		return 1;
	    double [] left=_left.get(0).getPosition();
	    double [] right=_right.get(0).getPosition();
	    if((left==right)&&left==null)return Integer.MAX_VALUE;
	    if(left==null)return Integer.MIN_VALUE;
	    if(right==null)return Integer.MAX_VALUE;
	    if(_left.get(0) instanceof RelevantEverywhere)
		left=new double[]{center[0],center[1],center[2]+left[2]*1114112};
	    if(_right.get(0) instanceof RelevantEverywhere)
		right=new double[]{center[0],center[1],center[2]+right[2]*1114112};
	    final int diff = (int)(Vect3D.taxicabDistance(center,left)-Vect3D.taxicabDistance(center,right));
	    return diff!=0?diff:_left.hashCode()-_right.hashCode();
	    }}//end sync()
	}//end compare()
	@Override
	public boolean equals(Object o){
	    return o.getClass()==this.getClass();
	}//end equals
    };//end new Comparator
    }//end getComparator()
    
    public static Comparator<PositionedRenderable> getComparator(final Camera camera){
   	return new Comparator<PositionedRenderable>(){
   	@Override
   	public int compare(PositionedRenderable _left, PositionedRenderable _right) {
   	    final double [] cPos = camera.getPosition();
   	    double [] left       = _left .getPosition();
   	    double [] right      = _right.getPosition();
   	    if((left==right)&&left==null)return Integer.MAX_VALUE;
   	    if(left==null)return Integer.MIN_VALUE;
   	    if(right==null)return Integer.MAX_VALUE;
   	    if(_left instanceof RelevantEverywhere)
   		left=new double[]{cPos[0],cPos[1],cPos[2]+left[2]*1114112};
   	    if(_right instanceof RelevantEverywhere)
   		right=new double[]{cPos[0],cPos[1],cPos[2]+right[2]*1114112};
   	    final int diff = (int)(Vect3D.taxicabDistance(cPos,left)-Vect3D.taxicabDistance(cPos,right));
   	    return diff!=0?diff:_left.hashCode()-_right.hashCode();
   	}//end compare()
   	@Override
   	public boolean equals(Object o){
   	    return o.getClass()==this.getClass();
   	}//end equals
       };//end new Comparator
       }//end getComparator()
    
    public GridCubeProximitySorter setCenter(double [] center){
	this.center=center;
	return this;
    }
    
    private final TreeSet<List<PositionedRenderable>> sortedSet = 
	    new TreeSet<List<PositionedRenderable>>(new Comparator<List<PositionedRenderable>>(){

	@Override
	public int compare(List<PositionedRenderable> _left, List<PositionedRenderable> _right) {
	    synchronized(_left){
	    synchronized(_right){
	    if(_left.isEmpty()&&_right.isEmpty())
		return 0;
	    if(_left.isEmpty())
		return -1;
	    if(_right.isEmpty())
		return 1;
	    double [] left=_left.get(0).getPosition();
	    double [] right=_right.get(0).getPosition();
	    if((left==right)&&left==null)return Integer.MAX_VALUE;
	    if(left==null)return Integer.MIN_VALUE;
	    if(right==null)return Integer.MAX_VALUE;
	    if(_left.get(0) instanceof RelevantEverywhere)
		left=new double[]{center[0],center[1],center[2]+left[2]*1114112};
	    if(_right.get(0) instanceof RelevantEverywhere)
		right=new double[]{center[0],center[1],center[2]+right[2]*1114112};
	    final int diff = (int)(Vect3D.taxicabDistance(center,left)-Vect3D.taxicabDistance(center,right));
	    return diff!=0?diff:_left.hashCode()-_right.hashCode();
	    }}//end sync()
	}//end compare()
	@Override
	public boolean equals(Object o){
	    return o.getClass()==this.getClass();
	}
	
    });
    @Override
    public void submit(List<PositionedRenderable> item) {
	if(item==null)return;
	if(item.isEmpty())return;
	synchronized(sortedSet){sortedSet.add(item);}
    }
    
    public GridCubeProximitySorter reset(){
	synchronized(sortedSet){sortedSet.clear();}
	return this;
    }
    
    public GridCubeProximitySorter dumpPositionedRenderables(Submitter<PositionedRenderable> sub){
	synchronized(sortedSet){
	for(List<PositionedRenderable> gc:sortedSet){
	    sub.submit(gc);
	}//end for(...)
	}//end sync(sortedSet)
	return this;
    }//end dumpPositionedRenderables(...)

}//end GridCubeProximitySorter
