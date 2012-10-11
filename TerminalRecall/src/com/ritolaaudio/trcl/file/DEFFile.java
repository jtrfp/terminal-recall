/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl.file;

import com.ritolaaudio.jfdt1.ClassInclusion;
import com.ritolaaudio.jfdt1.FailureBehavior;
import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.ThirdPartyParseable;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class DEFFile implements ThirdPartyParseable
	{
	int numEnemyDefinitions;
	EnemyDefinition [] enemyDefinitions;
	int numPlacements;
	EnemyPlacement [] enemyPlacements;
	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.stringEndingWith("\r\n",Parser.property("numEnemyDefinitions", int.class), false);
		Parser.arrayOf(getNumEnemyDefinitions(), "enemyDefinitions", EnemyDefinition.class);
		Parser.stringEndingWith("\r\n", Parser.property("numPlacements",int.class), false);
		Parser.arrayOf(getNumPlacements(), "enemyPlacements", EnemyPlacement.class);
		}
	
	public static class EnemyPlacement implements ThirdPartyParseable
		{
		int defIndex,strength; Location3D locationOnMap; int pitch,roll,yaw;
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.stringCSVEndingWith(",", int.class, false, "defIndex","strength");
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class),ClassInclusion.classOf(Location3D.EndingWithComma.class));
			Parser.stringCSVEndingWith("\r\n", int.class, false, "pitch","roll","yaw");
			}
		/**
		 * The object index within the object definition section of this DEF file, starting at 0 to use
		 * for this placement.
		 * @return the defIndex
		 */
		public int getDefIndex()
			{
			return defIndex;
			}
		/**
		 * @param defIndex the defIndex to set
		 */
		public void setDefIndex(int defIndex)
			{
			this.defIndex = defIndex;
			}
		/**
		 * 
		 * @return strength of the enemy being placed (shields). 65536 is strength of the player.
		 */
		public int getStrength()
			{
			return strength;
			}
		/**
		 * @param strength strength of the enemy being placed (shields). 65536 is strength of the player. Zero places no object.
		 */
		public void setStrength(int strength)
			{
			this.strength = strength;
			}
		/**
		 * @return the locationOnMap
		 */
		public Location3D getLocationOnMap()
			{
			return locationOnMap;
			}
		/**
		 * @param locationOnMap the locationOnMap to set
		 */
		public void setLocationOnMap(Location3D locationOnMap)
			{
			this.locationOnMap = locationOnMap;
			}
		/**
		 * @return the pitch
		 */
		public int getPitch()
			{
			return pitch;
			}
		/**
		 * @param pitch the pitch to set
		 */
		public void setPitch(int pitch)
			{
			this.pitch = pitch;
			}
		/**
		 * @return the roll
		 */
		public int getRoll()
			{
			return roll;
			}
		/**
		 * @param roll the roll to set
		 */
		public void setRoll(int roll)
			{
			this.roll = roll;
			}
		/**
		 * @return the yaw
		 */
		public int getYaw()
			{
			return yaw;
			}
		/**
		 * @param yaw the yaw to set
		 */
		public void setYaw(int yaw)
			{
			this.yaw = yaw;
			}
		}//end EnemyPlacement
	
	public static class EnemyDefinition implements ThirdPartyParseable
		{
		EnemyLogic logic; int unknown1, boundingBoxRadius, pivotX,pivotY,pivotZ; String complexModelFile, simpleModel;
		int thrustSpeed, rotationSpeed, fireSpeed, fireStrength; Weapon weapon;
		boolean showOnBriefing, createRandomly; int powerupProbability; Powerup powerup;
		int numRandomFiringVertices; int [] firingVertices = new int[8];
		//;NewHit
		int numNewHBoxes; int [] hboxVertices = new int[16];
		//!NewAtakRet
		int attackDistance,retreatDistance; boolean objectIsBoss; int unknown;
		String description;
		//#New2ndweapon
		FireSpread fireSpread; Weapon secondaryWeapon; int secondWeaponDistance, fireVelocity;
		//%SFX
		String bossFireSFXFile;
		String bossYellSFXFile;
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.stringEndingWith(",", 	Parser.property("logic", 			EnemyLogic.class), false);
			Parser.stringCSVEndingWith(",", int.class, false, "unknown1","boundingBoxRadius","cx","cy","cz");
			Parser.stringEndingWith(",", 	Parser.property("complexModelFile", String.class),false);
			Parser.stringEndingWith("\r\n", Parser.property("simpleModel", 		String.class),false);
			
			Parser.stringCSVEndingWith(",", int.class, false, "thrustSpeed","rotationSpeed","fireSpeed","fireStrength");
			Parser.stringEndingWith("\r\n", Parser.property("weapon", 			Weapon.class),false);
			
			Parser.stringEndingWith(",", 	Parser.property("showOnBriefing", 	boolean.class),false);
			Parser.stringEndingWith(",", 	Parser.property("createRandomly", 	boolean.class),false);
			Parser.stringEndingWith(",",	Parser.property("powerupProbability",int.class),false);
			Parser.stringEndingWith("\r\n", Parser.property("powerup", 			Powerup.class),false);
			
			Parser.stringEndingWith(",", Parser.property("numRandomFiringVertices", int.class),false);
			for(int i=0; i<7;i++)
				{Parser.stringEndingWith(",", Parser.indexedProperty("firingVertices", int.class, i), false);}
			Parser.stringEndingWith("\r\n", Parser.indexedProperty("firingVertices",int.class,7), false);
			
			Parser.expectString(";NewHit\r\n", FailureBehavior.IGNORE);
			Parser.stringEndingWith(",",Parser.property("numNewHBoxes", int.class), false);
			for(int i=0; i<15; i++)
				{
				//System.out.println(i);
				Parser.stringEndingWith(",", Parser.indexedProperty("hboxVertices", int.class, i), false);
				}
			Parser.stringEndingWith("\r\n", Parser.indexedProperty("hboxVertices",int.class,15), false);//last one, ending in newline.
			
			
			Parser.expectString("!NewAtakRet\r\n", FailureBehavior.IGNORE);
			Parser.stringEndingWith(",", 	Parser.property("attackDistance", 		int.class), 		false);
			Parser.stringEndingWith(",", 	Parser.property("retreatDistance", 		int.class), 		false);
			Parser.stringEndingWith(",", 	Parser.property("objectIsBoss", 		boolean.class), 	false);
			Parser.stringEndingWith("\r\n", Parser.property("unknown", 				int.class), 		false);
			
			Parser.stringEndingWith("\r\n", Parser.property("description", 			String.class), 		false);
			
			Parser.expectString("#New2ndweapon\r\n", null);
			Parser.stringEndingWith(",", 	Parser.property("fireSpread", 			FireSpread.class), 	false);
			Parser.stringEndingWith(",",	Parser.property("secondaryWeapon",		Weapon.class), 		false);
			Parser.stringEndingWith(",", 	Parser.property("secondWeaponDistance", int.class), 		false);
			Parser.stringEndingWith("\r\n", Parser.property("fireVelocity", 		int.class), 		false);
			
			Parser.expectString("%SFX\r\n", null);
			
			Parser.stringEndingWith("\r\n", Parser.property("bossFireSFXFile", 		String.class), 		false);
			Parser.stringEndingWith("\r\n", Parser.property("bossYellSFXFile", 		String.class), 		false);


			}//end describeFormat()
		public static enum FireSpread
			{
			forward,
			horiz3,
			horizVert5;
			}
		public static enum Weapon
			{
			purpleLaser,
			PAC,
			ION,
			RTL,
			fireBall,
			greenLaser,
			redLaser,
			blueLaser,
			bullet,
			purpleBall,
			blueFireBall,
			goldBall,
			atomWeapon,
			purpleRing,
			bossW6,
			bossW7,
			bossW8,
			enemyMissile,
			MAM;
			}
		
		public static enum EnemyLogic
			{
			groundDumb,
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
			factory;
			}//end EnemyLogic

		/**
		 * @return the behavior of this enemy.
		 */
		public EnemyLogic getLogic()
			{
			return logic;
			}

		/**
		 * @param logic The behavior of this enemy to use.
		 */
		public void setLogic(EnemyLogic logic)
			{
			this.logic = logic;
			}

		/**
		 * @return the unknown1
		 */
		public int getUnknown1()
			{
			return unknown1;
			}

		/**
		 * @param unknown1 the unknown1 to set
		 */
		public void setUnknown1(int unknown1)
			{
			this.unknown1 = unknown1;
			}

		/**
		 * @return the boundingBoxRadius
		 */
		public int getBoundingBoxRadius()
			{
			return boundingBoxRadius;
			}

		/**
		 * @param boundingBoxRadius the boundingBoxRadius to set
		 */
		public void setBoundingBoxRadius(int boundingBoxRadius)
			{
			this.boundingBoxRadius = boundingBoxRadius;
			}

		/**
		 * The coordinate for the object's Pivot Point in rotation.
		 * @return 
		 */
		public int getPivotX()
			{
			return pivotX;
			}
		public void setPivotX(int x)
			{
			this.pivotX = x;
			}
		public int getPivotY()
			{
			return pivotY;
			}
		public void setPivotY(int y)
			{
			this.pivotY = y;
			}
		public int getPivotZ()
			{
			return pivotZ;
			}
		public void setPivotZ(int z)
			{
			this.pivotZ = z;
			}

		/**
		 * @return the complexModelFile
		 */
		public String getComplexModelFile()
			{
			return complexModelFile;
			}

		/**
		 * @param complexModelFile the complexModelFile to set
		 */
		public void setComplexModelFile(String complexModelFile)
			{
			this.complexModelFile = complexModelFile;
			}

		/**
		 * @return the simpleModel
		 */
		public String getSimpleModel()
			{
			return simpleModel;
			}

		/**
		 * @param simpleModel the simpleModel to set
		 */
		public void setSimpleModel(String simpleModel)
			{
			this.simpleModel = simpleModel;
			}

		/**
		 * @return the thrustSpeed
		 */
		public int getThrustSpeed()
			{
			return thrustSpeed;
			}

		/**
		 * @param thrustSpeed the thrustSpeed to set
		 */
		public void setThrustSpeed(int thrustSpeed)
			{
			this.thrustSpeed = thrustSpeed;
			}

		/**
		 * @return the rotationSpeed
		 */
		public int getRotationSpeed()
			{
			return rotationSpeed;
			}

		/**
		 * @param rotationSpeed the rotationSpeed to set
		 */
		public void setRotationSpeed(int rotationSpeed)
			{
			this.rotationSpeed = rotationSpeed;
			}

		/**
		 * @return the fireSpeed
		 */
		public int getFireSpeed()
			{
			return fireSpeed;
			}

		/**
		 * @param fireSpeed the fireSpeed to set
		 */
		public void setFireSpeed(int fireSpeed)
			{
			this.fireSpeed = fireSpeed;
			}

		/**
		 * @return the fireStrength
		 */
		public int getFireStrength()
			{
			return fireStrength;
			}

		/**
		 * @param fireStrength the fireStrength to set
		 */
		public void setFireStrength(int fireStrength)
			{
			this.fireStrength = fireStrength;
			}

		/**
		 * @return the weapon
		 */
		public Weapon getWeapon()
			{
			return weapon;
			}

		/**
		 * @param weapon the weapon to set
		 */
		public void setWeapon(Weapon weapon)
			{
			this.weapon = weapon;
			}

		/**
		 * @return the showOnBriefing
		 */
		public boolean isShowOnBriefing()
			{
			return showOnBriefing;
			}

		/**
		 * @param showOnBriefing the showOnBriefing to set
		 */
		public void setShowOnBriefing(boolean showOnBriefing)
			{
			this.showOnBriefing = showOnBriefing;
			}

		/**
		 * @return the createRandomly
		 */
		public boolean isCreateRandomly()
			{
			return createRandomly;
			}

		/**
		 * @param createRandomly the createRandomly to set
		 */
		public void setCreateRandomly(boolean createRandomly)
			{
			this.createRandomly = createRandomly;
			}

		/**
		 * @return the powerupProbability
		 */
		public int getPowerupProbability()
			{
			return powerupProbability;
			}

		/**
		 * @param powerupProbability the powerupProbability to set
		 */
		public void setPowerupProbability(int powerupProbability)
			{
			this.powerupProbability = powerupProbability;
			}

		/**
		 * @return the powerup
		 */
		public Powerup getPowerup()
			{
			return powerup;
			}

		/**
		 * @param powerup the powerup to set
		 */
		public void setPowerup(Powerup powerup)
			{
			this.powerup = powerup;
			}

		/**
		 * @return the numRandomFiringVertices
		 */
		public int getNumRandomFiringVertices()
			{
			return numRandomFiringVertices;
			}

		/**
		 * @param numRandomFiringVertices the numRandomFiringVertices to set
		 */
		public void setNumRandomFiringVertices(int numRandomFiringVertices)
			{
			this.numRandomFiringVertices = numRandomFiringVertices;
			}

		/**
		 * @return the firingVertices
		 */
		public int[] getFiringVertices()
			{
			return firingVertices;
			}

		/**
		 * @param firingVertices the firingVertices to set
		 */
		public void setFiringVertices(int[] firingVertices)
			{
			this.firingVertices = firingVertices;
			}

		/**
		 * @return the numNewHBoxes
		 */
		public int getNumNewHBoxes()
			{
			return numNewHBoxes;
			}

		/**
		 * @param numNewHBoxes the numNewHBoxes to set
		 */
		public void setNumNewHBoxes(int numNewHBoxes)
			{
			this.numNewHBoxes = numNewHBoxes;
			}

		/**
		 * @return the hboxVertices
		 */
		public int[] getHboxVertices()
			{
			return hboxVertices;
			}

		/**
		 * @param hboxVertices the hboxVertices to set
		 */
		public void setHboxVertices(int[] hboxVertices)
			{
			this.hboxVertices = hboxVertices;
			}

		/**
		 * @return the attackDistance
		 */
		public int getAttackDistance()
			{
			return attackDistance;
			}

		/**
		 * @param attackDistance the attackDistance to set
		 */
		public void setAttackDistance(int attackDistance)
			{
			this.attackDistance = attackDistance;
			}

		/**
		 * @return the retreatDistance
		 */
		public int getRetreatDistance()
			{
			return retreatDistance;
			}

		/**
		 * @param retreatDistance the retreatDistance to set
		 */
		public void setRetreatDistance(int retreatDistance)
			{
			this.retreatDistance = retreatDistance;
			}

		/**
		 * @return the objectIsBoss
		 */
		public boolean isObjectIsBoss()
			{
			return objectIsBoss;
			}

		/**
		 * @param objectIsBoss the objectIsBoss to set
		 */
		public void setObjectIsBoss(boolean objectIsBoss)
			{
			this.objectIsBoss = objectIsBoss;
			}

		/**
		 * @return the unknown
		 */
		public int getUnknown()
			{
			return unknown;
			}

		/**
		 * @param unknown the unknown to set
		 */
		public void setUnknown(int unknown)
			{
			this.unknown = unknown;
			}

		/**
		 * @return the description
		 */
		public String getDescription()
			{
			return description;
			}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description)
			{
			this.description = description;
			}

		/**
		 * @return the fireSpread
		 */
		public FireSpread getFireSpread()
			{
			return fireSpread;
			}

		/**
		 * @param fireSpread the fireSpread to set
		 */
		public void setFireSpread(FireSpread fireSpread)
			{
			this.fireSpread = fireSpread;
			}

		/**
		 * @return the secondaryWeapon
		 */
		public Weapon getSecondaryWeapon()
			{
			return secondaryWeapon;
			}

		/**
		 * @param secondaryWeapon the secondaryWeapon to set
		 */
		public void setSecondaryWeapon(Weapon secondaryWeapon)
			{
			this.secondaryWeapon = secondaryWeapon;
			}

		/**
		 * @return the secondWeaponDistance
		 */
		public int getSecondWeaponDistance()
			{
			return secondWeaponDistance;
			}

		/**
		 * @param secondWeaponDistance the secondWeaponDistance to set
		 */
		public void setSecondWeaponDistance(int secondWeaponDistance)
			{
			this.secondWeaponDistance = secondWeaponDistance;
			}

		/**
		 * @return the fireVelocity
		 */
		public int getFireVelocity()
			{
			return fireVelocity;
			}

		/**
		 * @param fireVelocity the fireVelocity to set
		 */
		public void setFireVelocity(int fireVelocity)
			{
			this.fireVelocity = fireVelocity;
			}

		/**
		 * @return the bossFireSFXFile
		 */
		public String getBossFireSFXFile()
			{
			return bossFireSFXFile;
			}

		/**
		 * @param bossFireSFXFile the bossFireSFXFile to set
		 */
		public void setBossFireSFXFile(String bossFireSFXFile)
			{
			this.bossFireSFXFile = bossFireSFXFile;
			}

		/**
		 * @return the bossYellSFXFile
		 */
		public String getBossYellSFXFile()
			{
			return bossYellSFXFile;
			}

		/**
		 * @param bossYellSFXFile the bossYellSFXFile to set
		 */
		public void setBossYellSFXFile(String bossYellSFXFile)
			{
			this.bossYellSFXFile = bossYellSFXFile;
			}
		}//end EnemyDefinition

	/**
	 * @return the numEnemyDefinitions
	 */
	public int getNumEnemyDefinitions()
		{
		return numEnemyDefinitions;
		}

	/**
	 * @param numEnemyDefinitions the numEnemyDefinitions to set
	 */
	public void setNumEnemyDefinitions(int numEnemyDefinitions)
		{
		this.numEnemyDefinitions = numEnemyDefinitions;
		}

	/**
	 * @return the enemydefinitions
	 */
	public EnemyDefinition[] getEnemyDefinitions()
		{
		return enemyDefinitions;
		}

	/**
	 * @param enemydefinitions the enemydefinitions to set
	 */
	public void setEnemyDefinitions(EnemyDefinition[] enemydefinitions)
		{
		this.enemyDefinitions = enemydefinitions;
		}

	/**
	 * @return the numPlacements
	 */
	public int getNumPlacements()
		{
		return numPlacements;
		}

	/**
	 * @param numPlacements the numPlacements to set
	 */
	public void setNumPlacements(int numPlacements)
		{
		this.numPlacements = numPlacements;
		}

	/**
	 * @return the enemyPlacements
	 */
	public EnemyPlacement[] getEnemyPlacements()
		{
		return enemyPlacements;
		}

	/**
	 * @param enemyPlacements the enemyPlacements to set
	 */
	public void setEnemyPlacements(EnemyPlacement[] enemyPlacements)
		{
		this.enemyPlacements = enemyPlacements;
		}
	}//end DEFFile
