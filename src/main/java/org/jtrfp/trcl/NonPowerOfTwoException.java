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
package org.jtrfp.trcl;

public class NonPowerOfTwoException extends Exception {
    private static final long serialVersionUID = -3279647254036178260L;

    public NonPowerOfTwoException(String name) {
	super("Entity of name `" + name
		+ " has a non-power of two side length.");
    }
}// end NonPowerOfTwoException
