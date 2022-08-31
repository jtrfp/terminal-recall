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

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;

public class ChangesBehaviorWhenTargeted extends Behavior implements
	NAVTargetableBehavior {
    private final boolean enabled;
    private final Class<? extends Behavior>[]behaviorsToChange;
    
    @SafeVarargs
    public ChangesBehaviorWhenTargeted(final boolean enabled, final Class<? extends Behavior> ... behaviorsToChange){
	super();
	this.enabled=enabled;
	this.behaviorsToChange=behaviorsToChange;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void notifyBecomingCurrentTarget() {
	for(Class<? extends Behavior> c:behaviorsToChange){
	    getParent().probeForBehaviors(bcSubmitter, (Class<Behavior>)c);
	}//end for(behaviors)
    }//end notifyBecomingCurrentTarget()
    
    private final Submitter<Behavior> bcSubmitter = new AbstractSubmitter<Behavior>(){
	@Override
	public void submit(Behavior item) {
	    item.setEnable(enabled);
	}
    };//end bcSubmitter
}//end ChangesBehaviorWhenTargeted
