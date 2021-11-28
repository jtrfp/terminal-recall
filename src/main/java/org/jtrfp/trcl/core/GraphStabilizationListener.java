/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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

/**
 * If a Feature probes for other Features without knowing exactly what it is looking 
 * for, such as getAllFeaturesOf(...), it may not get all available Features if called early.
 * GraphStabilizationListeners are notified when the Feature graph in which they are
 * present becomes stabilized, i.e. when all Features are present in the tree.<br><br>
 * Calling Features.get() only assures that the subfeatures of the specified feature are
 * present in the graph, but not its peer features.
 * @author Chuck Ritola
 *
 */
public interface GraphStabilizationListener {
 public void graphStabilized(Object target);
}//end GraphStabilizationListener
