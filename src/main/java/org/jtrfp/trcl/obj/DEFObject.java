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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.trcl.beh.AdjustAltitudeToPlayerBehavior;
import org.jtrfp.trcl.beh.AutoFiring;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.Bobbing;
import org.jtrfp.trcl.beh.BuzzByPlayerSFX;
import org.jtrfp.trcl.beh.CollidesWithPlayer;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CustomDeathBehavior;
import org.jtrfp.trcl.beh.CustomPlayerWithinRangeBehavior;
import org.jtrfp.trcl.beh.DamageTrigger;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.beh.DamagedByCollisionWithDEFObject;
import org.jtrfp.trcl.beh.DamagedByCollisionWithPlayer;
import org.jtrfp.trcl.beh.DamagedByCollisionWithSurface;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.HorizAimAtPlayerBehavior;
import org.jtrfp.trcl.beh.LeavesPowerupOnDeathBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.PositionLimit;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.RandomSFXPlayback;
import org.jtrfp.trcl.beh.ResetsRandomlyAfterDeath;
import org.jtrfp.trcl.beh.SmartPlaneBehavior;
import org.jtrfp.trcl.beh.SpawnsRandomSmoke;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior.SpinMode;
import org.jtrfp.trcl.beh.SteadilyRotating;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.beh.TunnelRailed;
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.phy.PulledDownByGravityBehavior;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.BINFile.AnimationControl;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition.EnemyLogic;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.gpu.BINFileExtractor;
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.gpu.BufferedModelTarget;
import org.jtrfp.trcl.gpu.InterpolatedAnimatedModelSource;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.RotatedModelSource;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class DEFObject extends WorldObject {
    private final double boundingHeight, boundingWidth;
    private HitBox [] hitBoxes;
    private WorldObject ruinObject;
    private final EnemyLogic logic;
    private final EnemyDefinition def;
    private boolean mobile,canTurn,foliage,boss,
    		    shieldGen,isRuin,spinCrash,ignoringProjectiles;
    private Anchoring anchoring;
    private RotatedModelSource              rotatedModelSource;
    public static final String [] BIG_EXP_SOUNDS = new String[]{"EXP3.WAV","EXP4.WAV","EXP5.WAV"};
    public static final String [] MED_EXP_SOUNDS = new String[]{"EXP1.WAV","EXP2.WAV"};
    
public DEFObject(final TR tr,Model model, EnemyDefinition def, EnemyPlacement pl){
    super(tr,model);
    this.def=def;
    Vector3D max = Vector3D.ZERO;
    if(model!=null)
	max = model.getMaximumVertexDims();
    else
	max = new Vector3D(TR.legacy2Modern(def.getBoundingBoxRadius()),TR.legacy2Modern(def.getBoundingBoxRadius()),0)
		.scalarMultiply(1./1.5);
    boundingWidth =max.getX();
    boundingHeight=max.getY();
    anchoring=Anchoring.floating;
    logic  =def.getLogic();
    mobile =true;
    canTurn=true;
    foliage=false;
    boss   =def.isObjectIsBoss();
    
    final int    numHitBoxes = def.getNumNewHBoxes();
    final int [] rawHBoxData = def.getHboxVertices();
    if(numHitBoxes!=0){
	final HitBox [] boxes = new HitBox[numHitBoxes];
	for(int i=0; i<numHitBoxes; i++){
		final HitBox hb = new HitBox();
		hb.setVertexID(rawHBoxData[i*2]);
		hb.setSize    (rawHBoxData[i*2+1] / TR.crossPlatformScalar);
		boxes[i]=hb;
	    }//end for(boxes)
	setHitBoxes(boxes);
    }//end if(hitboxes)
    //Default Direction
    setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));
    boolean customExplosion=false;
    this.setModelOffset(
	    TR.legacy2Modern(def.getPivotX()), 
	    TR.legacy2Modern(def.getPivotY()), 
	    TR.legacy2Modern(def.getPivotZ()));
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    canTurn=false;
    	    anchoring=Anchoring.terrain;
    	    break;
    	case groundTargeting://Ground turrets
    	    {mobile=false;
    	    canTurn=true;
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	 ProjectileFiringBehavior pfb;
    	    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
	    addBehavior(pfb=new ProjectileFiringBehavior().
		    setProjectileFactory(tr.getResourceManager().
			    getProjectileFactories()[def.getWeapon().ordinal()]).
			    setFiringPositions(getModelSource(),firingVertices));
	    try{pfb.addSupply(9999999);}catch(SupplyNotNeededException e){}
	    addBehavior(new AutoFiring().
	     setProjectileFiringBehavior(pfb).
	     setPatternOffsetMillis((int)(Math.random()*2000)).
	     setMaxFiringDistance(TR.mapSquareSize*3).
	     setSmartFiring(false).
	     setMaxFireVectorDeviation(.5).
	     setTimePerPatternEntry(500));
	    anchoring=Anchoring.terrain;
    	    break;}
    	case flyingDumb:
    	    canTurn=false;
    	    break;
    	case groundTargetingDumb:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    anchoring=Anchoring.terrain;
    	    break;
    	case flyingSmart:
    	    smartPlaneBehavior(tr,def,false);
    	    break;
    	case bankSpinDrill:
    	    unhandled(def);
    	    break;
    	case sphereBoss:
    	    projectileFiringBehavior();
    	    mobile=true;
    	    break;
    	case flyingAttackRetreatSmart:
    	    smartPlaneBehavior(tr,def,false);
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    break;
    	case splitShipSmart://TODO
    	    smartPlaneBehavior(tr,def,false);
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    break;
    	case groundStaticRuin://Destroyed object is replaced with another using SimpleModel i.e. weapons bunker
    	    mobile=false;
    	    canTurn=false;
    	    anchoring=Anchoring.terrain;
    	    break;
    	case targetHeadingSmart:
    	    mobile=false;//Belazure's crane bots
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    projectileFiringBehavior();
    	    anchoring=Anchoring.terrain;
    	    break;
    	case targetPitchSmart:
    	    mobile=false;
	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    anchoring=Anchoring.terrain;
	    break;
    	case coreBossSmart:
    	    mobile=false;
    	    projectileFiringBehavior();
    	    break;
    	case cityBossSmart:
    	    mobile=false;
    	    projectileFiringBehavior();
    	    break;
    	case staticFiringSmart:{
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior(); 
    	    try{pfb.addSupply(99999999);}catch(SupplyNotNeededException e){}
    	    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
    	    pfb.
    	     setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]).
    	     setFiringPositions(getModelSource(),firingVertices);
    	    addBehavior(pfb);
    	    addBehavior(new AutoFiring().
    		    setProjectileFiringBehavior(pfb).
    		    setPatternOffsetMillis((int)(Math.random()*2000)).
    		    setMaxFiringDistance(TR.mapSquareSize*8).
    		    setSmartFiring(true));
    	    
    	    mobile=false;
    	    canTurn=false;
    	    break;}
    	case sittingDuck:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case tunnelAttack:{
	    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior();
	    try{pfb.addSupply(99999999);}catch(SupplyNotNeededException e){}
	    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
	    pfb.setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]).
	     setFiringPositions(getModelSource(),firingVertices);
	    addBehavior(pfb);
	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    addBehavior(new AutoFiring().
		    setProjectileFiringBehavior(pfb).
		    setPatternOffsetMillis((int)(Math.random()*2000)).
		    setMaxFiringDistance(TR.mapSquareSize*.2).
		    setSmartFiring(false).
		    setMaxFireVectorDeviation(.3).
		    setTimePerPatternEntry(2000));
	    /*addBehavior(new Bobbing().
		    setPhase(Math.random()).
		    setBobPeriodMillis(10*1000+Math.random()*3000).setAmplitude(2000).
		    setAdditionalHeight(0));*/ //Conflicts with TunnelRailed
	    mobile=false;
	    break;}
    	case takeoffAndEscape:
    	    addBehavior(new MovesByVelocity());
    	    addBehavior((Behavior)(new HasPropulsion().setMinPropulsion(0).setPropulsion(def.getThrustSpeed()/1.2)));
    	    addBehavior(new AccelleratedByPropulsion().setEnable(false));
    	    addBehavior(new VelocityDragBehavior().setDragCoefficient(.86));
    	    addBehavior(new CustomPlayerWithinRangeBehavior(){
    		@Override
    		public void withinRange(){
    		    DEFObject.this.
    		     probeForBehavior(AccelleratedByPropulsion.class).
    		     setThrustVector(Vector3D.PLUS_J).
    		     setEnable(true);
    		}
    	    }).setRange(TR.mapSquareSize*10);
    	    addBehavior(new LoopingPositionBehavior());
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast,BIG_EXP_SOUNDS[(int)(Math.random()*3)]));
    	    customExplosion=true;
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case fallingAsteroid:
    	    anchoring=Anchoring.floating;
    	    fallingObjectBehavior();
    	    customExplosion=true;
    	    addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion,MED_EXP_SOUNDS[(int)(Math.random()*2)]));
    	    //setVisible(false);
    	    //addBehavior(new FallingDebrisBehavior(tr,model));
    	    break;
    	case cNome://Walky bot?
    	    anchoring=Anchoring.terrain;
    	    break;
    	case cNomeLegs://Walky bot?
    	    anchoring=Anchoring.terrain;
    	    break;
    	case cNomeFactory:
    	    mobile=false;
    	    break;
    	case geigerBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    projectileFiringBehavior();
    	    anchoring=Anchoring.terrain;
    	    mobile=false;
    	    break;
    	case volcanoBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
    	    projectileFiringBehavior();
    	    anchoring=Anchoring.terrain;
    	    mobile=false;
    	    break;
    	case volcano://Wat.
    	    unhandled(def);
    	    canTurn=false;
    	    mobile=false;
    	    anchoring=Anchoring.terrain;
    	    break;
    	case missile://Silo?
    	    mobile=false;//TODO
    	    anchoring=Anchoring.terrain;
    	    break;
    	case bob:
    	    addBehavior(new Bobbing().setAdditionalHeight(TR.mapSquareSize*1));
    	    addBehavior(new SteadilyRotating());
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast,MED_EXP_SOUNDS[(int)(Math.random()*2)]));
    	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    customExplosion=true;
	    anchoring=Anchoring.floating;
    	    mobile=false;
    	    canTurn=false;//ironic?
    	    break;
    	case alienBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case canyonBoss1:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case lavaMan://Also terraform-o-bot
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case arcticBoss:
    	    //ARTIC / Ymir. Hangs from ceiling.
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
	    anchoring=Anchoring.ceiling;
    	    break;
    	case helicopter://TODO
    	    break;
    	case tree:
    	    canTurn=false;
    	    mobile=false;
    	    foliage=true;
    	    anchoring=Anchoring.terrain;
    	    break;
    	case ceilingStatic:
    	    canTurn=false;
    	    mobile=false;
    	    setTop(Vector3D.MINUS_J);
    	    anchoring=Anchoring.ceiling;
    	    break;
    	case bobAndAttack:{
    	    addBehavior(new SteadilyRotating().setRotationPhase(2*Math.PI*Math.random()));
    	    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior(); 
    	    try{pfb.addSupply(99999999);}catch(SupplyNotNeededException e){}
    	    pfb.setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
    	    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
    	    pfb.setFiringPositions(getModelSource(),firingVertices);
    	    addBehavior(pfb);//Bob and attack don't have the advantage of movement, so give them the advantage of range.
    	    addBehavior(new AutoFiring().
    		    setProjectileFiringBehavior(pfb).
    		    setPatternOffsetMillis((int)(Math.random()*2000)).
    		    setMaxFiringDistance(TR.mapSquareSize*17).
    		    setSmartFiring(true));
    	    addBehavior(new Bobbing().
    		    setPhase(Math.random()).
    		    setBobPeriodMillis(10*1000+Math.random()*3000));
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast,BIG_EXP_SOUNDS[(int)(Math.random()*3)]));
    	    
    	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    customExplosion=true;
    	    mobile=false;
    	    canTurn=false;
    	    anchoring=Anchoring.floating;
    	    break;}
    	case forwardDrive:
    	    canTurn=false;
    	    anchoring=Anchoring.terrain;
    	    break;
    	case fallingStalag:
    	    fallingObjectBehavior();
    	    customExplosion=true;
	    addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion,MED_EXP_SOUNDS[(int)(Math.random()*2)]));
    	    //canTurn=false;
    	    //mobile=false;
    	    anchoring=Anchoring.floating;
    	    break;
    	case attackRetreatBelowSky:
    	    smartPlaneBehavior(tr,def,false);
    	    anchoring=Anchoring.floating;
    	    break;
    	case attackRetreatAboveSky:
    	    smartPlaneBehavior(tr,def,true);
    	    anchoring=Anchoring.floating;
    	    break;
    	case bobAboveSky:
    	    addBehavior(new Bobbing().setAdditionalHeight(TR.mapSquareSize*5));
	    addBehavior(new SteadilyRotating());
	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    mobile=false;
	    canTurn=false;
	    anchoring=Anchoring.floating;
	    break;
    	case factory:
    	    canTurn=false;
    	    mobile=false;
    	    anchoring=Anchoring.floating;
    	    break;
    	}//end switch(logic)
    ///////////////////////////////////////////////////////////
    //Position Limit
     {final PositionLimit posLimit = new PositionLimit();
     posLimit.getPositionMaxima()[1]=tr.getWorld().sizeY;
     posLimit.getPositionMinima()[1]=-tr.getWorld().sizeY;
     addBehavior(posLimit);}
     
    if(anchoring==Anchoring.terrain){
	addBehavior(new CustomDeathBehavior(new Runnable(){
	    @Override
	    public void run(){
		tr.getGame().getCurrentMission().notifyGroundTargetDestroyed();
	    }
	}));
	addBehavior(new TerrainLocked());
	}
    else if(anchoring==Anchoring.ceiling){
	addBehavior(new TerrainLocked().setLockedToCeiling(true));
    }
    else addBehavior(new CustomDeathBehavior(new Runnable(){
	    @Override
	    public void run(){
		tr.getGame().getCurrentMission().notifyAirTargetDestroyed();
	    }//end run()
	}));
    //Misc
    addBehavior(new TunnelRailed(tr));//Centers in tunnel when appropriate
    addBehavior(new DeathBehavior());
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()+(spinCrash?16:0)).setMaxHealth(pl.getStrength()+(spinCrash?16:0)).setEnable(!boss));
    setActive(!boss);
    addBehavior(new DamagedByCollisionWithDEFObject());
    if(!foliage)addBehavior(new DebrisOnDeathBehavior());
    else{addBehavior(new CustomDeathBehavior(new Runnable(){
	@Override
	public void run(){
	    tr.getGame().getCurrentMission().notifyFoliageDestroyed();
	}}));}
    if(canTurn||boss){
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior()).setDragCoefficient(.86);
	addBehavior(new AutoLeveling());
    }
    if(foliage){
	addBehavior(new ExplodesOnDeath(ExplosionType.Billow));
    }else if((anchoring == Anchoring.terrain) && !customExplosion){
	addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion,BIG_EXP_SOUNDS[(int)(Math.random()*3)]));
    }else if(!customExplosion){
	addBehavior(new ExplodesOnDeath(ExplosionType.Blast,MED_EXP_SOUNDS[(int)(Math.random()*2)]));
    }
    if(mobile){
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new VelocityDragBehavior());
	
	if(anchoring==Anchoring.terrain){}
	else 	{//addBehavior(new BouncesOffSurfaces().setReflectHeading(false));
	    	addBehavior(new CollidesWithTerrain().setAutoNudge(true).setNudgePadding(40000));
	    	}
	probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	probeForBehavior(Propelled.class).setMinPropulsion(0);
	probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed()/1.2);
	
	addBehavior(new LoopingPositionBehavior());
    	}//end if(mobile)
    if(def.getPowerup()!=null && Math.random()*100. < def.getPowerupProbability()){
	addBehavior(new LeavesPowerupOnDeathBehavior(def.getPowerup()));}
    addBehavior(new CollidesWithPlayer());
    addBehavior(new DamagedByCollisionWithPlayer(8024,250));
    
    proposeRandomYell();
    }//end DEFObject

