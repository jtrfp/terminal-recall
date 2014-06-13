package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.beh.ReportsCollisionsToStdout;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileObject3D extends WorldObject implements Projectile {
    public static final long LIFESPAN_MILLIS=4500;
    private WorldObject objectOfOrigin;
    public ProjectileObject3D(TR tr,Model m, Weapon w, ExplosionType explosionType){
	super(tr,m);
	addBehavior(new ProjectileBehavior(this,w.getDamage(),explosionType,w.isHoning()));
	addBehavior(new ReportsCollisionsToStdout().setEnable(false));
    }

    @Override
    public void reset(double [] newPos, Vector3D newVelocity, WorldObject objectOfOrigin){
	this.objectOfOrigin=objectOfOrigin;
	if(newVelocity.getNorm()!=0)setHeading(newVelocity.normalize());
	else {setHeading(Vector3D.PLUS_I);newVelocity=Vector3D.PLUS_I;}//meh.
	setPosition(newPos[0],newPos[1],newPos[2]);
	setVisible(true);
	setActive(true);
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
	getBehavior().probeForBehavior(ProjectileBehavior.class).reset(newVelocity.normalize(),newVelocity.getNorm());
    }//end reset()

    @Override
    public WorldObject getObjectOfOrigin() {
	return objectOfOrigin;
    }
}//end ProjectilObject3D
