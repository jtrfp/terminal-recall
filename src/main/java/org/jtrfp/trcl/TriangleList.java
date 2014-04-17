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
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TriangleVertex2FlatDoubleWindow;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.core.WindowAnimator;

public class TriangleList extends PrimitiveList<Triangle> {
    private Controller controller;
    private int timeBetweenFramesMsec;
    private final boolean animateUV;
    private final TR tr;
    private final WindowAnimator xyzAnimator;
    private TriangleVertex2FlatDoubleWindow flatTVWindow;

    public TriangleList(Triangle[][] triangles, int timeBetweenFramesMsec,
	    String debugName, boolean animateUV, Controller controller, TR tr, Model m) {
	super(debugName, triangles, new TriangleVertexWindow(tr, debugName), tr,m);
	this.timeBetweenFramesMsec = timeBetweenFramesMsec;
	this.animateUV = animateUV;
	this.controller = controller;
	this.tr = tr;
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
    }

    private static class XYZXferFunc implements IntTransferFunction {
	private final int startIndex;
	public static final int BACK_STRIDE_LEN=8;
	public static final int FRONT_STRIDE_LEN=6;
	private static final byte [] STRIDE_PATTERN = new byte[]{
	    0,1,2,//XYZ
	    //UV
	    5,6,7//NxNyNz
	};

	public XYZXferFunc(int startIndex) {
	    this.startIndex = startIndex;
	}// end constructor

	@Override
	public int transfer(int input) {
	    //return (input / FRONT_STRIDE_LEN) * BACK_STRIDE_LEN + (input % FRONT_STRIDE_LEN) + startIndex;
	    return startIndex + STRIDE_PATTERN[input%FRONT_STRIDE_LEN]+(input/FRONT_STRIDE_LEN)*BACK_STRIDE_LEN;
	}// end transfer(...)
    }// end class XYZXferFunc

    private static class UVXferFunc implements IntTransferFunction {
	private final int startIndex;
	public static final int BACK_STRIDE_LEN=8;
	private final int FRONT_STRIDE_LEN=2;
	
	public UVXferFunc(int startIndex) {
	    this.startIndex = startIndex;
	}// end constructor

	@Override
	public int transfer(int input) {
	    return (input / FRONT_STRIDE_LEN) * BACK_STRIDE_LEN + (input % FRONT_STRIDE_LEN) + startIndex + 3;
	}// end transfer(...)
    }// end class XYZXferFunc

    public TriangleList[] getAllLists() {
	return getAllArrayLists().toArray(new TriangleList[] {});
    }

    private Controller getVertexSequencer(int timeBetweenFramesMsec, int nFrames) {
	return controller;
    }

    private Triangle triangleAt(int frame, int tIndex) {
	return getPrimitives()[frame][tIndex];
    }

    private void setupVertex(int vIndex, int gpuTVIndex, int triangleIndex)
	    throws ExecutionException, InterruptedException {
	final int numFrames = getPrimitives().length;
	Triangle t = triangleAt(0, triangleIndex);
	Vector3D pos = t.getVertices()[vIndex].getPosition();
	final TriangleVertexWindow vw = (TriangleVertexWindow) getMemoryWindow();
	final int textureID=10;// TODO: This is a stub for actual texture TOC vec4 location
	if (numFrames == 1) {
	    vw.textureIDLo.set(gpuTVIndex, (byte)(textureID & 0xFF));
	    vw.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi.set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
	    vw.x.set(gpuTVIndex, (short) applyScale(pos.getX()));
	    vw.y.set(gpuTVIndex, (short) applyScale(pos.getY()));
	    vw.z.set(gpuTVIndex, (short) applyScale(pos.getZ()));
	    final Vector3D normal = t.getVertices()[vIndex].getNormal();
	    vw.normX.set(gpuTVIndex, (byte)(normal.getX()*127));
	    vw.normY.set(gpuTVIndex, (byte)(normal.getY()*127));
	    vw.normZ.set(gpuTVIndex, (byte)(normal.getZ()*127));
	} else if (numFrames > 1) {
	    vw.textureIDLo.set(gpuTVIndex, (byte)(textureID & 0xFF));
	    vw.textureIDMid.set(gpuTVIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi.set(gpuTVIndex, (byte)((textureID >> 16) & 0xFF));
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

	TextureDescription td = t.getTexture().get();
	if (td instanceof Texture) {// Static texture
	    final Texture.TextureTreeNode tx;
	    tx = ((Texture) t.getTexture().get()).getNodeForThisTexture();
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
	}// end if(Texture)
	else {// Animated texture
	    AnimatedTexture at = ((AnimatedTexture) t.getTexture().get());
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
	}// end animated texture
    }// end setupVertex

    private void setupTriangle(int triangleIndex) throws ExecutionException,
	    InterruptedException {
	setupVertex(0, getMemoryWindow().create(), triangleIndex);
	setupVertex(1, getMemoryWindow().create(), triangleIndex);
	setupVertex(2, getMemoryWindow().create(), triangleIndex);
    }

    public void uploadToGPU(GL3 gl) {
	int nPrimitives = getNumElements();
	try {
	    for (int tIndex = 0; tIndex < nPrimitives; tIndex++) {
		setupTriangle(tIndex);
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    e.printStackTrace();
	}
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
	Vector3D result = Vector3D.ZERO;
	Triangle[][] t = getPrimitives();
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