@Override
public void destroy(){
    if(ruinObject!=null){
	//Give the ruinObject is own position because it is sharing positions with the original WorldObject, 
	//which is going to be sent to xyz=Double.INFINITY soon.
	ruinObject.setPosition(Arrays.copyOf(ruinObject.getPosition(), 3));
	ruinObject.setActive(true);}
    super.destroy();
}

private void proposeRandomYell(){
    final String sfxFile = def.getBossYellSFXFile();
    if(sfxFile != null && !sfxFile.toUpperCase().contentEquals("NULL")){
	final SoundTexture soundTexture = getTr().getResourceManager().soundTextures.get(sfxFile);
	final RandomSFXPlayback randomSFXPlayback = new RandomSFXPlayback()
	    .setSoundTexture(soundTexture)
	    .setDisableOnDeath(true)
	    .setVolumeScalar(SoundSystem.DEFAULT_SFX_VOLUME*1.5);
	addBehavior(randomSFXPlayback);
    }//end if(!NULL)
}//end proposeRandomYell()

private void projectileFiringBehavior(){
    ProjectileFiringBehavior pfb;
    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
	    addBehavior(pfb=new ProjectileFiringBehavior().
		    setProjectileFactory(getTr().getResourceManager().
			    getProjectileFactories()[def.getWeapon().ordinal()]).setFiringPositions(getModelSource(),firingVertices)
	    );

	    final String fireSfxFile = def.getBossFireSFXFile();
	    if(!fireSfxFile.toUpperCase().contentEquals("NULL"))
		pfb.setFiringSFX(getTr().getResourceManager().soundTextures.get(fireSfxFile));
	    try{pfb.addSupply(99999999);}catch(SupplyNotNeededException e){}
    final AutoFiring af;
    addBehavior(af=new AutoFiring().
	    setProjectileFiringBehavior(pfb).
	    setPatternOffsetMillis((int)(Math.random()*2000)).
	    setMaxFiringDistance(TR.mapSquareSize*5).
	    setSmartFiring(true).
	    setMaxFireVectorDeviation(2.).
	    setTimePerPatternEntry(!boss?2000:350));
    if(boss)af.setFiringPattern(new boolean []{true,true,true,true,false,false,true,false}).setAimRandomness(.07);
}

