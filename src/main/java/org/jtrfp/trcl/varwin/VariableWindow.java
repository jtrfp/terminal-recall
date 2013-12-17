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
package org.jtrfp.trcl.varwin;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;

public class VariableWindow extends JFrame
	{
	TR tr;
	JLabel camX=new JLabel(),camY=new JLabel(),camZ=new JLabel();//Scaled down positive coordinates used in game
	JLabel camLX=new JLabel(),camLY=new JLabel(),camLZ=new JLabel();//Legacy coordinates (cartesian*crossplatformScalar)
	JLabel gameX=new JLabel(),gameZ=new JLabel();//Coordinates shown in the game
	public VariableWindow(TR tr)
		{
		super("Variables");
		this.tr=tr;
		JPanel mainPanel = new JPanel();
		setSize(300,300);
		mainPanel.setLayout(new GridLayout(9,2));//rows, cols
		mainPanel.add(new JLabel("Camera X: "));
		mainPanel.add(camX);
		mainPanel.add(new JLabel("Camera Y: "));
		mainPanel.add(camY);
		mainPanel.add(new JLabel("Camera Z: "));
		mainPanel.add(camZ);
		//LEGACY COORDINATES
		mainPanel.add(new JLabel("Camera X(Legacy): "));
		mainPanel.add(camLX);
		mainPanel.add(new JLabel("Camera Y(Legacy): "));
		mainPanel.add(camLY);
		mainPanel.add(new JLabel("Camera Z(Legacy): "));
		mainPanel.add(camLZ);
		//GAME COORDINATES
		mainPanel.add(new JLabel("Game X: "));
		mainPanel.add(gameX);
		mainPanel.add(new JLabel("Game Z: "));
		mainPanel.add(gameZ);
		getContentPane().add(mainPanel);
		this.addWindowListener(new WindowListener()
			{
				@Override
				public void windowActivated(WindowEvent arg0)
					{}

				@Override
				public void windowClosed(WindowEvent arg0)
					{}

				@Override
				public void windowClosing(WindowEvent arg0)
					{VariableWindow.this.setVisible(false);}

				@Override
				public void windowDeactivated(WindowEvent arg0)
					{}

				@Override
				public void windowDeiconified(WindowEvent arg0)
					{}

				@Override
				public void windowIconified(WindowEvent arg0)
					{}

				@Override
				public void windowOpened(WindowEvent arg0)
					{}
			});
		}//end constructor()
	
	public void cameraUpdated(Vector3D newPos)
		{
		camX.setText(""+newPos.getX());camLX.setText(""+((newPos.getX()-TR.mapCartOffset)*TR.crossPlatformScalar));gameX.setText(""+(int)(newPos.getX()/TR.mapSquareSize));
		camY.setText(""+newPos.getY());camLY.setText(""+((newPos.getY()-TR.mapCartOffset)*TR.crossPlatformScalar));
		camZ.setText(""+newPos.getZ());camLZ.setText(""+((newPos.getZ()-TR.mapCartOffset)*TR.crossPlatformScalar));gameZ.setText(""+(int)(newPos.getZ()/TR.mapSquareSize));
		repaint();
		}
	}//VariableWindow
