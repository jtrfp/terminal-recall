/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior.SpinMode;
import org.jtrfp.trcl.beh.phy.PulledDownByGravityBehavior;
import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.RedFlashOnDamage;
import org.jtrfp.trcl.obj.SpawnsRandomExplosionsAndDebris;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundSystem;


public class SpinCrashDeathBehavior extends DamageTrigger {

    public SpinCrashDeathBehavior(){
	super();
	this.setThreshold(1024*16);
    }//end constructor

    @Override
    public void healthBelowThreshold(){// Spinout and crash
	final WorldObject 	parent 	= getParent();
	if(probeForBehavior(DamageableBehavior.class).getHealth()<1)
	    return;//No point; already dying.
	//Trigger small boom
	final TR tr = parent.getTr();
	tr.soundSystem.get().getPlaybackFactory().
	create(tr.getResourceManager().soundTextures.get("EXP2.WAV"), new double[]{.5*SoundSystem.DEFAULT_SFX_VOLUME*2,.5*SoundSystem.DEFAULT_SFX_VOLUME*2});

	parent.addBehavior(new PulledDownByGravityBehavior().setEnable(true));
	//parent.probeForBehavior(Propelled.class).setMaxPropulsion(4096).setPropulsion(4096);
	probeForBehavior(DamagedByCollisionWithSurface.class).setEnable(true);
	probeForBehavior(CollidesWithTerrain.class).setNudgePadding(0);
	probeForBehavior(DamageableBehavior.class).setAcceptsProjectileDamage(false);
	probeForBehavior(ExplodesOnDeath.class).setExplosionType(ExplosionType.BigExplosion).setExplosionSound(DEFObject.BIG_EXP_SOUNDS[(int)(Math.random()*3)]);
	probeForBehavior(AfterburnerBehavior.class).setEnable(false);
	probeForBehavior(UserInputRudderElevatorControlBehavior.class).setEnable(false);
	probeForBehavior(UserInputThrottleControlBehavior.class).setEnable(false);
	final Propelled propelled = probeForBehavior(Propelled.class);
	propelled.setPropulsion(propelled.getMaxPropulsion());
	parent.addBehavior(new AutoLeveling().setLevelingAxis(LevelingAxis.HEADING).setLevelingVector(Vector3D.MINUS_J).setRetainmentCoeff(.98, .98, .98));
	
	/*if(def.getThrustSpeed()<800000){
	    probeForBehavior(HasPropulsion.class).setPropulsion(0);
	    probeForBehavior(VelocityDragBehavior.class).setEnable(false);
	}*/
	//Catastrophy
	final double spinSpeedCoeff=.5;
	parent.addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.LATERAL).setSpinAccelleration(.009*spinSpeedCoeff));
	parent.addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.EQUATORIAL).setSpinAccelleration(.006*spinSpeedCoeff));
	parent.addBehavior(new SpinAccellerationBehavior().setSpinMode(SpinMode.POLAR).setSpinAccelleration(.007*spinSpeedCoeff));
	//TODO: Sparks, and other fun stuff.
	parent.addBehavior(new SpawnsRandomExplosionsAndDebris(parent.getTr()));
	parent.addBehavior(new SpawnsRandomSmoke(parent.getTr()));
	//Set up for ground explosion
	parent.probeForBehavior(DamagedByCollisionWithSurface.class).setCollisionDamage(65535);
	parent.probeForBehavior(RedFlashOnDamage.class).setEnable(false);
	System.out.println("end healthBelowThreshold()");
    }//end healthBelowThreshold

}//end SpinCrashBehavior