private void unhandled(EnemyDefinition def){
    System.err.println("UNHANDLED DEF LOGIC: "+def.getLogic()+". MODEL="+def.getComplexModelFile()+" DESC="+def.getDescription());
}

private void fallingObjectBehavior(){
    canTurn=false;
    mobile=false;//Technically wrong but propulsion is unneeded.
    //addBehavior(new PulledDownByGravityBehavior());
    final MovesByVelocity mbv = new MovesByVelocity();
    mbv.setVelocity(new Vector3D(3500,-100000,5000));
    addBehavior(mbv);
    //addBehavior(new VelocityDragBehavior().setDragCoefficient(.99)); // For some reason it falls like pine tar
    addBehavior(new DamageableBehavior().setMaxHealth(10).setHealth(10));
    addBehavior(new DeathBehavior());
    addBehavior(new CollidesWithTerrain().setIgnoreCeiling(true));
    addBehavior(new DamagedByCollisionWithSurface());
    addBehavior(new RotationalMomentumBehavior()
    	.setEquatorialMomentum(.01).setLateralMomentum(.02).setPolarMomentum(.03));
    {final DEFObject thisObject = this;
    final TR thisTr = getTr();
    addBehavior(new ResetsRandomlyAfterDeath()
    	.setMinWaitMillis(1000)
    	.setMaxWaitMillis(5000)
    	.setRunOnReset(new Runnable(){
    	    @Override
    	    public void run(){
    		final Vector3D centerPos = thisObject.probeForBehavior(DeathBehavior.class).getLocationOfLastDeath();
    		thisObject.probeForBehavior(MovesByVelocity.class).setVelocity(new Vector3D(7000,-200000,1000));
    		final double [] pos = thisObject.getPosition();
    		pos[0]=centerPos.getX()+Math.random()*TR.mapSquareSize*3-TR.mapSquareSize*1.5;
    		pos[1]=thisTr.getWorld().sizeY/2+thisTr.getWorld().sizeY*(Math.random())*.3;
    		pos[2]=centerPos.getZ()+Math.random()*TR.mapSquareSize*3-TR.mapSquareSize*1.5;
    		thisObject.notifyPositionChange();
    	    }//end run()
    	}));}
}

