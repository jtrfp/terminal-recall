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

public class Vertex extends GLElement
	{
	double x,y,z;
	
	public Vertex(){}
	
	public Vertex(double x, double y, double z)
		{
		this.x=x;
		this.y=y;
		this.z=z;
		}
	
	@Override
	public void writeToBuffer(ByteBuffer buf)
		{
		buf.putDouble(x);
		buf.putDouble(y);
		buf.putDouble(z);
		}
	/**
	 * @return the x
	 */
	public double getX()
		{
		return x;
		}
	/**
	 * @param x the x to set
	 */
	public void setX(double x)
		{
		this.x = x;
		}
	/**
	 * @return the y
	 */
	public double getY()
		{
		return y;
		}
	/**
	 * @param y the y to set
	 */
	public void setY(double y)
		{
		this.y = y;
		}
	/**
	 * @return the z
	 */
	public double getZ()
		{
		return z;
		}
	/**
	 * @param z the z to set
	 */
	public void setZ(double z)
		{
		this.z = z;
		}

	@Override
	public void buildFrom(ByteBuffer buf)
		{
		x=buf.getDouble();
		y=buf.getDouble();
		z=buf.getDouble();
		}

	@Override
	public int getElementSizeInBytes()
		{
		return 8*3;
		}
	
	@Override
	public String toString()
		{
		return "Vertex { x="+x+" y="+y+" z="+z+" }";
		}
	}//end Vertex
