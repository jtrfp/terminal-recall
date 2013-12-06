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

import java.awt.Color;

public class LineSegment
	{
	private final double [] x = new double[2];
	private final double [] y = new double[2];
	private final double [] z = new double[2];
	//Bad values given to make unset-variable bugs obvious.
	private Color color=null;
	private double thickness = Double.POSITIVE_INFINITY;
	/**
	 * @return the color
	 */
	public Color getColor()
		{
		return color;
		}
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
		{
		this.color = color;
		}
	/**
	 * @return the thickness
	 */
	public double getThickness()
		{
		return thickness;
		}
	/**
	 * @param thickness the thickness to set
	 */
	public void setThickness(double thickness)
		{
		this.thickness = thickness;
		}
	/**
	 * @return the x
	 */
	public double[] getX()
		{
		return x;
		}
	/**
	 * @return the y
	 */
	public double[] getY()
		{
		return y;
		}
	/**
	 * @return the z
	 */
	public double[] getZ()
		{
		return z;
		}
	
	}//end LineSegment
