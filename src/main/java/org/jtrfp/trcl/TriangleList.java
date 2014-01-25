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

import java.util.concurrent.ExecutionException;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TriangleVertex2FlatDoubleWindow;
import org.jtrfp.trcl.core.TriangleVertex2FlatDoubleWindow.Variable;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.core.WindowAnimator;

public class TriangleList extends PrimitiveList<Triangle,GPUTriangleVertex>
	{
	private Controller controller;
	private int timeBetweenFramesMsec;
	private final boolean animateUV;
	private final TR tr;
	private final WindowAnimator xyzAnimator;
	private final TriangleVertex2FlatDoubleWindow flatTVWindow;
	public TriangleList(Triangle [][] triangles, int timeBetweenFramesMsec, String debugName, boolean animateUV, Controller controller, TR tr)
		{super(debugName,triangles,GPUTriangleVertex.createVertexBlock(triangles[0].length*3,tr),tr);
		this.timeBetweenFramesMsec=timeBetweenFramesMsec;
		this.animateUV=animateUV;
		this.controller=controller;
		this.tr=tr;
		this.flatTVWindow = tr.getTv2fdWindow();
		if(getPrimitives().length>1){
		    this.xyzAnimator = new WindowAnimator(flatTVWindow,
		      this.getNumPrimitives()*3*3,// 3 vertices per triangle, XYZ per vertex
		      getPrimitives().length, true, controller,new XYZXferFunc(getGPUPrimitiveStartIndex()*5));
		      animators.add(xyzAnimator);}
		else{this.xyzAnimator=null;}
		}
	
	private static class XYZXferFunc implements IntTransferFunction{
	    private final int startIndex;
	    public XYZXferFunc(int startIndex){
		this.startIndex=startIndex;
	    }//end constructor
	    @Override
	    public int transfer(int input) {
		return (input/3)*5+(input%3)+startIndex;
	    }//end transfer(...)
	}//end class XYZXferFunc
	
	private static class UVXferFunc implements IntTransferFunction{
	    private final int startIndex;
	    public UVXferFunc(int startIndex){
		this.startIndex=startIndex;
	    }//end constructor
	    @Override
	    public int transfer(int input) {
		return (input/2)*5+(input%2)+startIndex+3;
	    }//end transfer(...)
	}//end class XYZXferFunc
	
	public TriangleList [] getAllLists()
		{return getAllArrayLists().toArray(new TriangleList [] {});}
	private Controller getVertexSequencer(int timeBetweenFramesMsec, int nFrames){
		return controller;
		}
	private Triangle triangleAt(int frame, int tIndex)
		{return getPrimitives()[frame][tIndex];}
	private void setupVertex(int vIndex, int gpuTVIndex, int triangleIndex) throws ExecutionException, InterruptedException
		{final int numFrames = getPrimitives().length;
		Triangle t=triangleAt(0,triangleIndex);
		final TriangleVertexWindow vw = tr.getTriangleVertexWindow();
		//System.out.println("TriangleList: windowAnimator="+windowAnimator+" numFrames="+numFrames);
		if(numFrames==1){
		    	vw.setX(gpuTVIndex, (short)applyScale(t.x[vIndex]));
		    	vw.setY(gpuTVIndex, (short)applyScale(t.y[vIndex]));
		    	vw.setZ(gpuTVIndex, (short)applyScale(t.z[vIndex]));
		    	/*
			vtx.x.set((short)applyScale(t.x[vIndex]));
			vtx.y.set((short)applyScale(t.y[vIndex]));
			vtx.z.set((short)applyScale(t.z[vIndex]));
			*/
			}
		else if(numFrames>1)
			{
			//vtx.x.set((short)applyScale(t.x[vIndex]));
			//vtx.y.set((short)applyScale(t.y[vIndex]));
			//vtx.z.set((short)applyScale(t.z[vIndex]));
			
			double []xFrames = new double[numFrames];
			double []yFrames = new double[numFrames];
			double []zFrames = new double[numFrames];
			for(int i=0; i<numFrames; i++)
				{xFrames[i]=Math.round(triangleAt(i,triangleIndex).x[vIndex]/scale);}
			xyzAnimator.addFrames(xFrames);
			//animators.add(new AttribAnimator(vtx.x,getVertexSequencer(timeBetweenFramesMsec,numFrames),xFrames));
			
			for(int i=0; i<numFrames; i++)
				{yFrames[i]=Math.round(triangleAt(i,triangleIndex).y[vIndex]/scale);}
			xyzAnimator.addFrames(yFrames);
			//animators.add(new AttribAnimator(vtx.y,getVertexSequencer(timeBetweenFramesMsec,numFrames),yFrames));
			
			for(int i=0; i<numFrames; i++)
				{zFrames[i]=Math.round(triangleAt(i,triangleIndex).z[vIndex]/scale);}
			xyzAnimator.addFrames(zFrames);
			//animators.add(new AttribAnimator(vtx.z,getVertexSequencer(timeBetweenFramesMsec,numFrames),zFrames));
			}
		//else{throw new RuntimeException("Empty triangle vertex!");}
		
		TextureDescription td = t.getTexture().get();
		if(td instanceof Texture){//Static texture
			final Texture.TextureTreeNode tx;
			tx= ((Texture)t.getTexture().get()).getNodeForThisTexture();
			if(animateUV&&numFrames>1){//Animated UV
			    double []uFrames = new double[numFrames];
			    double []vFrames = new double[numFrames];
			    final WindowAnimator uvAnimator =  new WindowAnimator(flatTVWindow,
				    2,// UV per vertex
				    numFrames, false, getVertexSequencer(timeBetweenFramesMsec,numFrames),
				    new UVXferFunc(gpuTVIndex*5));
			    	animators.add(uvAnimator);
			    for(int i=0; i<numFrames; i++){
				uFrames[i]=uvUpScaler*tx.getGlobalUFromLocal(triangleAt(i,triangleIndex).u[vIndex]);
				vFrames[i]=uvUpScaler*tx.getGlobalVFromLocal(triangleAt(i,triangleIndex).v[vIndex]);
			    }//end for(numFrames)
			    //TODO
			    uvAnimator.addFrames(uFrames);
			    uvAnimator.addFrames(vFrames);
			    //animators.add(new AttribAnimator(vtx.u,getVertexSequencer(timeBetweenFramesMsec,numFrames),uFrames));
			   // animators.add(new AttribAnimator(vtx.v,getVertexSequencer(timeBetweenFramesMsec,numFrames),vFrames));
			
			}else{//end if(animateUV)
			    vw.setU(gpuTVIndex, (short)(uvUpScaler*tx.getGlobalUFromLocal(t.u[vIndex])));
			    vw.setV(gpuTVIndex, (short)(uvUpScaler*tx.getGlobalVFromLocal(t.v[vIndex])));
			    //vtx.u.set((short)(uvUpScaler*tx.getGlobalUFromLocal(t.u[vIndex])));
			    //vtx.v.set((short)(uvUpScaler*tx.getGlobalVFromLocal(t.v[vIndex])));
			}//end if(!animateUV)
		    }//end if(Texture)
		else //TODO
			{//Animated texture
			AnimatedTexture at =((AnimatedTexture)t.getTexture().get());
			
			Texture.TextureTreeNode tx=at.
				getFrames()
				[0].get().
				getNodeForThisTexture();//Default frame
			final WindowAnimator uvAnimator =  new WindowAnimator(flatTVWindow,
				    2,// UV per vertex
				    at.getFrames().length, false, at.getTextureSequencer(),
				    new UVXferFunc(gpuTVIndex*5));
			animators.add(uvAnimator);
			//vtx.u.set((short)(uvUpScaler*tx.getGlobalUFromLocal(t.u[vIndex])));
			//vtx.v.set((short)(uvUpScaler*tx.getGlobalVFromLocal(t.v[vIndex])));
			
			final int numTextureFrames = at.getFrames().length;
			double [] uFrames = new double[numTextureFrames];
			double [] vFrames = new double[numTextureFrames];
			for(int ti=0; ti<numTextureFrames;ti++){
				tx=at.getFrames()[ti].get().getNodeForThisTexture();
				uFrames[ti]=(short)(uvUpScaler*tx.getGlobalUFromLocal(t.u[vIndex]));
				vFrames[ti]=(short)(uvUpScaler*tx.getGlobalVFromLocal(t.v[vIndex]));
				}//end for(frame)
			
			uvAnimator.addFrames(uFrames);
			uvAnimator.addFrames(vFrames);
			}//end animated texture
		}//end setupVertex
	
	private void setupTriangle(int gpuTriangleVertIndex, int triangleIndex) throws ExecutionException,InterruptedException{
		setupVertex(0,gpuTriangleVertIndex+0,triangleIndex);
		setupVertex(1,gpuTriangleVertIndex+1,triangleIndex);
		setupVertex(2,gpuTriangleVertIndex+2,triangleIndex);
		}
	
	public void uploadToGPU(GL3 gl){
		int nPrimitives=getNumPrimitives();
		System.out.println("Model: "+this.getDebugName()+" NumPrimitives: "+nPrimitives);
		try{
		for(int tIndex=0;tIndex<nPrimitives;tIndex++)
			{
		    	setupTriangle(getGPUPrimitiveStartIndex()+tIndex*3,tIndex);
			}//end for(getPrimitives)
		}catch(InterruptedException e){e.printStackTrace();}
		catch(ExecutionException e){e.printStackTrace();}
		}//end allocateIndices(...)

	@Override
	public int getPrimitiveSizeInVec4s(){return 3;}

	@Override
	public int getGPUVerticesPerPrimitive(){return 3;}

	@Override
	public byte getPrimitiveRenderMode()
		{return PrimitiveRenderMode.RENDER_MODE_TRIANGLES;}

	@Override
	public org.jtrfp.trcl.PrimitiveList.RenderStyle getRenderStyle()
		{return RenderStyle.OPAQUE;}
	
	public Vector3D getMaximumVertexDims(){
		Vector3D result=Vector3D.ZERO;
		Triangle [][]t=getPrimitives();
		for(Triangle [] frame:t){
			for(Triangle tri:frame){
				for(int i=0; i<3; i++){
					double v;
					v=(tri.x[i]);
					result=result.getX()<v?new Vector3D(v,result.getY(),result.getZ()):result;
					v=(tri.y[i]);
					result=result.getY()<v?new Vector3D(result.getX(),v,result.getZ()):result;
					v=(tri.z[i]);
					result=result.getZ()<v?new Vector3D(result.getX(),result.getY(),v):result;
					}//end for(vertex)
				}//end for(triangle)
			}//end for(triangles)
		return result;
		}//end getMaximumVertexDims()
	public Vector3D getMinimumVertexDims(){
		Vector3D result=new Vector3D(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
		Triangle [][]t=getPrimitives();
		for(Triangle [] frame:t){
			for(Triangle tri:frame){
				for(int i=0; i<3; i++){
					double v;
					v=(tri.x[i]);
					result=result.getX()>v?new Vector3D(v,result.getY(),result.getZ()):result;
					v=(tri.y[i]);
					result=result.getY()>v?new Vector3D(result.getX(),v,result.getZ()):result;
					v=(tri.z[i]);
					result=result.getZ()>v?new Vector3D(result.getX(),result.getY(),v):result;
					}//end for(vertex)
				}//end for(triangle)
			}//end for(triangles)
		return result;
		}//end getMaximumVertexDims()
	
	public double getMaximumVertexValue(){
		double result=0;
		Triangle [][]t=getPrimitives();
		for(Triangle [] frame:t){
			for(Triangle tri:frame){
				for(int i=0; i<3; i++){
					double v;
					v=Math.abs(tri.x[i]);
					result=result<v?v:result;
					v=Math.abs(tri.y[i]);
					result=result<v?v:result;
					v=Math.abs(tri.z[i]);
					result=result<v?v:result;
					}//end for(vertex)
				}//end for(triangle)
			}//end for(triangles)
		return result;
		}//end getMaximumVertexValue()
	}//end SingleTextureTriangleList
