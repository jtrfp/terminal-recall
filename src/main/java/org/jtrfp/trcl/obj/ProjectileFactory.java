/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.DestroysEverythingBehavior;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.ModelingType;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class ProjectileFactory {
    private int projectileIndex=0;
    private final TR tr;
    private final Projectile [] projectiles = new Projectile[20];
    private final double projectileSpeed;
    private final Weapon weapon;
    private final SoundTexture soundTexture;
    
    public ProjectileFactory(TR tr, Weapon weapon, ExplosionType explosionType, String debugName){
    	this.tr=tr;
    	this.weapon=weapon;
    	this.projectileSpeed=weapon.getSpeed()/TR.crossPlatformScalar;
    	Model modelToUse;
    	TextureDescription t;
  	 Triangle [] tris;
  	 final int damageOnImpact=weapon.getDamage();
    	try{
    	 modelToUse = new Model(false,tr,"ProjectileFactory."+debugName);
   	 final ModelingType modelingType = weapon.getModelingType();
   	 if(modelingType instanceof ModelingType.FlatModelingType){
   	 ModelingType.FlatModelingType mt = (ModelingType.FlatModelingType)modelingType;
   	 Dimension dims = mt.getSegmentSize();
   	 final int laserplaneLength = (int)(dims.getWidth()/TR.crossPlatformScalar);
   	 final int laserplaneWidth = (int)(dims.getHeight()/TR.crossPlatformScalar);
   	 t = tr.getResourceManager().getRAWAsTexture(
   		tr.config._getGameVersion()!=GameVersion.TV?mt.getF3RawFileName():mt.getTvRawFileName(),
   		tr.getDarkIsClearPaletteVL(), null,
   		false);
   	 final double Y_SLANT=1024;
   	 tris =(Triangle.quad2Triangles(new double[]{-laserplaneLength/2.,laserplaneLength/2.,laserplaneLength/2.,-laserplaneLength/2.}, //X
   		new double[]{0,0,Y_SLANT,Y_SLANT}, new double[]{-laserplaneWidth/2.,-laserplaneWidth/2.,laserplaneWidth/2.,laserplaneWidth/2.}, //YZ
   		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC,Vector3D.ZERO,"ProjectileFactory:"+weapon.toString()));//UVtr
   	 tris[0].setAlphaBlended(true);
   	 tris[1].setAlphaBlended(true);
   	 modelToUse.addTriangles(tris);
 	 //modelToUse.finalizeModel();
   	 //modelToUse = tr.getResourceManager().getBINModel("LASER3.BIN", tr.getGlobalPaletteVL(), null, null);
   	 for(int i=0; i<projectiles.length; i++){
   	    projectiles[i]=new ProjectileObject3D(tr,modelToUse, weapon, explosionType);}
    	}//end if(isLaser)
   	 else if(modelingType instanceof ModelingType.BillboardModelingType){
   	     final ModelingType.BillboardModelingType mt = (ModelingType.BillboardModelingType)modelingType;
   	     final Texture [] frames = new Texture[mt.getRawFileNames().length];
   	     final String [] fileNames = mt.getRawFileNames();
   	     final ResourceManager mgr = tr.getResourceManager();
   	     final ColorPaletteVectorList pal = tr.getGlobalPaletteVL();
   	     GL3 gl = tr.gpu.get().getGl();
   	     for(int i=0; i<frames.length;i++){
   		 frames[i]=(Texture)mgr.getRAWAsTexture(fileNames[i], pal, null, false);
   	     }//end for(frames)
   	  TextureDescription tex = new AnimatedTexture(new Sequencer(mt.getTimeInMillisPerFrame(),frames.length,false), frames);
	     for(int i=0; i<projectiles.length; i++){
	   	    projectiles[i]=new ProjectileBillboard(tr,weapon,tex,ExplosionType.Billow,debugName);}
   	 }//end (billboard)
   	 else if(modelingType instanceof ModelingType.BINModelingType){
   	     final ModelingType.BINModelingType mt = (ModelingType.BINModelingType)modelingType;
   	     modelToUse = tr.getResourceManager().getBINModel(mt.getBinFileName(), tr.getGlobalPaletteVL(),null, tr.gpu.get().getGl());
   	     for(int i=0; i<projectiles.length; i++){
   		 projectiles[i]=new ProjectileObject3D(tr,modelToUse, weapon, explosionType);
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
    	
    	//Sound
    	String soundFile=null;
    	switch(weapon){
    	//PEW!!!
    	case PAC:
    	{
    	    soundFile="LASER2.WAV";
    	    break;
    	}
    	case ION:
    	{
    	    soundFile="LASER3.WAV";
    	    break;
    	}
    	case RTL:
    	{
    	    soundFile="LASER4.WAV";
    	    break;
    	}
    	case DAM:
    	case redLaser:
    	case blueLaser:
    	case greenLaser:
    	case purpleLaser:
    	case purpleRing:
    	{
    	    soundFile="LASER5.WAV";
    	    break;
    	}
    	//Missile
    	case enemyMissile:
    	case SWT:
    	case MAM:
    	case SAD:
    	{
    	    soundFile="MISSILE.WAV";
    	    break;
    	}
    	//SILENT
    	case purpleBall:
    	case goldBall:
    	case fireBall:
    	case blueFireBall:
    	case bullet:
    	case bossW8:
    	case bossW7:
    	case bossW6:
    	case atomWeapon:
    	default:
    	    break;
    	}//end case()
    	soundTexture = soundFile!=null?tr.getResourceManager().soundTextures.get(soundFile):null;
       }//end constructor(...)
    
    public Projectile fire(double[] newPosition, Vector3D heading, WorldObject objectOfOrigin) {
	return fire(newPosition, heading, objectOfOrigin, true);
    }
    
    public Projectile fire(double[] newPosition, Vector3D heading, WorldObject objectOfOrigin, boolean sumWithProjectorVel) {
	assert !Vect3D.isAnyNaN(newPosition);
	assert heading.getNorm()!=0 && heading.getNorm()!=Double.NaN;
	
	final Projectile result = projectiles[projectileIndex];
	result.destroy();
	final Vector3D newVelocity;
	if(weapon.isSumWithProjectorVel() && sumWithProjectorVel){
	    Vector3D originVelocity = Vector3D.ZERO;
	    try{final Velocible vel = objectOfOrigin.probeForBehavior(Velocible.class);
	    originVelocity = vel.getVelocity();
	    }catch(BehaviorNotFoundException e){}
	    newVelocity = heading.scalarMultiply(projectileSpeed).add(originVelocity);
	}else// !sumWithProjector
	    newVelocity = heading.scalarMultiply(projectileSpeed);
	result.reset(newPosition, newVelocity, objectOfOrigin);
	((WorldObject)result).setTop(objectOfOrigin.getTop());
	tr.getDefaultGrid().add((WorldObject)result);
	if(soundTexture!=null)
	    tr.soundSystem.get().enqueuePlaybackEvent(
		    tr.soundSystem
			    .get()
			    .getPlaybackFactory()
			    .create(soundTexture,
				     (WorldObject)result,
				     tr.mainRenderer.get().getCamera(),
				     (objectOfOrigin instanceof Player?.6:1)*SoundSystem.DEFAULT_SFX_VOLUME));//TODO: Use configuration volume instead
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
