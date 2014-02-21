package org.jtrfp.trcl.obj;

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AdjustAltitudeToPlayerBehavior;
import org.jtrfp.trcl.beh.AutoFiring;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.Bobbing;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CustomPlayerWithinRangeBehavior;
import org.jtrfp.trcl.beh.DamageTrigger;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamagedByCollisionWithGameplayObject;
import org.jtrfp.trcl.beh.DamagedByCollisionWithSurface;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.HorizAimAtPlayerBehavior;
import org.jtrfp.trcl.beh.LeavesPowerupOnDeathBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.PositionLimit;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.ResetsRandomlyAfterDeath;
import org.jtrfp.trcl.beh.SmartPlaneBehavior;
import org.jtrfp.trcl.beh.SpawnsRandomSmoke;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior.SpinMode;
import org.jtrfp.trcl.beh.SteadilyRotating;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.phy.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.phy.PulledDownByGravityBehavior;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition.EnemyLogic;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class DEFObject extends WorldObject {
    private final double boundingRadius;
    private WorldObject ruinObject;
    private final EnemyLogic logic;
    private final EnemyDefinition def;
    private boolean mobile,canTurn,foliage,boss,groundLocked;
    boolean spinCrash=false;
    boolean ignoringProjectiles=false;
public DEFObject(TR tr,Model model, EnemyDefinition def, EnemyPlacement pl){
    super(tr,model);
    this.def=def;
    boundingRadius = TR.legacy2Modern(def.getBoundingBoxRadius())/1.5;
    logic = def.getLogic();
    mobile=true;
    canTurn=true;
    foliage=false;
    boss=def.isObjectIsBoss();
    groundLocked=false;
    boolean customExplosion=false;
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case groundTargeting://Ground turrets
    	    {mobile=false;
    	    canTurn=true;
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    //TODO: def.getFiringVertices() needs actual vertex lookup.
    	 ProjectileFiringBehavior pfb;
	    addBehavior(pfb=new ProjectileFiringBehavior().
		    setProjectileFactory(tr.getResourceManager().
			    getProjectileFactories()[def.getWeapon().ordinal()]).
			    setFiringPositions(new Vector3D[]{new Vector3D(0,0,0)
	    }));
	    pfb.addSupply(9999999);
	    addBehavior(new AutoFiring().
	     setProjectileFiringBehavior(pfb).
	     setPatternOffsetMillis((int)(Math.random()*2000)).
	     setMaxFiringDistance(TR.mapSquareSize*3).
	     setSmartFiring(false).
	     setMaxFireVectorDeviation(.5).
	     setTimePerPatternEntry(500));
    	    break;}
    	case flyingDumb:
    	    canTurn=false;
    	    break;
    	case groundTargetingDumb:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    groundLocked=true;
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
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case splitShipSmart://TODO
    	    smartPlaneBehavior(tr,def,false);
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case groundStaticRuin://Destroyed object is replaced with another using SimpleModel i.e. weapons bunker
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case targetHeadingSmart:
    	    mobile=false;//Belazure's crane bots
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    projectileFiringBehavior();
    	    break;
    	case targetPitchSmart:
    	    mobile=false;
	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
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
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior(); 
    	    pfb.addSupply(99999999);
    	    pfb.setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
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
	    pfb.addSupply(99999999);
	    pfb.setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
	    addBehavior(pfb);
	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    addBehavior(new AutoFiring().
		    setProjectileFiringBehavior(pfb).
		    setPatternOffsetMillis((int)(Math.random()*2000)).
		    setMaxFiringDistance(TR.mapSquareSize*.2).
		    setSmartFiring(false).
		    setMaxFireVectorDeviation(.3).
		    setTimePerPatternEntry(2000));
	    addBehavior(new Bobbing().
		    setPhase(Math.random()).
		    setBobPeriodMillis(10*1000+Math.random()*3000).setAmplitude(2000).
		    setAdditionalHeight(0));
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
    		    DEFObject.this.getBehavior().
    		     probeForBehavior(AccelleratedByPropulsion.class).
    		     setThrustVector(Vector3D.PLUS_J).
    		     setEnable(true);
    		}
    	    }).setRange(TR.mapSquareSize*10);
    	    addBehavior(new LoopingPositionBehavior());
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    	    customExplosion=true;
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case fallingAsteroid:
    	    fallingObjectBehavior();
    	    addBehavior(new RotationalMomentumBehavior()
    	    	.accellerateEquatorialMomentum(1)
    	    	.accellerateLateralMomentum(1)
    	    	.accelleratePolarMomentum(1));
    	    { final DEFObject thisObject = this;
    	    final Vector3D centerPos = new Vector3D(this.getPosition());
    	    final TR thisTr = tr;
    	    addBehavior(new ResetsRandomlyAfterDeath()
    	    	.setMinWaitMillis(100)
    	    	.setMaxWaitMillis(1000)
    	    	.setRunOnReset(new Runnable(){
    	    	    @Override
    	    	    public void run(){
    	    		final double [] pos = thisObject.getPosition();
    	    		pos[0]=centerPos.getX()+Math.random()*TR.mapSquareSize*10;
    	    		pos[1]=thisTr.getWorld().sizeY/1.5;
    	    		pos[2]=centerPos.getZ()+Math.random()*TR.mapSquareSize*10;
    	    		thisObject.notifyPositionChange();
    	    	    }//end run()
    	    	}));}
    	    break;
    	case cNome://Walky bot?
    	    groundLocked=true;
    	    break;
    	case cNomeLegs://Walky bot?
    	    groundLocked=true;
    	    break;
    	case cNomeFactory:
    	    mobile=false;
    	    break;
    	case geigerBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    projectileFiringBehavior();
    	    mobile=false;
    	    break;
    	case volcanoBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    projectileFiringBehavior();
    	    mobile=false;
    	    break;
    	case volcano://Wat.
    	    unhandled(def);
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case missile://Silo?
    	    mobile=false;//TODO
    	    break;
    	case bob:
    	    addBehavior(new Bobbing().setAdditionalHeight(TR.mapSquareSize*1));
    	    addBehavior(new SteadilyRotating());
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    customExplosion=true;
    	    mobile=false;
    	    canTurn=false;//ironic?
    	    break;
    	case alienBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case canyonBoss1:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case lavaMan://Also terraform-o-bot
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case arcticBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    projectileFiringBehavior();
	    mobile=false;
    	    break;
    	case helicopter://TODO
    	    break;
    	case tree:
    	    canTurn=false;
    	    mobile=false;
    	    foliage=true;
    	    break;
    	case ceilingStatic:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case bobAndAttack:{
    	    addBehavior(new SteadilyRotating().setRotationPhase(2*Math.PI*Math.random()));
    	    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior(); 
    	    pfb.addSupply(99999999);
    	    pfb.setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
    	    addBehavior(pfb);//Bob and attack don't have the advantage of movement, so give them the advantage of range.
    	    addBehavior(new AutoFiring().
    		    setProjectileFiringBehavior(pfb).
    		    setPatternOffsetMillis((int)(Math.random()*2000)).
    		    setMaxFiringDistance(TR.mapSquareSize*17).
    		    setSmartFiring(true));
    	    addBehavior(new Bobbing().
    		    setPhase(Math.random()).
    		    setBobPeriodMillis(10*1000+Math.random()*3000));
    	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    	    
    	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    customExplosion=true;
    	    mobile=false;
    	    canTurn=false;
    	    break;}
    	case forwardDrive:
    	    canTurn=false;
    	    groundLocked=true;
    	    break;
    	case fallingStalag:
    	    fallingObjectBehavior();
	    {final DEFObject thisObject = this;
	    final Vector3D centerPos = new Vector3D(this.getPosition());
	    final TR thisTr = tr;
	    addBehavior(new ResetsRandomlyAfterDeath()
	    	.setMinWaitMillis(100)
	    	.setMaxWaitMillis(1000)
	    	.setRunOnReset(new Runnable(){
	    	    @Override
	    	    public void run(){
	    		final double [] pos = thisObject.getPosition();
	    		pos[0]=centerPos.getX()+Math.random()*TR.mapSquareSize*10;
	    		pos[1]=thisTr.getWorld().sizeY/1.5;
	    		pos[2]=centerPos.getZ()+Math.random()*TR.mapSquareSize*10;
	    		thisObject.notifyPositionChange();
	    	    }//end run()
	    	}));}
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case attackRetreatBelowSky:
    	    smartPlaneBehavior(tr,def,false);
    	    break;
    	case attackRetreatAboveSky:
    	    smartPlaneBehavior(tr,def,true);
    	    break;
    	case bobAboveSky:
    	    addBehavior(new Bobbing().setAdditionalHeight(TR.mapSquareSize*5));
	    addBehavior(new SteadilyRotating());
	    possibleBobbingSpinAndCrashOnDeath(.5,def);
	    mobile=false;
	    canTurn=false;
	    break;
    	case factory:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	}//end switch(logic)
    ///////////////////////////////////////////////////////////
    //Position Limit
     {final PositionLimit posLimit = new PositionLimit();
     posLimit.getPositionMaxima()[1]=TR.mapSquareSize*10;
     posLimit.getPositionMinima()[1]=-TR.mapSquareSize;
     addBehavior(posLimit);}
     
    //Misc
    addBehavior(new DeathBehavior());
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()+(spinCrash?16:0)).setMaxHealth(pl.getStrength()+(spinCrash?16:0)).setEnable(!boss));
    setActive(!boss);
    addBehavior(new DamagedByCollisionWithGameplayObject());
    if(!foliage)addBehavior(new DebrisOnDeathBehavior());
    if(canTurn||boss){
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior()).setDragCoefficient(.86);
	addBehavior(new AutoLeveling());
    }
    if(foliage){
	addBehavior(new ExplodesOnDeath(ExplosionType.Billow));
    }else if((!mobile || groundLocked) && !customExplosion){
	addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));
    }else if(!customExplosion){
	addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    }
    if(mobile){
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new VelocityDragBehavior());
	
	if(groundLocked){
	    addBehavior(new TerrainLocked());}
	else 	{addBehavior(new BouncesOffSurfaces().setReflectHeading(false));
	    	addBehavior(new CollidesWithTerrain());
	    	}
	
	getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed()/1.2);
	
	addBehavior(new LoopingPositionBehavior());
    	}//end if(mobile)
    if(boss){bossBehavior(tr,def);}
    if(def.getPowerup()!=null && Math.random()*100. < def.getPowerupProbability()){
	addBehavior(new LeavesPowerupOnDeathBehavior(def.getPowerup()));}
    }//end DEFObject

