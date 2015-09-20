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
package org.jtrfp.trcl.gpu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Provides hint that all methods invoked by this method are from within a GL thread.
 * @author Chuck Ritola
 *
 */
@Target({ElementType.METHOD})
public @interface ProvidesGLThread {

}
