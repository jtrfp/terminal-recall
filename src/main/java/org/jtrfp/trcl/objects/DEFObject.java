package org.jtrfp.trcl.objects;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ai.AccelleratedByPropulsion;
import org.jtrfp.trcl.ai.AutoLeveling;
import org.jtrfp.trcl.ai.BouncesOffSurfaces;
import org.jtrfp.trcl.ai.ChaseBehavior;
import org.jtrfp.trcl.ai.CollidesWithTerrain;
import org.jtrfp.trcl.ai.DamageableBehavior;
import org.jtrfp.trcl.ai.ExplodesOnDeath;
import org.jtrfp.trcl.ai.HasPropulsion;
import org.jtrfp.trcl.ai.LoopingPositionBehavior;
import org.jtrfp.trcl.ai.MovesByVelocity;
import org.jtrfp.trcl.ai.RotationalDragBehavior;
import org.jtrfp.trcl.ai.RotationalMomentumBehavior;
import org.jtrfp.trcl.ai.TerrainLocked;
import org.jtrfp.trcl.ai.VelocityDragBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition.EnemyLogic;
import org.jtrfp.trcl.objects.Explosion.ExplosionType;

public class DEFObject extends WorldObject {
public DEFObject(TR tr,Model model, EnemyDefinition def){
    super(tr,model);
    final EnemyLogic logic = def.getLogic();
    System.out.println(logic);
    boolean mobile=true;
    boolean groundLocked=false;
    switch(logic){
    	case groundDumb:
    	    mobile=false;
    	    break;
    	case groundTargeting:
    	    mobile=false;
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
	addBehavior(new DamageableBehavior());
	addBehavior(new ExplodesOnDeath(ExplosionType.BigExplosion));
	getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
	getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
	getBehavior().probeForBehavior(Propelled.class).setPropulsion(def.getThrustSpeed());
	getBehavior().probeForBehavior(RotationalDragBehavior.class).setDragCoefficient(.86);
    	}//end if(mobile)
    else{
	addBehavior(new ExplodesOnDeath(ExplosionType.Billow));
        }
    }//end DEFObject
}//end DEFObject
