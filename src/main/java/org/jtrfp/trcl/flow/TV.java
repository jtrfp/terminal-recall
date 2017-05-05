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
	result.setLevels(new MissionLevel[] { 
		//Ymir
		missionLevel(1, 1, "ARTIC.LVL"),
		missionLevel(1, 2, "ARTIC3.LVL"),
		missionLevel(1, 3, "ARTIC6.LVL"),

		//Crythania
		missionLevel(2, 1, "CANYON.LVL"),
		missionLevel(2, 2, "CANYON3.LVL"),
		missionLevel(2, 3, "CANYON6.LVL"),

		//Moon Dagger
		missionLevel(3, 1, "ALIEN.LVL"), 
		missionLevel(3, 2, "ALIEN3.LVL"),
		missionLevel(3, 3, "ALIEN6.LVL"),

		//Tei Tenga
		missionLevel(4, 1, "DESERT.LVL"),
		missionLevel(4, 2, "DESERT3.LVL"),
		missionLevel(4, 3, "DESERT6.LVL"),

		//Ositsho
		missionLevel(5, 1, "LAVA.LVL"),
		missionLevel(5, 2, "LAVA3.LVL"),
		missionLevel(5, 3, "LAVA6.LVL"),

		//Erigone
		missionLevel(6, 1, "MINE.LVL"),
		missionLevel(6, 2, "MINE3.LVL"),
		missionLevel(6, 3, "MINE6.LVL"),

		//Centauri III
		missionLevel(7, 1, "BUGHUNT.LVL"),
		missionLevel(7, 2, "BUGHUNT3.LVL"),
		missionLevel(7, 3, "BUGHUNT6.LVL"),

		//Ceres Asteroid
		missionLevel(8, 1, "ASTROID.LVL"),
		missionLevel(8, 2, "ASTROID3.LVL"),
		missionLevel(8, 3, "ASTROID6.LVL"),
		
		//Proxmia Seven
		missionLevel(9, 1, "CORE.LVL"),
		missionLevel(9, 2, "MCORE.LVL"),
		
		//Unknown Body
		missionLevel(10, 1, "GEIGER.LVL"),
		
		//Multiplayer
		missionLevel(11, 1, "MULTI1.LVL"),
		missionLevel(11, 2, "MULTI2.LVL"),
		missionLevel(11, 3, "MULTI3.LVL"),
		missionLevel(11, 4, "MULTI4.LVL"),
		missionLevel(11, 5, "MULTI5.LVL"),
		missionLevel(11, 6, "MULTI6.LVL")});
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
