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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;
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
        target.addPropertyChangeListener(WorldObject.POSITION,positionListener);
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
	private final RealMatrix tailMatrix4x4;
	
	public TailOffsetMode(RealMatrix tailMatrix4x4){
	    if(tailMatrix4x4==null)
		throw new NullPointerException("tailMatrix intolerably null.");
	    this.tailMatrix4x4 = tailMatrix4x4;
	}//end constructor
	
	@Override
	public void processPosition(double[] position, MatchPosition mp) {
	   final double [] lookAt = mp.getTarget().getHeadingArray();
	   final double [] tail   = tailMatrix4x4.operate(new double[]{lookAt[0],lookAt[1],lookAt[2],1});
	   Vect3D.add(position, tail, position);
	}//end processPosition(...)
    }//end TailOffsetMode
    
    public OffsetMode getOffsetMode() {
        return offsetMode;
    }
    public MatchPosition setOffsetMode(OffsetMode offsetMode) {
        this.offsetMode = offsetMode;
        return this;
    }
}//end MatchPosition
