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

public class CustomNAVTargetableBehavior extends Behavior implements NAVTargetableBehavior {
    private boolean notYetTargeted=true;
    private final Runnable r;
    public CustomNAVTargetableBehavior(Runnable r){
	this.r=r;
    }
    @Override
    public void notifyBecomingCurrentTarget() {
	if(notYetTargeted)
	    r.run();
	notYetTargeted=false;
    }
}//end CustomNAVTargetableBehavior
