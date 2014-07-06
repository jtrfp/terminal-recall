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
package org.jtrfp.trcl;

import java.util.Timer;
import java.util.TimerTask;

import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.obj.MeterBar;
import org.jtrfp.trcl.obj.Sprite2D;

public class HUDSystem extends RenderableSpacePartitioningGrid {
    private static final double Z = -1;
    private static final double[] HEALTH_POS = new double[] { .13000 - 1,
	    1 - .205, 0 };
    private static final double[] THROTTLE_POS = new double[] { .18875 - 1,
	    1 - .205, 0 };
    private static final double[] LOADING_POS = new double[] { 0,.1,0};
    private static final double LOADING_WIDTH=.04;
    private static final double LOADING_LENGTH=.7;
    private static final double METER_WIDTH = .02;
    private static final double METER_HEIGHT = .16;
    private static final int UPFRONT_HEIGHT = 23;
    private int upfrontDisplayCountdown = 0;
    private final CharLineDisplay objective;
    private final CharLineDisplay distance;
    private final CharLineDisplay weapon;
    private final CharLineDisplay sector;
    private final CharLineDisplay ammo;
    private final CharLineDisplay upfrontBillboard;
    private final ManuallySetController throttleMeter, healthMeter, loadingMeter;
    private final MeterBar		throttleMeterBar,healthMeterBar,loadingMeterBar;
    private final Timer timer = new Timer();
    private final Dashboard	  dashboard;
    private final Crosshairs	  crosshairs;
    private final CharLineDisplay startupText;
    private final Sprite2D	  startupLogo;
    

