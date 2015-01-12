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
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Smoke.SmokeType;
import org.jtrfp.trcl.obj.SmokeSystem;

public class SpawnsRandomSmoke extends Behavior {
    private final SmokeSystem Smokes;
    public SpawnsRandomSmoke(TR tr){
	this.Smokes=tr.getResourceManager().getSmokeSystem();
    }
    @Override
    public void _tick(long timeMillis){
	if(Math.random()<.6){
	    Vector3D pos = new Vector3D(getParent().getPositionWithOffset());
	    Smokes.triggerSmoke(
		    pos.add(new Vector3D(Math.random()*2000-1000,
			    0,
			    Math.random()*2000-1000)), 
			    SmokeType.Puff);}
    }//end _tick(...)
}//end SpawnsRandomSmoke
