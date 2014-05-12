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

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Vertex;

public class Triangle {
    private Vertex[] vertices = new Vertex[3];
    private Vector2D[] uv = new Vector2D[3];
    private RenderMode renderMode;
    private boolean isAlphaBlended = false;
    private Vector3D centroidNormal;
    private final StackTraceElement[] creationStackTrace;

    public final TRFutureTask<TextureDescription> texture;
    public static boolean debugTriangles = false;

    public Triangle(TRFutureTask<TextureDescription> texture) {
	if (debugTriangles)
	    creationStackTrace = new Exception().getStackTrace();
	else
	    creationStackTrace = null;
	this.texture=texture;
    }

    public Triangle(Vertex[] vertices, TRFutureTask<TextureDescription> texture) {
	this(texture);
	setVertex(vertices[0], 0);
	setVertex(vertices[1], 1);
	setVertex(vertices[2], 2);
    }

    public static Triangle[] quad2Triangles(Vertex[] vertices, Vector2D[] uvs,
	    TRFutureTask<TextureDescription> texture, RenderMode mode,
	    boolean hasAlpha, Vector3D centroidNormal, String debugName) {
	return quad2Triangles(vertices, uvs, texture, mode, hasAlpha,
		centroidNormal, new Triangle[2], 0, debugName);
    }

    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode mode, Vector3D[] normals, int ringRotation,
	    Triangle[] dest, int destOffset) {
	return quad2Triangles(x, y, z, u, v, texture, mode, false, normals,
		ringRotation, dest, destOffset);
    }

    public void setVertex(Vertex vtx, int index) {
	if (vertices[index] != null) {
	    vertices[index].removeTriangle(this);
	}
	vertices[index] = vtx;
	vtx.addTriangle(this);
    }// end setVertex(...)

    public void setUV(Vector2D uv, int index) {
	this.uv[index] = uv;
    }

    public void setUVNoCopy(Vector2D[] uv) {
	this.uv = uv;
    }

    public void setUVCopy(Vector2D[] uv) {
	for (int i = 0; i < 3; i++) {
	    this.uv[i] = uv[i];
	}
    }// end setUVCopy

    public Vector2D getUV(int index) {
	return this.uv[index];
    }

    public static Triangle[] quad2Triangles(Vertex[] vertices, Vector2D[] uv,
	    TRFutureTask<TextureDescription> texture, RenderMode mode,
	    boolean hasAlpha, Triangle[] dest, int destOffset) {
	Triangle t;
	t = new Triangle(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	t.setVertex(vertices[0], 0);
	t.setVertex(vertices[1], 1);
	t.setVertex(vertices[2], 2);
	t.setUV(uv[0], 0);
	t.setUV(uv[1], 1);
	t.setUV(uv[2], 2);
	dest[0 + destOffset] = t;
	t = new Triangle(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);

	t.setVertex(vertices[2], 0);
	t.setVertex(vertices[3], 1);
	t.setVertex(vertices[0], 2);
	t.setUV(uv[2], 0);
	t.setUV(uv[3], 1);
	t.setUV(uv[0], 2);
	dest[1 + destOffset] = t;
	return dest;
    }

    public static Triangle[] quad2Triangles(Vertex[] vertices, Vector2D[] uv,
	    TRFutureTask<TextureDescription> texture, RenderMode mode,
	    boolean hasAlpha, Vector3D centroidNormal, Triangle[] dest,
	    int destOffset, String debugName) {
	Triangle t;
	t = new Triangle(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	t.setVertex(vertices[0], 0);
	t.setVertex(vertices[1], 1);
	t.setVertex(vertices[2], 2);
	t.setUV(uv[0], 0);
	t.setUV(uv[1], 1);
	t.setUV(uv[2], 2);
	t.setCentroidNormal(centroidNormal);
	dest[0 + destOffset] = t;
	t = new Triangle(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);

	t.setVertex(vertices[2], 0);
	t.setVertex(vertices[3], 1);
	t.setVertex(vertices[0], 2);
	t.setUV(uv[2], 0);
	t.setUV(uv[3], 1);
	t.setUV(uv[0], 2);
	t.setCentroidNormal(centroidNormal);
	dest[1 + destOffset] = t;
	return dest;
    }

    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D centroidNormal,
	    String debugName) {
	return quad2Triangles(x, y, z, u, v, texture, mode, hasAlpha,
		centroidNormal, new Triangle[2], 0, debugName);
    }

    /**
     * Converts supplied quad coordinates to a pair of triangles in clockwise
     * order, top-left being index zero.
     * 
     */
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D centroidNormal,
	    Triangle[] dest, int destOffset, String debugName) {
	final Vertex[] vertices = new Vertex[] {
		new Vertex().setPosition(new Vector3D(x[0], y[0], z[0])),
		new Vertex().setPosition(new Vector3D(x[1], y[1], z[1])),
		new Vertex().setPosition(new Vector3D(x[2], y[2], z[2])),
		new Vertex().setPosition(new Vector3D(x[3], y[3], z[3])), };
	Vector2D[] uvs = new Vector2D[4];
	for (int i = 0; i < 4; i++) {
	    uvs[i] = new Vector2D(u[i], v[i]);
	}
	return quad2Triangles(vertices, uvs, texture, mode, hasAlpha,
		centroidNormal, dest, destOffset, debugName);
    }// end quad2Triangles(...)

    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D[] normals,
	    int ringRotation) {
	return quad2Triangles(x, y, z, u, v, texture, mode, hasAlpha, normals,
		ringRotation, new Triangle[2], 0);
    }

    /**
     * Converts supplied quad coordinates to a pair of triangles in clockwise
     * order, top-left being index zero+ringRotation.
     * 
     * @param ringRotation
     * 
     */
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D[] normals,
	    int ringRotation, Triangle[] dest, int destOffset) {
	final Vertex[] vertices = new Vertex[] {
		new Vertex().setPosition(
			new Vector3D(x[(0 + ringRotation) % 4],
				y[(0 + ringRotation) % 4],
				z[(0 + ringRotation) % 4])).setNormal(
			normals[(0 + ringRotation) % 4]),
		new Vertex().setPosition(
			new Vector3D(x[(1 + ringRotation) % 4],
				y[(1 + ringRotation) % 4],
				z[(1 + ringRotation) % 4])).setNormal(
			normals[(1 + ringRotation) % 4]),
		new Vertex().setPosition(
			new Vector3D(x[(2 + ringRotation) % 4],
				y[(2 + ringRotation) % 4],
				z[(2 + ringRotation) % 4])).setNormal(
			normals[(2 + ringRotation) % 4]),
		new Vertex().setPosition(
			new Vector3D(x[(3 + ringRotation) % 4],
				y[(3 + ringRotation) % 4],
				z[(3 + ringRotation) % 4])).setNormal(
			normals[(3 + ringRotation) % 4]), };
	Vector2D[] uvs = new Vector2D[4];
	for (int i = 0; i < 4; i++) {
	    final int rotI = (i + ringRotation) % 4;
	    uvs[i] = new Vector2D(u[rotI], v[rotI]);
	}
	return quad2Triangles(vertices, uvs, texture, mode, hasAlpha, dest,
		destOffset);
    }// end quad2Triangles(...)

    /**
     * @return the renderMode
     */
    public RenderMode getRenderMode() {
	return renderMode;
    }

    /**
     * @param renderMode
     *            the renderMode to set
     */
    public void setRenderMode(RenderMode renderMode) {
	this.renderMode = renderMode;
    }

    /**
     * @return the isAlphaBlended
     */
    public boolean isAlphaBlended() {
	return isAlphaBlended;
    }

    /**
     * @param isAlphaBlended
     *            the isAlphaBlended to set
     */
    public void setAlphaBlended(boolean isAlphaBlended) {
	this.isAlphaBlended = isAlphaBlended;
    }

    /**
     * @return the vertices
     */
    public Vertex[] getVertices() {
	return vertices;
    }

    /**
     * @param vertices
     *            the vertices to set
     */
    public void setVertices(Vertex[] vertices) {
	this.vertices = vertices;
    }

    /**
     * @return the centroidNormal
     */
    public Vector3D getCentroidNormal() {
	return centroidNormal;
    }

    /**
     * @param centroidNormal
     *            the centroidNormal to set
     */
    public void setCentroidNormal(Vector3D centroidNormal) {
	this.centroidNormal = centroidNormal;
    }

    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode renderMode, Vector3D[] normals, int ringRotation) {
	return quad2Triangles(x, y, z, u, v, texture, renderMode, normals,
		ringRotation, new Triangle[2], 0);
    }

    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, TRFutureTask<TextureDescription> texture,
	    RenderMode renderMode, Vector3D centroidNormal, String debugName) {
	return quad2Triangles(x, y, z, u, v, texture, renderMode, false,
		centroidNormal, new Triangle[2], 0, debugName);
    }

    /**
     * @return the creationStackTrace
     */
    public StackTraceElement[] getCreationStackTrace() {
	return creationStackTrace;
    }
}// Triangle

