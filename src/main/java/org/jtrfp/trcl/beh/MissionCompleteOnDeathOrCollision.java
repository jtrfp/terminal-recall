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

import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class MissionCompleteOnDeathOrCollision extends Behavior implements DeathListener, CollisionBehavior {

    @Override
    public void notifyDeath() {
	getParent().getTr().getGame().missionComplete();
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof Player){getParent().getTr().getGame().missionComplete();}
    }
}
