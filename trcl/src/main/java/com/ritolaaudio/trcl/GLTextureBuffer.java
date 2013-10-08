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

import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

public abstract class GLTextureBuffer extends RawGLBuffer
	{
	private final int textureID;
	public static final int BYTES_PER_VEC4=4*4;
	private static final int DEADBEEF = 0xEFBEADDE;
	
	/**
	 * 
	 * @param sizeInBytes	Number of bytes to allocate. NOTE: This will round up to next multiple of 1024 to reduce probability of segfault in AMD driver.
	 * @param gl
	 */
	public GLTextureBuffer(int sizeInBytes, final GL3 gl)
		{
		super(roundToNextKB(sizeInBytes),gl);
		//Allocate a texture id
		textureID=Texture.createTextureID(gl);
		gl.glBindTexture(getBindingTarget(), getTextureID());
		gl.glTexBuffer(getBindingTarget(),GL2.GL_RGBA32UI,this.getBufferID());
		this.map(gl);
		IntBuffer buf = this.getUnderlyingBuffer().asIntBuffer();
		buf.rewind();
		//Fill with empty data
		while(buf.remaining()>0)
			{buf.put(DEADBEEF);}
		this.unmap(gl);
		}
	
	/**
	 * This gets around a quirk where AMD driver will segfault with certain-sized texture buffers.
	 * No explicit pattern found, however multiples of 1024B appear safe for now.
	 * @param sizeInBytes
	 * @return
	 * @since Dec 12, 2012
	 */
	private static int roundToNextKB(int sizeInBytes)
		{
		int kb = (sizeInBytes/1024)+1;//Round to lower KB, add 1KB
		return kb*1024;
		}
	
	@Override
	protected int getBindingTarget()
		{
		return GL2.GL_TEXTURE_BUFFER;
		}
	
	/**
	 * @return the textureID
	 */
	public final int getTextureID()
		{
		return textureID;
		}
	
	public final void bindToTextureUnit(GL3 gl, int textureUnit)
		{
		gl.glActiveTexture(GL2.GL_TEXTURE0+textureUnit);
		gl.glBindTexture(getBindingTarget(), getTextureID());
		}
	
	public final void bindToUniform(GL3 gl, int textureUnit, int uniformIndex)
		{
		if(uniformIndex==-1)throw new RuntimeException("UnformIndex is -1, which is invalid.");
		bindToTextureUnit(gl,textureUnit);
		System.out.println("Binding texture unit "+textureUnit+" to uniform index "+uniformIndex);
		gl.glUniform1i(uniformIndex, textureUnit);
		}

	}//end GLTextureBuffer(...)
