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

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.TriangleVertex2FlatDoubleWindow;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.core.WindowAnimator;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.mem.MemoryWindow;

public class TriangleList extends PrimitiveList<Triangle> {
    private 		Controller 			controller;
    private 		int 				timeBetweenFramesMsec;
    private final 	boolean 			animateUV;
    private final 	WindowAnimator 			xyzAnimator;
    private 		TriangleVertex2FlatDoubleWindow flatTVWindow;

    public TriangleList(Triangle[][] triangles, int timeBetweenFramesMsec,
	    String debugName, boolean animateUV, Controller controller, TR tr, Model m) {
	super(debugName, triangles, new TriangleVertexWindow(tr, debugName), tr,m);
	this.timeBetweenFramesMsec 	= timeBetweenFramesMsec;
	this.animateUV 			= animateUV;
	this.controller 		= controller;
	if (getPrimitives().length > 1) {
	    this.xyzAnimator = new WindowAnimator(
		    getFlatTVWindow(),
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

    private void setupVertex(int vIndex, int gpuTVIndex, int triangleIndex, TextureDescription td)
	    throws ExecutionException, InterruptedException {
	final int 	numFrames	= getPrimitives().length;
	final Triangle 	t 		= triangleAt(0, triangleIndex);
	final Vector3D 	pos 		= t.getVertices()[vIndex].getPosition();
	final TriangleVertexWindow vw 	= (TriangleVertexWindow) getMemoryWindow();
	////////////////////// V E R T E X //////////////////////////////
	if (numFrames == 1) {
	    vw.x.set(gpuTVIndex, (short) applyScale(pos.getX()));
	    vw.y.set(gpuTVIndex, (short) applyScale(pos.getY()));
	    vw.z.set(gpuTVIndex, (short) applyScale(pos.getZ()));
	    final Vector3D normal = t.getVertices()[vIndex].getNormal();
	    vw.normX.set(gpuTVIndex, (byte)(normal.getX()*127));
	    vw.normY.set(gpuTVIndex, (byte)(normal.getY()*127));
	    vw.normZ.set(gpuTVIndex, (byte)(normal.getZ()*127));
	} else if (numFrames > 1) {
	    float[] xFrames = new float[numFrames];
	    float[] yFrames = new float[numFrames];
	    float[] zFrames = new float[numFrames];
	    float[] nxFrames = new float[numFrames];
	    float[] nyFrames = new float[numFrames];
	    float[] nzFrames = new float[numFrames];
	    for (int i = 0; i < numFrames; i++) {
		xFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getX()
			/ scale);
	    }
	    xyzAnimator.addFrames(xFrames);

	    for (int i = 0; i < numFrames; i++) {
		yFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getY()
			/ scale);
	    }
	    xyzAnimator.addFrames(yFrames);

	    for (int i = 0; i < numFrames; i++) {
		zFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getPosition().getZ()
			/ scale);
	    }
	    xyzAnimator.addFrames(zFrames);
	    
	    for (int i = 0; i < numFrames; i++) {
		nxFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getX()*127);
	    }
	    xyzAnimator.addFrames(nxFrames);

	    for (int i = 0; i < numFrames; i++) {
		nyFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getY()*127);
	    }
	    xyzAnimator.addFrames(nyFrames);

