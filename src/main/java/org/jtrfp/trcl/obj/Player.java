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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.AfterburnerBehavior;
import org.jtrfp.trcl.beh.Cloakable;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.beh.DamagedByCollisionWithDEFObject;
import org.jtrfp.trcl.beh.DamagedByCollisionWithSurface;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.RollLevelingBehavior;
import org.jtrfp.trcl.beh.RollNudgeOnDamage;
import org.jtrfp.trcl.beh.SurfaceImpactSFXBehavior;
import org.jtrfp.trcl.beh.UpdatesNAVRadar;
import org.jtrfp.trcl.beh.UpgradeableProjectileFiringBehavior;
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.phy.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.beh.ui.UpdatesHealthMeterBehavior;
import org.jtrfp.trcl.beh.ui.UpdatesThrottleMeterBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.gpu.Model;

public class Player extends WorldObject implements RelevantEverywhere{
    //private final Camera 	camera;
    //private int 		cameraDistance 			= 0;
    public static final int 	CLOAK_COUNTDOWN_START 		= ThreadManager.GAMEPLAY_FPS * 30;// 30sec
    public static final int 	INVINCIBILITY_COUNTDOWN_START 	= ThreadManager.GAMEPLAY_FPS * 30;// 30sec
    private final 		ProjectileFiringBehavior[] weapons = new ProjectileFiringBehavior[Weapon
                  		                                   .values().length];

    public Player(final TR tr, final Model model) {
	super(tr, model);
	setVisible(false);
	DamageableBehavior db = new DamageableBehavior();
	addBehavior(db);
	String godMode = System.getProperty("org.jtrfp.trcl.godMode");
	if (godMode != null) {
	    if (godMode.toUpperCase().contains("TRUE")) {
		db.setEnable(false);
	    }
	}
	
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new CollidesWithTunnelWalls(true, true));
	addBehavior(new UserInputThrottleControlBehavior(tr.getControllerInputs()));
	addBehavior(new VelocityDragBehavior());
	addBehavior(new RollLevelingBehavior());
	addBehavior(new UserInputRudderElevatorControlBehavior(tr.getControllerInputs()));
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior());
	addBehavior(new CollidesWithTerrain().setTunnelEntryCapable(true).setIgnoreHeadingForImpact(false));
	addBehavior(new AfterburnerBehavior());
	addBehavior(new LoopingPositionBehavior());
	addBehavior(new HeadingXAlwaysPositiveBehavior().setEnable(false));
	addBehavior(new UpdatesThrottleMeterBehavior().setController(tr
		.getGame().getHUDSystem().getThrottleMeter()));
	addBehavior(new UpdatesHealthMeterBehavior().setController(tr
		.getGame().getHUDSystem().getHealthMeter()));
	addBehavior(new DamagedByCollisionWithDEFObject());
	addBehavior(new DamagedByCollisionWithSurface());
	addBehavior(new BouncesOffSurfaces());
	addBehavior(new UpdatesNAVRadar());
	addBehavior(new Cloakable());
	addBehavior(new SurfaceImpactSFXBehavior(tr));
	addBehavior(new RedFlashOnDamage());
	addBehavior(new RollNudgeOnDamage());
	
	final Weapon[] allWeapons = Weapon.values();
	
	for (int i = 0; i < allWeapons.length; i++) {
	    final Weapon w = allWeapons[i];
	    if (w.getButtonToSelect() != -1) {
		final ProjectileFiringBehavior pfb;
		if (w.isLaser()) {// LASER
		    pfb = new UpgradeableProjectileFiringBehavior()
			    .setProjectileFactory(tr.getResourceManager()
				    .getProjectileFactories()[w.ordinal()]);
		    ((UpgradeableProjectileFiringBehavior) pfb)
			    .setMaxCapabilityLevel(2)
			    .setCapabilityLevel(i==0?0:-1)
			    .setFiringMultiplexMap(
				    new Vector3D[][] {
					    new Vector3D[] {
						    new Vector3D(5000, -3000, 0),
						    new Vector3D(-5000, -3000,
							    0) },// Level 0,
								 // single
					    new Vector3D[] {
						    new Vector3D(5000, -3000, 0),
						    new Vector3D(-5000, -3000,
							    0) },// Level 1,
								 // double
					    new Vector3D[] {
						    new Vector3D(5000, -3000, 0),
						    new Vector3D(-5000, -3000,
							    0),// Level 2 quad
						    new Vector3D(5000, 3000, 0),
						    new Vector3D(-5000, 3000, 0) } })//Level 2 cont'd
				.setTimeBetweenFiringsMillis(w.getFiringIntervalMS());
		}// end if(isLaser)
		else {// NOT LASER
		    pfb = new ProjectileFiringBehavior().setFiringPositions(
			    new Vector3D[] { new Vector3D(5000, -3000, 0),
				    new Vector3D(-5000, -3000, 0) })
			    .setProjectileFactory(
				    tr.getResourceManager()
					    .getProjectileFactories()[w
					    .ordinal()])
		            .setTimeBetweenFiringsMillis(w.getFiringIntervalMS());
		    if (w == Weapon.DAM)
			pfb.setAmmoLimit(1);
		}
		addBehavior(pfb);
		weapons[w.getButtonToSelect() - 1] = pfb;
		if(System.getProperties().containsKey("org.jtrfp.trcl.allAmmo")){
		    if(System.getProperty("org.jtrfp.trcl.allAmmo").toUpperCase().contains("TRUE")){
			System.out.println("allAmmo cheat active for weapon "+w.getButtonToSelect());
			pfb.setAmmoLimit(Integer.MAX_VALUE);
			try{pfb.addSupply(Double.POSITIVE_INFINITY);}catch(SupplyNotNeededException e){}
		    }//end if(property=true)
		}//end if(allAmmo)
	    }// end if(hasButton)
	}
	addBehavior(new UserInputWeaponSelectionBehavior(tr.getControllerInputs()).setBehaviors(weapons));
	//camera = tr.renderer.get().getCamera();
	probeForBehavior(VelocityDragBehavior.class)
		.setDragCoefficient(.86);
	probeForBehavior(Propelled.class).setMinPropulsion(0);
	probeForBehavior(Propelled.class)
		.setMaxPropulsion(900000);
	probeForBehavior(RotationalDragBehavior.class)
		.setDragCoefficient(.86);
	setActive(false);
    }//end constructor
    
    public void resetVelocityRotMomentum(){
	//probeForBehavior(HasPropulsion.class).setPropulsion(0);
	probeForBehavior(RotationalMomentumBehavior.class).
		setEquatorialMomentum(0).
		setLateralMomentum(0).
		setPolarMomentum(0);
	probeForBehavior(MovesByVelocity.class).setVelocity(Vector3D.ZERO);
    }

    @Override
    public void setHeading(Vector3D lookAt) {
	/*camera.setLookAtVector(lookAt);
	camera.setPosition(new Vector3D(getPosition()).subtract(lookAt
		.scalarMultiply(cameraDistance)));*/
	super.setHeading(lookAt);
    }

    @Override
    public void setTop(Vector3D top) {
	//camera.setUpVector(top);
	super.setTop(top);
    }

    @Override
    public Player setPosition(double[] pos) {
	super.setPosition(pos);
	return this;
    }

    /**
     * @return the weapons
     */
    public ProjectileFiringBehavior[] getWeapons() {
	return weapons;
    }
}// end Player
