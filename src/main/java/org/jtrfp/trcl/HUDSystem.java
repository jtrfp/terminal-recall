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

import java.io.IOException;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.obj.MeterBar;

public class HUDSystem extends RenderableSpacePartitioningGrid {
    private static final double Z = -1;
    private static final double[] HEALTH_POS = new double[] { .13000 - 1,
	    1 - .205, 0 };
    private static final double[] THROTTLE_POS = new double[] { .18875 - 1,
	    1 - .205, 0 };
    private static final double METER_WIDTH = .02;
    private static final double METER_HEIGHT = .16;
    private final CharLineDisplay objective;
    private final CharLineDisplay distance;
    private final CharLineDisplay weapon;
    private final CharLineDisplay sector;
    private final CharLineDisplay ammo;
    private final ManuallySetController throttleMeter, healthMeter;
    private final MeterBar		throttleMeterBar,healthMeterBar;
    private final Dashboard	  dashboard;
    private final Crosshairs	  crosshairs;
    

    public HUDSystem(World world, GLFont font) throws IOException {
	super(world);
	// Dash Text
	final TR tr = world.getTr();
	final ResourceManager rm = tr.getResourceManager();
	final double TOP_LINE_Y = .93;
	final double BOTTOM_LINE_Y = .82;
	final double FONT_SIZE = .04;
	
	add(dashboard=new Dashboard(tr));
	
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
	
	add(crosshairs=new Crosshairs(tr));
	add(healthMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(ImageIO.read(Texture.class
			.getResourceAsStream("/OrangeOrangeGradient.png")),null,
			"HealthBar orangeOrange",false), METER_WIDTH, METER_HEIGHT,
		false));
	
	healthMeterBar.setPosition(HEALTH_POS);
	healthMeter = healthMeterBar.getController();
	add(throttleMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/BlueBlackGradient.png")),null,
			"ThrottleBar blackBlue",false), METER_WIDTH, METER_HEIGHT,
		false));
	throttleMeterBar.setPosition(THROTTLE_POS);
	throttleMeter = throttleMeterBar.getController();
	
	//Set all to invisible
	throttleMeterBar.setVisible(true);
	healthMeterBar.setVisible(true);
	objective.setVisible(true);
	distance.setVisible(true);
	weapon.setVisible(true);
	sector.setVisible(true);
	ammo.setVisible(true);
	crosshairs.setVisible(true);
	dashboard.setVisible(true);
    }// end constructor
    
    
    
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
}// end HUDSystem
