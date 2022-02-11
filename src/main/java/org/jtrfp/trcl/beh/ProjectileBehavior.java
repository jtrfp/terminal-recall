/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.DEFObject.HitBox;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.WorldObject;

public class ProjectileBehavior extends Behavior implements
	SurfaceImpactListener, DEFObjectCollisionListener,
	PlayerCollisionListener {
    public static final long LIFESPAN_MILLIS = 4500;
    public static final int BOUNDING_RADIUS = 6000;
    private final int damageOnImpact;
    private final DeathBehavior deathBehavior;
    private final Projectile parent;
    private WeakReference<WorldObject> honingTarget = new WeakReference<WorldObject>(null);
    private int honingAdjustmentUpdate = 0;
    private boolean honing = false;
    private double speed;
    private final MovesByVelocity movesByVelocity;

    public ProjectileBehavior(WorldObject parent, int damageOnImpact,
	    ExplosionType explosionType, boolean honing) {
	this.damageOnImpact = damageOnImpact;
	this.parent = (Projectile) parent;
	this.honing = honing;
	movesByVelocity = new MovesByVelocity();
	parent.addBehavior(movesByVelocity);
	parent.addBehavior(new CollidesWithTunnelWalls(false, false));
	parent.addBehavior(new CollidesWithTerrain());
	deathBehavior = parent.addBehavior(new DeathBehavior());
	parent.addBehavior(new ExplodesOnDeath(explosionType));
	parent.addBehavior(new CollidesWithDEFObjects(BOUNDING_RADIUS));
	parent.addBehavior(new CollidesWithPlayer());
	parent.addBehavior(new LimitedLifeSpan().reset(LIFESPAN_MILLIS));
	parent.addBehavior(new LoopingPositionBehavior());
	if (honing) {
	    parent.addBehavior(new AutoLeveling().setRetainmentCoeff(.88, .88,
		    .88).setLevelingAxis(LevelingAxis.HEADING));
	}// end if(honingTarget)
    }// end constructor
    
    private final double [] targetPosWorkTriplet = new double[3];

    public void reset(Vector3D heading, double speed) {
	this.speed = speed;
	honingTarget=null;
	final WorldObject parent = getParent();
	parent.setHeading(heading);
	if (honing) {
	    // Find target
	    Positionable closestObject = null;
	    double closestDistance = Double.POSITIVE_INFINITY;
	    Collection<Positionable> possibleTargets;
	    try{
	     possibleTargets =
	     World.relevanceExecutor.submit(new Callable<Collection<Positionable>>(){
		@Override
		public Collection<Positionable> call() {
		    return new ArrayList<Positionable>(getParent().getTr().mainRenderer.getCamera().getFlatRelevanceCollection());
		}
	    }).get();}catch(Exception e){throw new RuntimeException(e);}
	    synchronized(possibleTargets){
	    for (Positionable possibleTarget : possibleTargets) {
		if (possibleTarget instanceof DEFObject) {
		    DEFObject possibleDEFTarget = (DEFObject)possibleTarget;
		    if (!possibleDEFTarget.isIgnoringProjectiles() && !possibleDEFTarget.isRuin()) {
			final Vector3D targetPos = new Vector3D(getNearestTarget(possibleDEFTarget, targetPosWorkTriplet));
			final Vector3D proposedHeading = targetPos.subtract(new Vector3D(
				getParent().getPosition())).normalize();
			final double headingDelta = getParent().getHeading().distance(proposedHeading);
			if (headingDelta < .5) {
			    if (headingDelta < closestDistance) {
				closestDistance = headingDelta;
				closestObject = possibleTarget;
				parent.setHeading(proposedHeading);
				probeForBehavior(AutoLeveling.class)
				.setLevelingVector(heading);
			    }// end if(closesObject)
			}// end if(headingDelta<1)
		    }// end if(isIgnoringProjectiles)
		}// end if(DEFObject)
	    }}// end for(WorldObject others)
	    honingTarget = new WeakReference<WorldObject>((WorldObject)closestObject);
		probeForBehavior(AutoLeveling.class)
		.setLevelingVector(heading);
		final double [] velocityDest = movesByVelocity.getVelocity();
		Vect3D.scalarMultiply(getParent().getHeadingArray(), speed, velocityDest);
		movesByVelocity.setVelocity(velocityDest);//Just to be sure.
		//movesByVelocity.setVelocity(getParent().getHeading()
		//	.scalarMultiply(speed));
	}// end if(honing)
	probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	probeForBehavior(DeathBehavior.class).reset();
    }// end reset()
    
    private final double [] nearestTargetPosWithOff  = new double[3];
    private final double [] nearestTargetHoningVector = new double[3];
    
    private double [] getNearestTarget(WorldObject target, double [] dest) {
	final WorldObject parent  = getParent();
	final double [] pHeading = parent.getHeadingArray();
	final double [] tgtPos    = target.getPositionWithOffset();
	final double [] pPosWO = parent.getPositionWithOffset();
	double closest = Double.POSITIVE_INFINITY;
	if(target instanceof DEFObject) {
	    final DEFObject targetDEF = (DEFObject)target;
	    final HitBox [] hitBoxes  = targetDEF.getHitBoxes();
	    if(hitBoxes != null) {
		final BasicModelSource src = targetDEF.getModelSource();
		for(HitBox box : hitBoxes) {
		    Vect3D.add(src.getVertex(box.getVertexID()), tgtPos, nearestTargetPosWithOff);
		    Vect3D.subtract(nearestTargetPosWithOff, pPosWO, nearestTargetHoningVector);
		    Vect3D.normalize(nearestTargetHoningVector);
		    final double dist = Vect3D.distance(nearestTargetHoningVector, pHeading);
		    if(dist < closest) {
			closest = dist;
			System.arraycopy(nearestTargetPosWithOff, 0, dest, 0, 3);
		    }
		}//end for(hitBoxes)
	    }//end if(hitBoxes)
	}//end if(DEFObject)
	Vect3D.subtract(tgtPos, pPosWO, nearestTargetHoningVector);
	Vect3D.normalize(nearestTargetHoningVector);
	final double defDist = Vect3D.distance(nearestTargetHoningVector, pHeading);
	//final double defDist = Vect3D.distance(parentPos, tgtPos);//TODO
	if(defDist < closest)
	    System.arraycopy(target.getPosition(), 0, dest, 0, 3);
	return dest;
    }//end getNearestTarget()

    private final double [] tickNearestTargetWorkTriplet = new double[3];
    private final double [] tickHoningVector = new double[3];
    
    @Override
    public void tick(long tickTimeMillis) {//TODO: Optimize weak ref
	if (honingTarget != null) {
	    if(honingTarget.get()==null)return;
	    if (honingAdjustmentUpdate++ % 5 == 0) {
		if (!honingTarget.get().isVisible())
		    return;// Dead or otherwise.
		final WorldObject parent = getParent();
		getNearestTarget(honingTarget.get(), tickNearestTargetWorkTriplet);
		Vect3D.subtract(tickNearestTargetWorkTriplet, parent.getPositionWithOffset(), tickHoningVector);
		Vect3D.normalize(tickHoningVector);
		/*
		final Vector3D honingVector = new Vector3D(
			targetPositionWithOffset).subtract(new Vector3D(
			parent.getPosition())).normalize();//TODO: Optimize to arrays
		*/
		//Sanity check
		if(Double.isNaN(tickHoningVector[0]))return;
		if(Double.isNaN(tickHoningVector[1]))return;
		if(Double.isNaN(tickHoningVector[2]))return;
		probeForBehavior(AutoLeveling.class)
			.setLevelingVector(new Vector3D(tickHoningVector));
		final double [] destVelocity = movesByVelocity.getVelocity();
		Vect3D.scalarMultiply(parent.getHeadingArray(), speed, destVelocity);
		movesByVelocity.setVelocity(destVelocity);//Just to be sure.
	    }// end if(updateHoningVector)
	}// end if(honingTarget)
    }//end _tick()

    @Override
    public void collidedWithSurface(WorldObject wo, double[] surfaceNormal) {
	deathBehavior.die();
    }

    @Override
    public void collidedWithDEFObject(DEFObject other) {
	if (other.isIgnoringProjectiles())
	    return;
	if (other == parent.getObjectOfOrigin())
	    return;// Don't shoot yourself.
	if (parent.getObjectOfOrigin() instanceof DEFObject)
	    return;// Don't shoot your buddy.
	other.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
	    @Override
	    public void submit(DamageableBehavior item) {
		final DamageListener.ProjectileDamage dmg = 
			new DamageListener.ProjectileDamage();
		dmg.setDamageAmount(damageOnImpact);
		item.proposeDamage(dmg);
	    }}, DamageableBehavior.class);
	deathBehavior.die();
    }//end collidedWithDEFObject

    public void forceCollision(WorldObject other) {
	other.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
	    @Override
	    public void submit(DamageableBehavior item) {
		final DamageListener.ProjectileDamage dmg = 
			new DamageListener.ProjectileDamage();
		dmg.setDamageAmount(damageOnImpact);
		item.proposeDamage(dmg);
	    }}, DamageableBehavior.class);
	deathBehavior.die();
    }//end forceCollision(...)

    @Override
    public void collidedWithPlayer(Player other) {
	if (other == parent.getObjectOfOrigin())
	    return;// Don't shoot yourself.
	other.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
	    @Override
	    public void submit(DamageableBehavior item) {
		final DamageListener.ProjectileDamage dmg = 
			new DamageListener.ProjectileDamage();
		dmg.setDamageAmount(damageOnImpact);
		item.proposeDamage(dmg);
	    }}, DamageableBehavior.class);
	deathBehavior.die();
    }
}// end ProjectileBehavior
