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

import java.io.FileNotFoundException;

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.Powerup;

public class PowerupSystem extends RenderableSpacePartitioningGrid{
    private final PowerupFactory [] factories = new PowerupFactory[Powerup.values().length];
    public PowerupSystem(TR tr){
	super();
	for(Powerup p:Powerup.values()){
	    try {factories[p.ordinal()]=new PowerupFactory(p, this);}
	    catch(FileNotFoundException e) {
		factories[p.ordinal()] = null;//Probably won't be used.
	    }
	}//end for(Powerups)
    }//end constructor
    
    public PowerupObject spawn(double[] ds, Powerup type) {
	if(type == Powerup.Random){//Spawn a random powerup
	    final double numPowerups = Powerup.values().length;
	    return spawn(ds,Powerup.values()[(int)(Math.random()*numPowerups)]);}
	return factories[type.ordinal()].spawn(ds);
    }//end spawn(...)
}//end PluralizedPowerupFactory
