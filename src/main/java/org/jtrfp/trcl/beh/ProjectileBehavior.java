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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.DamageListener.ProjectileDamage;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.WorldObject;

public class ProjectileBehavior extends Behavior implements
	SurfaceImpactListener, DEFObjectCollisionListener,
	PlayerCollisionListener {
    public static final long LIFESPAN_MILLIS = 4500;
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
	parent.addBehavior(new CollidesWithDEFObjects(2000));
	parent.addBehavior(new CollidesWithPlayer());
	parent.addBehavior(new LimitedLifeSpan().reset(LIFESPAN_MILLIS));
	parent.addBehavior(new LoopingPositionBehavior());
	if (honing) {
	    parent.addBehavior(new AutoLeveling().setRetainmentCoeff(.88, .88,
		    .88).setLevelingAxis(LevelingAxis.HEADING));
	}// end if(honingTarget)
    }// end constructor

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
		    return new ArrayList<Positionable>(getParent().getTr().mainRenderer.get().getCamera().getFlatRelevanceCollection());
		}
	    }).get();}catch(Exception e){throw new RuntimeException(e);}
	    synchronized(possibleTargets){
	    for (Positionable possibleTarget : possibleTargets) {
		if (possibleTarget instanceof DEFObject) {
		    DEFObject possibleDEFTarget = (DEFObject)possibleTarget;
		    if (!possibleDEFTarget.isIgnoringProjectiles() && !possibleDEFTarget.isRuin()) {
			final Vector3D targetPos = new Vector3D(
				((WorldObject)possibleTarget).getPositionWithOffset());
			final Vector3D delta = targetPos.subtract(new Vector3D(
				getParent().getPosition()));
			final double dist = delta.getNorm();
			final Vector3D proposedHeading = delta.normalize();
			final Vector3D headingDelta = getParent().getHeading()
				.subtract(proposedHeading);
			final double compositeHeadingDelta = headingDelta.getNorm();
			if (compositeHeadingDelta < .5) {
			final double compositeDistance = dist; 
			    if (compositeDistance < closestDistance) {
				closestDistance = dist;
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
		movesByVelocity.setVelocity(getParent().getHeading()
			.scalarMultiply(speed));
	}// end if(honingTarget)
	probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	probeForBehavior(DeathBehavior.class).reset();
    }// end reset()

    @Override
    public void tick(long tickTimeMillis) {
	if (honingTarget != null) {
	    if(honingTarget.get()==null)return;
	    if (honingAdjustmentUpdate++ % 5 == 0) {
		if (!honingTarget.get().isVisible())
		    return;// Dead or otherwise.
		final Vector3D honingVector = new Vector3D(
			honingTarget.get().getPositionWithOffset()).subtract(new Vector3D(
			getParent().getPosition())).normalize();
		//Sanity check
		if(Double.isNaN(honingVector.getX()))return;
		if(Double.isNaN(honingVector.getY()))return;
		if(Double.isNaN(honingVector.getZ()))return;
		probeForBehavior(AutoLeveling.class)
			.setLevelingVector(honingVector);
		movesByVelocity.setVelocity(getParent().getHeading()
			.scalarMultiply(speed));
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
		item.proposeDamage(new ProjectileDamage(damageOnImpact));
	    }}, DamageableBehavior.class);
	deathBehavior.die();
    }//end collidedWithDEFObject

    public void forceCollision(WorldObject other) {
	other.probeForBehaviors(new AbstractSubmitter<DamageableBehavior>(){
	    @Override
	    public void submit(DamageableBehavior item) {
		item.proposeDamage(new ProjectileDamage(damageOnImpact));
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
		item.proposeDamage(new ProjectileDamage(damageOnImpact));
	    }}, DamageableBehavior.class);
	deathBehavior.die();
    }
}// end ProjectileBehavior
