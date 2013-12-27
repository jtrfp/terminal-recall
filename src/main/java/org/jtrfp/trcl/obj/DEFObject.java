package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.ChaseBehavior;
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
public DEFObject(TR tr,Model model, EnemyDefinition def, EnemyPlacement pl){
    super(tr,model);
    final EnemyLogic logic = def.getLogic();
    boolean mobile=true;
    boolean groundLocked=false;
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    break;
    	case groundTargeting:
    	    mobile=true;
    	    groundLocked=true;
    	    break;
    	case flyingDumb:
    	    mobile=false;
    	    break;
    	case groundTargetingDumb:
    	    groundLocked=true;
    	    break;
    	case flyingSmart:
    	    break;
    	case bankSpinDrill:
    	    break;
    	case sphereBoss:
    	    mobile=true;
    	    break;
    	case flyingAttackRetreatSmart:
    	    break;
    	case splitShipSmart:
    	    break;
    	case groundStaticRuin:
    	    mobile=false;
    	    break;
    	case targetHeadingSmart:
    	    break;
    	case targetPitchSmart:
    	    break;
    	case coreBossSmart:
    	    mobile=false;
    	    break;
    	case cityBossSmart:
    	    mobile=false;
    	    break;
    	case staticFiringSmart:
    	    mobile=false;
    	    break;
    	case sittingDuck:
    	    mobile=false;
    	    break;
    	case tunnelAttack:
    	    mobile=false;
    	    break;
    	case takeoffAndEscape:
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
    	    mobile=false;
    	    break;
    	case volcanoBoss:
    	    mobile=false;
    	    break;
    	case volcano:
    	    mobile=false;
    	    break;
    	case missile://Silo?
    	    mobile=false;
    	    break;
    	case bob:
    	    mobile=false;
    	    break;
    	case alienBoss:
    	    break;
    	case canyonBoss1:
    	    mobile=false;
    	    break;
    	case canyonBoss2:
    	    mobile=false;
    	    break;
    	case lavaMan:
    	    mobile=false;
    	    break;
    	case arcticBoss:
    	    mobile=false;
    	    break;
    	case helicopter:
    	    break;
    	case tree:
    	    mobile=false;
    	    break;
    	case ceilingStatic:
    	    mobile=false;
    	    break;
    	case bobAndAttack:
    	    mobile=false;
    	    break;
    	case forwardDrive:
    	    groundLocked=true;
    	    break;
    	case fallingStalag:
    	    mobile=false;
    	    break;
    	case attackRetreatBelowSky:
    	    break;
    	case attackRetreatAboveSky:
    	    break;
    	case bobAboveSky:
    	    mobile=false;
    	    break;
    	case factory:
    	    mobile=false;
    	    break;
    	}//end switch(logic)
    addBehavior(new DeathBehavior());
    addBehavior(new DamageableBehavior().setHealth(pl.getStrength()));
    
    if(mobile){
	addBehavior(new ChaseBehavior(tr.getPlayer()));
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new VelocityDragBehavior());
	addBehavior(new AutoLeveling());
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior());
	
	if(groundLocked)addBehavior(new TerrainLocked());
	else 	{addBehavior(new BouncesOffSurfaces());
	    	addBehavior(new CollidesWithTerrain());}
	
	addBehavior(new LoopingPositionBehavior());
	addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));
	getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed());
	getBehavior().probeForBehavior(RotationalDragBehavior.class).setDragCoefficient(.86);
	addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
    	}//end if(mobile)
    else{addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));}
    }//end DEFObject
}//end DEFObject
