/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;

/**
 * A mappable GL buffer.
 * http://www.jquantlib.org/index.php/Using_TypeTokens_to_retrieve_generic_parameters
 * @author Chuck Ritola
 *
 * @param <ELEMENT_TYPE>
 */
public abstract class GLBuffer<ELEMENT_TYPE extends GLElement>
	{
	private int bufferID;
	protected ByteBuffer localBuffer;
	boolean mapped=false;
	
	public GLBuffer(int sizeInElements)
		{
		GL2 gl = TR.canvas.getGL().getGL2();
		//localBuffer = ByteBuffer.allocateDirect(sizeInElements*elementSizeInBytes());
		//localBuffer.order(ByteOrder.LITTLE_ENDIAN);
		IntBuffer iBuf = IntBuffer.allocate(1);
		gl.glGenBuffers(1, iBuf);
		
		bufferID=iBuf.get();
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferID);
		//if(getElementClass()==Index.class)
		//	{gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, sizeInElements*elementSizeInBytes(), null, GL2.GL_DYNAMIC_READ);}
		/*else {*/gl.glBufferData(GL2.GL_ARRAY_BUFFER, sizeInElements*elementSizeInBytes(), null, GL2.GL_DYNAMIC_READ);//}
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		//map(gl);
		}
	
	public void rewind()
		{
		localBuffer.rewind();
		}
	
	public void map(GL2 gl)
		{
		if(mapped)return;
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,bufferID);
		localBuffer = gl.glMapBuffer(GL2.GL_ARRAY_BUFFER, GL2.GL_READ_WRITE);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		mapped=true;
		}
	
	public void unmap(GL2 gl)
		{
		if(!mapped)return;
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferID);
		gl.glUnmapBuffer(GL2.GL_ARRAY_BUFFER);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		mapped=false;
		}
	
	public void free(GL2 gl)
		{
		unmap(gl);
		gl.glDeleteBuffers(1, IntBuffer.wrap(new int [] {bufferID}));
		localBuffer=null;
		}
	
	public void put(ELEMENT_TYPE elm)
		{
		elm.writeToBuffer(localBuffer);
		}
	
	public void position(int pos)
		{
		localBuffer.position(pos);
		}
	
	public int position()
		{
		return localBuffer.position();
		}
	
	public ELEMENT_TYPE get()
		{
		try
			{
			ELEMENT_TYPE result = getElementClass().newInstance();
			result.buildFrom(localBuffer);
			return result;
			}
		catch(IllegalAccessException e){e.printStackTrace();}
		catch(InstantiationException e){e.printStackTrace();}
		return null;
		}
	
	public void set(int elementIndex, ELEMENT_TYPE elm)
		{
		position(elementIndex*elementSizeInBytes());
		put(elm);
		}
	
	public ELEMENT_TYPE get(int elementIndex)
		{
		int oldPos = localBuffer.position();
		localBuffer.rewind();
		localBuffer.position(elementIndex*elementSizeInBytes());
		ELEMENT_TYPE result = get();
		localBuffer.position(oldPos);
		return result;
		}
	
	public int capacity()
		{
		return localBuffer.capacity()/elementSizeInBytes();
		}
	
	//protected abstract int elementSizeInBytes();
	public int elementSizeInBytes()
		{
		try
			{
			Class <ELEMENT_TYPE> ec = getElementClass();
			return ec.newInstance().getElementSizeInBytes();
			}
		catch(IllegalAccessException e){e.printStackTrace();}
		catch(InstantiationException e){e.printStackTrace();}
		System.exit(0);
		return -1;
		}
	
	public Class <ELEMENT_TYPE> getElementClass()
		{
		return (Class)((ParameterizedType)(getClass().getGenericSuperclass())).getActualTypeArguments()[0];
		}

	/**
	 * @return the bufferID
	 */
	public int getBufferID()
		{
		return bufferID;
		}

	/**
	 * @param bufferID the bufferID to set
	 */
	public void setBufferID(int bufferID)
		{
		this.bufferID = bufferID;
		}
	
	@Override
	public String toString()
		{
		GL2 gl = TR.canvas.getGL().getGL2();
		gl.getContext().makeCurrent();
		map(gl);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<capacity();i++)
			{
			sb.append(get(i).toString());
			sb.append("\n");
			}
		unmap(gl);
		gl.getContext().release();
		return sb.toString();
		}//end toString()
	}//end GLBuffer
