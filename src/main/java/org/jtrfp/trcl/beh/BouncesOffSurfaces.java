package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class BouncesOffSurfaces extends Behavior implements
	SurfaceImpactListener {

    @Override
    public void collidedWithSurface(WorldObject wo, Vector3D surfaceNormal) {
	final WorldObject parent = getParent();
	final Vector3D oldHeading = parent.getHeading();
	final Vector3D oldTop = parent.getTop();
	Vector3D newHeading = (surfaceNormal.scalarMultiply(surfaceNormal.dotProduct(oldHeading)*-2).add(oldHeading));
	parent.setHeading(newHeading);
	final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
	Vector3D newTop = resultingRotation.applyTo(oldTop);
	if(newTop.getY()<0)newTop=newTop.negate();
	parent.setTop(newTop);
	if(parent instanceof Velocible){
	    final Velocible velocible = (Velocible)parent;
	    final Vector3D oldVelocity = velocible.getVelocity();
	    
	    velocible.setVelocity((surfaceNormal.scalarMultiply(surfaceNormal.dotProduct(oldVelocity)*-2).add(oldVelocity)));
	    //Nudge
	    parent.setPosition(parent.getPosition().add(velocible.getVelocity().scalarMultiply(2.)));
	}
    }//end collidedWithSurface(...)

}
