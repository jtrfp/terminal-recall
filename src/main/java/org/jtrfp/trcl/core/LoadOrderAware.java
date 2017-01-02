/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.core;

public interface LoadOrderAware {
    public static final int FIRST   = 0;
    public static final int LAST    = 65535;
    public static final int DEFAULT = (FIRST + LAST) / 2;
    public int getFeatureLoadPriority();
}//end LoadOrderAware
