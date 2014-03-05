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
import java.util.concurrent.Future;

import org.jtrfp.trcl.core.TR;

public class Model
	{
	// [FRAME][LIST]
	private ArrayList<ArrayList<Triangle>> tLists = new ArrayList<ArrayList<Triangle>>();
	private ArrayList<ArrayList<Triangle>> ttLists = new ArrayList<ArrayList<Triangle>>();
	private ArrayList<ArrayList<LineSegment>> lsLists = new ArrayList<ArrayList<LineSegment>>();
	private TransparentTriangleList ttpList;
	private TriangleList tpList;
	private LineSegmentList lsList;
	private int frameDelay;
	private boolean smoothAnimation;
	private String debugName="[unnamed]";
	private boolean animateUV=false;
	private Controller controller;
	private final TR tr;
	
	public Model(boolean smoothAnimation,TR tr){
	    	this.tr=tr;
		this.smoothAnimation=smoothAnimation;
		//Frame zero
		tLists.add(new ArrayList<Triangle>());
		lsLists.add(new ArrayList<LineSegment>());
		ttLists.add(new ArrayList<Triangle>());
		}
	public TriangleList getTriangleList(){
		try{return tpList;}
		catch(IndexOutOfBoundsException e){return null;}
		}
	public LineSegmentList getLineSegmentList(){
		try{return lsList;}
		catch(IndexOutOfBoundsException e){return null;}
		}
	public TransparentTriangleList getTransparentTriangleList(){
		try{return ttpList;}
		catch(IndexOutOfBoundsException e){return null;}
		}
	
	ArrayList<ArrayList<Triangle>> getRawTriangleLists()
		{return tLists;}
	ArrayList<ArrayList<Triangle>> getRawTransparentTriangleLists()
		{return ttLists;}
	ArrayList<ArrayList<LineSegment>> getRawLineSegmentLists()
		{return lsLists;}
	
	/**
	 * Sets up formal GPU primitive lists
	 * @return
	 */
	public Model finalizeModel()
		{
	    	Controller c = controller;
	    	if(c==null)c=new Sequencer(getFrameDelayInMillis(),tLists.size(),true);
		Triangle [][] tris = new Triangle[tLists.size()][];
		for(int i=0; i<tLists.size(); i++)
			{tris[i]=tLists.get(i).toArray(new Triangle[]{});}//Get all frames for each triangle
		if(tris[0].length!=0)tpList=new TriangleList(tris,getFrameDelayInMillis(),debugName, animateUV,c,tr);
		else tpList=null;
		
		Triangle [][] ttris = new Triangle[ttLists.size()][];
		for(int i=0; i<ttLists.size(); i++)
			{ttris[i]=ttLists.get(i).toArray(new Triangle[]{});}//Get all frames for each triangle
		if(ttris[0].length!=0)ttpList=new TransparentTriangleList(ttris,getFrameDelayInMillis(),debugName, animateUV,c,tr);
		else ttpList=null;
		
		LineSegment [][] segs = new LineSegment[lsLists.size()][];
		for(int i=0; i<lsLists.size(); i++)
			{segs[i]=lsLists.get(i).toArray(new LineSegment[]{});}//Get all frames for each line seg
		if(segs[0].length!=0)lsList=new LineSegmentList(segs,debugName,tr);
		else lsList=null;
		return this;
		}//end finalizeModel()
	
	public void addFrame(Model m)
		{
		//Opaque Triangles
			{tLists.add(m.getRawTriangleLists().get(0));}
		//Transparent triangles
			{ttLists.add(m.getRawTransparentTriangleLists().get(0));}
		//Line Segs	
			{lsLists.add(m.getRawLineSegmentLists().get(0));}
		}//end addFrame(...)
	
	/**
	 * 
	 * @return`The time between frames in milliseconds
	 * @since Jan 5, 2013
	 */
	public int getFrameDelayInMillis()
		{return frameDelay;}

	/**
	 * @param frameDelay the frameDelay to set
	 */
	public void setFrameDelayInMillis(int frameDelayInMillis)
		{
		this.frameDelay = frameDelayInMillis;
		}

	public void addTriangle(Triangle triangle)
		{
		if(triangle.isAlphaBlended())
			{ttLists.get(0).add(triangle);}
		else tLists.get(0).add(triangle);
		}
	public void addLineSegment(LineSegment seg)
		{lsLists.get(0).add(seg);}
	public void addTriangles(Triangle [] tris)
		{
		for(Triangle t:tris)
			{addTriangle(t);}
		}
	public void addLineSegments(LineSegment [] lss)
		{
		for(LineSegment ls:lss)
			{addLineSegment(ls);}
		}//end addLineSegments
	/**
	 * @return the smoothAnimation
	 */
	public boolean isSmoothAnimation(){
		return smoothAnimation;
		}
	/**
	 * @param smoothAnimation the smoothAnimation to set
	 */
	public void setSmoothAnimation(boolean smoothAnimation){
		this.smoothAnimation = smoothAnimation;
		}
	
	public static Model buildCube(double w, double h, double d, Future<TextureDescription> tunnelTexturePalette, double[] origin,boolean hasAlpha,TR tr){
		return buildCube(w, h, d, tunnelTexturePalette,origin, 0,0,1,1,hasAlpha,tr);
		}
	
	public static Model buildCube(double w, double h, double d, Future<TextureDescription> tunnelTexturePalette, double[] origin,TR tr){
		return buildCube(w, h, d, tunnelTexturePalette,origin, 0,0,1,1,tr);
		}
	
	public static Model buildCube(double w, double h, double d, Future<TextureDescription> tunnelTexturePalette, double[] origin, double u0, double v0, double u1, double v1, TR tr){
	    return buildCube(w,h,d,tunnelTexturePalette,origin,u0,v0,u1,v1,false,tr);
	}
	
	public static Model buildCube(double w, double h, double d, Future<TextureDescription> tunnelTexturePalette, double[] origin, double u0, double v0, double u1, double v1, boolean hasAlpha, TR tr){
		Model m = new Model(false, tr);
		//Front
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin[0],w-origin[0],w-origin[0],0-origin[0]}, 
				new double [] {0-origin[1],0-origin[1],h-origin[1],h-origin[1]}, 
				new double [] {0-origin[2],0-origin[2],0-origin[2],0-origin[2]}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC,hasAlpha));
		//Left
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin[0],0-origin[0],0-origin[0],0-origin[0]}, 
				new double [] {0-origin[1],0-origin[1],h-origin[1],h-origin[1]}, 
				new double [] {0-origin[2],d-origin[2],d-origin[2],0-origin[2]}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC,hasAlpha));
		//Right
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {w-origin[0],w-origin[0],w-origin[0],w-origin[0]}, 
				new double [] {0-origin[1],0-origin[1],h-origin[1],h-origin[1]}, 
				new double [] {0-origin[2],d-origin[2],d-origin[2],0-origin[2]}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC,hasAlpha));
		//Back
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin[0],w-origin[0],w-origin[0],0-origin[0]}, 
				new double [] {0-origin[1],0-origin[1],h-origin[1],h-origin[1]}, 
				new double [] {d-origin[2],d-origin[2],d-origin[2],d-origin[2]}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC,hasAlpha));
		m.finalizeModel();
		return m;
		}//end buildCube
	/**
	 * @return the debugName
	 */
	public String getDebugName(){
		return debugName;
		}
	/**
	 * @param debugName the debugName to set
	 */
	public void setDebugName(String debugName){
		this.debugName = debugName;
		}
	/**
	 * @return the animateUV
	 */
	public boolean isAnimateUV() {
	    return animateUV;
	}
	/**
	 * @param animateUV the animateUV to set
	 */
	public void setAnimateUV(boolean animateUV) {
	    this.animateUV = animateUV;
	}
	
	/**
	 * @param controller the controller to set
	 */
	public void setController(Controller controller) {
	    this.controller = controller;
	}
	/**
	 * @return the controller
	 */
	public Controller getController() {
	    return controller;
	}
}//end Model