    public HUDSystem(World world) {
	super(world);
	// Dash Text
	final TR tr = world.getTr();
	final ResourceManager rm = tr.getResourceManager();
	final GLFont font, upfrontFont;
	final double TOP_LINE_Y = .93;
	final double BOTTOM_LINE_Y = .82;
	final double FONT_SIZE = .04;
	
	startupLogo = new Sprite2D(tr, .000000001, .9, .9, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/TrclLogo.png")), "logoImage", false), true);
	add(startupLogo);
	startupLogo.setPosition(0,0,Z);
	startupLogo.notifyPositionChange();
	startupLogo.setActive(true);
	startupLogo.setVisible(true);
	
	font = new GLFont(rm.getFont("capacitor.zip", "capacitor.ttf"),tr);
	
	startupText = new CharLineDisplay(tr,this,FONT_SIZE, 32, font);
	startupText.setCentered(true);
	startupText.setPosition(0,0,Z);
	startupText.setContent("Reticulating Splines...");
	
	add(dashboard=new Dashboard(tr));
	NDXFile ndx = rm.getNDXFile("STARTUP\\FONT.NDX");
	upfrontFont = new GLFont(rm.getFontBIN("STARTUP\\FONT.BIN", ndx),
		    UPFRONT_HEIGHT, ndx.getWidths(), 32,tr);
	
	objective = new CharLineDisplay(tr, this, FONT_SIZE, 16, font);
	objective.setContent("LOADING...");
	objective.setPosition(-.45, TOP_LINE_Y, Z);

	distance = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	distance.setContent("---");
	distance.setPosition(.42, TOP_LINE_Y, Z);

	weapon = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	weapon.setContent("---");
	weapon.setPosition(-.44, BOTTOM_LINE_Y, Z);

	sector = new CharLineDisplay(tr, this, FONT_SIZE, 7, font);
	sector.setContent("---");
	sector.setPosition(.38, BOTTOM_LINE_Y, Z);

	ammo = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	ammo.setContent("---");
	ammo.setPosition(.01, BOTTOM_LINE_Y, Z);

	upfrontBillboard = new CharLineDisplay(tr, this, .2, 35, upfrontFont);
	/*
	 * CharLineDisplay upfrontBillboard0 = new
	 * CharLineDisplay(tr,this,.22,35,upfrontFont); CharLineDisplay
	 * upfrontBillboard1 = new CharLineDisplay(tr,this,.22,35,upfrontFont);
	 * CharLineDisplay upfrontBillboard2 = new
	 * CharLineDisplay(tr,this,.22,35,upfrontFont);
	 * upfrontBillboard.setContent("Shaquille O'Neal is");
	 * upfrontBillboard0.setContent("quite possibly");
	 * upfrontBillboard1.setContent("the greatest actor");
	 * upfrontBillboard2.setContent("of all time.");
	 */
	upfrontBillboard.setPosition(0, .2, Z);
	upfrontBillboard.setVisible(false);
	upfrontBillboard.setCentered(true);
	/*
	 * upfrontBillboard0.setPosition(-.8,0,Z);
	 * upfrontBillboard1.setPosition(-.8,-.2,Z);
	 * upfrontBillboard2.setPosition(-.8,-.4,Z);
	 */
	add(crosshairs=new Crosshairs(tr));
	add(loadingMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/BlueWhiteGradient.png")),
			"LoadingBar blackBlue",false), LOADING_WIDTH, LOADING_LENGTH,
		true));
	loadingMeterBar.setPosition(LOADING_POS);
	loadingMeter = loadingMeterBar.getController();
	add(healthMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/OrangeOrangeGradient.png")),
			"HealthBar orangeOrange",false), METER_WIDTH, METER_HEIGHT,
		false));
	healthMeterBar.setPosition(HEALTH_POS);
	healthMeter = healthMeterBar.getController();
	add(throttleMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/BlueBlackGradient.png")),
			"ThrottleBar blackBlue",false), METER_WIDTH, METER_HEIGHT,
		false));
	throttleMeterBar.setPosition(THROTTLE_POS);
	throttleMeter = throttleMeterBar.getController();
	tr.getThreadManager().getLightweightTimer()
		.scheduleAtFixedRate(new TimerTask() {
		    @Override
		    public void run() {
			try{
			if(upfrontDisplayCountdown>0)
			    upfrontDisplayCountdown -= 200;
			if (upfrontDisplayCountdown <= 0
				&& upfrontDisplayCountdown != Integer.MIN_VALUE) {
			    upfrontBillboard.setVisible(false);
			    upfrontDisplayCountdown = Integer.MIN_VALUE;
			}// end if(timeout)
			}catch(Exception e){e.printStackTrace();}
		    }
		}, 1, 200);
	//Set all to invisible
	throttleMeterBar.setVisible(false);
	healthMeterBar.setVisible(false);
	loadingMeterBar.setVisible(false);
	upfrontBillboard.setVisible(false);
	objective.setVisible(false);
	distance.setVisible(false);
	weapon.setVisible(false);
	sector.setVisible(false);
	ammo.setVisible(false);
	crosshairs.setVisible(false);
	dashboard.setVisible(false);
    }// end constructor

    public HUDSystem submitMomentaryUpfrontMessage(String message) {
	upfrontBillboard.setContent(message);
	upfrontDisplayCountdown = 2000;
	upfrontBillboard.setVisible(true);
	return this;
    }
    
    public HUDSystem earlyLoadingMode(){
	startupLogo.setVisible(true);
	startupText.setVisible(true);
	upfrontBillboard.setVisible(false);
	healthMeterBar.setVisible(false);
	throttleMeterBar.setVisible(false);
	loadingMeterBar.setVisible(false);
	objective.setVisible(false);
	distance.setVisible(false);
	weapon.setVisible(false);
	sector.setVisible(false);
	ammo.setVisible(false);
	crosshairs.setVisible(false);
	dashboard.setVisible(false);
	return this;
    }
    
    public HUDSystem loadingMode(String levelName){
	startupLogo.setVisible(false);
	startupText.setVisible(false);
	upfrontBillboard.setContent(levelName);
	upfrontDisplayCountdown=Integer.MAX_VALUE;
	upfrontBillboard.setVisible(true);
	healthMeterBar.setVisible(false);
	throttleMeterBar.setVisible(false);
	loadingMeterBar.setVisible(true);
	objective.setVisible(false);
	distance.setVisible(false);
	weapon.setVisible(false);
	sector.setVisible(false);
	ammo.setVisible(false);
	crosshairs.setVisible(false);
	dashboard.setVisible(false);
	return this;
    }
    
    public HUDSystem setLoadingProgress(double unitPercent){
	loadingMeter.setFrame(1.-unitPercent);
	return this;
    }
    
    public HUDSystem gameplayMode(){
	startupLogo.setVisible(false);
	startupText.setVisible(false);
	upfrontDisplayCountdown=0;
	upfrontBillboard.setVisible(false);
	healthMeterBar.setVisible(true);
	throttleMeterBar.setVisible(true);
	loadingMeterBar.setVisible(false);
	objective.setVisible(true);
	distance.setVisible(true);
	weapon.setVisible(true);
	sector.setVisible(true);
	ammo.setVisible(true);
	crosshairs.setVisible(true);
	dashboard.setVisible(true);
	return this;
    }

    /**
     * @return the objective
     */
    public CharLineDisplay getObjective() {
	return objective;
    }

    /**
     * @return the distance
     */
    public CharLineDisplay getDistance() {
	return distance;
    }

    /**
     * @return the weapon
     */
    public CharLineDisplay getWeapon() {
	return weapon;
    }

    /**
     * @return the sector
     */
    public CharLineDisplay getSector() {
	return sector;
    }

    /**
     * @return the ammo
     */
    public CharLineDisplay getAmmo() {
	return ammo;
    }

    /**
     * @return the throttleMeter
     */
    public ManuallySetController getThrottleMeter() {
	return throttleMeter;
    }

    /**
     * @return the healthMeter
     */
    public ManuallySetController getHealthMeter() {
	return healthMeter;
    }

    public void startupMessage(String string) {
	startupText.setContent(string);
    }
}// end HUDSystem
