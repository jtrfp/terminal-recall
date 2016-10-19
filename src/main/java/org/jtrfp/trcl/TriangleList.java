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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.FlatDoubleWindow;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TriangleVertex2FlatDoubleWindow;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.core.WindowAnimator;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.DynamicTexture;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.PortalTexture;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.mem.MemoryWindow;

public class TriangleList extends PrimitiveList<Triangle> {
    private 		Controller 			controller;
    private 		int 				timeBetweenFramesMsec;
    private 		int [] 				triangleVertexIndices;
    private final 	boolean 			animateUV;
    private final 	WindowAnimator 			xyzAnimator;
    private 		TriangleVertex2FlatDoubleWindow flatTVWindow;
    private volatile	Vector3D			cachedMinimumVertexDims,
    							cachedMaximumVertexDims;
    private volatile	Double				cachedMaximumVertexValue;

    public TriangleList(Triangle[][] triangles, int timeBetweenFramesMsec,
	    String debugName, boolean animateUV, Controller controller, TR tr, GL33Model m) {
	super(debugName, triangles, 
		new TriangleVertexWindow(Features.get(tr, GPUFeature.class),debugName), tr,m);
	this.timeBetweenFramesMsec 	= timeBetweenFramesMsec;
	this.animateUV 			= animateUV;
	this.controller 		= controller;
	if (getPrimitives().length > 1) {
	    final FlatDoubleWindow fdw = new TriangleVertex2FlatDoubleWindow(
		    (TriangleVertexWindow) this.getMemoryWindow().newContextWindow());
	    this.xyzAnimator = new WindowAnimator(
		    fdw,
		    this.getNumElements() * 3 * XYZXferFunc.FRONT_STRIDE_LEN,
		    				// 3 vertices per triangle,
						// XYZ+NxNyNz per vertex
		    getPrimitives().length, true, controller,
		    new XYZXferFunc(0));
	    getModel().addTickableAnimator(xyzAnimator);
	} else if (animateUV) {
	    this.xyzAnimator = null;
	} else {
	    this.xyzAnimator = null;
	}
    }//end constructor

    private static class XYZXferFunc implements IntTransferFunction {
	private final int startIndex;
	public static final  int 	BACK_STRIDE_LEN		= 8;
	public static final  int 	FRONT_STRIDE_LEN	= 6;
	private static final byte 	[] STRIDE_PATTERN 	= new byte[]{
	    0,1,2,//XYZ
	    //..UV
	    5,6,7//NxNyNz
	};

	public XYZXferFunc(int startIndex) {
	    this.startIndex = startIndex;
	}// end constructor

	@Override
	public int transfer(int input) {
	    return startIndex + STRIDE_PATTERN[input%FRONT_STRIDE_LEN]+(input/FRONT_STRIDE_LEN)*BACK_STRIDE_LEN;
	}// end transfer(...)
    }// end class XYZXferFunc

    private static class UVXferFunc implements IntTransferFunction {
	private final 		int startIndex;
	public static final 	int BACK_STRIDE_LEN=8;
	private final 		int FRONT_STRIDE_LEN=2;
	
	public UVXferFunc(int startIndex) {
	    this.startIndex = startIndex;
	}// end constructor

	@Override
	public int transfer(int input) {
	    return (input / FRONT_STRIDE_LEN) * BACK_STRIDE_LEN + (input % FRONT_STRIDE_LEN) + startIndex + 3;
	}// end transfer(...)
    }// end class XYZXferFunc

    private Controller getVertexSequencer(int timeBetweenFramesMsec, int nFrames) {
	return controller;
    }

    private Triangle triangleAt(int frame, int tIndex) {
	return getPrimitives()[frame][tIndex];
    }

