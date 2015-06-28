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

import org.jtrfp.trcl.AltitudeMap;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class LeavesPowerupOnDeathBehavior extends Behavior implements
	DeathListener {
    private static final int OVER_TERRAIN_PAD=20000;
    private final Powerup pup;
    public LeavesPowerupOnDeathBehavior(Powerup p){
	this.pup=p;
    }
    @Override
    public void notifyDeath() {//Y-fudge to ensure powerup is not too close to ground.
	final WorldObject p=getParent();
	final double [] thisPos=p.probeForBehavior(DeathBehavior.class).getLocationOfLastDeath().toArray();
	double height;
	final AltitudeMap map=
		p.getTr().
		getGame().
		getCurrentMission().
		getOverworldSystem().
		getAltitudeMap();
	if(map!=null)height= map.heightAt((thisPos[0]), 
		    (thisPos[2]));
	else{height=Double.NEGATIVE_INFINITY;}
	final double [] yFudge=thisPos[1]<height+OVER_TERRAIN_PAD?new double[]{0,OVER_TERRAIN_PAD,0}:new double[3];
	getParent().getTr().getResourceManager().getPowerupSystem().
		spawn(Vect3D.add(thisPos, yFudge, yFudge), pup);
    }//end notifyDeath()
}//end LeavesPowerupOnDeathBehavior
