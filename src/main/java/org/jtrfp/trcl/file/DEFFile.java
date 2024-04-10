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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jtrfp.jfdt.CSV;
import org.jtrfp.jfdt.ClassInclusion;
import org.jtrfp.jfdt.FailureBehavior;
import org.jtrfp.jfdt.IntParser;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jfdt.Parser.ParseMode;

public class DEFFile extends SelfParsingFile implements ThirdPartyParseable {
    public DEFFile(InputStream is) throws IOException, IllegalAccessException {
	super(new BufferedInputStream(is,1024));
    }
    
    public DEFFile() {}

    int numEnemyDefinitions;
    ArrayList<EnemyDefinition> enemyDefinitions;
    int numPlacements;
    ArrayList<EnemyPlacement> enemyPlacements;

    @Override
    public void describeFormat(Parser prs) throws UnrecognizedFormatException {
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("numEnemyDefinitions", int.class), false);
	prs.arrayOf(getNumEnemyDefinitions(), "enemyDefinitions",
		EnemyDefinition.class);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("numPlacements", int.class),
		false);
	prs.arrayOf(getNumPlacements(), "enemyPlacements", EnemyPlacement.class);
    }

    public static class EnemyDefinition implements ThirdPartyParseable {
	EnemyDefinition.EnemyLogic logic;
	int unknown1, boundingBoxRadius, pivotX, pivotY, pivotZ;
	String complexModelFile, simpleModel;
	int thrustSpeed, rotationSpeed, fireSpeed, fireStrength;
	Weapon weapon;
	boolean showOnBriefing, createRandomly;
	int powerupProbability;
	Powerup powerup;
	int numRandomFiringVertices;
	Integer[] firingVertices = new Integer[8];
	// ;NewHit
	//int numNewHBoxes;
	//int[] hboxVertices = new int[16];
	String hitboxVerticesString = "";
	// !NewAtakRet
	int attackDistance, retreatDistance, numHboxVertcies;
	boolean objectIsBoss;
	int unknown;
	String description;
	// #New2ndweapon
	EnemyDefinition.FireSpread fireSpread;
	Weapon secondaryWeapon;
	int secondWeaponDistance, fireVelocity;
	// %SFX
	String bossFireSFXFile;
	String bossYellSFXFile;
	// = cannonDamage, laserDamage, missileDamage
	private Integer laserDamage, missileDamage, cannonDamage;
	private Boolean friendly;
	private String escapeSFX, destroySFX;
	private Integer pathToFollow;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    // LINE 1
	    prs.stringEndingWith(",",
		    prs.property("logic", EnemyDefinition.EnemyLogic.class),
		    false);
	    prs.stringCSVEndingWith(",", int.class, false, "unknown1",
		    "boundingBoxRadius", "pivotX", "pivotY", "pivotZ");
	    prs.stringEndingWith(",",
		    prs.property("complexModelFile", String.class), false);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("simpleModel", String.class), false);
	    // LINE 2 (5 entries)
	    prs.stringCSVEndingWith(",", int.class, false, "thrustSpeed",
		    "rotationSpeed", "fireSpeed", "fireStrength");
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("weapon", Weapon.class),
		    false);
	    // LINE 3 (4 entries)
	    prs.stringEndingWith(",",
		    prs.property("showOnBriefing", boolean.class), false);
	    prs.stringEndingWith(",",
		    prs.property("createRandomly", boolean.class), false);
	    prs.stringEndingWith(",",
		    prs.property("powerupProbability", int.class), false);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("powerup", Powerup.class), false);
	    // LINE 4 (9 entries) / (5 entries)
	    // TODO: This needs to adjust accordingly
	    // TODO: String CSV ending with... routed to List
	    prs.stringEndingWith(",",
		    prs.property("numRandomFiringVertices", int.class), false);
	    prs.stringEndingWith("\r\n", new CSV(new IntParser()),
		    prs.property("firingVertices", int[].class), false);
	    /*
	     * for(int i=0; i<7;i++) {prs.stringEndingWith(",",
	     * prs.indexedProperty("firingVertices", int.class, i), false);}
	     * prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
	     * prs.indexedProperty("firingVertices",int.class,7), false);
	     */
	    try {
		prs.expectString(";NewHit\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(",",
			prs.property("numNewHBoxes", int.class), false);//This is a kludge to handle varying numbers of hitboxes
		prs.stringEndingWith("\r\n", prs.property("hitboxVerticesString", String.class), false);
		/*
		for (int i = 0; i < 15; i++) {
		    prs.stringEndingWith(",",
			    prs.indexedProperty("hboxVertices", int.class, i),
			    false);
		}
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.indexedProperty("hboxVertices", int.class, 15),
			false);// last one, ending in newline.
		*/
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("NewHit not given for this def");
	    }

	    try {
		prs.expectString("!NewAtakRet\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(",",
			prs.property("attackDistance", int.class), false);
		prs.stringEndingWith(",",
			prs.property("retreatDistance", int.class), false);
		prs.stringEndingWith(",",
			prs.property("objectIsBoss", boolean.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("unknown", int.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("NewAttackRet not given for this def");
	    }

	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("description", String.class), false);
	    
	    try {
		prs.expectString("#New2ndweapon\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(",", prs.property("fireSpread",
			EnemyDefinition.FireSpread.class), false);
		prs.stringEndingWith(",",
			prs.property("secondaryWeapon", Weapon.class), false);
		prs.stringEndingWith(",",
			prs.property("secondWeaponDistance", int.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("fireVelocity", int.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("2nd Weapon not given for this def");
	    }
	    
	    prs.ignoreEOF = true;
	    
	    try {
		
		prs.expectString("%SFX\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("bossFireSFXFile", String.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("bossYellSFXFile", String.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("%SFX not given for this def.");
	    }
	    
	    try {
		if(prs.parseMode == ParseMode.WRITE && getPathToFollow() == null)
		    throw new UnrecognizedFormatException();//Skip if not applicable on write
		prs.expectString(": Path to follow\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("pathToFollow", int.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println(": Path to follow not given for this def.");
	    }
	    
	    try {
		if(prs.parseMode == ParseMode.WRITE && (getCannonDamage() == null || getLaserDamage() == null || getMissileDamage() == null))
		    throw new UnrecognizedFormatException();//Skip if not applicable on write
		prs.expectString("= cannonDamage, laserDamage, missileDamage\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("cannonDamage", int.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("laserDamage", int.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("missileDamage", int.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("{cannonDamage, laserDamage, missileDamage} not given for this def.");
	    }
	    
	    try {
		if(prs.parseMode == ParseMode.WRITE && isFriendly() == null)
		    throw new UnrecognizedFormatException();//Skip if not applicable on write
		prs.expectString("@ Friendly flag\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("friendly", boolean.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("SFX not given for this def.");
	    }
	    
	    try {
		if(prs.parseMode == ParseMode.WRITE && getEscapeSFX() == null && getDestroySFX() == null)
		    throw new UnrecognizedFormatException();//Skip if not applicable on write
		prs.expectString("{ Escape and destroy sound files\r\n",
			FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("escapeSFX", String.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.property("destroySFX", String.class), false);
	    } catch (UnrecognizedFormatException e) {
		//System.out.println("Escape and destroy sound files not given for this def.");
	    }
	}// end describeFormat()

	public static enum FireSpread {
	    forward, horiz3, horizVert5;
	}

	public static enum EnemyLogic {
	    groundStatic, 
	    groundTargeting, 
	    flyingDumb, 
	    groundTargetingDumb, 
	    flyingSmart, 
	    bankSpinDrill, 
	    sphereBoss, 
	    flyingAttackRetreatSmart, 
	    splitShipSmart, 
	    groundStaticRuin, 
	    targetHeadingSmart, 
	    targetPitchSmart, 
	    coreBossSmart, 
	    cityBossSmart, 
	    staticFiringSmart, 
	    sittingDuck, 
	    tunnelAttack, 
	    takeoffAndEscape, 
	    fallingAsteroid, 
	    cNome, 
	    cNomeLegs, 
	    cNomeFactory, 
	    geigerBoss, 
	    volcanoBoss, 
	    volcano, 
	    missile, 
	    bob, 
	    alienBoss, 
	    canyonBoss1, 
	    canyonBoss2, 
	    lavaMan, 
	    arcticBoss, 
	    helicopter, 
	    tree, 
	    ceilingStatic, 
	    bobAndAttack, 
	    forwardDrive, 
	    fallingStalag, 
	    attackRetreatBelowSky, 
	    attackRetreatAboveSky, 
	    bobAboveSky, 
	    factory, 
	    shootOnThrustFrame,
	    unknown0,
	    unknown1,
	    unknown2,
	    unknown3,
	    unknown4,
	    unknown5,
	    unknown6,
	    unknown7,
	    unknown8,
	    unknown9,
	    unknown10,
	    unknown11,
	    unknown12,
	    unknown13,
	    unknown14,
	    unknown15,
	    unknown16,
	    unknown17,
	    unknown18,
	    unknown19,
	    unknown20,
	    unknown21,
	    unknown22;
	    // Shoot
	    // on
	    // frame
	    // defined
	    // by
	    // thrust,
	    // smart
	    // target,
	    // yaw-only
	    // rotate
	}// end EnemyLogic

	/**
	 * @return the behavior of this enemy.
	 */
	public EnemyDefinition.EnemyLogic getLogic() {
	    return logic;
	}

	/**
	 * @param logic
	 *            The behavior of this enemy to use.
	 */
	public void setLogic(EnemyDefinition.EnemyLogic logic) {
	    this.logic = logic;
	}

	/**
	 * @return the unknown1
	 */
	public int getUnknown1() {
	    return unknown1;
	}

	/**
	 * @param unknown1
	 *            the unknown1 to set
	 */
	public void setUnknown1(int unknown1) {
	    this.unknown1 = unknown1;
	}

	/**
	 * @return the boundingBoxRadius
	 */
	public int getBoundingBoxRadius() {
	    return boundingBoxRadius;
	}

	/**
	 * @param boundingBoxRadius
	 *            the boundingBoxRadius to set
	 */
	public void setBoundingBoxRadius(int boundingBoxRadius) {
	    this.boundingBoxRadius = boundingBoxRadius;
	}

	/**
	 * The coordinate for the object's Pivot Point in rotation.
	 * 
	 * @return
	 */
	public int getPivotX() {
	    return pivotX;
	}

	public void setPivotX(int x) {
	    this.pivotX = x;
	}

	public int getPivotY() {
	    return pivotY;
	}

	public void setPivotY(int y) {
	    this.pivotY = y;
	}

	public int getPivotZ() {
	    return pivotZ;
	}

	public void setPivotZ(int z) {
	    this.pivotZ = z;
	}

	/**
	 * @return the complexModelFile
	 */
	public String getComplexModelFile() {
	    return complexModelFile;
	}

	/**
	 * @param complexModelFile
	 *            the complexModelFile to set
	 */
	public void setComplexModelFile(String complexModelFile) {
	    this.complexModelFile = complexModelFile;
	}

	/**
	 * @return the simpleModel
	 */
	public String getSimpleModel() {
	    return simpleModel;
	}

	/**
	 * @param simpleModel
	 *            the simpleModel to set
	 */
	public void setSimpleModel(String simpleModel) {
	    this.simpleModel = simpleModel;
	}

	/**
	 * @return the thrustSpeed
	 */
	public int getThrustSpeed() {
	    return thrustSpeed;
	}

	/**
	 * @param thrustSpeed
	 *            the thrustSpeed to set
	 */
	public void setThrustSpeed(int thrustSpeed) {
	    this.thrustSpeed = thrustSpeed;
	}

	/**
	 * @return the rotationSpeed
	 */
	public int getRotationSpeed() {
	    return rotationSpeed;
	}

	/**
	 * @param rotationSpeed
	 *            the rotationSpeed to set
	 */
	public void setRotationSpeed(int rotationSpeed) {
	    this.rotationSpeed = rotationSpeed;
	}

	/**
	 * @return the fireSpeed
	 */
	public int getFireSpeed() {
	    return fireSpeed;
	}

	/**
	 * @param fireSpeed
	 *            the fireSpeed to set
	 */
	public void setFireSpeed(int fireSpeed) {
	    this.fireSpeed = fireSpeed;
	}

	/**
	 * @return the fireStrength
	 */
	public int getFireStrength() {
	    return fireStrength;
	}

	/**
	 * @param fireStrength
	 *            the fireStrength to set
	 */
	public void setFireStrength(int fireStrength) {
	    this.fireStrength = fireStrength;
	}

	/**
	 * @return the weapon
	 */
	public Weapon getWeapon() {
	    return weapon;
	}

	/**
	 * @param weapon
	 *            the weapon to set
	 */
	public void setWeapon(Weapon weapon) {
	    this.weapon = weapon;
	}

	/**
	 * @return the showOnBriefing
	 */
	public boolean isShowOnBriefing() {
	    return showOnBriefing;
	}

	/**
	 * @param showOnBriefing
	 *            the showOnBriefing to set
	 */
	public void setShowOnBriefing(boolean showOnBriefing) {
	    this.showOnBriefing = showOnBriefing;
	}

	/**
	 * @return the createRandomly
	 */
	public boolean isCreateRandomly() {
	    return createRandomly;
	}

	/**
	 * @param createRandomly
	 *            the createRandomly to set
	 */
	public void setCreateRandomly(boolean createRandomly) {
	    this.createRandomly = createRandomly;
	}

	/**
	 * @return the powerupProbability
	 */
	public int getPowerupProbability() {
	    return powerupProbability;
	}

	/**
	 * @param powerupProbability
	 *            the powerupProbability to set
	 */
	public void setPowerupProbability(int powerupProbability) {
	    this.powerupProbability = powerupProbability;
	}

	/**
	 * @return the powerup
	 */
	public Powerup getPowerup() {
	    return powerup;
	}

	/**
	 * @param powerup
	 *            the powerup to set
	 */
	public void setPowerup(Powerup powerup) {
	    this.powerup = powerup;
	}

	/**
	 * @return the numRandomFiringVertices
	 */
	public int getNumRandomFiringVertices() {
	    return numRandomFiringVertices;
	}

	/**
	 * @param numRandomFiringVertices
	 *            the numRandomFiringVertices to set
	 */
	public void setNumRandomFiringVertices(int numRandomFiringVertices) {
	    this.numRandomFiringVertices = numRandomFiringVertices;
	}

	/**
	 * @return the firingVertices
	 */
	public Integer[] getFiringVertices() {
	    return firingVertices;
	}

	/**
	 * @param firingVertices
	 *            the firingVertices to set
	 */
	public void setFiringVertices(Integer[] firingVertices) {
	    this.firingVertices = firingVertices;
	}

	/**
	 * @return the numNewHBoxes
	 */
	public int getNumNewHBoxes() {
	    return this.numHboxVertcies;
	}

	/**
	 * @param numNewHBoxes
	 *            the numNewHBoxes to set
	 */
	public void setNumNewHBoxes(int numNewHBoxes) {
	    this.numHboxVertcies = numNewHBoxes;
	}

	/**
	 * @return the hboxVertices
	 */
	public int[] getHboxVertices() {
	    final String s = getHitboxVerticesString();
	    final String[] ss = s.split("\\s*,\\s*");
	    final String[] result = new String[this.numHboxVertcies];
	    System.arraycopy(ss, 0, result, 0, this.numHboxVertcies);
	    int [] subResult = Stream.of(result).map(str->Integer.parseInt(str)).mapToInt(x->x).toArray();
	    final int [] padded = new int[16];
	    System.arraycopy(subResult, 0, padded, 0, subResult.length);
	    return padded;
	}

	/**
	 * @param hboxVertices
	 *            the hboxVertices to set
	 */
	public void setHboxVertices(int[] hboxVertices) {
	    String s = "";
	    for(int i = 0; i < hboxVertices.length; i++) {
		s+=hboxVertices[i]+"";
		if(i < (hboxVertices.length-1))
		    s+=",";
	    }
	    setHitboxVerticesString(s);
	}//end setHboxVertices(...)

	/**
	 * @return the attackDistance
	 */
	public int getAttackDistance() {
	    return attackDistance;
	}

	/**
	 * @param attackDistance
	 *            the attackDistance to set
	 */
	public void setAttackDistance(int attackDistance) {
	    this.attackDistance = attackDistance;
	}

	/**
	 * @return the retreatDistance
	 */
	public int getRetreatDistance() {
	    return retreatDistance;
	}

	/**
	 * @param retreatDistance
	 *            the retreatDistance to set
	 */
	public void setRetreatDistance(int retreatDistance) {
	    this.retreatDistance = retreatDistance;
	}

	/**
	 * @return the objectIsBoss
	 */
	public boolean isObjectIsBoss() {
	    return objectIsBoss;
	}

	/**
	 * @param objectIsBoss
	 *            the objectIsBoss to set
	 */
	public void setObjectIsBoss(boolean objectIsBoss) {
	    this.objectIsBoss = objectIsBoss;
	}

	/**
	 * @return the unknown
	 */
	public int getUnknown() {
	    return unknown;
	}

	/**
	 * @param unknown
	 *            the unknown to set
	 */
	public void setUnknown(int unknown) {
	    this.unknown = unknown;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
	    return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
	    this.description = description;
	}

	/**
	 * @return the fireSpread
	 */
	public EnemyDefinition.FireSpread getFireSpread() {
	    return fireSpread;
	}

	/**
	 * @param fireSpread
	 *            the fireSpread to set
	 */
	public void setFireSpread(EnemyDefinition.FireSpread fireSpread) {
	    this.fireSpread = fireSpread;
	}

	/**
	 * @return the secondaryWeapon
	 */
	public Weapon getSecondaryWeapon() {
	    return secondaryWeapon;
	}

	/**
	 * @param secondaryWeapon
	 *            the secondaryWeapon to set
	 */
	public void setSecondaryWeapon(Weapon secondaryWeapon) {
	    this.secondaryWeapon = secondaryWeapon;
	}

	/**
	 * @return the secondWeaponDistance
	 */
	public int getSecondWeaponDistance() {
	    return secondWeaponDistance;
	}

	/**
	 * @param secondWeaponDistance
	 *            the secondWeaponDistance to set
	 */
	public void setSecondWeaponDistance(int secondWeaponDistance) {
	    this.secondWeaponDistance = secondWeaponDistance;
	}

	/**
	 * @return the fireVelocity
	 */
	public int getFireVelocity() {
	    return fireVelocity;
	}

	/**
	 * @param fireVelocity
	 *            the fireVelocity to set
	 */
	public void setFireVelocity(int fireVelocity) {
	    this.fireVelocity = fireVelocity;
	}

	/**
	 * @return the bossFireSFXFile
	 */
	public String getBossFireSFXFile() {
	    return bossFireSFXFile;
	}

	/**
	 * @param bossFireSFXFile
	 *            the bossFireSFXFile to set
	 */
	public void setBossFireSFXFile(String bossFireSFXFile) {
	    this.bossFireSFXFile = bossFireSFXFile;
	}

	/**
	 * @return the bossYellSFXFile
	 */
	public String getBossYellSFXFile() {
	    return bossYellSFXFile;
	}

	/**
	 * @param bossYellSFXFile
	 *            the bossYellSFXFile to set
	 */
	public void setBossYellSFXFile(String bossYellSFXFile) {
	    this.bossYellSFXFile = bossYellSFXFile;
	}

	public String getHitboxVerticesString() {
	    return hitboxVerticesString;
	}

	public void setHitboxVerticesString(String hitboxVerticesString) {
	    this.hitboxVerticesString = hitboxVerticesString;
	}

	public int getNumHboxVertcies() {
	    return numHboxVertcies;
	}

	public void setNumHboxVertcies(int numHboxVertcies) {
	    this.numHboxVertcies = numHboxVertcies;
	}

	public Integer getCannonDamage() {
	    return cannonDamage;
	}

	public void setCannonDamage(int cannonDamage) {
	    this.cannonDamage = cannonDamage;
	}

	public Boolean isFriendly() {
	    return friendly;
	}

	public void setFriendly(boolean friendly) {
	    this.friendly = friendly;
	}

	public String getEscapeSFX() {
	    return escapeSFX;
	}

	public void setEscapeSFX(String escapeSFX) {
	    this.escapeSFX = escapeSFX;
	}

	public String getDestroySFX() {
	    return destroySFX;
	}

	public void setDestroySFX(String destroySFX) {
	    this.destroySFX = destroySFX;
	}

	public Integer getPathToFollow() {
	    return pathToFollow;
	}

	public void setPathToFollow(Integer pathToFollow) {
	    this.pathToFollow = pathToFollow;
	}

	public Integer getLaserDamage() {
	    return laserDamage;
	}

	public void setLaserDamage(Integer laserDamage) {
	    this.laserDamage = laserDamage;
	}

	public Integer getMissileDamage() {
	    return missileDamage;
	}

	public void setMissileDamage(Integer missileDamage) {
	    this.missileDamage = missileDamage;
	}

	public Boolean getFriendly() {
	    return friendly;
	}

	public void setFriendly(Boolean friendly) {
	    this.friendly = friendly;
	}

	public void setCannonDamage(Integer cannonDamage) {
	    this.cannonDamage = cannonDamage;
	}
    }// end EnemyDefinition

    public static class EnemyPlacement implements ThirdPartyParseable {
	int defIndex, strength;
	Location3D locationOnMap;
	int pitch, roll, yaw;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.stringCSVEndingWith(",", int.class, false, "defIndex",
		    "strength");
	    prs.subParseProposedClasses(
		    prs.property("locationOnMap", Location3D.class),
		    ClassInclusion.classOf(Location3D.EndingWithComma.class));
	    prs.stringCSVEndingWith("\r\n", int.class, false, "pitch", "roll",
		    "yaw");
	}

	/**
	 * The object index within the object definition section of this DEF
	 * file, starting at 0 to use for this placement.
	 * 
	 * @return the defIndex
	 */
	public int getDefIndex() {
	    return defIndex;
	}

	/**
	 * @param defIndex
	 *            the defIndex to set
	 */
	public void setDefIndex(int defIndex) {
	    this.defIndex = defIndex;
	}

	/**
	 * 
	 * @return strength of the enemy being placed (shields). 65536 is
	 *         strength of the player.
	 */
	public int getStrength() {
	    return strength;
	}

	/**
	 * @param strength
	 *            strength of the enemy being placed (shields). 65536 is
	 *            strength of the player. Zero places no object.
	 */
	public void setStrength(int strength) {
	    this.strength = strength;
	}

	/**
	 * @return the locationOnMap
	 */
	public Location3D getLocationOnMap() {
	    return locationOnMap;
	}

	/**
	 * @param locationOnMap
	 *            the locationOnMap to set
	 */
	public void setLocationOnMap(Location3D locationOnMap) {
	    this.locationOnMap = locationOnMap;
	}

	/**
	 * @return the pitch
	 */
	public int getPitch() {
	    return pitch;
	}

	/**
	 * @param pitch
	 *            the pitch to set
	 */
	public void setPitch(int pitch) {
	    this.pitch = pitch;
	}

	/**
	 * @return the roll
	 */
	public int getRoll() {
	    return roll;
	}

	/**
	 * @param roll
	 *            the roll to set
	 */
	public void setRoll(int roll) {
	    this.roll = roll;
	}

	/**
	 * @return the yaw
	 */
	public int getYaw() {
	    return yaw;
	}

	/**
	 * @param yaw
	 *            the yaw to set
	 */
	public void setYaw(int yaw) {
	    this.yaw = yaw;
	}
	
	@Override
	    public EnemyPlacement clone(){
		EnemyPlacement result = new EnemyPlacement();
		result.setPitch(getPitch());
		result.setStrength(getStrength());
		result.setRoll(getRoll());
		result.setYaw(getYaw());
		result.setStrength(getStrength());
		return result;
	    }//end clone()
    }// end EnemyPlacement

    /**
     * @return the numEnemyDefinitions
     */
    public int getNumEnemyDefinitions() {
	return numEnemyDefinitions;
    }

    /**
     * @param numEnemyDefinitions
     *            the numEnemyDefinitions to set
     */
    public void setNumEnemyDefinitions(int numEnemyDefinitions) {
	this.numEnemyDefinitions = numEnemyDefinitions;
    }

    /**
     * @return the enemydefinitions
     */
    public List<EnemyDefinition> getEnemyDefinitions() {
	return enemyDefinitions;
    }

    /**
     * @param enemydefinitions
     *            the enemydefinitions to set
     */
    public void setEnemyDefinitions(ArrayList<EnemyDefinition> enemydefinitions) {
	this.enemyDefinitions = enemydefinitions;
    }

    /**
     * @return the numPlacements
     */
    public int getNumPlacements() {
	return numPlacements;
    }

    /**
     * @param numPlacements
     *            the numPlacements to set
     */
    public void setNumPlacements(int numPlacements) {
	this.numPlacements = numPlacements;
    }

    /**
     * @return the enemyPlacements
     */
    public List<EnemyPlacement> getEnemyPlacements() {
	return enemyPlacements;
    }

    /**
     * @param enemyPlacements
     *            the enemyPlacements to set
     */
    public void setEnemyPlacements(ArrayList<EnemyPlacement> enemyPlacements) {
	this.enemyPlacements = enemyPlacements;
    }
}// end DEFFile
