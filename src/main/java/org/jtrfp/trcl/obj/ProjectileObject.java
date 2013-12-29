package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithDEFObjects;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DEFObjectCollisionListener;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MovesByVelocity;
import org.jtrfp.trcl.beh.SurfaceImpactListener;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileObject extends WorldObject implements Projectile {
    private static final long LIFESPAN_MILLIS=4500;
    private final int damageOnImpact;
    private final ExplosionType explosionType;
    private final DeathBehavior deathBehavior;
    public ProjectileObject(TR tr,Model m, int damageOnImpact, ExplosionType explosionType){
	super(tr,m);
	this.damageOnImpact=damageOnImpact;
	this.explosionType=explosionType;
	addBehavior(new MovesByVelocity());
	addBehavior(new CollidesWithTerrain());deathBehavior=
	addBehavior(new DeathBehavior());
	addBehavior(new ExplodesOnDeath(explosionType));
	addBehavior(new ProjectileBehavior());
	addBehavior(new CollidesWithDEFObjects(2000));
    }
    private class ProjectileBehavior extends Behavior implements SurfaceImpactListener,DEFObjectCollisionListener{

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
	    other.getBehavior().probeForBehavior(DamageableBehavior.class).damage(damageOnImpact);
	    deathBehavior.die();
	}
    }//end LaserBehavior
    
    public void reset(Vector3D newPos, Vector3D newVelocity){
	//getBehavior().probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	setHeading(newVelocity.normalize());
	setPosition(newPos);
	setVisible(true);
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
	getBehavior().probeForBehavior(DeathBehavior.class).reset();
	addBehavior(new LoopingPositionBehavior());
    }//end reset()

    /**
     * @return the damageOnImpact
     */
    public double getDamageOnImpact() {
        return damageOnImpact;
    }

    /**
     * @return the explosionType
     */
    public ExplosionType getExplosionType() {
        return explosionType;
    }
}