private void possibleSpinAndCrashOnDeath(double probability, final EnemyDefinition def){
    spinCrash=Math.random()<probability;
    if(spinCrash){
    final DamageTrigger spinAndCrash = new DamageTrigger(){
	@Override
	public void healthBelowThreshold(){// Spinout and crash
	    final WorldObject 	parent 	= getParent();
	    if(probeForBehavior(DamageableBehavior.class).getHealth()<1)
		return;//No point; already dying.
	    //Trigger small boom
	    final TR tr = parent.getTr();
	    tr.soundSystem.get().getPlaybackFactory().
	     create(tr.getResourceManager().soundTextures.get("EXP2.WAV"), new double[]{.5*SoundSystem.DEFAULT_SFX_VOLUME*2,.5*SoundSystem.DEFAULT_SFX_VOLUME*2});
	    
	    addBehavior(new PulledDownByGravityBehavior().setEnable(true));
	    probeForBehavior(DamagedByCollisionWithSurface.class).setEnable(true);
	    probeForBehavior(CollidesWithTerrain.class).setNudgePadding(0);
	    probeForBehavior(DamageableBehavior.class).setAcceptsProjectileDamage(false);
	    probeForBehavior(ExplodesOnDeath.class).setExplosionType(ExplosionType.BigExplosion).setExplosionSound(BIG_EXP_SOUNDS[(int)(Math.random()*3)]);
	    if(def.getThrustSpeed()<800000){
		probeForBehavior(HasPropulsion.class).setPropulsion(0);
		probeForBehavior(VelocityDragBehavior.class).setEnable(false);
		}
	    //Catastrophy
	    final double spinSpeedCoeff=Math.max(def.getThrustSpeed()!=0?def.getThrustSpeed()/1600000:.3,.4);
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.LATERAL).setSpinAccelleration(.009*spinSpeedCoeff));
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.EQUATORIAL).setSpinAccelleration(.006*spinSpeedCoeff));
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.POLAR).setSpinAccelleration(.007*spinSpeedCoeff));
	    //TODO: Sparks, and other fun stuff.
	    addBehavior(new SpawnsRandomExplosionsAndDebris(parent.getTr()));
	    addBehavior(new SpawnsRandomSmoke(parent.getTr()));
	}//end healthBelowThreshold
    }.setThreshold(2048);
    addBehavior(new DamagedByCollisionWithSurface().setCollisionDamage(65535).setEnable(false));
    addBehavior(spinAndCrash);}
}

