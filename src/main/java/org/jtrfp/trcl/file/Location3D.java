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
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class Location3D extends AbstractVector {
    public static class EndingWithComma extends Location3D {
	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.stringEndingWith(",", prs.property("x", Integer.class), false);
	    prs.stringEndingWith(",", prs.property("y", Integer.class), false);
	    prs.stringEndingWith(",", prs.property("z", Integer.class), false);
	}
    }// end EndingWithComma
}// end Location3D
