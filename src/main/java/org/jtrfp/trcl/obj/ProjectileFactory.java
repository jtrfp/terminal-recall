package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileFactory {
    private int projectileIndex=0;
    private final TR tr;
    private final ProjectileObject [] projectiles = new ProjectileObject[20];
    private final double projectileSpeed;
    public ProjectileFactory(TR tr, Model modelToUse, double projectileSpeed, int damageOnImpact, ExplosionType explosionType){
    	this.tr=tr;
    	this.projectileSpeed=projectileSpeed;
    	try{
    	 int i;
    	for(i=0; i<projectiles.length; i++){
    	    projectiles[i]=new ProjectileObject(tr,modelToUse, damageOnImpact, explosionType);}
	 }
	catch(Exception e){e.printStackTrace();}
       }//end constructor(...)
    public ProjectileObject fire(Vector3D firingPosition, Vector3D heading) {
	final ProjectileObject result = projectiles[projectileIndex];
	result.destroy();
	result.reset(firingPosition, heading.scalarMultiply(projectileSpeed));
	tr.getWorld().add(result);
	projectileIndex++;
	projectileIndex%=projectiles.length;
	return result;
    }//end fire(...)
}//end ProjectileFactory
