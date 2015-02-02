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

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.BarrierCube;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class CubeCollisionBehavior extends Behavior implements CollisionBehavior {
private double [] dims;
private double [] origin;
private final double [] rotTransPosVar = new double[3];
private int damageOnImpact= 6554;
	public CubeCollisionBehavior(){super();}
	public CubeCollisionBehavior(BarrierCube bc){
	    super();
	    origin=bc.getOrigin();
	    dims=bc.getDims();
	}
	public CubeCollisionBehavior(WorldObject wo) {
	    super();
	    Vector3D max= wo.getModel().getTriangleList().getMaximumVertexDims();
	    Vector3D min= wo.getModel().getTriangleList().getMinimumVertexDims();
	    origin = new double[]{(max.getX()+min.getX())/2.,(max.getY()+min.getY())/2.,(max.getZ()+min.getZ())/2.};
	    dims = new double[]{max.getX()-min.getX(),max.getY()-min.getY(),max.getZ()-min.getZ()};
	}
	@Override
	public void proposeCollision(WorldObject obj){
	    if(obj instanceof Player){
		final WorldObject p = getParent();
		final double [] relPos=TR.twosComplimentSubtract(obj.getPosition(), p.getPosition(), new double[3]);
		final Rotation rot = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,p.getHeading(),p.getTop());
		final double[] rotPos=rot.applyInverseTo(new Vector3D(relPos)).toArray();
		final double [] rotTransPos=Vect3D.add(rotPos,origin,rotTransPosVar);
		if(TR.twosComplimentDistance(obj.getPosition(), p.getPosition())<80000)
		if(	rotTransPos[0]>0 && rotTransPos[0]<dims[0] &&
			rotTransPos[1]>0 && rotTransPos[1]<dims[1] &&
			rotTransPos[2]>0 && rotTransPos[2]<dims[2]){
		    obj.probeForBehaviors(new Submitter<DamageListener>(){
			@Override
			public void submit(DamageListener item) {
			    item.groundCollisionDamage(damageOnImpact);
			}
			@Override
			public void submit(Collection<DamageListener> items) {
			    for(DamageListener item:items)
				item.groundCollisionDamage(damageOnImpact);
			}}, DamageListener.class);
		    }//end if(withinRange)
	    }//end if(Player)
	}//end _proposeCollision(...)

	/**
	 * @return the dims
	 */
	public double[] getDims() {
	    return dims;
	}

	/**
	 * @param dims the dims to set
	 */
	public void setDims(double [] dims) {
	    this.dims = dims;
	}

	/**
	 * @return the origin
	 */
	public double[] getOrigin() {
	    return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(double[] origin) {
	    this.origin = origin;
	}
	/**
	 * @return the damageOnImpact
	 */
	public int getDamageOnImpact() {
	    return damageOnImpact;
	}
	/**
	 * @param damageOnImpact the damageOnImpact to set
	 */
	public CubeCollisionBehavior setDamageOnImpact(int damageOnImpact) {
	    this.damageOnImpact = damageOnImpact;
	    return this;
	}
}//end CubeCollisionBehavior
