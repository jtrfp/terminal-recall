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

public class HUDSystem extends RenderableSpacePartitioningGrid
	{
	public HUDSystem(World world)
		{
		super(world.getRootGrid());
		
		//Dash Text
		GLFont font;
		try
			{//TODO: Have TR allocate the font ahead of time.
			addAlwaysVisible(new Dashboard(world,world.getTr()));
			font = new GLFont(world.getTr().getResourceManager().getFont("capacitor"));
			}
		catch(Exception e)
			{
			System.out.println("Failed to get HUD font.");
			e.printStackTrace();
			return;
			}
		
		final double TOP_LINE_Y=.93;
		final double BOTTOM_LINE_Y=.82;
		final double FONT_SIZE=.04;
		
		CharLineDisplay objective = new CharLineDisplay(this,FONT_SIZE,16,font);
		objective.setContent("FLY TO JUMP ZONE");
		objective.setPosition(new Vector3D(-.45,TOP_LINE_Y,0));
		
		CharLineDisplay distance = new CharLineDisplay(this,FONT_SIZE,5,font);
		distance.setContent("01234");
		distance.setPosition(new Vector3D(.42,TOP_LINE_Y,0));
		
		CharLineDisplay weapon = new CharLineDisplay(this,FONT_SIZE,5,font);
		weapon.setContent("RFL20");
		weapon.setPosition(new Vector3D(-.44,BOTTOM_LINE_Y,0));
		
		CharLineDisplay sector = new CharLineDisplay(this,FONT_SIZE,7,font);
		sector.setContent("255,255");
		sector.setPosition(new Vector3D(.38,BOTTOM_LINE_Y,0));
		
		CharLineDisplay ammo = new CharLineDisplay(this,FONT_SIZE,5,font);
		ammo.setContent("1337");
		ammo.setPosition(new Vector3D(.01,BOTTOM_LINE_Y,0));
		
		addAlwaysVisible(new Crosshairs(world));
		}//end constructor
	}//end HUDSystem
