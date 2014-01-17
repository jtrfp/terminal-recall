package org.jtrfp.trcl.obj;

import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.Future;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.ColorProcessor;
import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.DestroysEverythingBehavior;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.ModelingType;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ProjectileFactory {
    private int projectileIndex=0;
    private final TR tr;
    private final Projectile [] projectiles = new Projectile[20];
    private final double projectileSpeed;
    private final Weapon weapon;
    public ProjectileFactory(TR tr, Weapon weapon, ExplosionType explosionType){
    	this.tr=tr;
    	this.weapon=weapon;
    	this.projectileSpeed=weapon.getSpeed()/TR.crossPlatformScalar;
    	Model modelToUse;
    	Future<TextureDescription> t;
  	 Triangle [] tris;
  	 final int damageOnImpact=weapon.getDamage();
    	try{
    	 modelToUse = new Model(false);
   	 final ModelingType modelingType = weapon.getModelingType();
   	 if(modelingType instanceof ModelingType.FlatModelingType){
   	 ModelingType.FlatModelingType mt = (ModelingType.FlatModelingType)modelingType;
   	 Dimension dims = mt.getSegmentSize();
   	 final int laserplaneLength = (int)(dims.getWidth()/TR.crossPlatformScalar);
   	 final int laserplaneWidth = (int)(dims.getHeight()/TR.crossPlatformScalar);
   	 t = tr.getResourceManager().getRAWAsTexture(
   		mt.getRawFileName(),
   		tr.getDarkIsClearPalette(), 
   		GammaCorrectingColorProcessor.singleton,
   		tr.getGPU().getGl());
   	 tris =(Triangle.quad2Triangles(new double[]{-laserplaneLength/2.,laserplaneLength/2.,laserplaneLength/2.,0}, //X
   		new double[]{0,0,0,0}, new double[]{-laserplaneWidth/2.,-laserplaneWidth/2.,laserplaneWidth/2.,laserplaneWidth/2.}, //YZ
   		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
   	 tris[0].setAlphaBlended(true);
   	 tris[1].setAlphaBlended(true);
   	 modelToUse.addTriangles(tris);
 	 modelToUse.finalizeModel();
   	 for(int i=0; i<projectiles.length; i++){
   	    projectiles[i]=new ProjectileObject3D(tr,modelToUse, damageOnImpact, explosionType);}
    	}//end if(isLaser)
   	 else if(modelingType instanceof ModelingType.BillboardModelingType){
   	     final ModelingType.BillboardModelingType mt = (ModelingType.BillboardModelingType)modelingType;
   	     final Future<Texture> [] frames = new Future[mt.getRawFileNames().length];
   	     final String [] fileNames = mt.getRawFileNames();
   	     final ResourceManager mgr = tr.getResourceManager();
   	     final Color [] pal = tr.getGlobalPalette();
   	     ColorProcessor proc = GammaCorrectingColorProcessor.singleton;
   	     GL3 gl = tr.getGPU().getGl();
   	     for(int i=0; i<frames.length;i++){
   		 frames[i]=(Future)mgr.getRAWAsTexture(fileNames[i], pal, proc, gl);
   	     }//end for(frames)
   	     Future<TextureDescription> tex = new DummyFuture<TextureDescription>(new AnimatedTexture(new Sequencer(mt.getTimeInMillisPerFrame(),frames.length,false), frames));
	     ProjectileBillboard bb = new ProjectileBillboard(tr,weapon,tex,ExplosionType.Billow);
	     for(int i=0; i<projectiles.length; i++){
	   	    projectiles[i]=bb;}
   	 }//end (billboard)
   	 else if(modelingType instanceof ModelingType.BINModelingType){
   	     final ModelingType.BINModelingType mt = (ModelingType.BINModelingType)modelingType;
   	     modelToUse = tr.getResourceManager().getBINModel(mt.getBinFileName(), tr.getGlobalPalette(), tr.getGPU().getGl());
   	     for(int i=0; i<projectiles.length; i++){
   		 projectiles[i]=new ProjectileObject3D(tr,modelToUse, damageOnImpact, explosionType);
   		 }
   	 }//end BIN Modeling Type
   	 else{throw new RuntimeException("Unhandled ModelingType: "+modelingType.getClass().getName());}
    	}//end try
	catch(Exception e){e.printStackTrace();}
    	//CLAUSE: FFF needs destroysEvertyhing behavior
    	if(weapon==Weapon.DAM){
    	for(int i=0; i<projectiles.length; i++){
		 ((WorldObject)projectiles[i]).addBehavior(new DestroysEverythingBehavior());
		 }
    	}//end if(DAM)
       }//end constructor(...)
    public Projectile fire(double[] ds, Vector3D heading) {
	final Projectile result = projectiles[projectileIndex];
	result.destroy();
	result.reset(ds, heading.scalarMultiply(projectileSpeed));
	tr.getWorld().add((WorldObject)result);
	projectileIndex++;
	projectileIndex%=projectiles.length;
	return result;
    }//end fire(...)
    public Projectile[] getProjectiles() {
	return projectiles;
    }
    public Weapon getWeapon() {
	return weapon;
    }
}//end ProjectileFactory
