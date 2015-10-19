/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class PODINIFile extends SelfParsingFile {
    private Integer             numPODEntries;
    private ArrayList<PODEntry> podEntries;

    public PODINIFile(InputStream is) throws IllegalAccessException, IOException {
	super(is);
    }

    @Override
    public void describeFormat(Parser p) throws UnrecognizedFormatException {
	p.stringEndingWith("\r\n", p.property("numPODEntries", Integer.class),
		false);
	p.arrayOf(getNumPODEntries(), "podEntries", PODEntry.class);
    }
    
    public static class PODEntry implements ThirdPartyParseable{
	String podName;

	@Override
	public void describeFormat(Parser p)
		throws UnrecognizedFormatException {
	    p.stringEndingWith(TRParsers.LINE_DELIMITERS, p.property("podName", String.class), false);
	}

	/**
	 * @return the podName
	 */
	public String getPodName() {
	    return podName;
	}

	/**
	 * @param podName the podName to set
	 */
	public void setPodName(String podName) {
	    this.podName = podName;
	}
	
    }//end PODEntry

    /**
     * @return the numPODEntries
     */
    public int getNumPODEntries() {
        return numPODEntries;
    }

    /**
     * @param numPODEntries the numPODEntries to set
     */
    public void setNumPODEntries(int numPODEntries) {
        this.numPODEntries = numPODEntries;
    }

    /**
     * @return the podEntries
     */
    public ArrayList<PODEntry> getPodEntries() {
        return podEntries;
    }

    /**
     * @param podEntries the podEntries to set
     */
    public void setPodEntries(ArrayList<PODEntry> podEntries) {
        this.podEntries = podEntries;
    }
}// end MissionBriefFile
