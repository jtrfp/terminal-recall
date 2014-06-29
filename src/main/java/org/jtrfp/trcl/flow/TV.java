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

public class TV {
    public static VOXFile getDefaultMission() {
	VOXFile result = new VOXFile();
	result.setMissionName("Terminal Velocity");
	result.setNumEntries(24);
	result.setLevels(new MissionLevel[] { missionLevel(1, 1, "ARTIC.LVL"),
		missionLevel(1, 2, "ARTIC3.LVL"),
		missionLevel(1, 3, "ARTIC6.LVL"),

		missionLevel(2, 1, "CANYON.LVL"),
		missionLevel(2, 2, "CANYON3.LVL"),
		missionLevel(2, 3, "CANYON6.LVL"),

		missionLevel(3, 1, "ALIEN.LVL"), 
		missionLevel(3, 2, "ALIEN3.LVL"),
		missionLevel(3, 3, "ALIEN6.LVL"),

		missionLevel(4, 1, "DESERT.LVL"),
		missionLevel(4, 2, "DESERT3.LVL"),
		missionLevel(4, 3, "DESERT6.LVL"),

		missionLevel(5, 1, "LAVA.LVL"),
		missionLevel(5, 2, "LAVA3.LVL"),
		missionLevel(5, 3, "LAVA6.LVL"),

		missionLevel(6, 1, "MINE.LVL"),
		missionLevel(6, 2, "MINE3.LVL"),
		missionLevel(6, 3, "MINE6.LVL"),

		missionLevel(7, 1, "BUGHUNT.LVL"),
		missionLevel(7, 2, "BUGHUNT3.LVL"),
		missionLevel(7, 3, "BUGHUNT6.LVL"),

		missionLevel(8, 1, "ASTROID.LVL"),
		missionLevel(8, 2, "ASTROID3.LVL"),
		missionLevel(8, 3, "ASTROID6.LVL") });
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
}// end TV
