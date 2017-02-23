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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.DEFObject.HitBox;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Projectile;
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
	    
	    otherDEF=new WeakReference<DEFObject>(def);
	    if(hitBoxes == null){// No custom hitboxes
		final Vector3D max = def.getModel().getMaximumVertexDims(),
			       min = def.getModel().getMinimumVertexDims();
		if(boxCollision(
			parent.getPositionWithOffset(), 
			other.getPositionWithOffset(),
			min,max, boundingRadius,
			other.getHeading(),other.getTop()))
		            parent.probeForBehaviors(sub, DEFObjectCollisionListener.class);
	    }else{//Custom hitboxes
		boolean doCollision = false;
		BasicModelSource mSource = def.getModelSource();
		if(mSource == null)
		    throw new NullPointerException("DEF's model source intolerably null.");
		for(HitBox box:hitBoxes){
		    final double limit = boundingRadius+box.getSize();
		    final double [] vPos = Vect3D.add(mSource.getVertex(box.getVertexID()),defPos,workTriplet);
		    Vect3D.subtract(vPos, pPos, vPos);
		    Vect3D.abs(vPos, vPos);
		    boolean localDoCollision = true;
		    for(int i=0; i<3; i++){
			final double dist = TRFactory.rolloverDistance(vPos[i]);
			//if(((Projectile)getParent()).getObjectOfOrigin() instanceof Player)
			 //System.out.println("hBox "+def.getModel().getDebugName()+" dist="+dist+" limit="+limit);
			localDoCollision &= dist < limit;
		    }
		    doCollision |= localDoCollision;
		}//end for(hitBoxes)
		if(doCollision){
		    //System.out.println("HBOX COLLISION "+other.getModel().getDebugName());
		 parent.probeForBehaviors(sub, DEFObjectCollisionListener.class);
		 }
	    }//end if(custom hitboxes)
	}//end if(DEFObject)
    }//end _proposeCollision()
    
    private static boolean boxCollision(double [] testSpherePos, double [] boxPos, Vector3D min, Vector3D max, double boundingRadius, Vector3D boxHeading, Vector3D boxTop){
	//Calculate other object to parent's space.
	Vector3D relPosThis = new Vector3D(testSpherePos).
		subtract(new Vector3D(boxPos));
	//Factor in rollover
	relPosThis = new Vector3D(
		TRFactory.rolloverDistance(relPosThis.getX()),
		TRFactory.rolloverDistance(relPosThis.getY()),
		TRFactory.rolloverDistance(relPosThis.getZ())
		);
	Rotation rot = new Rotation(Vector3D.PLUS_K, Vector3D.PLUS_J,boxHeading,boxTop);
	//Rotate the other position relative to the parent's heading/top.
	relPosThis = rot.applyInverseTo(relPosThis);
	final boolean withinRange = 
		relPosThis.getX()<max.getX() + boundingRadius &&
		relPosThis.getX()>min.getX() - boundingRadius &&
		relPosThis.getY()<max.getY() + boundingRadius &&
		relPosThis.getY()>min.getY() - boundingRadius &&
		relPosThis.getZ()<max.getZ() + boundingRadius &&
		relPosThis.getZ()>min.getZ() - boundingRadius;
	return withinRange;
    }//end boxCollision(...)
    
    private final Submitter<DEFObjectCollisionListener> sub = new AbstractSubmitter<DEFObjectCollisionListener>(){
	@Override
	public void submit(DEFObjectCollisionListener l){
	    DEFObject other = otherDEF.get();
	    if(other!=null)
		l.collidedWithDEFObject(other);
	}//end submit(...)
    };//end Submitter
}
