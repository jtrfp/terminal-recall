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

public class TXTMissionBriefFile implements ThirdPartyParseable
	{
	String planetModelFile;
	String planetTextureFile;
	String missionText;
	
	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.stringEndingWith("\r\n", Parser.property("planetModelFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("planetTextureFile", String.class), false);
		Parser.stringEndingWith(null, Parser.property("missionText", String.class), false);// null means go until EOF
		}

	/**
	 * @return the planetModelFile
	 */
	public String getPlanetModelFile()
		{
		return planetModelFile;
		}

	/**
	 * @param planetModelFile the planetModelFile to set
	 */
	public void setPlanetModelFile(String planetModelFile)
		{
		this.planetModelFile = planetModelFile;
		}

	/**
	 * @return the planetTextureFile
	 */
	public String getPlanetTextureFile()
		{
		return planetTextureFile;
		}

	/**
	 * @param planetTextureFile the planetTextureFile to set
	 */
	public void setPlanetTextureFile(String planetTextureFile)
		{
		this.planetTextureFile = planetTextureFile;
		}

	/**
	 * @return the missionText
	 */
	public String getMissionText()
		{
		return missionText;
		}

	/**
	 * @param missionText the missionText to set
	 */
	public void setMissionText(String missionText)
		{
		this.missionText = missionText;
		}

	}//end MissionBriefFile
