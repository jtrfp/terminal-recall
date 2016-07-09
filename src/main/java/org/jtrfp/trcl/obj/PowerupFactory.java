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

import java.lang.ref.WeakReference;

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.Powerup;

public class PowerupFactory{
    private final int POOL_SIZE = 20;
    private final WeakReference<RenderableSpacePartitioningGrid> parentGrid;
    private final PowerupObject [] objects = new PowerupObject[POOL_SIZE];
    private int powerupIndex=0;
    private final TR tr;
    public PowerupFactory(TR tr, Powerup type, RenderableSpacePartitioningGrid pGrid){
	this.tr=tr;
	parentGrid=new WeakReference<RenderableSpacePartitioningGrid>(pGrid);
	for(int i=0; i<objects.length;i++){
	    (objects[i]=new PowerupObject(type)).setVisible(false);
	}//end for(objects)
    }//end constructor
    public PowerupObject spawn(double[] ds) {
	final PowerupObject result = objects[powerupIndex];
	result.destroy();
	result.reset(ds);
	final RenderableSpacePartitioningGrid 
		grid = parentGrid.get();
	if(grid==null)return result;
	grid.add(result);
	powerupIndex++;
	powerupIndex%=objects.length;
	return result;
    }//end fire(...)
}//end PowerupFactory
