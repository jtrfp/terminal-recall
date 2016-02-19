/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import org.jtrfp.trcl.Controller;

public class FireOnFrame extends Behavior {
    private Controller controller;
    private int        frame;
    private int        numShots;
    private int        shotsRemaining;
    private int        timeBetweenShotsMillis;
    private long       minTimeForNextShot;
    
    @Override
    public void tick(long tickTimeMillis){
	final int frame = (int)getController().getCurrentFrame();
	if(frame == getFrame()){
	    if(shotsRemaining > 0 && tickTimeMillis >= minTimeForNextShot)
		performShot(tickTimeMillis);
	    shotsRemaining--;
	}else
	    shotsRemaining = getNumShots();
    }//end tick(...)
    
    private void performShot(long tickTimeMillis){
	getParent().probeForBehavior(ProjectileFiringBehavior.class).requestFire();
	minTimeForNextShot = tickTimeMillis + getTimeBetweenShotsMillis();
    }
    
    public Controller getController() {
        return controller;
    }
    public FireOnFrame setController(Controller controller) {
        this.controller = controller;
        return this;
    }
    public int getFrame() {
        return frame;
    }
    public FireOnFrame setFrame(int frame) {
        this.frame = frame;
        return this;
    }
    public int getNumShots() {
        return numShots;
    }
    public FireOnFrame setNumShots(int numShots) {
        this.numShots = numShots;
        return this;
    }

    public int getTimeBetweenShotsMillis() {
        return timeBetweenShotsMillis;
    }

    public FireOnFrame setTimeBetweenShotsMillis(int timeBetweenShotsMillis) {
        this.timeBetweenShotsMillis = timeBetweenShotsMillis;
        return this;
    }

}//end FireOnFrame