    private void setupVertex(int vIndex, int gpuTVIndex, int triangleIndex, Texture td, TriangleVertexWindow vwContext)
	    throws ExecutionException, InterruptedException {
	final int 	numFrames	= getPrimitives().length;
	final Triangle 	t 		= triangleAt(0, triangleIndex);
	final Vector3D 	pos 		= t.getVertices()[vIndex].getPosition();
	//final TriangleVertexWindow vw 	= (TriangleVertexWindow) getMemoryWindow();
	
	////////////////////// V E R T E X //////////////////////////////
	if (numFrames == 1) {
	    vwContext.x.set(gpuTVIndex, (short) applyScale(pos.getX()));
	    vwContext.y.set(gpuTVIndex, (short) applyScale(pos.getY()));
	    vwContext.z.set(gpuTVIndex, (short) applyScale(pos.getZ()));
	    final Vector3D normal = t.getVertices()[vIndex].getNormal();
	    vwContext.normX.set(gpuTVIndex, (byte)(normal.getX()*127));
	    vwContext.normY.set(gpuTVIndex, (byte)(normal.getY()*127));
	    vwContext.normZ.set(gpuTVIndex, (byte)(normal.getZ()*127));
	    if(debugName.contains("MiniMap"))
		    if(pos.getZ() != 0)
			new Exception().printStackTrace();
	} else {
	    float[] xFrames = new float[numFrames];
	    float[] yFrames = new float[numFrames];
	    float[] zFrames = new float[numFrames];
	    float[] nxFrames = new float[numFrames];
	    float[] nyFrames = new float[numFrames];
	    float[] nzFrames = new float[numFrames];
	    for (int i = 0; i < numFrames; i++) 
		xFrames[i] = (float)applyScale(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getX());
	    
	    xyzAnimator.addFrames(xFrames);

	    for (int i = 0; i < numFrames; i++) 
		yFrames[i] = (float)applyScale(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getY());
	    
	    xyzAnimator.addFrames(yFrames);

	    for (int i = 0; i < numFrames; i++) 
		zFrames[i] = (float)applyScale(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getZ());
	    
	    xyzAnimator.addFrames(zFrames);
	    
	    for (int i = 0; i < numFrames; i++) 
		nxFrames[i] = (float)Math.rint(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getX()*127);
	    
	    xyzAnimator.addFrames(nxFrames);

	    for (int i = 0; i < numFrames; i++) 
		nyFrames[i] = (float)Math.rint(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getY()*127);
	    
	    xyzAnimator.addFrames(nyFrames);

	    for (int i = 0; i < numFrames; i++) 
		nzFrames[i] = (float)Math.rint(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getZ()*127);
	    
	    xyzAnimator.addFrames(nzFrames);
	}//end else(frames!=1)
	//////////////// T E X T U R E ///////////////////////////
	if(td==null){
	    System.err.println("Stack trace of triangle creation below. NullPointerException follows.");
	    final StackTraceElement [] elements = t.getCreationStackTrace();
	    if(elements == null)
		throw new NullPointerException("Stack trace elements null! Did you remember to turn on Triangle.debugTriangles?");
	    for(StackTraceElement el:elements){
		System.err.println("\tat "+el.getClassName()+"."+el.getMethodName()+"("+el.getFileName()+":"+el.getLineNumber()+")");
	    }//end for(stackTrace)
	    throw new NullPointerException("Texture for triangle in "+debugName+" intolerably null.");}
	else if (td instanceof PortalTexture){
	    PortalTexture portalTexture = (PortalTexture)td;
	    portalTexture.addRelevantVertexIndex(gpuTVIndex);
	    portalTexture.setTriangleVertexWindow(vwContext);
	    //final int textureID = 65536-((PortalTexture)td).getPortalFramebufferNumber();
	    //vw.textureIDLo .set(gpuTVIndex, (byte)(textureID & 0xFF));
	    //vw.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    //vw.textureIDHi .set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
	} if (td instanceof VQTexture ) {// Static texture
	    final int sideScalar = ((VQTexture)td).getSideLength()-1;
	    if (animateUV && numFrames > 1) {// Animated UV
		float[] uFrames = new float[numFrames];
		float[] vFrames = new float[numFrames];
		
		final FlatDoubleWindow fdw = new TriangleVertex2FlatDoubleWindow(
			    (TriangleVertexWindow) this.getMemoryWindow().newContextWindow());
		final WindowAnimator uvAnimator = new WindowAnimator(
			fdw, 2,// UV per vertex
			numFrames, false, getVertexSequencer(
				timeBetweenFramesMsec, numFrames),
			new UVXferFunc(gpuTVIndex * UVXferFunc.BACK_STRIDE_LEN));
		getModel().addTickableAnimator(uvAnimator);
		uvAnimator.setDebugName(debugName + ".uvAnimator");
		for (int i = 0; i < numFrames; i++) {
		    uFrames[i] = (float) Math.rint(sideScalar*triangleAt(i, triangleIndex).getUV(vIndex).getX());
		    vFrames[i] = (float) Math.rint(sideScalar*(1-triangleAt(i, triangleIndex).getUV(vIndex).getY()));
		}// end for(numFrames)
		uvAnimator.addFrames(uFrames);
		uvAnimator.addFrames(vFrames);
	    } else {// end if(animateUV)
		vwContext.u.set(gpuTVIndex, (short) Math.rint(sideScalar * t.getUV(vIndex).getX()));
		vwContext.v.set(gpuTVIndex, (short) Math.rint(sideScalar * (1-t.getUV(vIndex).getY())));
	    }// end if(!animateUV)
	    final int textureID = ((VQTexture)td).getTexturePage();
	    vwContext.textureIDLo .set(gpuTVIndex, (byte)(textureID & 0xFF));
	    vwContext.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    vwContext.textureIDHi .set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
	}// end if(Texture)
	if(td instanceof DynamicTexture){//Animated texture
	    final DynamicTexture dt = (DynamicTexture)td;
	    if (animateUV && numFrames > 1 && dt instanceof AnimatedTexture) {// Animated UV
		final AnimatedTexture at = (AnimatedTexture)dt;
		float[] uFrames = new float[numFrames];
		float[] vFrames = new float[numFrames];
		final WindowAnimator uvAnimator = new WindowAnimator(
			getFlatTVWindow(), 2,// UV per vertex
			numFrames, false, getVertexSequencer(
				timeBetweenFramesMsec, numFrames),
			new UVXferFunc(gpuTVIndex * UVXferFunc.BACK_STRIDE_LEN));
		getModel().addTickableAnimator(uvAnimator);
		for (int i = 0; i < numFrames; i++) {
		    final int sideScalar = at.getFrames()[i].getSideLength()-1;
		    uFrames[i] = (float) Math.rint(sideScalar * triangleAt(i, triangleIndex).getUV(vIndex).getX());
		    vFrames[i] = (float) Math.rint(sideScalar * (1-triangleAt(i, triangleIndex).getUV(vIndex).getY()));
		}// end for(numFrames)
		uvAnimator.addFrames(uFrames);
		uvAnimator.addFrames(vFrames);
	    } else if(dt instanceof AnimatedTexture) {// end if(animateUV)
		final AnimatedTexture at = (AnimatedTexture)dt;
		final int sideScalar = at.getFrames()[0].getSideLength()-1;
		vwContext.u.set(gpuTVIndex, (short) Math.rint(sideScalar * t.getUV(vIndex).getX()));
		vwContext.v.set(gpuTVIndex, (short) Math.rint(sideScalar * (1-t.getUV(vIndex).getY())));
	    }// end if(!animateUV)
	    final TexturePageAnimator texturePageAnimator = new TexturePageAnimator(dt,(TriangleVertexWindow)this.getMemoryWindow(),gpuTVIndex);
	    texturePageAnimator.setU(t.getUV(vIndex).getX());
	    texturePageAnimator.setV((1-t.getUV(vIndex).getY()));
	    texturePageAnimator.setDebugName(debugName + ".texturePageAnimator");
	    getModel().addTickableAnimator(texturePageAnimator);
	}//end if(animated texture)
    }// end setupVertex

