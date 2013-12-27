package org.jtrfp.trcl.objects;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.objects.Explosion.ExplosionType;

public class ProjectileFactory {
    private int projectileIndex=0;
    private final TR tr;
    private final ProjectileObject [] projectiles = new ProjectileObject[20];
    private final double projectileSpeed;//40
    private final ExplosionType explosionType;
    private final double damageOnImpact;
    public ProjectileFactory(TR tr, Model modelToUse, double projectileSpeed, double damageOnImpact, ExplosionType explosionType){
    	this.tr=tr;
    	this.projectileSpeed=projectileSpeed;
    	this.explosionType=explosionType;
    	this.damageOnImpact=damageOnImpact;
    	try{
    	 int i;
    	for(i=0; i<projectiles.length; i++){
    	    projectiles[i]=new ProjectileObject(tr,modelToUse, damageOnImpact, explosionType);}
    	 /*
    	 Model m = new Model(false);
    	 final Color [] stdPalette = tr.getGlobalPalette();
    	 
    	 //White lasers
    	 TextureDescription t = tr.getResourceManager().getRAWAsTexture(
    		"NEWLASER.RAW", 
    		stdPalette, 
    		GammaCorrectingColorProcessor.singleton, 
    		tr.getGPU().getGl());
    	 Triangle [] tris =(Triangle.quad2Triangles(new double[]{0,SEG_LEN,SEG_LEN,0}, //X
    		new double[]{0,0,0,0}, new double[]{0,0,SEG_LEN*7,SEG_LEN*7}, //YZ
    		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
    	 tris[0].setAlphaBlended(true);
    	 tris[1].setAlphaBlended(true);
    	 m.addTriangles(tris);
    	 m.finalizeModel();
    	 for(i=0; i<projectiles.length; i++){
    	    projectiles[i]=new ProjectileObject(tr,m, damageOnImpact, explosionType);}
    	 
    	 for(i=0; i<redLasers.length; i++){
     	    redLasers[i]=new ProjectileObject(tr,m);}
    	 */
	 }
	catch(Exception e){e.printStackTrace();}
       }
    /*
    public LaserProjectile triggerRedLaser(Vector3D location,Vector3D heading){
	redLaserIndex++;redLaserIndex%=redLasers.length;
	final LaserProjectile result = redLasers[redLaserIndex];
	result.destroy();
	result.reset(location, heading.scalarMultiply(projectileSpeed));
	tr.getWorld().add(result);
	return result;
	}
	*/
    public ProjectileObject fire(Vector3D firingPosition, Vector3D heading) {
	final ProjectileObject result = projectiles[projectileIndex];
	result.destroy();
	result.reset(firingPosition, heading.scalarMultiply(projectileSpeed));
	tr.getWorld().add(result);
	projectileIndex++;
	projectileIndex%=projectiles.length;
	return result;
	
    }
}//end ProjectileFactory
