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

import java.lang.ref.WeakReference;

import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;

public class RemovesNAVObjectiveOnDeath extends Behavior implements
	DeathListener {
    private final WeakReference<NAVObjective> objective;
    private final WeakReference<Mission> m;
    public RemovesNAVObjectiveOnDeath(NAVObjective objective, Mission m) {
	this.objective=new WeakReference<NAVObjective>(objective);
	this.m=new WeakReference<Mission>(m);
    }

    @Override
    public void notifyDeath() {
	m.get().removeNAVObjective(objective.get());
    }

}//ebd RemovesNAVObjectiveOnDeath
