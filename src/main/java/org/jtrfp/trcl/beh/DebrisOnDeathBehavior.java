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
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject;

public class DebrisOnDeathBehavior extends Behavior implements DeathListener {
    private final double MAX_SPEED=110000;
    private final int MIN_FRAGS=12;
    @Override
    public void notifyDeath() {
	WorldObject p = getParent();
	final double maxVertexValue;
	final Model model = p.getModel();
	if(model.getTriangleList()!=null)maxVertexValue=model.getTriangleList().getMaximumVertexValue();
	else if(model.getTransparentTriangleList()!=null)maxVertexValue=model.getTransparentTriangleList().getMaximumVertexValue();
	else return;//Give up
	for(int i=0; i<MIN_FRAGS+maxVertexValue/4000; i++){
	    p.getTr().getResourceManager().getDebrisFactory().spawn(p.getPosition(), 
	    new Vector3D(
		Math.random()*MAX_SPEED-MAX_SPEED/2.,
		Math.random()*MAX_SPEED+60000,
		Math.random()*MAX_SPEED-MAX_SPEED/2.));
	}//end for(NUM_FRAGS)
    }//end constructor

}