private void possibleBobbingSpinAndCrashOnDeath(double probability, EnemyDefinition def){
    possibleSpinAndCrashOnDeath(probability,def);
	    if(spinCrash){
		addBehavior(new CollidesWithTerrain());
		addBehavior(new MovesByVelocity()).setEnable(false);
		addBehavior(new HasPropulsion()).setEnable(false);
		addBehavior(new AccelleratedByPropulsion()).setEnable(false);
		addBehavior(new VelocityDragBehavior()).setEnable(false);
		addBehavior(new RotationalMomentumBehavior()).setEnable(false);
		addBehavior(new RotationalDragBehavior()).setDragCoefficient(.86);
		final DamageTrigger spinAndCrashAddendum = new DamageTrigger(){
		@Override
		public void healthBelowThreshold(){
		    final WorldObject 	parent 	= getParent();
		    parent.probeForBehavior(MovesByVelocity.class).setEnable(true);
		parent.probeForBehavior(HasPropulsion.class).setEnable(true);
		parent.probeForBehavior(AccelleratedByPropulsion.class).setEnable(true);
		parent.probeForBehavior(VelocityDragBehavior.class).setEnable(true);
		parent.probeForBehavior(RotationalMomentumBehavior.class).setEnable(true);
		
		    parent.probeForBehavior(SteadilyRotating.class).setEnable(false);
		    parent.probeForBehavior(Bobbing.class).setEnable(false);
		   // parent.getBehavior().probeForBehavior(AutoFiring.class).setBerzerk(true)
		   // 	.setFiringPattern(new boolean[]{true}).setTimePerPatternEntry(100);
		}};
		addBehavior(spinAndCrashAddendum);
	    }//end if(spinCrash)
}//end possibleBobbingSpinAndCrashOnDeath

