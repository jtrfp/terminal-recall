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

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.gui.F3DashboardLayout;
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
    

    public HUDSystem(TR tr, GLFont font, DashboardLayout layout) throws IOException {
	super();
	// Dash Text
	final double TOP_LINE_Y = .93;
	final double BOTTOM_LINE_Y = .82;
	final double FONT_SIZE = .04;
	Point2D.Double pos;
	
	add(dashboard=new Dashboard(tr,layout));
	
	pos = layout.getObjectivePosition();
	objective = new CharLineDisplay(tr, this, FONT_SIZE, 16, font);
	objective.setContent("LOADING...");
	objective.setPosition(pos.getX(), pos.getY(), Z);

	pos = layout.getDistancePosition();
	distance = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	distance.setContent("---");
	distance.setPosition(pos.getX(),pos.getY(), Z);

	pos = layout.getWeaponNamePosition();
	weapon = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	weapon.setContent("---");
	weapon.setPosition(pos.getX(),pos.getY(), Z);

	pos = layout.getSectorPosition();
	sector = new CharLineDisplay(tr, this, FONT_SIZE, 7, font);
	sector.setContent("---");
	sector.setPosition(pos.getX(),pos.getY(), Z);

	pos = layout.getAmmoPosition();
	ammo = new CharLineDisplay(tr, this, FONT_SIZE, 5, font);
	ammo.setContent("---");
	ammo.setPosition(pos.getX(), pos.getY(), Z);
	InputStream is = null;
	add(crosshairs=new Crosshairs(tr));
	try{add(healthMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(ImageIO.read(is = VQTexture.class
			.getResourceAsStream("/OrangeOrangeGradient.png")),null,
			"HealthBar orangeOrange",false), METER_WIDTH, layout.getShieldBarLength(),
		layout.isShieldHorizontal(), "HUDSystem"));}
	finally{if(is!=null)try{is.close();}catch(Exception e){e.printStackTrace();}}
	
	pos = layout.getShieldPosition();
	healthMeterBar.setPosition(new double[]{pos.getX(),pos.getY(),0});
	healthMeter = healthMeterBar.getController();
	
	try{add(throttleMeterBar = new MeterBar(tr, 
		tr.gpu.get().textureManager.get().newTexture(VQTexture.RGBA8FromPNG(is = VQTexture.class
			.getResourceAsStream("/BlueBlackGradient.png")),null,
			"ThrottleBar blackBlue",false), -METER_WIDTH, layout.getThrottleBarLength(),
		layout.isThrottleHorizontal(), "HUDSystem"));}
	finally{if(is!=null)try{is.close();}catch(Exception e){e.printStackTrace();}}
	pos = layout.getThrottlePosition();
	throttleMeterBar.setPosition(new double[]{pos.getX(),pos.getY(),0});
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
