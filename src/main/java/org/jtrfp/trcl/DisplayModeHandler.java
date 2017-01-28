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

import java.util.HashSet;
import java.util.Set;

public class DisplayModeHandler {
    private Set<RenderableSpacePartitioningGrid> currentMode 
    	= new HashSet<RenderableSpacePartitioningGrid>();
    private Set<RenderableSpacePartitioningGrid> newMode
    	= new HashSet<RenderableSpacePartitioningGrid>();
    
    private final SpacePartitioningGrid defaultGrid;
    
    public DisplayModeHandler(SpacePartitioningGrid defaultGrid){
	this.defaultGrid=defaultGrid;
    }
    
    public synchronized void setDisplayMode(Object [] items){
	if(items == null)
	    throw new NullPointerException("Display mode array intolerably null.");
	newMode.clear();
	recursiveNewDisplayModeImpl(items);
	System.out.println("DisplayModeHandler.setDisplayMode()");
	for(Object o:items)
	    System.out.print(" "+o.getClass().getName());
	System.out.println();
	try{World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		for(RenderableSpacePartitioningGrid grid:currentMode){
		    if(!newMode.contains(grid))
			defaultGrid.removeBranch(grid);
		}//end for(grids)
		for(RenderableSpacePartitioningGrid grid:newMode){
		    if(!currentMode.contains(grid))
			defaultGrid.addBranch(grid);
		}//end for(grids)
	    }}).get();}catch(Exception e){e.printStackTrace();}
	currentMode.clear();
	currentMode.addAll(newMode);
    }//end addDisplayMode()
    
    private void recursiveNewDisplayModeImpl(Object [] items){
	if(items==null)return;//Empty.
	for(Object o: items){
	    final Class oClass = o.getClass();
	    if(oClass.isArray()){
		//Recurse
		recursiveNewDisplayModeImpl((Object[])o);
	    }else if(o instanceof RenderableSpacePartitioningGrid){
		final RenderableSpacePartitioningGrid grid = (RenderableSpacePartitioningGrid)o;
		newMode.add(grid);
	    }//end if(RenderableSpacePartitioningGrid)
	}//end for(items)
    }//end setDisplayModeImpl(...)
}//end DisplayModeHandler
