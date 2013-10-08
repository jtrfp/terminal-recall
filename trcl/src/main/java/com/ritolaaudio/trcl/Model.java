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
package com.ritolaaudio.trcl;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

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
	
	public Model(boolean smoothAnimation)
		{
		this.smoothAnimation=smoothAnimation;
		//Frame zero
		tLists.add(new ArrayList<Triangle>());
		lsLists.add(new ArrayList<LineSegment>());
		ttLists.add(new ArrayList<Triangle>());
		}
	public TriangleList getTriangleList()
		{
		try{return tpList;}
		catch(IndexOutOfBoundsException e){return null;}
		}
	public LineSegmentList getLineSegmentList()
		{
		try{return lsList;}
		catch(IndexOutOfBoundsException e){return null;}
		}
	public TransparentTriangleList getTransparentTriangleList()
		{
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
		Triangle [][] tris = new Triangle[tLists.size()][];
		for(int i=0; i<tLists.size(); i++)
			{tris[i]=tLists.get(i).toArray(new Triangle[]{});}//Get all frames for each triangle
		if(tris[0].length!=0)tpList=new TriangleList(tris,getFrameDelayInMillis(),"TriangleList...");
		else tpList=null;
		
		Triangle [][] ttris = new Triangle[ttLists.size()][];
		for(int i=0; i<ttLists.size(); i++)
			{ttris[i]=ttLists.get(i).toArray(new Triangle[]{});}//Get all frames for each triangle
		if(ttris[0].length!=0)ttpList=new TransparentTriangleList(ttris,getFrameDelayInMillis(),"TransparentTriangleList...");
		else ttpList=null;
		
		LineSegment [][] segs = new LineSegment[lsLists.size()][];
		for(int i=0; i<lsLists.size(); i++)
			{segs[i]=lsLists.get(i).toArray(new LineSegment[]{});}//Get all frames for each line seg
		if(segs[0].length!=0)lsList=new LineSegmentList(segs,"Line Segment List...");
		else lsList=null;
		return this;
		}//end finalizeModel()
	
	public void addFrame(Model m)
		{
		//Opaque Triangles
			{
			tLists.add(m.getRawTriangleLists().get(0));
			}
		//Transparent triangles
			{
			ttLists.add(m.getRawTransparentTriangleLists().get(0));
			}
		//Line Segs	
			{
			lsLists.add(m.getRawLineSegmentLists().get(0));
			}
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
	public boolean isSmoothAnimation()
		{
		return smoothAnimation;
		}
	/**
	 * @param smoothAnimation the smoothAnimation to set
	 */
	public void setSmoothAnimation(boolean smoothAnimation)
		{
		this.smoothAnimation = smoothAnimation;
		}
	
	public static Model buildCube(double w, double h, double d, TextureDescription tunnelTexturePalette, Vector3D origin)
		{
		return buildCube(w, h, d, tunnelTexturePalette,origin, 0,0,1,1);
		}
	
	public static Model buildCube(double w, double h, double d, TextureDescription tunnelTexturePalette, Vector3D origin, double u0, double v0, double u1, double v1)
		{
		Model m = new Model(false);
		//Front
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin.getX(),w-origin.getX(),w-origin.getX(),0-origin.getX()}, 
				new double [] {0-origin.getY(),0-origin.getY(),h-origin.getY(),h-origin.getY()}, 
				new double [] {0-origin.getZ(),0-origin.getZ(),0-origin.getZ(),0-origin.getZ()}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC));
		//Left
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin.getX(),0-origin.getX(),0-origin.getX(),0-origin.getX()}, 
				new double [] {0-origin.getY(),0-origin.getY(),h-origin.getY(),h-origin.getY()}, 
				new double [] {0-origin.getZ(),d-origin.getZ(),d-origin.getZ(),0-origin.getZ()}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC));
		//Right
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {w-origin.getX(),w-origin.getX(),w-origin.getX(),w-origin.getX()}, 
				new double [] {0-origin.getY(),0-origin.getY(),h-origin.getY(),h-origin.getY()}, 
				new double [] {0-origin.getZ(),d-origin.getZ(),d-origin.getZ(),0-origin.getZ()}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC));
		//Back
		m.addTriangles(Triangle.quad2Triangles(
				new double [] {0-origin.getX(),w-origin.getX(),w-origin.getX(),0-origin.getX()}, 
				new double [] {0-origin.getY(),0-origin.getY(),h-origin.getY(),h-origin.getY()}, 
				new double [] {d-origin.getZ(),d-origin.getZ(),d-origin.getZ(),d-origin.getZ()}, 
				
				new double [] {u0,u1,u1,u0}, 
				new double [] {v0,v0,v1,v1}, tunnelTexturePalette, RenderMode.STATIC));
		m.finalizeModel();
		return m;
		}//end buildCube
	/**
	 * @return the debugName
	 */
	public String getDebugName()
		{
		return debugName;
		}
	/**
	 * @param debugName the debugName to set
	 */
	public void setDebugName(String debugName)
		{
		this.debugName = debugName;
		}
	}//end Model
