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


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.Game;

import com.jogamp.opengl.util.FPSAnimator;

public final class TR
	{
	public static final double unitCircle=65535;
	public static final double crossPlatformScalar=16;//Shrinks everything so that we can use floats instead of ints
	public static final double mapSquareSize=Math.pow(2, 20)/crossPlatformScalar;
	public static final double mapWidth=mapSquareSize*256;
	public static final double mapCartOffset=mapWidth/2.;//This is the scaled-down version, not the full version
	public static final double visibilityDiameterInMapSquares=35;
	public static final double antiGamma=1.6;
	public static final boolean ANIMATED_TERRAIN=false;
	
	static {GLProfile.initSingleton();}
	
	private final GLProfile glProfile = GLProfile.get(GLProfile.GL2GL3);
	private final GLCapabilities capabilities = new GLCapabilities(glProfile);
	private final GLCanvas canvas = new GLCanvas(capabilities);
	private final JFrame frame = new JFrame("Terminal Recall");
	private Color [] globalPalette;
	public static final int FPS=60;
	private final FPSAnimator animator = new FPSAnimator(canvas,FPS);
	private final KeyStatus keyStatus;
	private ResourceManager resourceManager;
	/*
	private ThreadPoolExecutor threadPool = new ThreadPoolExecutor
			(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors()*2,
			2000,TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>());
	*/
	private TRConfiguration trConfig;
	private GL3 glCache;
	private ByteOrder byteOrder;
	
	/**
	 * Converts legacy coordinate to modern coordinate
	 * @param v
	 * @return
	 * @since Oct 17, 2012
	 */
	public static double legacy2Modern(double v)
		{
		return v<0?(v+268435456)/crossPlatformScalar:v/crossPlatformScalar;
		}
	
	public static double modern2Legacy(double v)
		{
		v*=crossPlatformScalar;
		return v>134217727?v-268435456:v;
		}
	
	public int glGet(int key)
		{
		IntBuffer buf = IntBuffer.wrap(new int[1]);
		glCache.glGetIntegerv(key, buf);
		return buf.get(0);
		}
	
	public String glGetString(int key)
		{
		return this.getGl().glGetString(key);
		}
	
	public TR()
		{
		keyStatus = new KeyStatus(canvas);
		try{
		SwingUtilities.invokeAndWait(new Runnable()
			{
			@Override
			public void run(){
			configureMenuBar();
			//frame.setBackground(Color.black);
			frame.getContentPane().add(canvas);
			frame.setVisible(true);
			frame.setSize(800,600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//frame.pack();
			}});
		}catch(Exception e){e.printStackTrace();}
		setResourceManager(new ResourceManager(this));
		}
	
	public void showStopper(final Exception e)
		{try{
		SwingUtilities.invokeLater(new Runnable(){public void run()
			{
			int c = JOptionPane.showConfirmDialog(getFrame(), "A component of Terminal Recall triggered a showstopper error:\n"+e.getLocalizedMessage()+"\n\nContinue anyway?",
					"Uh Oh...",
					JOptionPane.ERROR_MESSAGE);
			if(c==1)System.exit(1);
			}});
		} catch(Exception ite){ite.printStackTrace();}
		e.printStackTrace();
		}
	
	private void configureMenuBar()
		{
		frame.setJMenuBar(new JMenuBar());
		JMenu file=new JMenu("File"),window=new JMenu("Window");
		
		//And menus to menubar
		JMenuItem /*file_addRemovePODs=new JMenuItem("Add/Remove .POD file(s)"),*/ file_exit=new JMenuItem("Exit");
		//Menu item behaviors
		file_exit.addActionListener(new ActionListener()
					{@Override public void actionPerformed(ActionEvent arg0){System.exit(1);}});
		
		//file.add(file_addRemovePODs);
		file.add(file_exit);
		frame.getJMenuBar().add(file);
		frame.getJMenuBar().add(window);
		}
	
	public GL3 takeGL()
		{getGl().
			getContext().
			makeCurrent(); 
		return getGl();
		}
	
	public void releaseGL()
		{if(getGl().getContext().isCurrent())getGl().getContext().release();}
	
	public static int bidiMod(int v, int mod)
		{
		while(v<0)v+=mod;
		v%=mod;
		return v;
		}

	/**
	 * @return the trConfig
	 */
	public TRConfiguration getTrConfig()
		{
		return trConfig;
		}

	/**
	 * @param trConfig the trConfig to set
	 */
	public void setTrConfig(TRConfiguration trConfig)
		{
		this.trConfig = trConfig;
		}

	/**
	 * @return the gl
	 */
	public GL3 getGl()
		{
		if(glCache!=null)return glCache;
		GL gl1;
		//In case GL is not ready, wait and try again.
		try{for(int i=0; i<10; i++){gl1=canvas.getGL();if(gl1!=null){glCache=gl1.getGL3();System.out.println("getGL returning "+glCache);return glCache;} Thread.sleep(100);}}
		catch(InterruptedException e){e.printStackTrace();}
		return null;
		}

	/**
	 * @return the resourceManager
	 */
	public ResourceManager getResourceManager()
		{
		return resourceManager;
		}

	/**
	 * @param resourceManager the resourceManager to set
	 */
	public void setResourceManager(ResourceManager resourceManager)
		{
		this.resourceManager = resourceManager;
		}

	/**
	 * @return the glProfile
	 */
	public GLProfile getGlProfile()
		{
		return glProfile;
		}

	/**
	 * @return the capabilities
	 */
	public GLCapabilities getCapabilities()
		{
		return capabilities;
		}

	/**
	 * @return the canvas
	 */
	public GLCanvas getCanvas()
		{
		return canvas;
		}

	/**
	 * @return the animator
	 */
	public FPSAnimator getAnimator()
		{
		return animator;
		}

	/**
	 * @return the frame
	 */
	public JFrame getFrame()
		{
		return frame;
		}
	
	public static double [] floats2Doubles(float [] toConvert)
		{
		double [] result = new double[toConvert.length];
		for(int i=0; i<toConvert.length;i++)
			{
			result[i]=toConvert[i];
			}
		return result;
		}//end floats2Doubles
	
	public static float [] doubles2Floats(double [] toConvert)
		{
		float [] result = new float[toConvert.length];
		for(int i=0; i<toConvert.length;i++)
			{
			result[i]=(float)toConvert[i];
			}
		return result;
		}//end floats2Floats

	public boolean isStampingTextures()
		{return false;}

	public Game newGame(VOXFile mission)
		{
		return new Game(this, mission);
		}//end newGame(...)

	/**
	 * @return the keyStatus
	 */
	public KeyStatus getKeyStatus()
		{
		return keyStatus;
		}

	public void setGlobalPalette(Color[] palette)
		{globalPalette = palette;}
	public Color [] getGlobalPalette(){return globalPalette;}

	public ByteOrder getGPUByteOrder()
		{
		if(byteOrder==null)
			{
			byteOrder = System.getProperty("sun.cpu.endian").contentEquals("little")?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN;
			}
		return byteOrder;
		}
	}//end TR
