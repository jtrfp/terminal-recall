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

import java.awt.Dimension;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.ModelingType.BINModelingType;
import org.jtrfp.trcl.file.ModelingType.BillboardModelingType;
import org.jtrfp.trcl.file.ModelingType.FlatModelingType;
/**
 * LASER6.BIN - fireball == FIREBALL.BIN
 * TV-style colored Jenga pieces:
 * LASER.BIN - purple
 * LASER7.BIN - Green
 * LASER8.BIN - Orange-red
 * LASER9.BIN - Blue
 * @author Chuck Ritola
 *
 */
public enum Weapon {
    // / THESE ARE LISTED REPRESENTATIVE OF THE ORDINAL ORDER IN A DEF FILE. DO
    // NOT RE-ORDER!!
    purpleLaser(null, null,new BINModelingType("LASER.BIN"), 4096, 4456448, -1, 2048,
	    false, false), 
    PAC("PAC", "SKL", new BINModelingType("LASER3.BIN"), 4096, //LASER3.BIN
	    2359296, 1, 2048, true, false), 
    ION("ION",
	    "DC1", new BINModelingType("LASER4.BIN"), 8192,
	    1835008, 2, 2048, false, false), 
    RTL(
	    "RTL", "RFL20", new BINModelingType("LASER5.BIN"), 4096, 4456448,
	    3, 2048, true, false), 
    fireBall(null, null, new BINModelingType(
	    "FIREBALL.BIN"), 8192, ModelingType.MAP_SQUARE_SIZE * 8, -1, 1024,
	    false, false), 
    greenLaser(null, null, new BINModelingType("LASER7.BIN"), 8192,
	    1835008, -1, 2048, false, false), 
    redLaser(
	    null, null, new BINModelingType("LASER8.BIN"), 4096, 2359296, -1, 2048,
	    false, false), 
    blueLaser(null, null, new BINModelingType("LASER9.BIN"), 4096,
	    4456448, -1, 2048, false, false), 
    bullet(
	    null, null, new BINModelingType("BULLET.BIN"), 6554,
	    4456448, -1, 2048, false, false), 
    purpleBall(
	    null, null, new BillboardModelingType(new String[] { "PBALL1.RAW",
		    "PBALL3.RAW", "PBALL4.RAW" }, 70, new Dimension(320000,
		    320000)), 6554, 4456448, -1, 2048,
	    false, false), 
    blueFireBall(null, null, new BillboardModelingType(
	    new String[] { "BFIRJ0.RAW", "BFIRJ1.RAW", "BFIRJ2.RAW",
		    "BFIRJ3.RAW" }, 100, new Dimension(320000, 320000)), 6554,
		    4456448, -1, 2048, false, false), 
    goldBall(
	    null, null, new BINModelingType("FIREBALL.BIN"), 8000,
	    4456448, -1, 2048, false, false), 
    atomWeapon(
	    null, null, new BillboardModelingType(new String[] { "ATM2.RAW",
		    "ATM3.RAW" }, 70, new Dimension(320000, 320000)), 10000,
		    4456448, -1, 2048, false, false), 
    purpleRing(
	    null, null, new BillboardModelingType(new String[] {
		    "PURFRIN0.RAW", "PURFRIN1.RAW", "PURFRIN2.RAW",
		    "PURFRIN3.RAW" }, 45, new Dimension(320000, 320000)),
	    8192, 4456448, -1, 2048, false, false), 
    bossW6(
	    null, null, new BINModelingType("WBOSS6.BIN"), 6554,
	    4456448, -1, 2048, false, false), 
    bossW7(
	    null, null, new BINModelingType("WBOSS7.BIN"), 25,
	    4456448, -1, 2048, false, false), 
    bossW8(
	    null, null, new BINModelingType("WBOSS8.BIN"), 8192,
	    4456448, -1, 2048, false, false), 
    enemyMissile(
	    null, null, new BINModelingType("BRADMIS.BIN"), 8192,
	    4194304, -1, 2048, false, false), 
    MAM(
	    "MAM", "DOM", new BINModelingType("BRADMIS.BIN"), 16384,
	    4194304, 4, 2048, false, false),
    // ////// THESE ARE NOT PART OF THE ORDINAL ORDER OF A DEF FILE AND MAY BE
    // RE-ORDERED
    SAD("SAD", "VIP", new BINModelingType("BRADMIS.BIN"), 32768,
	    4194304, 5, 2048, false, true), 
    SWT("SWT",
	    "BFM", new BINModelingType("BRADMIS.BIN"), 65536,
	    4194304, 6, 2048, false, true), 
    DAM("DAM",
	    "FFF", new FlatModelingType("FIRBAL0.RAW","SQGLSR.RAW", new Dimension(80000,
		    560000)), Integer.MAX_VALUE, 0, 7,
		    4194304, false, false);
    private final String       tvDisplayName, f3DisplayName;
    private final int 	       damage, speed, buttonToSelect;
    private final ModelingType modelingType;
    private final boolean      laser, honing;

    Weapon(String tvDisplayName, String f3DisplayName,
	    ModelingType modelingType, int damage, int speed,
	    int buttonToSelect, int hitRadius, boolean laser, boolean honing) {
	this.modelingType 	= modelingType;
	this.damage 		= damage;
	this.speed 		= speed;
	this.buttonToSelect 	= buttonToSelect;
	this.tvDisplayName 	= tvDisplayName;
	this.f3DisplayName 	= f3DisplayName;
	this.laser 		= laser;
	this.honing 		= honing;
    }// end constructor

    /**
     * @return the damage
     */
    public int getDamage() {
	return damage;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
	return speed;
    }

    /**
     * @return the java.awt.event.KeyEvent.KV** constant representing desired
     *         button, or Integer.MIN_VALUE if unavailable
     */
    public int getButtonToSelect() {
	return buttonToSelect;
    }

    /**
     * @return the tvDisplayName
     */
    public String getTvDisplayName() {
	return tvDisplayName;
    }

    /**
     * @return the f3DisplayName
     */
    public String getF3DisplayName() {
	return f3DisplayName;
    }

    @Override
    public String toString() {
	return "enum Weapon " + super.toString() + " tvName=" + tvDisplayName
		+ " f3Name=" + f3DisplayName;
    }

    /**
     * @return the modelingType
     */
    public ModelingType getModelingType() {
	return modelingType;
    }

    /**
     * @return the laser
     */
    public boolean isLaser() {
	return laser;

    }

    /**
     * @return the honing
     */
    public boolean isHoning() {
	return honing;
    }
}// end Weapon
