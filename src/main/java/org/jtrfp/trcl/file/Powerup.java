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
    RTL(Weapon.RTL, 100, 0, 0, 0, 0, true, Integer.MAX_VALUE, new String[] {
	    "LASE0000.RAW", "LASE0001.RAW", "LASE0002.RAW", "LASE0003.RAW",
	    "LASE0004.RAW", "LASE0005.RAW", "LASE0006.RAW", "LASE0007.RAW" },
	    "Rapid Targeting Laser", "Rapid Fire Laser"), 
    PAC(Weapon.PAC, 100,
	    0, 0, 0, 0, false, Integer.MAX_VALUE, new String[] {
		    "PLAS0000.RAW", "PLAS0001.RAW", "PLAS0002.RAW",
		    "PLAS0003.RAW", "PLAS0004.RAW", "PLAS0005.RAW",
		    "PLAS0006.RAW", "PLAS0007.RAW" }, "Plasma Assault Cannon",
	    "Servo-Kinetic Lasers"), 
    ION(Weapon.ION, 100, 0, 0, 0, 0, true,
	    Integer.MAX_VALUE, new String[] { "ANTI0000.RAW", "ANTI0001.RAW",
		    "ANTI0002.RAW", "ANTI0003.RAW", "ANTI0004.RAW",
		    "ANTI0005.RAW", "ANTI0006.RAW", "ANTI0007.RAW" },
	    "Ion Burst Cannon", "Dispersion Cannon"), 
    MAM(Weapon.MAM, 40, 0, 0,
	    0, 0, false, Integer.MAX_VALUE, new String[] { "MISS0000.RAW",
		    "MISS0001.RAW", "MISS0002.RAW", "MISS0003.RAW",
		    "MISS0004.RAW", "MISS0005.RAW", "MISS0006.RAW",
		    "MISS0007.RAW" }, "Manually-Aimed Missiles",
	    "Dead-On Missiles"), 
    SAD(Weapon.SAD, 20, 0, 0, 0, 0, false,
	    Integer.MAX_VALUE, new String[] { "GMIS0000.RAW", "GMIS0001.RAW",
		    "GMIS0002.RAW", "GMIS0003.RAW", "GMIS0004.RAW",
		    "GMIS0005.RAW", "GMIS0006.RAW", "GMIS0007.RAW" },
	    "Seek-and-Destroy Missiles", "Vipers"), 
    SWT(Weapon.SWT, 20, 0, 0,
	    0, 0, false, Integer.MAX_VALUE, new String[] { "SUPM0000.RAW",
		    "SUPM0001.RAW", "SUPM0002.RAW", "SUPM0003.RAW",
		    "SUPM0004.RAW", "SUPM0005.RAW", "SUPM0006.RAW",
		    "SUPM0007.RAW" }, "ShockWave Torpedoes",
	    "Bion Fury Missiles"), 
    shieldRestore(null, 0, 65535, 0, 0, 0,
	    false, Integer.MAX_VALUE, new String[] { "SHEI0000.RAW",
		    "SHEI0001.RAW", "SHEI0002.RAW", "SHEI0003.RAW",
		    "SHEI0004.RAW", "SHEI0005.RAW", "SHEI0006.RAW",
		    "SHEI0007.RAW" }, "Shields Restored", "Shields Restored"), 
    invisibility(null, 0, 0, 1000 * 30, 0, 0, false, Integer.MAX_VALUE,
	    new String[] { "INVI0000.RAW", "INVI0001.RAW", "INVI0002.RAW",
		    "INVI0003.RAW", "INVI0004.RAW", "INVI0005.RAW",
		    "INVI0006.RAW", "INVI0007.RAW" }, "Invisibility",
	    "Invisiblity"), 
    invincibility(null, 0, 0, 0, 1000 * 30, 0, false,
	    Integer.MAX_VALUE, new String[] { "INVN0000.RAW", "INVN0001.RAW",
		    "INVN0002.RAW", "INVN0003.RAW", "INVN0004.RAW",
		    "INVN0005.RAW", "INVN0006.RAW", "INVN0007.RAW" },
	    "Invincibility", "Invincibility"), 
    DAM(Weapon.DAM, 1, 0, 0, 0, 0,
	    false, 1, new String[] { "MEGA0000.RAW", "MEGA0001.RAW",
		    "MEGA0002.RAW", "MEGA0003.RAW", "MEGA0004.RAW",
		    "MEGA0005.RAW", "MEGA0006.RAW", "MEGA0007.RAW" },
	    "Discrete Annihilation Missile", "FFF"), 
    Afterburner(null, 0, 0, 0,
	    0, 120, false, Integer.MAX_VALUE, new String[] { "AFTR0000.RAW",
		    "AFTR0001.RAW", "AFTR0002.RAW", "AFTR0003.RAW",
		    "AFTR0004.RAW", "AFTR0005.RAW", "AFTR0006.RAW",
		    "AFTR0007.RAW" }, "Afterburner", "Turbo!"), 
    PowerCore(null,
	    0, 6554, 0, 0, 0, false, Integer.MAX_VALUE, new String[] {
		    "ENCA0000.RAW", "ENCA0001.RAW", "ENCA0002.RAW",
		    "ENCA0003.RAW", "ENCA0004.RAW", "ENCA0005.RAW",
		    "ENCA0006.RAW", "ENCA0007.RAW" }, "Shield Boost",
	    "Shield Boost!"), 
    Random(null, 0, 0, 0, 0, 0, false,
	    Integer.MAX_VALUE, null, "???", "???"); // Not for use in TV
    private final int	   shieldDelta, invisibiltyTimeDeltaMillis,
	    invincibilityTimeDeltaMillis, afterburnerDelta, weaponSupplyDelta;
    private final String[] billboardFrames;
    private final boolean  doublesFiringMultiplex;
    private final int 	   quantityLimit;
    private final String   tvDescription, f3Description;
    private final Weapon   weapon;

    Powerup(Weapon weapon, int weaponSupplyDelta, int shieldDelta,
	    int invisibilityTimeDeltaMillis, int invincibilityTimeDeltaMillis,
	    int afterburnerDelta, boolean doublesFiringMultiplex,
	    int quantityLimit, String[] billboardFrames, String tvDescription,
	    String f3Description) {
	this.shieldDelta 		  = shieldDelta;
	this.invisibiltyTimeDeltaMillis   = invisibilityTimeDeltaMillis;
	this.invincibilityTimeDeltaMillis = invincibilityTimeDeltaMillis;
	this.afterburnerDelta 		  = afterburnerDelta;
	this.billboardFrames 		  = billboardFrames;
	this.doublesFiringMultiplex 	  = doublesFiringMultiplex;
	this.quantityLimit 		  = quantityLimit;
	this.tvDescription 		  = tvDescription;
	this.f3Description 		  = f3Description;
	this.weapon 			  = weapon;
	this.weaponSupplyDelta 		  = weaponSupplyDelta;
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
     * @return the billboardFrames
     */
    public String[] getBillboardFrames() {
	return billboardFrames;
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
}//end Powerup
