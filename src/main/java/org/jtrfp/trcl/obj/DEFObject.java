package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamagedByCollisionWithGameplayObject;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.HorizAimAtPlayerBehavior;
import org.jtrfp.trcl.beh.LeavesPowerupOnDeathBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.phy.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
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
    	    break;
    	case flyingDumb:
    	    canTurn=false;
    	    break;
    	case groundTargetingDumb:
    	    groundLocked=true;
    	    break;
    	case flyingSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case bankSpinDrill:
    	    break;
    	case sphereBoss:
    	    mobile=true;
    	    break;
    	case flyingAttackRetreatSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case splitShipSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
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
    	    break;
    	case coreBossSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case cityBossSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case staticFiringSmart:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case sittingDuck:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case tunnelAttack:
    	    mobile=false;
    	    break;
    	case takeoffAndEscape:
    	    canTurn=false;
    	    break;
    	case fallingAsteroid:
    	    mobile=false;
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
    	case volcano:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case missile://Silo?
    	    mobile=false;
    	    break;
    	case bob:
    	    mobile=false;
    	    break;
    	case alienBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case canyonBoss1:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case lavaMan:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case arcticBoss:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case helicopter:
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
    	case bobAndAttack:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case forwardDrive:
    	    canTurn=false;
    	    groundLocked=true;
    	    break;
    	case fallingStalag:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case attackRetreatBelowSky:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case attackRetreatAboveSky:
    	    addBehavior(new HorizAimAtPlayerBehavior(tr.getPlayer()));
    	    break;
    	case bobAboveSky:
    	    mobile=false;
    	    break;
    	case factory:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	}//end switch(logic)
    addBehavior(new DeathBehavior());
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()).setEnable(!boss));
    addBehavior(new DamagedByCollisionWithGameplayObject());
    if(!foliage)addBehavior(new DebrisOnDeathBehavior());
    if(canTurn){
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
	else 	{addBehavior(new BouncesOffSurfaces());
	    	addBehavior(new CollidesWithTerrain());
	    	}
	
	getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed());
	
	addBehavior(new LoopingPositionBehavior());
    	}//end if(mobile)
    if(def.getPowerup()!=null && Math.random()*100. < def.getPowerupProbability()){addBehavior(new LeavesPowerupOnDeathBehavior(def.getPowerup()));}
    }//end DEFObject

@Override
public void destroy(){
    if(ruinObject!=null)ruinObject.setVisible(true);//TODO: Switch to setActive later.
    super.destroy();
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
