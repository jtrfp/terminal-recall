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
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.SpinAccellerationBehavior.SpinMode;
import org.jtrfp.trcl.beh.phy.PulledDownByGravityBehavior;
import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.RedFlashOnDamage;
import org.jtrfp.trcl.obj.SpawnsRandomExplosionsAndDebris;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundSystem;


public class SpinCrashDeathBehavior extends DamageTrigger {
    private SpinAccellerationBehavior lateralSAB,equatorialSAB,polarSAB;
    private SpawnsRandomExplosionsAndDebris explosionDebris;
    private SpawnsRandomSmoke randomSmoke;
    private PulledDownByGravityBehavior gravity;
    private AutoLeveling autoLeveling;
    
    public SpinCrashDeathBehavior(){
	super();
	this.setThreshold(1024*8);
    }//end constructor

    @Override
    public void healthBelowThreshold(){// Spinout and crash
	final WorldObject 	parent 	= getParent();
	System.out.println("healthBelowThreshold() "+probeForBehavior(DamageableBehavior.class).getHealth());
	if(probeForBehavior(DamageableBehavior.class).getHealth()<1)
	    return;//No point; already dying.
	//Trigger small boom
	final TR tr = parent.getTr();
	tr.soundSystem.get().getPlaybackFactory().
	create(
		tr.getResourceManager().soundTextures.get("EXP2.WAV"), 
		new double[] {.5*SoundSystem.DEFAULT_SFX_VOLUME*2,.5*SoundSystem.DEFAULT_SFX_VOLUME*2} );

	getGravity().setEnable(true);
	
	probeForBehavior(DamagedByCollisionWithSurface.class).setEnable(true);
	probeForBehavior(CollidesWithTerrain.class).setNudgePadding(0);
	probeForBehavior(DamageableBehavior.class).setAcceptsProjectileDamage(false);
	probeForBehavior(ExplodesOnDeath.class).setExplosionType(ExplosionType.BigExplosion).setExplosionSound(DEFObject.BIG_EXP_SOUNDS[(int)(Math.random()*3)]);
	probeForBehavior(AfterburnerBehavior.class).setEnable(false);
	probeForBehavior(UserInputRudderElevatorControlBehavior.class).setEnable(false);
	probeForBehavior(UserInputThrottleControlBehavior.class).setEnable(false);
	this.probeForBehaviors(new AbstractSubmitter<ProjectileFiringBehavior>(){
	    @Override
	    public void submit(ProjectileFiringBehavior item) {
		item.setEnable(false);
	    }
	}, ProjectileFiringBehavior.class);
	final Propelled propelled = probeForBehavior(Propelled.class);
	propelled.setPropulsion(propelled.getMaxPropulsion());
	getAutoLeveling().setEnable(true);
	
	//Catastrophy
	final double spinSpeedCoeff=.5;
	getLateralSAB()   .setSpinAccelleration(.009*spinSpeedCoeff).setEnable(true);
	getEquatorialSAB().setSpinAccelleration(.006*spinSpeedCoeff).setEnable(true);
	getPolarSAB()     .setSpinAccelleration(.007*spinSpeedCoeff).setEnable(true);
	
	parent.probeForBehaviors(new AbstractSubmitter<SpinAccellerationBehavior>(){
	    @Override
	    public void submit(SpinAccellerationBehavior item) {
		item.setEnable(true);
	    }}, SpinAccellerationBehavior.class);
	
	//TODO: Sparks, and other fun stuff.
	getExplosionDebris().setEnable(true);
	getRandomSmoke().setEnable(true);
	
	//Set up for ground explosion
	parent.probeForBehavior(DamagedByCollisionWithSurface.class).getCollisionDamage();
	parent.probeForBehavior(DamagedByCollisionWithSurface.class).setCollisionDamage(65535);
	parent.probeForBehavior(RedFlashOnDamage.class).setEnable(false);
	System.out.println("end healthBelowThreshold()");
    }//end healthBelowThreshold

    public SpinCrashDeathBehavior reset() {
	//Undo the results of damage triggering
	final WorldObject 	parent 	= getParent();
	
	getGravity().setEnable(false);
	
	probeForBehavior(DamagedByCollisionWithSurface.class).setEnable(true);
	probeForBehavior(CollidesWithTerrain.class).setNudgePadding(0);
	probeForBehavior(DamageableBehavior.class).setAcceptsProjectileDamage(true);
	probeForBehavior(ExplodesOnDeath.class).setExplosionType(ExplosionType.BigExplosion).setExplosionSound(DEFObject.BIG_EXP_SOUNDS[(int)(Math.random()*3)]);
	probeForBehavior(AfterburnerBehavior.class).setEnable(true);
	probeForBehavior(UserInputRudderElevatorControlBehavior.class).setEnable(true);
	probeForBehavior(UserInputThrottleControlBehavior.class).setEnable(true);
	final Propelled propelled = probeForBehavior(Propelled.class);
	propelled.setPropulsion(0);
	
	getAutoLeveling().setEnable(false);
	
	getLateralSAB()   .setEnable(false);
	getPolarSAB()     .setEnable(false);
	getEquatorialSAB().setEnable(false);
	getExplosionDebris().setEnable(false);
	getRandomSmoke()    .setEnable(false);
	//Set up for ground explosion
	parent.probeForBehavior(DamagedByCollisionWithSurface.class).setCollisionDamage(6554);
	parent.probeForBehavior(RedFlashOnDamage.class).setEnable(true);
	super.setTriggered(false);
	return this;
    }//end reset()

    protected SpinAccellerationBehavior getLateralSAB() {
	if(lateralSAB == null)
	    getParent().addBehavior(lateralSAB = new SpinAccellerationBehavior().setSpinMode(SpinMode.LATERAL));
        return lateralSAB;
    }

    protected SpinAccellerationBehavior getEquatorialSAB() {
	if(equatorialSAB == null)
	    getParent().addBehavior(equatorialSAB = new SpinAccellerationBehavior().setSpinMode(SpinMode.EQUATORIAL));
        return equatorialSAB;
    }

    protected SpinAccellerationBehavior getPolarSAB() {
	if(polarSAB == null)
	    getParent().addBehavior(polarSAB = new SpinAccellerationBehavior().setSpinMode(SpinMode.POLAR));
        return polarSAB;
    }

    protected SpawnsRandomExplosionsAndDebris getExplosionDebris() {
	if(explosionDebris == null)
	    getParent().addBehavior(explosionDebris = new SpawnsRandomExplosionsAndDebris(getParent().getTr()));
        return explosionDebris;
    }

    protected SpawnsRandomSmoke getRandomSmoke() {
	if(randomSmoke == null)
	    getParent().addBehavior(randomSmoke = new SpawnsRandomSmoke(getParent().getTr()));
        return randomSmoke;
    }

    protected PulledDownByGravityBehavior getGravity() {
	if(gravity == null)
	    getParent().addBehavior(gravity = new PulledDownByGravityBehavior());
        return gravity;
    }

    protected AutoLeveling getAutoLeveling() {
	if(autoLeveling == null)
	    getParent().addBehavior(autoLeveling = 
	     new AutoLeveling().setLevelingAxis(LevelingAxis.HEADING).setLevelingVector(Vector3D.MINUS_J).setRetainmentCoeff(.98, .98, .98));
        return autoLeveling;
    }

}//end SpinCrashBehavior
