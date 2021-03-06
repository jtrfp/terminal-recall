/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2017 Chuck Ritola
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

import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;


public interface PODRegistry {
 public CollectionActionDispatcher<String> getPodCollection();
 public IPodData getPodData(String path);
}//end PODRegistry
