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

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyStatus implements KeyEventDispatcher
	{
	boolean [] keyStates = new boolean[256];
	
	public KeyStatus(Component c)
		{KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent evt)
		{
		if(evt.getID()==KeyEvent.KEY_PRESSED)
			{keyStates[evt.getKeyCode()]=true;return true;}
		else if(evt.getID()==KeyEvent.KEY_RELEASED)
			{keyStates[evt.getKeyCode()]=false;return true;}
		return false;
		}
	
	public boolean isPressed(int index)
		{return keyStates[index];}
	
	public void keyTyped(KeyEvent evt)
		{}
	}//end KeyStatus
