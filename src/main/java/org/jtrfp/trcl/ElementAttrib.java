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

import java.nio.ByteBuffer;

import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class ElementAttrib<TYPE extends Number> implements Settable
	{
	private final int byteOffset;
	private final Setter setter;
	private final IndirectObject<Integer>arrayOffset;
	
	public static <T extends Number> ElementAttrib<T> create(IndirectObject<Integer> arrayOffset, int byteOffset, Class<T> type)
		{
		return new ElementAttrib<T>(arrayOffset,byteOffset,type);
		}
	
	private ElementAttrib(IndirectObject<Integer>arrayOffset, int byteOffset, Class<TYPE> type)
		{
		this.arrayOffset=arrayOffset;
		this.byteOffset=byteOffset;
		if(type==Short.class)setter=new ShortSetter();
		else if(type==Integer.class)setter=new IntSetter();
		else if(type==Float.class)setter=new FloatSetter();
		else if(type==Byte.class)setter=new ByteSetter();
		else throw new RuntimeException("Invalid class: "+type.getName());
		}
	@Override
	public void set(double value)
		{
		setter.set(value);
		}
	private abstract class Setter
		{
		public abstract void set(double val);
		}
	private class ByteSetter extends Setter
		{
		@Override
		public void set(double val)
			{
			final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			bb.position(byteOffset+arrayOffset.get());
			bb.put((byte)val);
			}
		}//end ShortSetter
	private class ShortSetter extends Setter
		{
		@Override
		public void set(double val)
			{
			final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			bb.position(byteOffset+arrayOffset.get());
			bb.putShort((short)val);
			}
		}//end ShortSetter
	private class FloatSetter extends Setter
		{
		@Override
		public void set(double val)
			{
			final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			bb.position(byteOffset+arrayOffset.get());
			bb.putFloat((float)val);
			}
		}//end ShortSetter
	private class IntSetter extends Setter
		{
		@Override
		public void set(double val)
			{
			final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			bb.position(byteOffset+arrayOffset.get());
			bb.putInt((int)val);
			}
		}//end ShortSetter
	}//end ElementAttrib
