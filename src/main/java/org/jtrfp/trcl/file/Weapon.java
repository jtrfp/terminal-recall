package org.jtrfp.trcl.file;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import org.jtrfp.trcl.file.ModelingType.BINModelingType;
import org.jtrfp.trcl.file.ModelingType.BillboardModelingType;
import org.jtrfp.trcl.file.ModelingType.FlatModelingType;

public enum Weapon{
    	/// THESE ARE LISTED REPRESENTATIVE OF THE ORDINAL ORDER IN A DEF FILE. DO NOT RE-ORDER!!
	purpleLaser(null,null,new FlatModelingType("PURFRIN2.RAW",new Dimension(80000,560000)),2048,ModelingType.MAP_SQUARE_SIZE*12,-1,2048,false),
	PAC("PAC","SKL",new FlatModelingType("BIGEX8.RAW",new Dimension(80000,560000)),2048,ModelingType.MAP_SQUARE_SIZE*12,1,2048,true),
	ION("ION","DC1",new BillboardModelingType(new String[]{"PULSE0.RAW","PULSE1.RAW","PULSE2.RAW"},70,new Dimension(320000,320000)),4096,ModelingType.MAP_SQUARE_SIZE*12,2,2048,false),
	RTL("RTL","RFL20",new FlatModelingType("NEWLASER.RAW",new Dimension(80000,560000)),4096,ModelingType.MAP_SQUARE_SIZE*18,3,2048,true),
	fireBall(null,null,new BINModelingType("FIREBALL.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*8,-1,2048,false),
	greenLaser(null,null,new FlatModelingType("GIDMIS.RAW",new Dimension(80000,560000)),2048,ModelingType.MAP_SQUARE_SIZE*12,-1,2048,false),
	redLaser(null,null,new FlatModelingType("BIGEX8.RAW",new Dimension(80000,560000)),2048,ModelingType.MAP_SQUARE_SIZE*14,-1,2048,false),
	blueLaser(null,null,new FlatModelingType("BFIRJ0.RAW",new Dimension(80000,560000)),2048,ModelingType.MAP_SQUARE_SIZE*15,-1,2048,false),
	bullet(null,null,new BINModelingType("BULLET.BIN"),6554,ModelingType.MAP_SQUARE_SIZE*6,-1,2048,false),
	purpleBall(null,null,new BillboardModelingType(new String[]{"PBALL1.RAW","PBALL3.RAW","PBALL4.RAW"},70,new Dimension(320000,320000)),6554,ModelingType.MAP_SQUARE_SIZE*7,-1,2048,false),
	blueFireBall(null,null,new BillboardModelingType(new String[]{"BFIRJ0.RAW","BFIRJ2.RAW"},100,new Dimension(320000,320000)),6554,ModelingType.MAP_SQUARE_SIZE*12,-1,2048,false),
	goldBall(null,null,new BINModelingType("FIREBALL.BIN"),8000,ModelingType.MAP_SQUARE_SIZE*10,-1,2048,false),
	atomWeapon(null,null,new BillboardModelingType(new String[]{"ATM2.RAW","ATM3.RAW"},70,new Dimension(320000,320000)),10000,ModelingType.MAP_SQUARE_SIZE*8,-1,2048,false),
	purpleRing(null,null,new BillboardModelingType(new String[]{"PURFRIN2.RAW","PURFRIN3.RAW"},70,new Dimension(320000,320000)),8192,ModelingType.MAP_SQUARE_SIZE*6,-1,2048,false),
	bossW6(null,null,new BINModelingType("MISSILE.BIN"),6554,ModelingType.MAP_SQUARE_SIZE*7,-1,2048,false),
	bossW7(null,null,new BillboardModelingType(new String[]{"BFIRJ0.RAW","BFIRJ2.RAW"},100,new Dimension(320000,320000)),8192,ModelingType.MAP_SQUARE_SIZE*9,-1,2048,false),
	bossW8(null,null,new BINModelingType("FIREBALL.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*12,-1,2048,false),
	enemyMissile(null,null,new BINModelingType("MISSILE.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*5,-1,2048,false),
	MAM("MAM","DOM",new BINModelingType("MISSILE.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*5,4,2048,false),
	//////// THESE ARE NOT PART OF THE ORDINAL ORDER OF A DEF FILE AND MAY BE RE-ORDERED
	SAD("SAD","VIP",new BINModelingType("MISSILE.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*5,5,2048,false),
	SWT("SWT","BFM",new BINModelingType("MISSILE.BIN"),8192,ModelingType.MAP_SQUARE_SIZE*7,6,2048,false),
	DAM("DAM","FFF",new FlatModelingType("FIRBAL0.RAW",new Dimension(80000,560000)),Integer.MAX_VALUE,0,7,ModelingType.MAP_SQUARE_SIZE*15,false);
	private final String tvDisplayName,f3DisplayName;
	private final int damage,speed,buttonToSelect;
	private final ModelingType modelingType;
	private final boolean laser;
	Weapon(String tvDisplayName, String f3DisplayName, ModelingType modelingType,
		int damage,int speed, int buttonToSelect, int hitRadius, boolean laser){
	    this.modelingType=modelingType;
	    this.damage=damage;
	    this.speed=speed;
	    this.buttonToSelect=buttonToSelect;
	    this.tvDisplayName=tvDisplayName;
	    this.f3DisplayName=f3DisplayName;
	    this.laser=laser;
	}//end constructor
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
	 * @return the java.awt.event.KeyEvent.KV** constant representing desired button, or Integer.MIN_VALUE if unavailable
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
	public String toString(){
	    return "enum Weapon "+super.toString()+" tvName="+tvDisplayName+" f3Name="+f3DisplayName;
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
}//end Weapon