@Override
public void destroy(){
    if(ruinObject!=null){
	//Give the ruinObject is own position because it is sharing positions with the original WorldObject, 
	//which is going to be sent to xyz=Double.INFINITY soon.
	ruinObject.setPosition(Arrays.copyOf(ruinObject.getPosition(), 3));
	ruinObject.setVisible(true);}
    super.destroy();
}

private void projectileFiringBehavior(){
    ProjectileFiringBehavior pfb;
	    addBehavior(pfb=new ProjectileFiringBehavior().
		    setProjectileFactory(getTr().getResourceManager().
			    getProjectileFactories()[def.getWeapon().ordinal()]).setFiringPositions(new Vector3D[]{
		    new Vector3D(0,0,0)
	    }));
	    pfb.addSupply(99999999);
    final AutoFiring af;
    addBehavior(af=new AutoFiring().
	    setProjectileFiringBehavior(pfb).
	    setPatternOffsetMillis((int)(Math.random()*2000)).
	    setMaxFiringDistance(TR.mapSquareSize*5).
	    setSmartFiring(false).
	    setMaxFireVectorDeviation(2.).
	    setTimePerPatternEntry(!boss?2000:350));
    if(boss)af.setFiringPattern(new boolean []{true,true,true,true,false,false,true,false}).setAimRandomness(.07);
}

