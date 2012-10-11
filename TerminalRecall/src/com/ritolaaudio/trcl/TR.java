/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;


import java.awt.Color;
import java.io.File;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class TR
	{
	public static final double unitCircle=65535;
	public static final double crossPlatformScalar=16;
	public static final double mapSquareSize=Math.pow(2, 20)/crossPlatformScalar;
	public static final double mapWidth=mapSquareSize*256;
	public static final double mapCartOffset=mapSquareSize/2.;
	public static final double visibilityDiameterInMapSquares=100;
	public static final double antiGamma=1.6;
	
	static {GLProfile.initSingleton();}
	public static final void wakeUp(File _podFile){resources = new ResourceManager(_podFile);}
	public static final GLProfile glp = GLProfile.get(GLProfile.GL2GL3);
	public static final GLCapabilities capabilities = new GLCapabilities(glp);
	//static{capabilities.setDoubleBuffered(true);}
	public static final GLCanvas canvas = new GLCanvas(capabilities);
	public static final JFrame frame = new JFrame("Terminal Recall");
	public static final int FPS=60;
	public static final FPSAnimator animator = new FPSAnimator(canvas,FPS);
	public static final GLUgl2 glu = new GLUgl2();
	public static final GLUT glut = new GLUT();
	public static ResourceManager resources;
	//private static final ConcurrentLinkedQueue<Runnable> glThreadTaskQueue = new ConcurrentLinkedQueue<Runnable>();
	public static Renderable currentRenderer;
	static
		{
		frame.setSize(800,600);
		frame.setBackground(Color.black);
		frame.add(canvas);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		animator.start();
		}//end static{}
	
	
	public static int bidiMod(int v, int mod)
		{
		while(v<0)v+=mod;
		v%=mod;
		return v;
		}
	}//end TR
