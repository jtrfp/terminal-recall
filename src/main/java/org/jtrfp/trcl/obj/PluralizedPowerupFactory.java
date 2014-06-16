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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Powerup;

public class PluralizedPowerupFactory {
    private final PowerupFactory [] factories = new PowerupFactory[Powerup.values().length];
    public PluralizedPowerupFactory(TR tr){
	for(Powerup p:Powerup.values()){
	    factories[p.ordinal()]=new PowerupFactory(tr, p);
	}//end for(Powerups)
    }//end constructor
    
    public PowerupObject spawn(double[] ds, Powerup type) {
	return factories[type.ordinal()].spawn(ds);
    }//end spawn(...)
}//end PluralizedPowerupFactory
