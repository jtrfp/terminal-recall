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

import org.jtrfp.trcl.Weapon;

public interface GameControllerListener
	{
	public void firePressed();
	public void fireReleased();
	public void turboPressed();
	public void turboReleased();
	
	public void updateThrottle(double t);
	
	public void incrementViewMode();
	
	public void updateJoyPos(Point2D.Double pos);
	
	public void updateHeadPan(double pan);
	
	public void setActiveWeapon(Weapon w);
	
	public void togglePause();
	}
