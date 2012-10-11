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

import jtrfp.common.raw.IRawData;

/**
 * Wraps a Terminal Reality / mtmX RAW data file as an abstract altitude map.
 * @author Chuck Ritola
 *
 */
public class RawAltitudeMapWrapper implements AltitudeMap
	{
	double [][] heights;
	int width,height;
	public RawAltitudeMapWrapper(IRawData dat)
		{//do not hold the IRawData object. That way the garbage collector can clear it out.
		heights = new double[dat.getWidth()][dat.getHeight()];
		width=dat.getWidth(); height=dat.getHeight();
		for(int z=0; z<height; z++)
			{
			for(int x=0; x<width; x++)
				{
				heights[x][z] = (double)dat.getValueAt(z, x)/256.;
				}//end for(x)
			}//end for(z)
		}//end RawAltitudeMapWrapper
	@Override
	public double heightAt(double x, double z)
		{
		return heights[(int)x][(int)z];
		}
	@Override
	public double getWidth()
		{
		return width;
		}
	@Override
	public double getHeight()
		{
		return height;
		}
	}//end RawAltitudeMapWrapper
