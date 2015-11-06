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

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithDEFObjects extends Behavior implements CollisionBehavior {
    private final double boundingRadius;
    private WeakReference<DEFObject> otherDEF;
    public CollidesWithDEFObjects(double boundingRadius){
	this.boundingRadius=boundingRadius;
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof DEFObject){
	    final double distance=TR.twosComplimentDistance(
		    other.getPositionWithOffset(), 
		    getParent().getPositionWithOffset());
	    otherDEF=new WeakReference<DEFObject>((DEFObject)other);
	    final double maxVtx = otherDEF.get().getModel().getMaximumVertexValue();
	    if(distance<(boundingRadius+maxVtx)){
		getParent().probeForBehaviors(sub, DEFObjectCollisionListener.class);
	    }
	}//end if(DEFObject)
    }//end _proposeCollision()
    
    private final Submitter<DEFObjectCollisionListener> sub = new AbstractSubmitter<DEFObjectCollisionListener>(){
	@Override
	public void submit(DEFObjectCollisionListener l){
	    DEFObject other = otherDEF.get();
	    if(other!=null)l.collidedWithDEFObject(other);
	}//end submit(...)
    };//end Submitter
}
