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

/**
 * Thrown when a Feature is cannot be applied to a specific target instance upon
 * further checking. Used when the state of a target is relevant to the applicability
 * of this Feature, such as a CanvasProvider which can only provide GL ES 2 when a
 * GLExecutor<GL3> needs a GL3 capable provider.
 */

package org.jtrfp.trcl.core;

public class FeatureNotApplicableException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -4444133268671396201L;
    public FeatureNotApplicableException()               {super();}
    public FeatureNotApplicableException(String msg)     {super(msg);}
    public FeatureNotApplicableException(Throwable cause){super(cause);}
}//end FeatureNotApplicableException
