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
package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.WorldObject;

public class DeathBehavior extends Behavior {
    private boolean dead=false;
    public void die(){
	if(dead)return;
	dead=true;//Only die once until reset
	WorldObject wo = getParent();
	wo.getBehavior().probeForBehaviors(sub,DeathListener.class);
	wo.destroy();
    }
    private final Submitter<DeathListener> sub = new Submitter<DeathListener>(){

	@Override
	public void submit(DeathListener item) {
	    item.notifyDeath();
	    
	}

	@Override
	public void submit(Collection<DeathListener> items) {
	   for(DeathListener l:items){submit(l);}
	    
	}
    };//end submitter

    public void reset(){
	 dead=false;
	}
}
