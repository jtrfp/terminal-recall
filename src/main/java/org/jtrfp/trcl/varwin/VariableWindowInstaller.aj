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
package com.ritolaaudio.trcl.varwin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.ritolaaudio.trcl.TR;
import com.ritolaaudio.trcl.World;

public aspect VariableWindowInstaller 
	{
	after()returning(final TR tr):call(TR.new())
		{
		//Add the window
		tr.setVariableWindow(new VariableWindow(tr));
		//Add item to Window menu
		JMenuBar bar = tr.getFrame().getJMenuBar();
		for(int i=0; i<bar.getMenuCount(); i++)
			{
			if(bar.getMenu(i).
					getText().
					contentEquals("Window"))
				{
				JMenuItem it = new JMenuItem("Variables");
				it.addActionListener(new ActionListener()
					{
					@Override
					public void actionPerformed(ActionEvent arg0)
						{
						tr.variableWindow.setVisible(true);
						}//end actionPerformed(...)
					});
				bar.getMenu(i).add(it);
				tr.getFrame().invalidate();
				return;
				}//end if(Window menu)
			}//end for(menus)
		//TODO: Report an error because Window menu wasn't found.
		}//end returning()
	
	after(Vector3D newPos):call(public void World.setCameraPosition(Vector3D)) && args(newPos)
	{((World)thisJoinPoint.getTarget()).getTr().getVariableWindow().cameraUpdated(newPos);}
	//New var declarations
	public VariableWindow TR.variableWindow;
	public VariableWindow TR.getVariableWindow(){return variableWindow;}
	public void TR.setVariableWindow(VariableWindow w){variableWindow=w;}
	}//end VariableWindowInstaller
