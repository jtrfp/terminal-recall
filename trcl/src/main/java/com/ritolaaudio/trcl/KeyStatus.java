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
package com.ritolaaudio.trcl;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyStatus implements KeyListener
	{
	boolean [] keyStates = new boolean[256];
	
	public KeyStatus(Canvas c)
		{
		c.addKeyListener(this);
		}
	
	public boolean isPressed(int index)
		{
		return keyStates[index];
		}
	
	@Override
	public void keyPressed(KeyEvent evt)
		{
		keyStates[evt.getKeyCode()]=true;
		}

	@Override
	public void keyReleased(KeyEvent evt)
		{
		keyStates[evt.getKeyCode()]=false;
		}

	@Override
	public void keyTyped(KeyEvent evt)
		{}
	}//end KeyStatus
