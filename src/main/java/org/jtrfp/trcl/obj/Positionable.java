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
package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.coll.PropertyListenable;

public interface Positionable extends PropertyListenable {
    //BEAN PROPERTIES
    public static final String POSITION    = "position";
    
    public double[] getPosition();

    public void setContainingGrid(SpacePartitioningGrid<? extends Positionable> grid);
    public SpacePartitioningGrid<? extends Positionable> getContainingGrid();
}//end Positionable
