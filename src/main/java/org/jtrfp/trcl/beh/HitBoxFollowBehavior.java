/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.DEFObject.HitBox;

public class HitBoxFollowBehavior extends Behavior {
    private final HitBox hitBox;
    private final DEFObject target;
    private final BasicModelSource modelSource;

    public HitBoxFollowBehavior(DEFObject obj, HitBox hitBox) {
	this.target = obj;
	this.hitBox = hitBox;
	this.modelSource = obj.getModelSource();
    }//end constructor
    
    @Override
    protected void tick(long tickTimeMillis) {
	final boolean targetActive = target.isActive();
	getParent().setVisible(targetActive);
	if(!targetActive)
	    return;
	final double [] tPos = target.getPositionWithOffset();
	final double [] vPos = modelSource.getVertex(hitBox.getVertexID());
	final Vector3D globalPos = new Vector3D(tPos[0], tPos[1], tPos[2]).add(new Vector3D(vPos[0],vPos[1],vPos[2]));
	getParent().setPosition(globalPos.getX(), globalPos.getY(), globalPos.getZ());
	getParent().notifyPositionChange();
    }//end tick(...)

}//end HitBoxFollowBehavior
