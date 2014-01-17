/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.MeterBar;

public class HUDSystem extends RenderableSpacePartitioningGrid
	{
	private static final double Z=-1;
	private static final double [] HEALTH_POS = new double[]{.13000-1,1-.205,0};
	private static final double [] THROTTLE_POS = new double[]{.18875-1,1-.205,0};
	private static final double METER_WIDTH = .02;
	private static final double METER_HEIGHT = .16;
	private final CharLineDisplay objective;
	private final CharLineDisplay distance;
	private final CharLineDisplay weapon;
	private final CharLineDisplay sector;
	private final CharLineDisplay ammo;
	private final ManuallySetController throttleMeter,healthMeter;
	public HUDSystem(World world){
		super(world);
		//Dash Text
		GLFont font;
		try	{//TODO: Have TR allocate the font ahead of time.
			add(new Dashboard(world.getTr()));
			font = new GLFont(world.getTr().getResourceManager().getFont("capacitor.zip","capacitor.ttf"));
			}
		catch(Exception e){
			System.out.println("Failed to get HUD font.");
			throw new RuntimeException(e);
			}
		
		final double TOP_LINE_Y=.93;
		final double BOTTOM_LINE_Y=.82;
		final double FONT_SIZE=.04;
		
		objective = new CharLineDisplay(world.getTr(),this,FONT_SIZE,16,font);
		objective.setContent("FLY TO JUMP ZONE");
		objective.setPosition(-.45,TOP_LINE_Y,Z);
		//objective.setPosition(new Vector3D(-.45,TOP_LINE_Y,Z));
		
		distance = new CharLineDisplay(world.getTr(),this,FONT_SIZE,5,font);
		distance.setContent("01234");
		distance.setPosition(.42,TOP_LINE_Y,Z);
		
		weapon = new CharLineDisplay(world.getTr(),this,FONT_SIZE,5,font);
		weapon.setContent("RFL20");
		weapon.setPosition(-.44,BOTTOM_LINE_Y,Z);
		
		sector = new CharLineDisplay(world.getTr(),this,FONT_SIZE,7,font);
		sector.setContent("255,255");
		sector.setPosition(.38,BOTTOM_LINE_Y,Z);
		
		ammo = new CharLineDisplay(world.getTr(),this,FONT_SIZE,5,font);
		ammo.setContent("1337");
		ammo.setPosition(.01,BOTTOM_LINE_Y,Z);
		
		add(new Crosshairs(world.getTr()));
		//try{
		    MeterBar mb;
		add(mb=new MeterBar(world.getTr(),
			new DummyFuture<TextureDescription>(new Texture(Texture.RGBA8FromPNG(Texture.class.getResourceAsStream("/OrangeOrangeGradient.png")),"HealthBar orangeOrange")),
			METER_WIDTH,METER_HEIGHT,false));
		mb.setPosition(HEALTH_POS);
		healthMeter = mb.getController();
		add(mb=new MeterBar(world.getTr(),
			new DummyFuture<TextureDescription>(new Texture(Texture.RGBA8FromPNG(Texture.class.getResourceAsStream("/BlueBlackGradient.png")),"ThrottleBar blackBlue")),
			METER_WIDTH,METER_HEIGHT,false));
		mb.setPosition(THROTTLE_POS);
		throttleMeter = mb.getController();
		//}catch(Exception e){e.printStackTrace();}
		}//end constructor
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
	}//end HUDSystem
