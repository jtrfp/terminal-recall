package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.ChaseBehavior;
import org.jtrfp.trcl.beh.CollidesWithDEFObjects;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.HasPropulsion;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MovesByVelocity;
import org.jtrfp.trcl.beh.RotationalDragBehavior;
import org.jtrfp.trcl.beh.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.TerrainLocked;
import org.jtrfp.trcl.beh.VelocityDragBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition.EnemyLogic;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class DEFObject extends WorldObject {
    private final double boundingRadius;
public DEFObject(TR tr,Model model, EnemyDefinition def, EnemyPlacement pl){
    super(tr,model);
    boundingRadius = TR.legacy2Modern(def.getBoundingBoxRadius())/1.5;
    final EnemyLogic logic = def.getLogic();
    boolean mobile=true;
    boolean canTurn=true;
    boolean groundLocked=false;
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case groundTargeting://Ground turrets
    	    mobile=false;
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case flyingDumb:
    	    canTurn=false;
    	    break;
    	case groundTargetingDumb:
    	    groundLocked=true;
    	    break;
    	case flyingSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case bankSpinDrill:
    	    break;
    	case sphereBoss:
    	    mobile=true;
    	    break;
    	case flyingAttackRetreatSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case splitShipSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case groundStaticRuin:
    	    mobile=false;
    	    canTurn=false;
    	    break;
    	case targetHeadingSmart:
    	    mobile=false;//Belazure's crane bots
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case targetPitchSmart:
    	    break;
    	case coreBossSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case cityBossSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case staticFiringSmart:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
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
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case volcanoBoss:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
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
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case canyonBoss1:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case lavaMan:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case arcticBoss:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    mobile=false;
    	    break;
    	case helicopter:
    	    break;
    	case tree:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case ceilingStatic:
    	    canTurn=false;
    	    mobile=false;
    	    break;
    	case bobAndAttack:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
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
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
    	    break;
    	case attackRetreatAboveSky:
    	    addBehavior(new ChaseBehavior(tr.getPlayer()));
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
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()));
    if(canTurn){
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior());
	addBehavior(new AutoLeveling());
    }
    if(mobile){
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new VelocityDragBehavior());
	
	if(groundLocked)addBehavior(new TerrainLocked());
	else 	{addBehavior(new BouncesOffSurfaces());
	    	addBehavior(new CollidesWithTerrain());}
	
	addBehavior(new LoopingPositionBehavior());
	getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed());
	getBehavior().probeForBehavior(RotationalDragBehavior.class).setDragCoefficient(.86);
	addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    	}//end if(mobile)
    else{addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));}
    }//end DEFObject
/**
 * @return the boundingRadius
 */
public double getBoundingRadius() {
    return boundingRadius;
}
}//end DEFObject
