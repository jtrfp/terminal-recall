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
package com.ritolaaudio.trcl.file;

import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.ThirdPartyParseable;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class AbstractVector implements ThirdPartyParseable
	{
	int x,y,z;

	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.stringEndingWith(",", Parser.property("x", Integer.class), false);
		Parser.stringEndingWith(",", Parser.property("y", Integer.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("z", Integer.class), false);
		}
	
	public static class EndingWithComma extends AbstractVector
		{
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.stringEndingWith(",", Parser.property("x", Integer.class), false);
			Parser.stringEndingWith(",", Parser.property("y", Integer.class), false);
			Parser.stringEndingWith(",", Parser.property("z", Integer.class), false);
			}
		}//end EndingWithComma
	
	/**
	 * @return the x
	 */
	public int getX()
		{
		return x;
		}

	/**
	 * @param x the x to set
	 */
	public void setX(int x)
		{
		this.x = x;
		}

	/**
	 * @return the y
	 */
	public int getY()
		{
		return y;
		}

	/**
	 * @param y the y to set
	 */
	public void setY(int y)
		{
		this.y = y;
		}

	/**
	 * @return the z
	 */
	public int getZ()
		{
		return z;
		}

	/**
	 * @param z the z to set
	 */
	public void setZ(int z)
		{
		this.z = z;
		}
	}//end DirectionVector
