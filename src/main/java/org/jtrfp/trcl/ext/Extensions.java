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

/**
 * This is a hard-coded list of built-in Extensions.
 */
package org.jtrfp.trcl.ext;

public class Extensions {
 @SuppressWarnings("unchecked")
public static final Class<? extends Extension<?>> [] builtInExtensions = new Class[]{
     GamePause.class
 };
}//end Extension
