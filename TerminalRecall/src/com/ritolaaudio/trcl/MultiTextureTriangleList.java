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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.opengl.GL2;


public class MultiTextureTriangleList
	{
	HashMap<Texture,SingleTextureTriangleList> lists = new HashMap<Texture,SingleTextureTriangleList>();
	
	public void add(Triangle t, GL2 gl)
		{
		if(!lists.containsKey(t.getTexture()))
			{lists.put(t.getTexture(), new SingleTextureTriangleList(t.getTexture()));}
		lists.get(t.getTexture()).add(t,gl);
		}//end add()
	
	public void render(GL2 gl)
		{
		int textureCounter=0;
		IntBuffer ib = ByteBuffer.allocateDirect(4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS,ib);
		final int numTextureUnits = ib.get();
		//System.out.println("NUM TEXTURE UNITS: "+numTextureUnits);
		for(Entry<Texture, SingleTextureTriangleList> ent:lists.entrySet())
			{ent.getValue().draw(gl, textureCounter++%numTextureUnits);}
		}//end render(...)
	
	public Collection<Triangle> dumpTriangles()
		{
		ArrayList<Triangle> result = new ArrayList<Triangle>();
		for(Entry<Texture,SingleTextureTriangleList> ent:lists.entrySet())
			{
			result.addAll(ent.getValue().dumpTriangles());
			}
		return result;
		}//end dumpTriangles
	
	public void free(GL2 gl)
		{
		for(Entry<Texture,SingleTextureTriangleList> ent:lists.entrySet())
			{
			ent.getValue().free(gl);
			}
		}//end free()
	}//end MultiTextureTriangleList
