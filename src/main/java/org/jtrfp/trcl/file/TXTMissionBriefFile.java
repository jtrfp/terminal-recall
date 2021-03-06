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

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class TXTMissionBriefFile implements ThirdPartyParseable {
    String planetModelFile;
    String planetTextureFile;
    String missionText;

    @Override
    public void describeFormat(Parser p) throws UnrecognizedFormatException {
	p.stringEndingWith(TRParsers.LINE_DELIMITERS, p.property("planetModelFile", String.class),
		false);
	p.stringEndingWith(TRParsers.LINE_DELIMITERS,
		p.property("planetTextureFile", String.class), false);
	p.stringEndingWith((String)null, p.property("missionText", String.class), false);
    }

    /**
     * @return the planetModelFile
     */
    public String getPlanetModelFile() {
	return planetModelFile;
    }

    /**
     * @param planetModelFile
     *            the planetModelFile to set
     */
    public void setPlanetModelFile(String planetModelFile) {
	this.planetModelFile = planetModelFile;
    }

    /**
     * @return the planetTextureFile
     */
    public String getPlanetTextureFile() {
	return planetTextureFile;
    }

    /**
     * @param planetTextureFile
     *            the planetTextureFile to set
     */
    public void setPlanetTextureFile(String planetTextureFile) {
	this.planetTextureFile = planetTextureFile;
    }

    /**
     * @return the missionText
     */
    public String getMissionText() {
	return missionText;
    }

    /**
     * @param missionText
     *            the missionText to set
     */
    public void setMissionText(String missionText) {
	this.missionText = missionText;
    }

}// end MissionBriefFile