    private void setupTriangle(final int triangleIndex, final Texture textureDescription,final int [] vertexIndices,
	                       TriangleVertexWindow window) throws ExecutionException,InterruptedException {
		int tIndex = triangleIndex*3;
		setupVertex(0, vertexIndices[tIndex], triangleIndex,textureDescription, window);
		setupVertex(1, vertexIndices[tIndex+1], triangleIndex,textureDescription, window);
		setupVertex(2, vertexIndices[tIndex+2], triangleIndex,textureDescription, window);
    }//setupTriangle
    /*
    @Override
    public void finalize(){
	final MemoryWindow mw = getMemoryWindow();
	for(int i=0; i<triangleVertexIndices.length;i++){
	    mw.free(triangleVertexIndices[i]);
	}//end for(triangleVertexIndices)
    }//end finalize()
*/
    public Future<Void> uploadToGPU() {
	final int nPrimitives = getNumElements();
	final int [] vertexIndices = new int[nPrimitives*3];
	final Texture [] textureDescriptions = new Texture[nPrimitives];
	final MemoryWindow mw = getMemoryWindow().newContextWindow();
	for (int vIndex = 0; vIndex < nPrimitives*3; vIndex++)
	    vertexIndices[vIndex]=mw.create();
	for (int tIndex = 0; tIndex < nPrimitives; tIndex++)
	    textureDescriptions[tIndex] = triangleAt(0, tIndex).texture;
	//calculateDims();
	//TODO: Remove memory sandbox after converting to flushable, flush in GL
	/*final Future<Void> result = tr.getThreadManager().submitToGPUMemAccess(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {*/
	try{
		final TriangleVertexWindow window = (TriangleVertexWindow)mw;
		for (int tIndex = 0; tIndex < nPrimitives; tIndex++)
		    setupTriangle(tIndex,textureDescriptions[tIndex],vertexIndices,window);
		window.flush();
		finalizePrimitives();
	}catch(Exception e){e.printStackTrace();}
	//    }//end Call()
	//});
	triangleVertexIndices = vertexIndices;
	return null;
    }// end uploadToGPU(...)

