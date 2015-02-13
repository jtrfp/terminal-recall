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

import java.awt.Dimension;

public interface ModelingType {
    public static final int MAP_SQUARE_SIZE = (int) Math.pow(2, 20);

    public static final class BINModelingType implements ModelingType {
	private final String binFileName;

	public BINModelingType(String binFileName) {
	    this.binFileName = binFileName;
	}

	/**
	 * @return the binFileName
	 */
	public String getBinFileName() {
	    return binFileName;
	}
    }//end constructor

    public static final class FlatModelingType implements ModelingType {
	private final String f3RawFileName, tvRawFileName;
	private final Dimension segmentSize;

	public FlatModelingType(String f3RawFileName, String tvRawFileName, Dimension segmentSize) {
	    this.segmentSize = segmentSize;
	    this.f3RawFileName = f3RawFileName;
	    this.tvRawFileName = tvRawFileName;
	}

	/**
	 * @return the rawFileName
	 */
	public String getF3RawFileName() {
	    return f3RawFileName;
	}

	/**
	 * @return the segmentSize
	 */
	public Dimension getSegmentSize() {
	    return segmentSize;
	}

	/**
	 * @return the tvRawFileName
	 */
	public String getTvRawFileName() {
	    return tvRawFileName;
	}
    }//end FlatModelingType

    public static final class BillboardModelingType implements ModelingType {
	private final String[] rawFileNames;
	private final Dimension billboardSize;
	private final int timeInMillisPerFrame;

	public BillboardModelingType(String[] rawFileNames,
		int timeInMillisPerFrame, Dimension billboardSize) {
	    this.billboardSize = billboardSize;
	    this.timeInMillisPerFrame = timeInMillisPerFrame;
	    this.rawFileNames = rawFileNames;
	}

	public String[] getRawFileNames() {
	    return rawFileNames;
	}

	/**
	 * @return the billboardSize
	 */
	public Dimension getBillboardSize() {
	    return billboardSize;
	}

	/**
	 * @return the timeInMillisPerFrame
	 */
	public int getTimeInMillisPerFrame() {
	    return timeInMillisPerFrame;
	}
    }//end BillboardModelingType
}// end interface ModelingType