private void smartPlaneBehavior(TR tr, EnemyDefinition def, boolean retreatAboveSky){
    final HorizAimAtPlayerBehavior haapb =new HorizAimAtPlayerBehavior(tr.getGame().getPlayer()).setLeftHanded(Math.random()>=.5);
    addBehavior(haapb);
    final AdjustAltitudeToPlayerBehavior aatpb = new AdjustAltitudeToPlayerBehavior(tr.getGame().getPlayer()).setAccelleration(1000);
    addBehavior(aatpb);
    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior().setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
    try{pfb.addSupply(99999999);}catch(SupplyNotNeededException e){}
    Integer [] firingVertices = Arrays.copyOf(def.getFiringVertices(),def.getNumRandomFiringVertices());
	    pfb.setFiringPositions(getModelSource(),firingVertices);
    addBehavior(pfb);
    
    possibleSpinAndCrashOnDeath(.4,def);
    if(spinCrash){
		final DamageTrigger spinAndCrashAddendum = new DamageTrigger(){
		@Override
		public void healthBelowThreshold(){
		    final WorldObject 	parent 	= getParent();
		    final HasPropulsion hp 	= probeForBehavior(HasPropulsion.class);
		    hp.setPropulsion(hp.getPropulsion()/1);
		    probeForBehavior(AutoLeveling.class).
		    	setLevelingAxis(LevelingAxis.HEADING).
		    	setLevelingVector(Vector3D.MINUS_J).setRetainmentCoeff(.985,.985,.985);
		}};
		addBehavior(spinAndCrashAddendum);
	    }//end if(spinCrash)
    AccelleratedByPropulsion escapeProp=null;
    if(retreatAboveSky){
      escapeProp = new AccelleratedByPropulsion();
      escapeProp.setThrustVector(new Vector3D(0,.1,0)).setEnable(false);
      addBehavior(escapeProp);}
    final AutoFiring afb = new AutoFiring();
    afb.setMaxFireVectorDeviation(.3);
    afb.setFiringPattern(new boolean [] {true,false,false,false,true,true,false});
    afb.setTimePerPatternEntry((int)(400+Math.random()*300));
    afb.setPatternOffsetMillis((int)(Math.random()*1000));
    afb.setProjectileFiringBehavior(pfb);
    addBehavior(afb);
    final SpinAccellerationBehavior sab = (SpinAccellerationBehavior)new SpinAccellerationBehavior().setEnable(false);
    addBehavior(sab);
    addBehavior(new SmartPlaneBehavior(haapb,afb,sab,aatpb,escapeProp,retreatAboveSky));
    addBehavior(new BuzzByPlayerSFX().setBuzzSounds(new String[]{
		    "FLYBY56.WAV","FLYBY60.WAV","FLYBY80.WAV","FLYBY81.WAV"}));
}//end smartPlaneBehavior()