    @Override
    public int getElementSizeInVec4s() {
	return 3;
    }

    @Override
    public int getGPUVerticesPerElement() {
	return 3;
    }

    @Override
    public org.jtrfp.trcl.PrimitiveList.RenderStyle getRenderStyle() {
	return RenderStyle.OPAQUE;
    }

    public Vector3D getMaximumVertexDims() {
	if(cachedMaximumVertexDims!=null)
	    return cachedMaximumVertexDims;
	throw new IllegalStateException("Primitives must first be finalized.");
    }// end getMaximumVertexDims()

    public Vector3D getMinimumVertexDims() {
	if(cachedMinimumVertexDims!=null)
	    return cachedMinimumVertexDims;
	throw new IllegalStateException("Primitives must first be finalized.");
    }// end getMaximumVertexDims()

    public double getMaximumVertexValue() {
	if(cachedMaximumVertexValue!=null)
	    return cachedMaximumVertexValue;
	throw new IllegalStateException("Primitives must first be finalized.");
    }// end getMaximumVertexValue()
    
    @Override
    protected void calculateDims(){
	Triangle[][] t = getPrimitives();
	{double result = 0;
	for (Triangle[] frame : t) {
	    for (Triangle tri : frame) {
		for (int i = 0; i < 3; i++) {
		    double v;
		    final Vector3D pos = tri.getVertices()[i].getPosition();
		    v = Math.abs(pos.getX());
		    result = result < v ? v : result;
		    v = Math.abs(pos.getY());
		    result = result < v ? v : result;
		    v = Math.abs(pos.getZ());
		    result = result < v ? v : result;
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	cachedMaximumVertexValue=result;
	}
	//reset
	double mx=Double.POSITIVE_INFINITY,my=Double.POSITIVE_INFINITY,mz=Double.POSITIVE_INFINITY;
	//max
	for (Triangle[] frame : t) {
	    for (Triangle tri : frame) {
		for (int i = 0; i < 3; i++) {
		    //double v;
		    final Vector3D pos = tri.getVertices()[i].getPosition();
		    //v = pos.getX();
		    mx = Math.min(mx, pos.getX());
		    /*result = result.getX() > v ? new Vector3D(v, result.getY(),
			    result.getZ()) : result;*/
		    //v = pos.getY();
		    my = Math.min(my, pos.getY());
		    /*result = result.getY() > v ? new Vector3D(result.getX(), v,
			    result.getZ()) : result;*/
		    //v = pos.getZ();
		    mz = Math.min(mz, pos.getZ());
		    /*result = result.getZ() > v ? new Vector3D(result.getX(),
			    result.getY(), v) : result;*/
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	cachedMinimumVertexDims=new Vector3D(mx,my,mz);
	//reset
	mx = 0; my = 0; mz = 0;
	//max
	for (int index=0; index<t.length; index++) {
	    final Triangle []frame = t[index];
	    assert (frame != null):"Frame intolerably null at index "+index+".";//Verify null frame is a race condition.
	    for (Triangle tri : frame) {
		for (int i = 0; i < 3; i++) {
		    //double v;
		    final Vector3D pos = tri.getVertices()[i].getPosition();
		    //v = pos.getX();
		    mx = Math.max(mx, pos.getX());
		    /*result = result.getX() < v ? new Vector3D(v, result.getY(),
			    result.getZ()) : result;*/
		    //v = pos.getY();
		    my = Math.max(my, pos.getY());
		    /*result = result.getY() < v ? new Vector3D(result.getX(), v,
			    result.getZ()) : result;*/
		    //v = pos.getZ();
		    mz = Math.max(mz, pos.getZ());
		    /*result = result.getZ() < v ? new Vector3D(result.getX(),
			    result.getY(), v) : result;*/
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	cachedMaximumVertexDims=new Vector3D(mx,my,mz);
    }//calculateMaxDims()

    /**
     * @return the flatTVWindow
     */
    private TriangleVertex2FlatDoubleWindow getFlatTVWindow() {
	if (flatTVWindow == null)
	    flatTVWindow = new TriangleVertex2FlatDoubleWindow(
		    (TriangleVertexWindow) this.getMemoryWindow());
	return flatTVWindow;
    }

    @Override
    public byte getPrimitiveRenderMode() {
	return PrimitiveRenderMode.RENDER_MODE_TRIANGLES;
    }

    @Override
    public int getNumMemoryWindowIndicesPerElement() {
	return 3;
    }
}// end SingleTextureTriangleList