	    for (int i = 0; i < numFrames; i++) {
		nzFrames[i] = Math.round(triangleAt(i, triangleIndex).getVertices()[vIndex].getNormal().getZ()*127);
	    }
	    xyzAnimator.addFrames(nzFrames);
	} else {
	    throw new RuntimeException("Empty triangle vertex!");
	}
	//////////////// T E X T U R E ///////////////////////////
	if(td==null){
	    System.err.println("Stack trace of triangle creation below. NullPointerException follows.");
	    for(StackTraceElement el:t.getCreationStackTrace()){
		System.err.println("\tat "+el.getClassName()+"."+el.getMethodName()+"("+el.getFileName()+":"+el.getLineNumber()+")");
	    }//end for(stackTrace)
	    throw new NullPointerException("Texture for triangle in "+debugName+" intolerably null.");}
	if (td instanceof Texture ) {// Static texture
	    final Texture.TextureTreeNode tx;
	    tx = ((Texture) td).getNodeForThisTexture();
	    if (animateUV && numFrames > 1) {// Animated UV
		float[] uFrames = new float[numFrames];
		float[] vFrames = new float[numFrames];
		final WindowAnimator uvAnimator = new WindowAnimator(
			getFlatTVWindow(), 2,// UV per vertex
			numFrames, false, getVertexSequencer(
				timeBetweenFramesMsec, numFrames),
			new UVXferFunc(gpuTVIndex * UVXferFunc.BACK_STRIDE_LEN));
		getModel().addTickableAnimator(uvAnimator);
		for (int i = 0; i < numFrames; i++) {
		    uFrames[i] = (float) (uvUpScaler * tx
			    .getGlobalUFromLocal(triangleAt(i, triangleIndex).getUV(vIndex).getX()));
		    vFrames[i] = (float) (uvUpScaler * tx
			    .getGlobalVFromLocal(triangleAt(i, triangleIndex).getUV(vIndex).getY()));
		}// end for(numFrames)
		uvAnimator.addFrames(uFrames);
		uvAnimator.addFrames(vFrames);
	    } else {// end if(animateUV)
		vw.u.set(gpuTVIndex, (short) (uvUpScaler * tx
			.getGlobalUFromLocal(t.getUV(vIndex).getX())));
		vw.v.set(gpuTVIndex, (short) (uvUpScaler * tx
			.getGlobalVFromLocal(t.getUV(vIndex).getY())));
	    }// end if(!animateUV)
	    final int textureID = tx.getTexturePage();
	    vw.textureIDLo .set(gpuTVIndex, (byte)(textureID & 0xFF));
	    vw.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi .set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
	}// end if(Texture)
	//TODO: Temporary. Remove this check and replace with else {}, this is a kludge to avoid nonfunctional animation.
	else if(!tr.getTrConfig().isUsingNewTexturing()) {// Animated texture
	    AnimatedTexture at = ((AnimatedTexture) td);
	    Texture.TextureTreeNode tx = at.getFrames()[0].get()
		    .getNodeForThisTexture();// Default frame
	    final int numTextureFrames = at.getFrames().length;
	    final WindowAnimator uvAnimator = new WindowAnimator(
		    getFlatTVWindow(),
		    2,// UV per vertex
		    numTextureFrames, false, at.getTextureSequencer(),
		    new UVXferFunc(gpuTVIndex * UVXferFunc.BACK_STRIDE_LEN));
	    uvAnimator.setDebugName(debugName + ".uvAnimator");
	    getModel().addTickableAnimator(uvAnimator);

	    float[] uFrames = new float[numTextureFrames];
	    float[] vFrames = new float[numTextureFrames];
	    for (int ti = 0; ti < numTextureFrames; ti++) {
		tx = at.getFrames()[ti].get().getNodeForThisTexture();
		uFrames[ti] = (short) (uvUpScaler * tx
			.getGlobalUFromLocal(t.getUV(vIndex).getX()));
		vFrames[ti] = (short) (uvUpScaler * tx
			.getGlobalVFromLocal(t.getUV(vIndex).getY()));
	    }// end for(frame)
	    uvAnimator.addFrames(uFrames);
	    uvAnimator.addFrames(vFrames);
	    final int textureID = tx.getTexturePage();
	    vw.textureIDLo .set(gpuTVIndex, (byte)(textureID & 0xFF));
	    vw.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi .set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
	}// end animated texture
	if(tr.getTrConfig().isUsingNewTexturing() && td instanceof AnimatedTexture){//Animated texture (new system)
	    if (animateUV && numFrames > 1) {// Animated UV
		float[] uFrames = new float[numFrames];
		float[] vFrames = new float[numFrames];
		final WindowAnimator uvAnimator = new WindowAnimator(
			getFlatTVWindow(), 2,// UV per vertex
			numFrames, false, getVertexSequencer(
				timeBetweenFramesMsec, numFrames),
			new UVXferFunc(gpuTVIndex * UVXferFunc.BACK_STRIDE_LEN));
		getModel().addTickableAnimator(uvAnimator);
		for (int i = 0; i < numFrames; i++) {
		    uFrames[i] = (float) (uvUpScaler * triangleAt(i, triangleIndex).getUV(vIndex).getX());
		    vFrames[i] = (float) (uvUpScaler * triangleAt(i, triangleIndex).getUV(vIndex).getY());
		}// end for(numFrames)
		uvAnimator.addFrames(uFrames);
		uvAnimator.addFrames(vFrames);
	    } else {// end if(animateUV)
		vw.u.set(gpuTVIndex, (short) (uvUpScaler * t.getUV(vIndex).getX()));
		vw.v.set(gpuTVIndex, (short) (uvUpScaler * t.getUV(vIndex).getY()));
	    }// end if(!animateUV)
	    AnimatedTexture at = ((AnimatedTexture) td);
	    final TexturePageAnimator texturePageAnimator = new TexturePageAnimator(at,vw,gpuTVIndex);
	    texturePageAnimator.setDebugName(debugName + ".texturePageAnimator");
	    getModel().addTickableAnimator(texturePageAnimator);
	}
    }// end setupVertex

    private void setupTriangle(final int triangleIndex, final TextureDescription textureDescription,final int [] vertexIndices) throws ExecutionException,
	    InterruptedException {
		setupVertex(0, vertexIndices[triangleIndex*3+0], triangleIndex,textureDescription);
		setupVertex(1, vertexIndices[triangleIndex*3+1], triangleIndex,textureDescription);
		setupVertex(2, vertexIndices[triangleIndex*3+2], triangleIndex,textureDescription);
    }//setupTriangle

    public void uploadToGPU(GL3 gl) {
	final int nPrimitives = getNumElements();
	final int [] triangleVertexIndices = new int[nPrimitives*3];
	final TextureDescription [] textureDescriptions = new TextureDescription[nPrimitives];
	final MemoryWindow mw = getMemoryWindow();
	for (int vIndex = 0; vIndex < nPrimitives*3; vIndex++) {
		triangleVertexIndices[vIndex]=mw.create();
	    }
	for (int tIndex = 0; tIndex < nPrimitives; tIndex++) {
	    textureDescriptions[tIndex] = triangleAt(0, tIndex).texture
		    .get();
	}
	// Submit the GL task to set up the triangles.
	if(tr.getTrConfig().isUsingNewTexturing()){
	    tr.getThreadManager().submitToGL(new Callable<Void>() {
		    @Override
		    public Void call() throws Exception {
			for (int tIndex = 0; tIndex < nPrimitives; tIndex++) {
				setupTriangle(tIndex,textureDescriptions[tIndex],triangleVertexIndices);}
			return null;
		    }//end Call()
		}).get();
	}else{
	    Texture.executeInGLFollowingFinalization.add(new GLRunnable() {
		    @Override
		    public boolean run(GLAutoDrawable d){
			for (int tIndex = 0; tIndex < nPrimitives; tIndex++) {
				try{setupTriangle(tIndex,textureDescriptions[tIndex],triangleVertexIndices);}
				catch(Exception e){throw new RuntimeException(e);}}
			return true;
		    }//end run()
		});
	}//end legacy texturing enqueue later.
	
    }// end allocateIndices(...)

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
	Vector3D 	result 	= Vector3D.ZERO;
	Triangle[][] 	t 	= getPrimitives();
	for (Triangle[] frame : t) {
	    for (Triangle tri : frame) {
		for (int i = 0; i < 3; i++) {
		    double v;
		    final Vector3D pos = tri.getVertices()[i].getPosition();
		    v = pos.getX();
		    result = result.getX() < v ? new Vector3D(v, result.getY(),
			    result.getZ()) : result;
		    v = pos.getY();
		    result = result.getY() < v ? new Vector3D(result.getX(), v,
			    result.getZ()) : result;
		    v = pos.getZ();
		    result = result.getZ() < v ? new Vector3D(result.getX(),
			    result.getY(), v) : result;
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	return result;
    }// end getMaximumVertexDims()

    public Vector3D getMinimumVertexDims() {
	Vector3D result = new Vector3D(Double.POSITIVE_INFINITY,
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	Triangle[][] t = getPrimitives();
	for (Triangle[] frame : t) {
	    for (Triangle tri : frame) {
		for (int i = 0; i < 3; i++) {
		    double v;
		    final Vector3D pos = tri.getVertices()[i].getPosition();
		    v = pos.getX();
		    result = result.getX() > v ? new Vector3D(v, result.getY(),
			    result.getZ()) : result;
		    v = pos.getY();
		    result = result.getY() > v ? new Vector3D(result.getX(), v,
			    result.getZ()) : result;
		    v = pos.getZ();
		    result = result.getZ() > v ? new Vector3D(result.getX(),
			    result.getY(), v) : result;
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	return result;
    }// end getMaximumVertexDims()

    public double getMaximumVertexValue() {
	double result = 0;
	Triangle[][] t = getPrimitives();
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
	return result;
    }// end getMaximumVertexValue()

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