@Override
public void setTop(Vector3D top){
    super.setTop(top);
}
public void setRuinObject(DEFObject ruin) {
    ruinObject=ruin;
}

/**
 * @return the logic
 */
public EnemyLogic getLogic() {
    return logic;
}

/**
 * @return the mobile
 */
public boolean isMobile() {
    return mobile;
}

/**
 * @return the canTurn
 */
public boolean isCanTurn() {
    return canTurn;
}

/**
 * @return the foliage
 */
public boolean isFoliage() {
    return foliage;
}

/**
 * @return the boss
 */
public boolean isBoss() {
    return boss;
}

/**
 * @return the groundLocked
 */
public boolean isGroundLocked() {
    return anchoring==Anchoring.terrain;
}

/**
 * @return the ignoringProjectiles
 */
public boolean isIgnoringProjectiles() {
    return ignoringProjectiles;
}

/**
 * @param ignoringProjectiles the ignoringProjectiles to set
 */
public void setIgnoringProjectiles(boolean ignoringProjectiles) {
    this.ignoringProjectiles = ignoringProjectiles;
}

public void setIsRuin(boolean b) {
    isRuin=b;
}

/**
 * @return the isRuin
 */
public boolean isRuin() {
    return isRuin;
}

/**
 * @param isRuin the isRuin to set
 */
