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
package com.ritolaaudio.trcl.file;

import com.ritolaaudio.jfdt11.Parser;
import com.ritolaaudio.jfdt11.ThirdPartyParseable;
import com.ritolaaudio.jfdt11.UnrecognizedFormatException;

public class PUPFile implements ThirdPartyParseable
	{
	int numPowerups;
	PowerupLocation [] powerupLocations;
	
	public static class PowerupLocation implements ThirdPartyParseable
		{
		int x,y,z;
		Powerup type;
		
		@Override
		public void describeFormat(Parser p) throws UnrecognizedFormatException
			{
			p.stringEndingWith(",",p.property( "x", int.class), false);
			p.stringEndingWith(",", p.property("y", int.class), false);
			p.stringEndingWith(",", p.property("z", int.class), false);
			p.stringEndingWith("\r\n", p.property("type", Powerup.class), false);
			}//end describeFormat()
		
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

		/**
		 * @return the type
		 */
		public Powerup getType()
			{
			return type;
			}

		/**
		 * @param type the type to set
		 */
		public void setType(Powerup type)
			{
			this.type = type;
			}

		}//end PowerupLocation
	/**
	 * @return the powerupLocations
	 */
	public PowerupLocation[] getPowerupLocations()
		{
		return powerupLocations;
		}
	/**
	 * @param powerupLocations the powerupLocations to set
	 */
	public void setPowerupLocations(PowerupLocation[] powerupLocations)
		{
		this.powerupLocations = powerupLocations;
		}
	@Override
	public void describeFormat(Parser p) throws UnrecognizedFormatException
		{
		p.stringEndingWith("\r\n", p.property("numPowerups", int.class), false);
		p.arrayOf(getNumPowerups(), "powerupLocations", PowerupLocation.class);
		}
	/**
	 * @return the numPowerups
	 */
	public int getNumPowerups()
		{
		return numPowerups;
		}
	/**
	 * @param numPowerups the numPowerups to set
	 */
	public void setNumPowerups(int numPowerups)
		{
		this.numPowerups = numPowerups;
		}
	}//end PUPFile
