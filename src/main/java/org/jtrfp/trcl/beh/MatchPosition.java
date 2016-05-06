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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public class MatchPosition extends Behavior {
    private WorldObject target;
    private OffsetMode  offsetMode = new NullOffsetMode();
    private final PositionListener positionListener = new PositionListener();
    private Vector3D position = Vector3D.ZERO;
    
    public static final NullOffsetMode NULL = new NullOffsetMode();
    @Override
    public void tick(long tickTimeMillis){
	if(target!=null){
	    /*
	    final WorldObject parent = getParent();
	    double [] pPos = parent.getPosition();
	    double [] pos  = target.getPosition();
	    System.arraycopy(pos, 0, pPos, 0, 3);
	    offsetMode.processPosition(pPos, this);
	    parent.notifyPositionChange();
	    */
	}//end if(!null)
    }//end _tick(...)
    /**
     * @return the target
     */
    public WorldObject getTarget() {
        return target;
    }
    /**
     * @param target the target to set
     */
    public MatchPosition setTarget(WorldObject target) {
	final WorldObject oldTarget = this.target;
	if(oldTarget!=null)
	    oldTarget.removePropertyChangeListener(positionListener);
        this.target = target;
        target.addPropertyChangeListener(positionListener);
        return this;
    }//end setTarget(...)
    
    private class PositionListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    if(!isEnabled())
		return;
	    final WorldObject parent = getParent();
	    double [] pPos = parent.getPosition();
	    double [] tPos = target.getPosition();
	    System.arraycopy(tPos, 0, pPos, 0, 3);
	    offsetMode.processPosition(pPos, MatchPosition.this);
	    parent.notifyPositionChange();
	}
    }//end PositionListener
    
    public static interface OffsetMode{
	public void processPosition(double [] position, MatchPosition mp);
    }//end OffsetMode
    
    public static class NullOffsetMode implements OffsetMode {
	@Override
	public void processPosition(double[] position, MatchPosition mp) {
	   //Do nothing
	}
    }//end NullOffsetMode
    
    public static class TailOffsetMode implements OffsetMode {
	private double [] workArray = new double[3];
	private Vector3D tailVector, offsetVector;
	
	public TailOffsetMode(Vector3D tailVector, Vector3D offsetVector){
	    setTailVector(tailVector);
	    setOffsetVector(offsetVector);
	}
	
	@Override
	public void processPosition(double[] position, MatchPosition mp) {
	    final WorldObject target = mp.getTarget();
	    final Rotation rot       = new Rotation(Vector3D.PLUS_K, Vector3D.PLUS_J, target.getHeading(), target.getTop());
	    final Vector3D newPos    = rot.applyTo(tailVector).add(new Vector3D(position).add(offsetVector));
	    System.arraycopy(newPos.toArray(), 0, position, 0, 3);
	    /*
	    System.arraycopy(lookAt, 0, workArray, 0, 3);
	    Vect3D.normalize(workArray, workArray);
	    //Need to take care of y before x and z due to dependency order.
	    workArray[1]*=-tailDistance;
	    workArray[1]+= floatHeight*Math.sqrt(workArray[0]*workArray[0]+workArray[2]*workArray[2]);
	    
	    workArray[0]*=-tailDistance;
	    workArray[2]*=-tailDistance;
	    Vect3D.add(position, workArray, position);
	    */
	}

	public Vector3D getTailVector() {
	    return tailVector;
	}

	public void setTailVector(Vector3D tailVector) {
	    this.tailVector = tailVector;
	}

	public Vector3D getOffsetVector() {
	    return offsetVector;
	}

	public void setOffsetVector(Vector3D offsetVector) {
	    this.offsetVector = offsetVector;
	}
    }//end TailOffsetMode
    
    public OffsetMode getOffsetMode() {
        return offsetMode;
    }
    public MatchPosition setOffsetMode(OffsetMode offsetMode) {
        this.offsetMode = offsetMode;
        return this;
    }
}//end MatchPosition
