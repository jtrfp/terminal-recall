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

public class Triangle
	{
	double [] x = new double[3];
	double [] y = new double[3];
	double [] z = new double[3];
	
	double [] u = new double[3];
	double [] v = new double[3];
	
	RenderMode renderMode;
	
	Texture texture;

	/**
	 * @return the x
	 */
	public double[] getX()
		{
		return x;
		}

	/**
	 * @param x the x to set
	 */
	public void setX(double[] x)
		{
		this.x = x;
		}

	/**
	 * @return the y
	 */
	public double[] getY()
		{
		return y;
		}

	/**
	 * @param y the y to set
	 */
	public void setY(double[] y)
		{
		this.y = y;
		}

	/**
	 * @return the z
	 */
	public double[] getZ()
		{
		return z;
		}

	/**
	 * @param z the z to set
	 */
	public void setZ(double[] z)
		{
		this.z = z;
		}

	/**
	 * @return the u
	 */
	public double[] getU()
		{
		return u;
		}

	/**
	 * @param u the u to set
	 */
	public void setU(double[] u)
		{
		this.u = u;
		}

	/**
	 * @return the v
	 */
	public double[] getV()
		{
		return v;
		}

	/**
	 * @param v the v to set
	 */
	public void setV(double[] v)
		{
		this.v = v;
		}

	/**
	 * @return the renderMode
	 */
	public RenderMode getRenderMode()
		{
		return renderMode;
		}

	/**
	 * @param renderMode the renderMode to set
	 */
	public void setRenderMode(RenderMode renderMode)
		{
		this.renderMode = renderMode;
		}

	/**
	 * @return the texture
	 */
	public Texture getTexture()
		{
		return texture;
		}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(Texture texture)
		{
		this.texture = texture;
		}
	}
