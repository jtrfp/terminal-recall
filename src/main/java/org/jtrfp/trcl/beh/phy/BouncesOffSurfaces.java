package org.jtrfp.trcl.beh.phy;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.SurfaceImpactListener;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class BouncesOffSurfaces extends Behavior implements
	SurfaceImpactListener {
    private boolean reflectHeading=true;

    @Override
    public void collidedWithSurface(WorldObject wo, double [] surfaceNormal) {
	final WorldObject parent = getParent();
	final Vector3D oldHeading = parent.getHeading();
	final Vector3D oldTop = parent.getTop();
	final Vector3D _surfaceNormal = new Vector3D(surfaceNormal);
	if(oldHeading==null)throw new NullPointerException("Parent heading is null.");
	if(surfaceNormal==null)throw new NullPointerException("Surface normal is null.");
	if(reflectHeading && new Rotation(oldHeading,_surfaceNormal).getAngle()>Math.PI/2.){
	    Vector3D newHeading = (_surfaceNormal.scalarMultiply(_surfaceNormal.dotProduct(oldHeading)*-2).add(oldHeading));
	    parent.setHeading(newHeading);
	    final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
	    Vector3D newTop = resultingRotation.applyTo(oldTop);
	    //if(newTop.getY()<0)newTop=newTop.negate();
	    parent.setTop(newTop);
	}//end if(should reflect)
	if(parent instanceof Velocible){
	    final Velocible velocible = (Velocible)parent;
	    final Vector3D oldVelocity = velocible.getVelocity();
	    if(new Rotation(oldVelocity.normalize(),_surfaceNormal).getAngle()>Math.PI/2.){
		velocible.setVelocity((_surfaceNormal.scalarMultiply(_surfaceNormal.dotProduct(oldVelocity)*-2).add(oldVelocity)));
		//Nudge
		//parent.setPosition(parent.getPosition().add(velocible.getVelocity().scalarMultiply(100.)));
	    }//end if(should bounce)
	}//end if(Velocible)
    }//end collidedWithSurface(...)

    /**
     * @return the reflectHeading
     */
    public boolean isReflectHeading() {
        return reflectHeading;
    }

    /**
     * @param reflectHeading the reflectHeading to set
     */
    public BouncesOffSurfaces setReflectHeading(boolean reflectHeading) {
        this.reflectHeading = reflectHeading;
        return this;
    }

}
