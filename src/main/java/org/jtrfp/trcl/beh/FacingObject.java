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
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.math.Vect3D.ZeroNormException;
import org.jtrfp.trcl.obj.WorldObject;

public class FacingObject extends Behavior {
    private WeakReference<WorldObject> target;
    private final double [] work = new double[3];
    private final double [] perp = new double[3];
    private final double [] UP = new double[]{0,1,0};
    private final double [] newTop = new double[3];
    private Rotation        readingOffset = Rotation.IDENTITY;
    private static final Vector3D DEFAULT_HEADING = Vector3D.PLUS_K;
    private static final Vector3D DEFAULT_TOP = Vector3D.PLUS_J;
    @Override
    public void tick(long tickTimeMillis){
	if(target!=null){
	    final WorldObject parent = getParent();
	    final double [] tPos = target.get().getPosition();
	    final double [] pPos = parent.getPosition();
	    
	    Vect3D.subtract(tPos, pPos, work);
	    
	    Vector3D newHeading;
	    try{newHeading = new Vector3D(Vect3D.normalize(work,work));}
	    catch(ZeroNormException e){return;}
	    
	    Vect3D.cross(work, UP, perp);
	    Vect3D.cross(perp, work, newTop);
	    final Rotation facingRot = 
		    new Rotation(DEFAULT_HEADING,DEFAULT_TOP,newHeading,new Vector3D(newTop));
	    
	    parent.setHeading(facingRot.applyTo(readingOffset).applyTo(DEFAULT_HEADING));
	    parent.setTop    (facingRot.applyTo(readingOffset).applyTo(DEFAULT_TOP));
	}//end if(!null)
    }//end _tick(...)
    /**
     * @return the target
     */
    public WorldObject getTarget() {
        return target.get();
    }
    /**
     * @param target the target to set
     */
    public FacingObject setTarget(WorldObject target) {
        this.target = new WeakReference<WorldObject>(target);
        return this;
    }
    /**
     * @return the readingOffset
     */
    public Rotation getHeadingOffset() {
        return readingOffset;
    }
    /**
     * @param readingOffset the readingOffset to set
     */
    public void setHeadingOffset(Rotation readingOffset) {
        this.readingOffset = readingOffset;
    }
}//end FacingObject
