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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class AdjustAltitudeToPlayerBehavior extends Behavior {
    private final Player 	player;
    private final Cloakable 	playerCloakability;
    private double[] 		DOWN 	   = new double[]{0,-80000,0},
	    			UP	   = new double[]{0,80000,0};
    private boolean 		reverse	   = false;
    private double 		hysteresis = 50000;
    
    public AdjustAltitudeToPlayerBehavior(Player player){
	this.player=player;
	this.playerCloakability=player.probeForBehavior(Cloakable.class);
    }//end AdjustAltitudeToPlayerBehavior
    
    @Override
    public void tick(long tickTimeInMillis){
	if(playerCloakability.isCloaked())return;
	final WorldObject thisObject = getParent();
	if(!reverse&&Math.abs(player.getPosition()[1]-thisObject.getPosition()[1])<hysteresis)
	    return;
	final boolean up=(player.getPosition()[1]>thisObject.getPosition()[1])!=reverse;
	    thisObject.probeForBehavior(MovesByVelocity.class).accellerate(up?UP:DOWN);
    }//end _tick(...)

    public AdjustAltitudeToPlayerBehavior setAccelleration(int accelleration) {
	UP=new double[]{0,accelleration,0};
	DOWN=new double[]{0,-accelleration,0};
	return this;
    }//end setAccelleration

    public void setReverse(boolean reverse) {
	this.reverse=reverse;
    }

    /**
     * @return the hysteresis
     */
    public double getHysteresis() {
        return hysteresis;
    }

    /**
     * @param hysteresis the hysteresis to set
     */
    public void setHysteresis(double hysteresis) {
        this.hysteresis = hysteresis;
    }
}//end AdjustAltitudeToPlayerBehavior
