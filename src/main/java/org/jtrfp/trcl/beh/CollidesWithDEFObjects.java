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
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.DEFObject.HitBox;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithDEFObjects extends Behavior implements CollisionBehavior {
    private final double boundingRadius;
    private WeakReference<DEFObject> otherDEF;
    private final double [] workTriplet = new double[3];
    public CollidesWithDEFObjects(double boundingRadius){
	this.boundingRadius=boundingRadius;
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof DEFObject){
	    final DEFObject def = (DEFObject)other;
	    final HitBox [] hitBoxes = def.getHitBoxes();
	    final WorldObject parent = getParent();
	   
	    final double [] defPos = def.getPositionWithOffset();
	    final double [] pPos   = parent.getPositionWithOffset();
	    
	    otherDEF=new WeakReference<DEFObject>((DEFObject)other);
	    if(hitBoxes == null){// No custom hitboxes
		final double maxVtx = otherDEF.get().getModel().getMaximumVertexValue();
		 final double distance=TR.twosComplimentDistance(
			    other.getPositionWithOffset(), 
			    parent.getPositionWithOffset());
		    if(distance<(boundingRadius+maxVtx))
			parent.probeForBehaviors(sub, DEFObjectCollisionListener.class);
	    }else{//Custom hitboxes
		boolean doCollision = false;
		BasicModelSource mSource = def.getModelSource();
		if(mSource == null)
		    throw new NullPointerException("DEF's model source intolerably null.");
		for(HitBox box:hitBoxes){
		    final double limit = boundingRadius+box.getSize();
		    final double [] vPos = Vect3D.add(mSource.getVertex(box.getVertexID()),defPos,workTriplet);
		    Vect3D.subtract(workTriplet, pPos, vPos);
		    Vect3D.abs(workTriplet, workTriplet);
		    boolean localDoCollision = true;
		    for(int i=0; i<3; i++)
		     localDoCollision &= TR.rolloverDistance(workTriplet[i]) < limit;
		    doCollision |= localDoCollision;
		}//end for(hitBoxes)
		System.out.println("Custom hitbox: ");
		if(doCollision)
		 parent.probeForBehaviors(sub, DEFObjectCollisionListener.class);
	    }//end if(custom hitboxes)
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
