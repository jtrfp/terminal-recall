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

package org.jtrfp.trcl;

import java.util.ArrayList;

public class DisplayModeHandler {
    private ArrayList<RenderableSpacePartitioningGrid> currentMode 
    	= new ArrayList<RenderableSpacePartitioningGrid>();
    private ArrayList<RenderableSpacePartitioningGrid> newMode
    	= new ArrayList<RenderableSpacePartitioningGrid>();
    public void setDisplayMode(Object [] items){
	newMode.clear();
	recursiveNewDisplayModeImpl(items);
	try{World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		for(RenderableSpacePartitioningGrid grid:currentMode){
		    if(!newMode.contains(grid))
			grid.deactivate();
		}//end for(grids)
		for(RenderableSpacePartitioningGrid grid:newMode){
		    if(!currentMode.contains(grid))
			grid.activate();
		}//end for(grids)
	    }}).get();}catch(Exception e){e.printStackTrace();}
	currentMode.clear();
	currentMode.addAll(newMode);
    }//end addDisplayMode()
    
    private void recursiveNewDisplayModeImpl(Object [] items){
	if(items==null)return;//Empty.
	for(Object o: items){
	    if(o.getClass().isArray()){
		//Recurse
		recursiveNewDisplayModeImpl((Object[])o);
	    }else if(o instanceof RenderableSpacePartitioningGrid){
		final RenderableSpacePartitioningGrid grid = (RenderableSpacePartitioningGrid)o;
		newMode.add(grid);
	    }//end if(RenderableSpacePartitioningGrid)
	}//end for(items)
    }//end setDisplayModeImpl(...)
}//end DisplayModeHandler
