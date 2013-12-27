package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.WorldObject;

public class ProjectileFiringBehavior extends Behavior {
    long timeWhenNextFiringPermittedMillis=0;
    long timeBetweenFiringsMillis=100;
    private Vector3D [] firingPositions;
    private int firingPositionIndex=0;
    private ProjectileFactory projectileFactory;
    private boolean pendingFiring=false;
    @Override
    public void _tick(long tickTimeMillis){
	if(tickTimeMillis>timeWhenNextFiringPermittedMillis && pendingFiring){
	    	final WorldObject p = getParent();
	    	final Vector3D heading = p.getHeading();
	    	final Vector3D firingPosition = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,
	    		heading,p.getTop()).applyTo(getNextFiringPosition());
	    	//final Vector3D firingPosition = new Rotation(heading,Vector3D.PLUS_K,
	    	//	p.getTop(),Vector3D.PLUS_J).applyTo(getNextFiringPosition());
	    	resetFiringTimer();
	    	projectileFactory.fire(p.getPosition().add(firingPosition), heading);
	    	pendingFiring=false;
	}//end timeWhenNextfiringPermitted
    }//end _tick
    
    public ProjectileFiringBehavior requestFire(){
	pendingFiring=true;
	return this;
    }
    
    private void resetFiringTimer(){
	timeWhenNextFiringPermittedMillis = System.currentTimeMillis()+timeBetweenFiringsMillis;}

    private Vector3D getNextFiringPosition(){
	firingPositionIndex++;
	firingPositionIndex%=firingPositions.length;
	return firingPositions[firingPositionIndex];
    }
    /**
     * @return the firingPositions
     */
    public Vector3D[] getFiringPositions() {
        return firingPositions;
    }
    /**
     * @param firingPositions the firingPositions to set
     */
    public ProjectileFiringBehavior setFiringPositions(Vector3D[] firingPositions) {
        this.firingPositions = firingPositions;
        return this;
    }//end ProjectileFiringBehavior

    /**
     * @return the projectileFactory
     */
    public ProjectileFactory getProjectileFactory() {
        return projectileFactory;
    }

    /**
     * @param projectileFactory the projectileFactory to set
     */
    public ProjectileFiringBehavior setProjectileFactory(ProjectileFactory projectileFactory) {
        this.projectileFactory = projectileFactory;
        return this;
    }
}//end ProjectileFiringBehavior
