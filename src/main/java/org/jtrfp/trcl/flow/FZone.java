package org.jtrfp.trcl.flow;

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

import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;

public class FZone {
    public static VOXFile getDefaultMission() {
	VOXFile result = new VOXFile();
	result.setMissionName("Fury Special Edition");
	result.setNumEntries(9);
	result.setLevels(new MissionLevel[] { 
		missionLevel(1, 1, "BELAZURE.LVL"),
		missionLevel(1, 2, "BELAZUR2.LVL"),
		missionLevel(1, 3, "BELAZUR3.LVL"),

		missionLevel(2, 1, "FUTRO.LVL"),
		missionLevel(2, 2, "FUTRO2.LVL"),
		missionLevel(2, 3, "FUTRO3.LVL"),

		missionLevel(3, 1, "BIONSHIP.LVL"), 
		missionLevel(3, 2, "BIONSHP2.LVL"),
		missionLevel(3, 3, "BIONSHP3.LVL")});
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
}//end FZone