public void setRuin(boolean isRuin) {
    this.isRuin = isRuin;
}

/**
 * @return the shieldGen
 */
public boolean isShieldGen() {
    return shieldGen;
}

/**
 * @param shieldGen the shieldGen to set
 */
public void setShieldGen(boolean shieldGen) {
    this.shieldGen = shieldGen;
}

@Override
public String toString(){
    return "DEFObject Model="+getModel().getDebugName()+" Logic="+logic+" Anchoring="+anchoring+
	    "\n\tmobile="+mobile+" isRuin="+isRuin+" foliage="+foliage+" boss="+boss+" spinCrash="+spinCrash+
	    "\n\tignoringProjectiles="+ignoringProjectiles+"\n"+
	    "\tRuinObject="+ruinObject;
}

enum Anchoring{
    floating(false),
    terrain(true),
    ceiling(true);
    
    private final boolean locked;
    private Anchoring(boolean locked){
	this.locked=locked;
    }
    
    public boolean isLocked()
     {return locked;}
 }//end Anchoring

public BasicModelSource getModelSource(){
    if(rotatedModelSource==null){//Assemble our decorator sandwich.
	final String complexModel = def.getComplexModelFile();
	if(complexModel==null)
	    return null;
	final ResourceManager rm = getTr().getResourceManager();
	BasicModelSource      bmt = null;
	final BINFileExtractor bfe   = new BINFileExtractor(rm);
	bfe.setDefaultTexture(getTr().gpu.get().textureManager.get().getFallbackTexture());
	try{bmt= new BufferedModelTarget();
	    bfe.extract(rm.getBinFileModel(def.getComplexModelFile()), (BufferedModelTarget)bmt);}
	catch(UnrecognizedFormatException e){//Animated BIN
	    try{final AnimationControl ac = rm.getAnimationControlBIN(def.getComplexModelFile());
	        List<String> bins = ac.getBinFiles();
	        bmt = new InterpolatedAnimatedModelSource();
	        for(String name:bins){
	            BufferedModelTarget bufferedTarget = new BufferedModelTarget();
	            bfe.extract(rm.getBinFileModel(name),bufferedTarget);
	            ((InterpolatedAnimatedModelSource)bmt).addModelFrame(bufferedTarget);}
	        ((InterpolatedAnimatedModelSource)bmt).setDelayBetweenFramesMillis(ac.getDelay());
		}
	    catch(Exception ee){ee.printStackTrace();}
	}
	catch(Exception e){e.printStackTrace();}
	rotatedModelSource           = new RotatedModelSource(bmt);
	rotatedModelSource.setRotatableSource  (this);
	}
    return rotatedModelSource;
}

/**
 * @return the boundingHeight
 */
public double getBoundingHeight() {
    return boundingHeight;
}

/**
 * @return the boundingWidth
 */
public double getBoundingWidth() {
    return boundingWidth;
}

public static class HitBox{
    private int vertexID;
    private double size;
    public int getVertexID() {
        return vertexID;
    }
    public void setVertexID(int vertexID) {
        this.vertexID = vertexID;
    }
    public double getSize() {
        return size;
    }
    public void setSize(double size) {
        this.size = size;
    }
 }//end HitBox

public HitBox[] getHitBoxes() {
    return hitBoxes;
}

public void setHitBoxes(HitBox[] hitBoxes) {
    this.hitBoxes = hitBoxes;
}
}//end DEFObject
