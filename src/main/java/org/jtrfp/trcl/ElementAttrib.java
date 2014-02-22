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
		if(type==Short.class)setter=shortSetter;
		else if(type==Integer.class)setter=intSetter;
		else if(type==Float.class)setter=floatSetter;
		else if(type==Byte.class)setter=byteSetter;
		else throw new RuntimeException("Invalid class: "+type.getName());
		}
	@Override
	public void set(double value)
		{
		setter.set(value, byteOffset+arrayOffset.get());
		}
	
	private static final ShortSetter shortSetter = new ShortSetter();
	private static final IntSetter intSetter = new IntSetter();
	private static final FloatSetter floatSetter = new FloatSetter();
	private static final ByteSetter byteSetter = new ByteSetter();
	
	private static interface Setter
		{
		public abstract void set(double val, int byteOffset);
		}
	private static final class ByteSetter implements Setter
		{
		@Override
		public void set(double val, int byteOffset)
			{
			//final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
		    	GlobalDynamicTextureBuffer.put(byteOffset,(byte)val);
			//bb.position(byteOffset);
			//bb.put((byte)val);
			}
		}//end ShortSetter
	private static final class ShortSetter implements Setter
		{
		@Override
		public void set(double val, int byteOffset){
			//final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			GlobalDynamicTextureBuffer.putShort(byteOffset,(short)val);
			//bb.position(byteOffset+arrayOffset.get());
			//bb.putShort((short)val);
			}
		}//end ShortSetter
	private static final class FloatSetter implements Setter
		{
		@Override
		public void set(double val, int byteOffset)
			{
			//final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
			GlobalDynamicTextureBuffer.putFloat(byteOffset,(float)val);
			//bb.position(byteOffset);
			//bb.putFloat((float)val);
			}
		}//end ShortSetter
	private static final class IntSetter implements Setter
		{
		@Override
		public void set(double val, int byteOffset)
			{
			//final ByteBuffer bb=GlobalDynamicTextureBuffer.getByteBuffer();
		    	GlobalDynamicTextureBuffer.putInt(byteOffset,(int)val);
			//bb.position(byteOffset);
			//bb.putInt((int)val);
			}
		}//end ShortSetter
	}//end ElementAttrib
