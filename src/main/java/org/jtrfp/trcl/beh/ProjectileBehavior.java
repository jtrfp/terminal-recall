package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileBehavior extends Behavior implements SurfaceImpactListener,DEFObjectCollisionListener{
    	public static final long LIFESPAN_MILLIS=4500;
	private final int damageOnImpact;
	private final DeathBehavior deathBehavior;
	public ProjectileBehavior(WorldObject parent, int damageOnImpact, ExplosionType explosionType){
	    this.damageOnImpact=damageOnImpact;
	    parent.addBehavior(new MovesByVelocity());
	    parent.addBehavior(new CollidesWithTerrain());deathBehavior=
	    parent.addBehavior(new DeathBehavior());
	    parent.addBehavior(new ExplodesOnDeath(explosionType));
	    parent.addBehavior(new CollidesWithDEFObjects(2000));
	    parent.addBehavior(new LimitedLifeSpan().reset(LIFESPAN_MILLIS));
	    parent.addBehavior(new LoopingPositionBehavior());
	}
	@Override
	public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal) {
	    {deathBehavior.die();}
	}
	@Override
	public void _proposeCollision(WorldObject wo){
	   /* if(wo instanceof DEFObject && wo.getPosition().distance(getParent().getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE){
		{destroy();System.out.println("Laserbeam destroyed by collision with DEFObject");}
	    }//end if(close by)*/
	}//end _proposeCollision(...)
	@Override
	public void collidedWithDEFObject(DEFObject other) {
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(damageOnImpact);
	    deathBehavior.die();
	}
	public void forceCollision(WorldObject other) {
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(damageOnImpact);
	    deathBehavior.die();
	    
	}
}//end ProjectileBehavior