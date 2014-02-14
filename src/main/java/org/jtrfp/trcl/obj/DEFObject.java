package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AdjustAltitudeToPlayerBehavior;
import org.jtrfp.trcl.beh.AutoFiring;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.Bobbing;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamagedByCollisionWithGameplayObject;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.HorizAimAtPlayerBehavior;
import org.jtrfp.trcl.beh.LeavesPowerupOnDeathBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.ResetsRandomlyAfterDeath;
import org.jtrfp.trcl.beh.SmartPlaneBehavior;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior;
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
    private boolean mobile,canTurn,foliage,boss,groundLocked;
public DEFObject(TR tr,Model model, EnemyDefinition def, EnemyPlacement pl){
    super(tr,model);
    boundingRadius = TR.legacy2Modern(def.getBoundingBoxRadius())/1.5;
    logic = def.getLogic();
    mobile=true;
    canTurn=true;
    foliage=false;
    boss=def.isObjectIsBoss();
    groundLocked=false;
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case groundTargeting://Ground turrets
    	    mobile=false;
    	    canTurn=true;
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    //TODO: def.getFiringVertices() needs actual vertex lookup.
    	    addBehavior(new ProjectileFiringBehavior().setFiringPositions(new Vector3D[]{
    		    new Vector3D(0,0,0)
    	    }));
    	    break;
    	case flyingDumb:
    	    canTurn=false;
    	    break;
    	case groundTargetingDumb:
    	    groundLocked=true;
    	    break;
    	case flyingSmart:
    	    smartPlaneBehavior(tr,def);
    	    break;
    	case bankSpinDrill:
    	    unhandled(def);
    	    break;
    	case sphereBoss:
    	    unhandled(def);
    	    mobile=true;
    	    break;
    	case flyingAttackRetreatSmart:
    	    smartPlaneBehavior(tr,def);
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case splitShipSmart://TODO
    	    smartPlaneBehavior(tr,def);
    	    //addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case groundStaticRuin://Destroyed object is replaced with another using SimpleModel i.e. weapons bunker
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case targetHeadingSmart:
    	    mobile=false;//Belazure's crane bots
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case targetPitchSmart:
    	    mobile=false;
	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    break;
    	case coreBossSmart:
    	    mobile=false;
    	    break;
    	case cityBossSmart:
    	    mobile=false;
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
	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
	    addBehavior(new AutoFiring().
		    setProjectileFiringBehavior(pfb).
		    setPatternOffsetMillis((int)(Math.random()*2000)).
		    setMaxFiringDistance(TR.mapSquareSize*1).
		    setSmartFiring(false).
		    setTimePerPatternEntry(1000));
	    addBehavior(new Bobbing().
		    setPhase(Math.random()).
		    setBobPeriodMillis(10*1000+Math.random()*3000).setAmplitude(2000));
	    mobile=false;
	    break;}
    	case takeoffAndEscape://TODO
    	    canTurn=false;
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
    	    mobile=false;
    	    break;
    	case volcanoBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
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
    	    addBehavior(new Bobbing());
    	    addBehavior(new SteadilyRotating());
    	    mobile=false;
    	    canTurn=false;//ironic?
    	    break;
    	case alienBoss:
    	    break;
    	case canyonBoss1:
    	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    mobile=false;
    	    break;
    	case lavaMan://Also terraform-o-bot
    	    mobile=false;
    	    break;
    	case arcticBoss:
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
    	    smartPlaneBehavior(tr,def);
    	    break;
    	case attackRetreatAboveSky:
    	    smartPlaneBehavior(tr,def);
    	    break;
    	case bobAboveSky:
    	    addBehavior(new Bobbing());
	    addBehavior(new SteadilyRotating());
	    mobile=false;
	    canTurn=false;
	    break;
    	case factory:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	}//end switch(logic)
    addBehavior(new DeathBehavior());
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()).setMaxHealth(pl.getStrength()).setEnable(!boss));
    setActive(!boss);
    addBehavior(new DamagedByCollisionWithGameplayObject());
    if(!foliage)addBehavior(new DebrisOnDeathBehavior());
    if(canTurn||boss){
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior()).setDragCoefficient(.86);
	addBehavior(new AutoLeveling());
    }
    if(!mobile || groundLocked){
	addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));
    }else{
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
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed());
	
	addBehavior(new LoopingPositionBehavior());
    	}//end if(mobile)
    if(boss){bossBehavior(tr,def);}
    if(def.getPowerup()!=null && Math.random()*100. < def.getPowerupProbability()){addBehavior(new LeavesPowerupOnDeathBehavior(def.getPowerup()));}
    }//end DEFObject

@Override
public void destroy(){
    if(ruinObject!=null)ruinObject.setVisible(true);//TODO: Switch to setActive later.
    super.destroy();
}

private void unhandled(EnemyDefinition def){
    System.err.println("UNHANDLED DEF LOGIC: "+def.getLogic()+". MODEL="+def.getComplexModelFile()+" DESC="+def.getDescription());
}

private void bossBehavior(TR tr, EnemyDefinition def){//Don't include hitzones for aiming.
    if(!def.getComplexModelFile().toUpperCase().contains("HITZO"))addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
}

private void fallingObjectBehavior(){
    canTurn=false;
    addBehavior(new PulledDownByGravityBehavior());
    addBehavior(new DamageableBehavior().setHealth(1));
    addBehavior(new CollidesWithTerrain());
}

private void smartPlaneBehavior(TR tr, EnemyDefinition def){
    final HorizAimAtPlayerBehavior haapb =new HorizAimAtPlayerBehavior(tr.getPlayer()).setLeftHanded(Math.random()>=.5);
    addBehavior(haapb);
    final AdjustAltitudeToPlayerBehavior aatpb = new AdjustAltitudeToPlayerBehavior(tr.getPlayer()).setAccelleration(1000);
    addBehavior(aatpb);
    final ProjectileFiringBehavior pfb = new ProjectileFiringBehavior().setProjectileFactory(tr.getResourceManager().getProjectileFactories()[def.getWeapon().ordinal()]);
    pfb.addSupply(99999999);
    addBehavior(pfb);
    final AutoFiring afb = new AutoFiring();
    afb.setFiringPattern(new boolean [] {true,false,false,false,true,true,false});
    afb.setTimePerPatternEntry((int)(400+Math.random()*300));
    afb.setPatternOffsetMillis((int)(Math.random()*1000));
    afb.setProjectileFiringBehavior(pfb);
    addBehavior(afb);
    //final EnsureUpright eu = new EnsureUpright();
    //addBehavior(eu);
    final SpinAccellerationBehavior sab = (SpinAccellerationBehavior)new SpinAccellerationBehavior().setEnable(false);
    addBehavior(sab);
    addBehavior(new SmartPlaneBehavior(haapb,afb,sab,aatpb));
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
}//end DEFObject
