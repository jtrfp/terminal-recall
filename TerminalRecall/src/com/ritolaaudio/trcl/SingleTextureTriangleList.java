/*******************************************************************************
 * Copyright (c) 2012 chuck.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL2;

public class SingleTextureTriangleList
	{
	Texture texture;
	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	GLBuffer<Vertex> vertices;
	GLBuffer<TextureCoordinate> tCoords;
	GLBuffer<Index> vIndices;
	
	public SingleTextureTriangleList(Texture t)
		{
		texture = t;
		}
	
	public void add(Triangle t, GL2 gl)
		{
		triangles.add(t);
		invalidate(gl);
		}
	
	private void invalidate(GL2 gl)
		{
		if(vertices!=null)vertices.free(gl);vertices=null;
		if(tCoords!=null)tCoords.free(gl);tCoords=null;
		}
	
	private boolean isValid()
		{
		return vertices!=null && tCoords!=null;
		}
	/**
	 * Write all triangles to the buffers
	 * 
	 * @since Oct 5, 2012
	 */
	protected void validate(GL2 gl)
		{
		if(isValid()){invalidate(gl);}
		vertices = new GLBuffer<Vertex>(triangles.size()*3){};
		tCoords = new GLBuffer<TextureCoordinate>(triangles.size()*3){};
		vIndices = new GLBuffer<Index>(triangles.size()*3){};
		//vertices.rewind();
		//tCoords.rewind();
		//vIndices.rewind();
		
		vertices.map(gl);
		tCoords.map(gl);
		vIndices.map(gl);
		
		vertices.rewind();
		tCoords.rewind();
		vIndices.rewind();
		
		int index=0;
		for(Triangle t:triangles)
			{
			int i=0;
			vertices.put(new Vertex(t.x[i],t.y[i],t.z[i]));
			tCoords.put(new TextureCoordinate(t.u[i],t.v[i]));
			vIndices.put(new Index(index++));
			i++;
			vertices.put(new Vertex(t.x[i],t.y[i],t.z[i]));
			tCoords.put(new TextureCoordinate(t.u[i],t.v[i]));
			vIndices.put(new Index(index++));
			i++;
			vertices.put(new Vertex(t.x[i],t.y[i],t.z[i]));
			tCoords.put(new TextureCoordinate(t.u[i],t.v[i]));
			vIndices.put(new Index(index++));
			}
		vertices.unmap(gl);
		tCoords.unmap(gl);
		vIndices.unmap(gl);
		}//end loadIntoTextureUnit(...)
	
	private void slowDraw(GL2 gl, int textureUnit)
		{
		//////////////
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureID());
		gl.glBegin(GL2.GL_TRIANGLES);
		final int n = triangles.size()*3;
		for(int i=0; i<n; i++)
			{
			Vertex v=vertices.get(i);
			TextureCoordinate tc=tCoords.get(i);
			gl.glTexCoord2d(tc.getU(), tc.getV());
			gl.glVertex3d(v.getX(), v.getY(),v.getZ());
			}
		gl.glEnd();
		
		///////
		}
	
	private void fastDraw(GL2 gl, int textureUnit)
		{
		//Bind VBOs
		//gl.glClientActiveTexture(GL2.GL_TEXTURE0+textureUnit);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,vertices.getBufferID());
		gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, 0);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, tCoords.getBufferID());
		gl.glTexCoordPointer(2, GL2.GL_DOUBLE, 0, 0);
		
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, vIndices.getBufferID());
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.getTextureID());
		
		//Draw
		gl.glDrawElements(GL2.GL_TRIANGLES,triangles.size()*3,GL2.GL_UNSIGNED_INT,0);
		//gl.glDrawArrays(GL2.GL_TRIANGLES, 0, triangles.size());
		
		//Clean up
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glClientActiveTexture(GL2.GL_TEXTURE0);
		}
	
	public void draw(GL2 gl, int textureUnit)
		{
		if(!isValid())validate(gl);
		fastDraw(gl,textureUnit);
		}
	
	public Collection<Triangle>dumpTriangles()
		{
		return triangles;
		}
	
	public void free(GL2 gl)
		{
		vertices.free(gl);
		tCoords.free(gl);
		vIndices.free(gl);
		vertices=null;
		tCoords=null;
		vIndices=null;
		}
	}//end SingleTextureTriangleList
