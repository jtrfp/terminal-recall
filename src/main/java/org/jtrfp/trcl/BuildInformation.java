/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

public interface BuildInformation {
    /**
     * Returns the SCM unique ID of this build. This may be a git checkout SHA, or something completely different.
     * @return SCM Unique ID String as defined by the backing implementation, or null if none available.
     * @since Nov 16, 2017
     */
 public String getUniqueBuildId();
 /**
  * Returns a String describing the branch of this build, if applicable.
  * @return Branch description String, or null if none available.
  * @since Nov 16, 2017
  */
 public String getBranch();
}//end BuildInformation
