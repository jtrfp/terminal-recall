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
import org.jtrfp.trcl.gpu.Vertex;

public class Triangle {
    private Vertex [] vertices = new Vertex[3];
    private Vector2D [] uv = new Vector2D[3];
    private RenderMode renderMode;
    private boolean isAlphaBlended = false;
    private Vector3D centroidNormal;

    private Future<TextureDescription> texture;
    
    public Triangle(){}
    public Triangle(Vertex [] vertices){
	setVertex(vertices[0],0);
	setVertex(vertices[1],1);
	setVertex(vertices[2],2);
	}
    
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, Future<TextureDescription> texture,
	    RenderMode mode, Vector3D centroidNormal) {
	return quad2Triangles(x, y, z, u, v, texture, mode, false, centroidNormal);
    }
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, Future<TextureDescription> texture,
	    RenderMode mode, Vector3D [] normals) {
	return quad2Triangles(x, y, z, u, v, texture, mode, false, normals);
    }
    
    public void setVertex(Vertex vtx, int index){
	if(vertices[index]!=null){
	    vertices[index].removeTriangle(this);
	}
	vertices[index]=vtx;
	vtx.addTriangle(this);
    }//end setVertex(...)
    
    public void setUV(Vector2D uv, int index){
	this.uv[index]=uv;
    }
    public void setUVNoCopy(Vector2D [] uv){
	this.uv=uv;
    }
    public void setUVCopy(Vector2D [] uv){
	for(int i=0; i<3; i++){
	    this.uv[i]=uv[i];
	}
    }//end setUVCopy
    
    public Vector2D getUV(int index){
	return this.uv[index];
    }
    
    public static Triangle[] quad2Triangles(
	    Vertex [] vertices, Vector2D [] uv,
	    Future<TextureDescription> texture, 
	    RenderMode mode, boolean hasAlpha){
	Triangle[] result = new Triangle[2];
	Triangle t;
	t = new Triangle();
	t.setTexture(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	t.setVertex(vertices[0], 0);
	t.setVertex(vertices[1], 1);
	t.setVertex(vertices[2], 2);
	t.setUV(uv[0],0);
	t.setUV(uv[1],1);
	t.setUV(uv[2],2);
	result[0]=t;
	t = new Triangle();
	t.setTexture(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	
	t.setVertex(vertices[2], 0);
	t.setVertex(vertices[3], 1);
	t.setVertex(vertices[0], 2);
	t.setUV(uv[2],0);
	t.setUV(uv[3],1);
	t.setUV(uv[0],2);
	result[1] = t;
	return result;
    }
    
    public static Triangle[] quad2Triangles(
	    Vertex [] vertices, Vector2D [] uv,
	    Future<TextureDescription> texture, 
	    RenderMode mode, boolean hasAlpha, Vector3D centroidNormal){
	Triangle[] result = new Triangle[2];
	Triangle t;
	t = new Triangle();
	t.setTexture(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	t.setVertex(vertices[0], 0);
	t.setVertex(vertices[1], 1);
	t.setVertex(vertices[2], 2);
	t.setUV(uv[0],0);
	t.setUV(uv[1],1);
	t.setUV(uv[2],2);
	t.setCentroidNormal(centroidNormal);
	result[0]=t;
	t = new Triangle();
	t.setTexture(texture);
	t.setRenderMode(mode);
	t.setAlphaBlended(hasAlpha);
	
	t.setVertex(vertices[2], 0);
	t.setVertex(vertices[3], 1);
	t.setVertex(vertices[0], 2);
	t.setUV(uv[2],0);
	t.setUV(uv[3],1);
	t.setUV(uv[0],2);
	t.setCentroidNormal(centroidNormal);
	result[1] = t;
	return result;
    }
    /**
     * Converts supplied quad coordinates to a pair of triangles in clockwise
     * order, top-left being index zero.
     * 
     */
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, Future<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D centroidNormal) {
	final Vertex [] vertices = new Vertex[]{
		new Vertex().setPosition(new Vector3D(x[0],y[0],z[0])),
		new Vertex().setPosition(new Vector3D(x[1],y[1],z[1])),
		new Vertex().setPosition(new Vector3D(x[2],y[2],z[2])),
		new Vertex().setPosition(new Vector3D(x[3],y[3],z[3])),
	};
	Vector2D [] uvs = new Vector2D[4];
	for(int i=0; i<4; i++){
	    uvs[i]=new Vector2D(u[i],v[i]);
	}
	return quad2Triangles(vertices, uvs, texture, mode, hasAlpha, centroidNormal);
    }//end quad2Triangles(...)
    
    /**
     * Converts supplied quad coordinates to a pair of triangles in clockwise
     * order, top-left being index zero.
     * 
     */
    public static Triangle[] quad2Triangles(double[] x, double[] y, double[] z,
	    double[] u, double[] v, Future<TextureDescription> texture,
	    RenderMode mode, boolean hasAlpha, Vector3D [] normals) {
	final Vertex [] vertices = new Vertex[]{
		new Vertex().setPosition(new Vector3D(x[0],y[0],z[0])).setNormal(normals[0]),
		new Vertex().setPosition(new Vector3D(x[1],y[1],z[1])).setNormal(normals[1]),
		new Vertex().setPosition(new Vector3D(x[2],y[2],z[2])).setNormal(normals[2]),
		new Vertex().setPosition(new Vector3D(x[3],y[3],z[3])).setNormal(normals[3]),
	};
	Vector2D [] uvs = new Vector2D[4];
	for(int i=0; i<4; i++){
	    uvs[i]=new Vector2D(u[i],v[i]);
	}
	return quad2Triangles(vertices, uvs, texture, mode, hasAlpha);
    }//end quad2Triangles(...)

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
     * @return the texture
     */
    public Future<TextureDescription> getTexture() {
	return texture;
    }

    /**
     * @param cloudTexture
     *            the texture to set
     */
    public void setTexture(Future<TextureDescription> cloudTexture) {
	this.texture = cloudTexture;
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
     * @param vertices the vertices to set
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
     * @param centroidNormal the centroidNormal to set
     */
    public void setCentroidNormal(Vector3D centroidNormal) {
        this.centroidNormal = centroidNormal;
    }
}// Triangle