private void unhandled(EnemyDefinition def){
    System.err.println("UNHANDLED DEF LOGIC: "+def.getLogic()+". MODEL="+def.getComplexModelFile()+" DESC="+def.getDescription());
}

private void bossBehavior(TR tr, EnemyDefinition def){//Don't include hitzones for aiming.
    if(!def.getComplexModelFile().toUpperCase().contains("HITZO")){
	addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	setIgnoringProjectiles(true);
    }
}//end bossBehavior(...)

private void fallingObjectBehavior(){
    canTurn=false;
    addBehavior(new PulledDownByGravityBehavior());
    addBehavior(new DamageableBehavior().setHealth(1));
    addBehavior(new CollidesWithTerrain());
}

private void possibleSpinAndCrashOnDeath(double probability, final EnemyDefinition def){
    spinCrash=Math.random()<probability;//40%
    if(spinCrash){
    final DamageTrigger spinAndCrash = new DamageTrigger(){
	@Override
	public void healthBelowThreshold(){// Spinout and crash
	    final WorldObject 	parent 	= getParent();
	    final Behavior 	beh 	= parent.getBehavior();
	    addBehavior(new PulledDownByGravityBehavior());
	    beh.probeForBehavior(DamagedByCollisionWithSurface.class).setEnable(true);
	    beh.probeForBehavior(DamageableBehavior.class).setAcceptsProjectileDamage(false);
	    beh.probeForBehavior(ExplodesOnDeath.class).setExplosionType(ExplosionType.BigExplosion);
	    //Catastrophy
	    final double spinSpeedCoeff=Math.max(def.getThrustSpeed()!=0?def.getThrustSpeed()/1600000:.3,.4);
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.LATERAL).setSpinAccelleration(.009*spinSpeedCoeff));
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.EQUATORIAL).setSpinAccelleration(.006*spinSpeedCoeff));
	    addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.POLAR).setSpinAccelleration(.007*spinSpeedCoeff));
	    //TODO: Smoke, sparks, and other fun stuff.
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
		    parent.getBehavior().probeForBehavior(MovesByVelocity.class).setEnable(true);
		parent.getBehavior().probeForBehavior(HasPropulsion.class).setEnable(true);
		parent.getBehavior().probeForBehavior(AccelleratedByPropulsion.class).setEnable(true);
		parent.getBehavior().probeForBehavior(VelocityDragBehavior.class).setEnable(true);
		parent.getBehavior().probeForBehavior(RotationalMomentumBehavior.class).setEnable(true);
		
		    parent.getBehavior().probeForBehavior(SteadilyRotating.class).setEnable(false);
		    parent.getBehavior().probeForBehavior(Bobbing.class).setEnable(false);
		   // parent.getBehavior().probeForBehavior(AutoFiring.class).setBerzerk(true)
		   // 	.setFiringPattern(new boolean[]{true}).setTimePerPatternEntry(100);
		}};
		addBehavior(spinAndCrashAddendum);
	    }//end if(spinCrash)
}//end possibleBobbingSpinAndCrashOnDeath

