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
package org.jtrfp.trcl.file;

import java.awt.Color;
import java.awt.Dimension;

import org.jtrfp.trcl.tools.Util;

public enum Powerup {
    RTL(Weapon.RTL, 100, 0, 0, 0, 0, true, Integer.MAX_VALUE,
	    "Rapid Targeting Laser", "Rapid Fire Laser","POWERLAS.BIN"), 
    PAC(Weapon.PAC, 100,
	    0, 0, 0, 0, false, Integer.MAX_VALUE, 
		    "Plasma Assault Cannon",
	    	    "Servo-Kinetic Lasers","POWERPLA.BIN"), 
    ION(Weapon.ION, 100, 0, 0, 0, 0, true,
	    Integer.MAX_VALUE,
	    "Ion Burst Cannon", "Dispersion Cannon", "POWERANT.BIN"), 
    MAM(Weapon.MAM, 40, 0, 0,
	    0, 0, false, Integer.MAX_VALUE, "Manually-Aimed Missiles",
	    "Dead-On Missiles","POWERMIS.BIN"), 
    SAD(Weapon.SAD, 20, 0, 0, 0, 0, false,
	    Integer.MAX_VALUE,
	    "Seek-and-Destroy Missiles", "Vipers","POWERGMI.BIN"), 
    SWT(Weapon.SWT, 20, 0, 0,
	    0, 0, false, Integer.MAX_VALUE,
		    "ShockWave Torpedoes",
	    "Bion Fury Missiles", "POWERSUP.BIN"), 
    shieldRestore(null, 0, 65535, 0, 0, 0,
	    false, Integer.MAX_VALUE,
			    "Shields Restored", "Shields Restored", "POWERSHE.BIN"), 
    invisibility(null, 0, 0, 1000 * 30, 0, 0, false, Integer.MAX_VALUE,
		     "Invisibility",
	    "Invisiblity","POWERVIS.BIN"), 
    invincibility(null, 0, 0, 0, 1000 * 30, 0, false,
	    Integer.MAX_VALUE,
	    "Invincibility", "Invincibility","POWERINV.BIN"), 
    DAM(Weapon.DAM, 1, 0, 0, 0, 0,//Also Hellion missile in HB
	    false, 1,
		    //TODO: What's the real billboard for this?
	    "Discrete Annihilation Missile", "FFF","POWERFIR.BIN"), 
    Afterburner(null, 0, 0, 0,
	    0, 120, false, Integer.MAX_VALUE,
		    "Afterburner", "Turbo!","POWERZAP.BIN"), //TODO: What's the real billboard for this?
    PowerCore(null,//Also referred to as "message pod"
	    0, 6554, 0, 0, 0, false, Integer.MAX_VALUE,
		    "Shield Boost",
	    "Shield Boost!", "POWERCAN.BIN"), 
    Random(null, 0, 0, 0, 0, 0, false,//Also used as Scorcher missile in HB?
	    Integer.MAX_VALUE,"???", "???","POWER1.BIN"), // Not for use in TV
    LegionMissile(Weapon.LegionMissile, 20, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Legion Missiles",
	    "Legion Missiles","NULL.BIN"), 
    IndependenceMissile(Weapon.IndependenceMissile, 20, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Independence Missiles",
	    "Independence Missiles","NULL.BIN"), 
    DoomsdayMine(Weapon.DoomsdayMine, 1, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Doomsday Mine",
	    "Doomsday Mine","NULL.BIN"), 
    GreenRepairDrone(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Green Repair Drone",
	    "Green Repair Drone","NULL.BIN"), 
    BlueRepairDrone(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Blue Repair Drone",
	    "Blue Repair Drone","NULL.BIN"), 
    RedRepairDrone(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Red Repair Drone",
	    "Red Repair Drone","NULL.BIN"), 
    GreenEnergyCell(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Green Energy Cell",
	    "Green Energy Cell","NULL.BIN"), 
    BlueEnergyCell(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Blue Energy Cell",
	    "Blue Energy Cell","NULL.BIN"), 
    RedEnergyCell(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Red Energy Cell",
	    "Red Energy Cell","NULL.BIN"),
    MessagePod(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Message Pod",
	    "Message Pod","NULL.BIN"),
    Unknown0(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown1(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown2(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown3(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown4(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown5(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown6(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown7(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown8(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown9(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown10(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown11(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown0","NULL.BIN"),
    Unknown12(null, 0, 0, 0,//Warning: Quantity speculative!
	    0, 0, false, Integer.MAX_VALUE, "Unknown",
	    "Unknown1","NULL.BIN");
    
    private final int	   shieldDelta, invisibiltyTimeDeltaMillis,
	    invincibilityTimeDeltaMillis, afterburnerDelta, weaponSupplyDelta;
    private final boolean  doublesFiringMultiplex;
    private final int 	   quantityLimit;
    private final String   tvDescription, f3Description, model;
    private final Weapon   weapon;

    Powerup(Weapon weapon, int weaponSupplyDelta, int shieldDelta,
	    int invisibilityTimeDeltaMillis, int invincibilityTimeDeltaMillis,
	    int afterburnerDelta, boolean doublesFiringMultiplex,
	    int quantityLimit, String tvDescription,
	    String f3Description, String model) {
	this.shieldDelta 		  = shieldDelta;
	this.invisibiltyTimeDeltaMillis   = invisibilityTimeDeltaMillis;
	this.invincibilityTimeDeltaMillis = invincibilityTimeDeltaMillis;
	this.afterburnerDelta 		  = afterburnerDelta;
	this.doublesFiringMultiplex 	  = doublesFiringMultiplex;
	this.quantityLimit 		  = quantityLimit;
	this.tvDescription 		  = tvDescription;
	this.f3Description 		  = f3Description;
	this.weapon 			  = weapon;
	this.weaponSupplyDelta 		  = weaponSupplyDelta;
	this.model                        = model;
    }// end constructor

    public static final int 	  TIME_PER_FRAME_MILLIS = 500;
    public static final Dimension BILLBOARD_SIZE = new Dimension(320000, 320000);
    public static final int 	  AFTERBURNER_TIME_PER_UNIT_MILLIS = 500;
    public static final int 	  MAX_HEALTH = 65535;
    public static final Color[]   PALETTE = Util.DEFAULT_PALETTE;

    /**
     * @return the shieldDelta
     */
    public int getShieldDelta() {
	return shieldDelta;
    }

    /**
     * @return the invisibiltyTimeDeltaMillis
     */
    public int getInvisibiltyTimeDeltaMillis() {
	return invisibiltyTimeDeltaMillis;
    }

    /**
     * @return the invincibilityTimeDeltaMillis
     */
    public int getInvincibilityTimeDeltaMillis() {
	return invincibilityTimeDeltaMillis;
    }

    /**
     * @return the afterburnerDelta
     */
    public int getAfterburnerDelta() {
	return afterburnerDelta;
    }

    /**
     * @return the doublesFiringMultiplex
     */
    public boolean isDoublesFiringMultiplex() {
	return doublesFiringMultiplex;
    }

    /**
     * @return the quantityLimit
     */
    public int getQuantityLimit() {
	return quantityLimit;
    }

    /**
     * @return the tvDescription
     */
    public String getTvDescription() {
	return tvDescription;
    }

    /**
     * @return the f3Description
     */
    public String getF3Description() {
	return f3Description;
    }

    /**
     * @return the weapon
     */
    public Weapon getWeapon() {
	return weapon;
    }

    /**
     * @return the weaponSupplyDelta
     */
    public int getWeaponSupplyDelta() {
	return weaponSupplyDelta;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }
}//end Powerup
