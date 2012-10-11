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

public interface AltitudeMap
	{
	/**
	 * Returns a height between 0 and 1
	 * @param x
	 * @param z
	 * @return
	 * @since Oct 3, 2012
	 */
	public double heightAt(double x,double z);
	public double getWidth();
	public double getHeight();
	}
