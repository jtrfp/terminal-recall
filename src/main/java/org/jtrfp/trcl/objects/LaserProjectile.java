package org.jtrfp.trcl.objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ai.Behavior;
import org.jtrfp.trcl.ai.LimitedLifeSpan;
import org.jtrfp.trcl.ai.MovesByVelocity;
import org.jtrfp.trcl.ai.SurfaceImpactListener;
import org.jtrfp.trcl.core.TR;

public class LaserProjectile extends WorldObject implements Projectile {
    private static final long LIFESPAN_MILLIS=4500;
    public LaserProjectile(TR tr,Model m){
	super(tr,m);
	addBehavior(new MovesByVelocity());
	addBehavior(new LaserBehavior());
	//addBehavior(new LimitedLifeSpan());
    }
    private class LaserBehavior extends Behavior implements SurfaceImpactListener{

	@Override
	public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal) {
	    {destroy();System.out.println("Laserbeam destroyed by collision with Surface");}
	}
	@Override
	public void _proposeCollision(WorldObject wo){
	   /* if(wo instanceof DEFObject && wo.getPosition().distance(getParent().getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE){
		{destroy();System.out.println("Laserbeam destroyed by collision with DEFObject");}
	    }//end if(close by)*/
	}//end _proposeCollision(...)
    }//end LaserBehavior
    
    public void reset(Vector3D newPos, Vector3D newVelocity){
	//getBehavior().probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	setHeading(newVelocity.normalize());
	setPosition(newPos);
	setVisible(true);
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
    }//end reset()
}
