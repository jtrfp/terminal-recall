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
package org.jtrfp.trcl.ext;

public interface Extension<EXTENDED> {
    /**
     * Expected to be invoked once and only once. Multiple invocations results in
     * undefined behavior. This is to be invoked before being added to the instance's extension registry.
     * @param extended Instance to which to apply this extension.
     * @since Aug 6, 2015
     */
 public void init(EXTENDED extended);
 /**
  * Applies this extension to the extended instance. This is to be invoked once, or once after removed.
  * @param extended
  * @since Aug 7, 2015
  */
 public void apply(EXTENDED extended);
 /**
  * Removes this extension. This is to be invoked only once after an apply() invocation.
  * @param extended
  * @since Aug 7, 2015
  */
 public void remove(EXTENDED extended);
 /**
  * May be invoked before init.
  * @return The class of which this Extension intends to extend.
  * @since Aug 6, 2015
  */
 public Class<EXTENDED> getExtendedClass();
 /**
  * This may be called before init().
  * @return Human-readable name of the Extension. No more than 64 chars
  * @since Aug 6, 2015
  */
 public String getHumanReadableName();
 /**
  * This may be called before init().
  * @return A short explanation of the Extension
  * @since Aug 6, 2015
  */
 public String getDescription();
}//end Extension
