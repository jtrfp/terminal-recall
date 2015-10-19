/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.file;

import org.jtrfp.jfdt.ClassInclusion;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class VOXFile implements ThirdPartyParseable {
    int numEntries;
    String missionName;
    MissionLevel[] levels;

    @Override
    public void describeFormat(Parser p) throws UnrecognizedFormatException {
	p.stringEndingWith(TRParsers.LINE_DELIMITERS, p.property("numEntries", int.class), false);
	p.stringEndingWith(TRParsers.LINE_DELIMITERS, p.property("missionName", String.class),
		false);
	for (int i = 0; i < getNumEntries(); i++) {
	    p.subParseProposedClasses(
		    p.indexedProperty("levels", MissionLevel.class, i),
		    ClassInclusion.classOf(MissionLevel.class));
	}
    }// end describeFormat()

    public static class MissionLevel implements ThirdPartyParseable {
	int planetNumber, stageNumber;
	String lvlFile;

	@Override
	public void describeFormat(Parser p) throws UnrecognizedFormatException {
	    p.stringCSVEndingWith(",", int.class, false, "planetNumber",
		    "stageNumber");
	    p.stringEndingWith(TRParsers.LINE_DELIMITERS, p.property("lvlFile", String.class),
		    false);
	}// end describeFormat()

	/**
	 * @return the planetNumber
	 */
	public int getPlanetNumber() {
	    return planetNumber;
	}

	/**
	 * Built-in Fury3 planet (level) order is 1 through 8. Addons typically
	 * start at 9 and up.
	 * 
	 * @param planetNumber
	 *            The planet number (zero not inclusive).
	 */
	public void setPlanetNumber(int planetNumber) {
	    this.planetNumber = planetNumber;
	}

	/**
	 * Built-in Fury3 planet (level) order is 1 through 8. Addons typically
	 * start at 9 and up.
	 * 
	 * @return the The sub-planet stage number (typical 1 to 3, zero not
	 *         inclusive)
	 */
	public int getStageNumber() {
	    return stageNumber;
	}

	/**
	 * @param stageNumber
	 *            The sub-planet stage number (typical 1 to 3, zero not
	 *            inclusive)
	 */
	public void setStageNumber(int stageNumber) {
	    this.stageNumber = stageNumber;
	}

	/**
	 * @return the lvlFile
	 */
	public String getLvlFile() {
	    return lvlFile;
	}

	/**
	 * @param lvlFile
	 *            the lvlFile to set
	 */
	public void setLvlFile(String lvlFile) {
	    this.lvlFile = lvlFile;
	}

    }// end MissionLevel

    /**
     * @return the numEntries
     */
    public int getNumEntries() {
	return numEntries;
    }

    /**
     * @param numEntries
     *            the numEntries to set
     */
    public void setNumEntries(int numEntries) {
	this.numEntries = numEntries;
    }

    /**
     * @return the missionName
     */
    public String getMissionName() {
	return missionName;
    }

    /**
     * @param missionName
     *            the missionName to set
     */
    public void setMissionName(String missionName) {
	this.missionName = missionName;
    }

    /**
     * @return the levels
     */
    public MissionLevel[] getLevels() {
	return levels;
    }

    /**
     * @param levels
     *            the levels to set
     */
    public void setLevels(MissionLevel[] levels) {
	this.levels = levels;
    }
}// end VOXFile
