package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.LimitedLifeSpan;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileObject3D extends WorldObject implements Projectile {
    public static final long LIFESPAN_MILLIS=4500;
    
    public ProjectileObject3D(TR tr,Model m, int damageOnImpact, ExplosionType explosionType){
	super(tr,m);
	addBehavior(new ProjectileBehavior(this,damageOnImpact,explosionType));
    }

    
    public void reset(Vector3D newPos, Vector3D newVelocity){
	getBehavior().probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	if(newVelocity.getNorm()!=0)setHeading(newVelocity.normalize());
	else setHeading(Vector3D.PLUS_I);//meh.
	setPosition(newPos);
	setVisible(true);
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
	getBehavior().probeForBehavior(DeathBehavior.class).reset();
    }//end reset()
}