private void smartPlaneBehavior(TR tr, EnemyDefinition def, boolean retreatAboveSky){
    final HorizAimAtPlayerBehavior haapb =new HorizAimAtPlayerBehavior(tr.getPlayer()).setLeftHanded(Math.random()>=.5);
    addBehavior(haapb);
    final AdjustAltitudeToPlayerBehavior aatpb = new AdjustAltitudeToPlayerBehavior(tr.getPlayer()).setAccelleration(1000);
    addBehavior(aatpb);
    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior().setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
    pfb.addSupply(99999999);
    addBehavior(pfb);
    
    possibleSpinAndCrashOnDeath(.4,def);
    if(spinCrash){
		final DamageTrigger spinAndCrashAddendum = new DamageTrigger(){
		@Override
		public void healthBelowThreshold(){
		    final WorldObject 	parent 	= getParent();
		    final Behavior	beh	= parent.getBehavior();
		    final HasPropulsion hp 	= beh.probeForBehavior(HasPropulsion.class);
		    hp.setPropulsion(hp.getPropulsion()/1);
		    beh.probeForBehavior(AutoLeveling.class).
		    	setLevelingAxis(LevelingAxis.HEADING).
		    	setLevelingVector(Vector3D.MINUS_J).setRetainmentCoeff(.985);
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
    //final EnsureUpright eu = new EnsureUpright();
    //addBehavior(eu);
    final SpinAccellerationBehavior sab = (SpinAccellerationBehavior)new SpinAccellerationBehavior().setEnable(false);
    addBehavior(sab);
    addBehavior(new SmartPlaneBehavior(haapb,afb,sab,aatpb,escapeProp,retreatAboveSky));
}
/**
 * @return the boundingRadius
 */
public double getBoundingRadius() {
    return boundingRadius;
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
    return groundLocked;
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
}//end DEFObject
