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

import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;

public class RemovesNAVObjectiveOnDeath extends Behavior implements
	DeathListener {
    private final NAVObjective objective;
    private final Mission m;
    public RemovesNAVObjectiveOnDeath(NAVObjective objective, Mission m) {
	this.objective=objective;
	this.m=m;
    }

    @Override
    public void notifyDeath() {
	m.removeNAVObjective(objective);
    }

}//ebd RemovesNAVObjectiveOnDeath
