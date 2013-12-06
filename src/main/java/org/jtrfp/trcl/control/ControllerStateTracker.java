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
package org.jtrfp.trcl.control;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import org.jtrfp.trcl.ViewMode;
import org.jtrfp.trcl.Weapon;

public class ControllerStateTracker implements GameControllerListener
	{
	private boolean firing,turbo,paused;
	private double throttle,headPan;
	private ViewMode viewMode;
	private Point2D.Double joyPos;
	private Weapon activeWeapon;
	private GameController target;
	
	public boolean isFiring(){return firing;}
	public boolean isTurbo(){return turbo;}
	/**
	 * Normalized value [0,1] where 0 is minimum and 1 is maximum
	 * @return
	 * @since May 19, 2013
	 */
	public double getThrottle(){return throttle;}
	public ViewMode getViewMode(){return viewMode;}
	/**
	 * Normalized [-1,1] for each axis. Zero is dead center.
	 * @return
	 * @since May 19, 2013
	 */
	public Point2D.Double getJoyPos(){return joyPos;}
	/**
	 * Zero is straight ahead, 1 is 90degreses right, -1 is 90degrees left.
	 * @return
	 * @since May 19, 2013
	 */
	public double getHeadPan(){return headPan;}
	public Weapon getActiveWeapon(){return activeWeapon;}
	public boolean isPaused(){return paused;}
	
	public GameController getTarget(){return target;}
	@Override
	public void firePressed()
		{firing=true;}
	@Override
	public void fireReleased()
		{firing=false;}
	@Override
	public void turboPressed()
		{turbo=true;}
	@Override
	public void turboReleased()
		{turbo=false;}
	@Override
	public void updateThrottle(double t)
		{throttle=t;}
	@Override
	public void incrementViewMode()
		{viewMode=ViewMode.values()[(viewMode.ordinal()+1)%ViewMode.values().length];}
	@Override
	public void updateJoyPos(Double pos)
		{joyPos=pos;}
	@Override
	public void updateHeadPan(double pan)
		{headPan=pan;}
	@Override
	public void setActiveWeapon(Weapon w)
		{activeWeapon=w;}
	@Override
	public void togglePause()
		{paused=!paused;}
	}//ControllerStateTracker
