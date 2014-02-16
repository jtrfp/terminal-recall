package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.WorldObject;

public class ProjectileBehavior extends Behavior implements SurfaceImpactListener,DEFObjectCollisionListener,PlayerCollisionListener,CollisionBehavior{
    	public static final long LIFESPAN_MILLIS=4500;
	private final int damageOnImpact;
	private final DeathBehavior deathBehavior;
	private final Projectile parent;
	public ProjectileBehavior(WorldObject parent, int damageOnImpact, ExplosionType explosionType){
	    this.damageOnImpact=damageOnImpact;
	    this.parent=(Projectile)parent;
	    parent.addBehavior(new MovesByVelocity());
	    parent.addBehavior(new CollidesWithTunnelWalls(false, false));
	    parent.addBehavior(new CollidesWithTerrain());deathBehavior=
	    parent.addBehavior(new DeathBehavior());
	    parent.addBehavior(new ExplodesOnDeath(explosionType));
	    parent.addBehavior(new CollidesWithDEFObjects(2000));
	    parent.addBehavior(new CollidesWithPlayer(2000));
	    parent.addBehavior(new LimitedLifeSpan().reset(LIFESPAN_MILLIS));
	    parent.addBehavior(new LoopingPositionBehavior());
	}
	@Override
	public void collidedWithSurface(WorldObject wo, double [] surfaceNormal) {
	    {deathBehavior.die();}
	}
	@Override
	public void proposeCollision(WorldObject wo){
	   /* if(wo instanceof DEFObject && wo.getPosition().distance(getParent().getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE){
		{destroy();System.out.println("Laserbeam destroyed by collision with DEFObject");}
	    }//end if(close by)*/
	}//end _proposeCollision(...)
	@Override
	public void collidedWithDEFObject(DEFObject other) {
	    if(other==parent.getObjectOfOrigin())return;//Don't shoot yourself.
	    if(parent.getObjectOfOrigin() instanceof DEFObject)return;//Don't shoot your buddy.
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).projectileDamage(damageOnImpact);
	    deathBehavior.die();
	}
	public void forceCollision(WorldObject other) {
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).projectileDamage(damageOnImpact);
	    deathBehavior.die();
	    
	}
	@Override
	public void collidedWithPlayer(Player other) {
	    if(other==parent.getObjectOfOrigin())return;//Don't shoot yourself.
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).projectileDamage(damageOnImpact);
	    deathBehavior.die();
	}
}//end ProjectileBehavior