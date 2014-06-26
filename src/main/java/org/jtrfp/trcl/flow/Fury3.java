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
package org.jtrfp.trcl.flow;

import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;

public class Fury3 {
    public static VOXFile getDefaultMission() {
	VOXFile result = new VOXFile();
	result.setMissionName("Microsoft Fury3");
	result.setNumEntries(24);
	result.setLevels(new MissionLevel[] { missionLevel(1, 1, "TERRAN.LVL"),
		missionLevel(1, 2, "TERRAN2.LVL"),
		missionLevel(1, 3, "TERRAN3.LVL"),

		missionLevel(2, 1, "ATMOS.LVL"),
		missionLevel(2, 2, "ATMOS2.LVL"),
		missionLevel(2, 3, "ATMOS3.LVL"),

		missionLevel(3, 1, "RED.LVL"), missionLevel(3, 2, "RED2.LVL"),
		missionLevel(3, 3, "RED3.LVL"),

		missionLevel(4, 1, "CITY.LVL"),
		missionLevel(4, 2, "CITY2.LVL"),
		missionLevel(4, 3, "CITY3.LVL"),

		missionLevel(5, 1, "EGYPT.LVL"),
		missionLevel(5, 2, "EGYPT2.LVL"),
		missionLevel(5, 3, "EGYPT3.LVL"),

		missionLevel(6, 1, "AMINE.LVL"),
		missionLevel(6, 2, "AMINE2.LVL"),
		missionLevel(6, 3, "AMINE3.LVL"),

		missionLevel(7, 1, "WATER.LVL"),
		missionLevel(7, 2, "WATER2.LVL"),
		missionLevel(7, 3, "WATER3.LVL"),

		missionLevel(8, 1, "BORG.LVL"),
		missionLevel(8, 2, "BORG2.LVL"),
		missionLevel(8, 3, "BORG3.LVL") });
	return result;
    }

    private static MissionLevel missionLevel(int planetNumber, int stageNumber,
	    String rootLVL) {
	MissionLevel result = new MissionLevel();
	result.setPlanetNumber(planetNumber);
	result.setStageNumber(stageNumber);
	result.setLvlFile(rootLVL);
	return result;
    }
}// end Fury3
