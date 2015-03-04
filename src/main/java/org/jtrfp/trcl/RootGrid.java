/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

public class RootGrid extends RenderableSpacePartitioningGrid {

    RootGrid(double sizeX, double sizeY, double sizeZ,
	    double gridBlockSize, double viewDepth) {
	super(sizeX, sizeY, sizeZ, gridBlockSize, viewDepth);
    }//end constructor(...)
    
    @Override
    public void notifyBranchAdded(SpacePartitioningGrid b){
    }//end notifyBranchAdded(...)
    
    @Override
    public void notifyBranchRemoved(SpacePartitioningGrid b){
    }//end notifyBranchRemoved(...)

}//end RootGrid
