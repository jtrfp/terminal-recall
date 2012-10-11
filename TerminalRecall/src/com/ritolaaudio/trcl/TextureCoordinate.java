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

public class TextureCoordinate extends GLElement
	{
	double u,v;
	
	public TextureCoordinate(){}
	
	public TextureCoordinate(double u, double v)
		{
		this.u=u;
		this.v=v;
		}
	
	@Override
	public void writeToBuffer(ByteBuffer buf)
		{
		buf.putDouble(u);
		buf.putDouble(v);
		}

	@Override
	public void buildFrom(ByteBuffer buf)
		{
		u=buf.getDouble();
		v=buf.getDouble();
		}

	/**
	 * @return the u
	 */
	public double getU()
		{
		return u;
		}

	/**
	 * @param u the u to set
	 */
	public void setU(double u)
		{
		this.u = u;
		}

	/**
	 * @return the v
	 */
	public double getV()
		{
		return v;
		}

	/**
	 * @param v the v to set
	 */
	public void setV(double v)
		{
		this.v = v;
		}

	@Override
	public int getElementSizeInBytes()
		{
		return 8*2;
		}
	
	@Override
	public String toString()
		{
		return "TextureUV { u="+u+" v="+v+" }";
		}
	}//end TextureCoordinate
