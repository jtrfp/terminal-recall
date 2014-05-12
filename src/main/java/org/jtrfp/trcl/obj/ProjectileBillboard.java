package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.LimitedLifeSpan;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.ModelingType;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileBillboard extends BillboardSprite implements Projectile {
    public static final long LIFESPAN_MILLIS=4500;
    private WorldObject objectOfOrigin;
    public ProjectileBillboard(TR tr,Weapon w,TRFutureTask<TextureDescription> textureToUse,ExplosionType explosionType) {
	super(tr);
	addBehavior(new ProjectileBehavior(this,w.getDamage(),explosionType,w.isHoning()));
	ModelingType.BillboardModelingType mt = (ModelingType.BillboardModelingType)w.getModelingType();
	this.setBillboardSize(new Dimension((int)(mt.getBillboardSize().getWidth()/TR.crossPlatformScalar),(int)(mt.getBillboardSize().getHeight()/TR.crossPlatformScalar)));
	this.setTexture(textureToUse, true);
    }
    public void reset(double [] newPos, Vector3D newVelocity, WorldObject objectOfOrigin){
	this.objectOfOrigin=objectOfOrigin;
	getBehavior().probeForBehavior(LimitedLifeSpan.class).reset(LIFESPAN_MILLIS);
	setHeading(newVelocity.normalize());
	setPosition(newPos[0],newPos[1],newPos[2]);
	setVisible(true);
	setActive(true);
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
	getBehavior().probeForBehavior(ProjectileBehavior.class).reset(newVelocity.normalize(),newVelocity.getNorm());
    }//end reset()
    @Override
    public WorldObject getObjectOfOrigin() {
	return objectOfOrigin;
    }//end getObjectOfOrigin()
}//end ProjectileBillboard
