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
package org.jtrfp.trcl.core;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL3;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.BackdropSystem;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.dbg.Reporter;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.GPUMemDump;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.tools.Util;

public final class TR
	{
	public static final double unitCircle=65535;
	public static final double crossPlatformScalar=16;//Shrinks everything so that we can use floats instead of ints
	public static final double mapSquareSize=Math.pow(2, 20)/crossPlatformScalar;
	public static final double mapWidth=mapSquareSize*256;
	public static final double mapCartOffset=mapWidth/2.;//This is the scaled-down version, not the full version
	public static final double visibilityDiameterInMapSquares=35;
	public static final int terrainChunkSideLengthInSquares=4;//Keep at power of two for now. 4x4 = 16. 16x6 = 96. 96 vertices per GLSL block means 1 chunk per block.
	public static final double antiGamma=1.6;
	public static final boolean ANIMATED_TERRAIN=false;
	
	private final GPU gpu = new GPU(this);
	private Player player;
	private final JFrame frame = new JFrame("Terminal Recall");
	private Color [] globalPalette, darkIsClearPalette;
	private final KeyStatus keyStatus;
	private ResourceManager resourceManager;
	public static final ExecutorService threadPool = Executors.newCachedThreadPool();//TODO: Migrate to ThreadManager
	private final ThreadManager threadManager;
	private final Renderer renderer;
	private final CollisionManager collisionManager = new CollisionManager(this);
	private final Reporter reporter = new Reporter();
	private OverworldSystem overworldSystem;
	private InterpolatingAltitudeMap altitudeMap;
	private BackdropSystem backdropSystem;
	private Game game = new Game();
	private NAVSystem navSystem;
	private HUDSystem hudSystem;
	private Mission currentMission;
	private final MatrixWindow matrixWindow = new MatrixWindow();
	private final ObjectListWindow objectListWindow = new ObjectListWindow();
	private final TriangleVertexWindow triangleVertexWindow = new TriangleVertexWindow();
	private final TriangleVertex2FlatDoubleWindow tv2fdWindow = new TriangleVertex2FlatDoubleWindow(triangleVertexWindow);
	/*
	private ThreadPoolExecutor threadPool = new ThreadPoolExecutor
			(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors()*2,
			2000,TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>());
	*/
	private TRConfiguration trConfig;
	private GL3 glCache;
	private ByteOrder byteOrder;
	private final World world;
	
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
	public static Vector3D twosComplimentSubtract(Vector3D l, Vector3D r){
	    return new Vector3D(
		    deltaRollover(l.getX()-r.getX()),
		    deltaRollover(l.getY()-r.getY()),
		    deltaRollover(l.getZ()-r.getZ()));
	}
	public static double twosComplimentDistance(Vector3D l, Vector3D r){
	    return twosComplimentSubtract(l,r).getNorm();
	}
	public static double deltaRollover(double v){
	    if(v>mapCartOffset)return v-mapWidth;
	    else if(v<-mapCartOffset)return v+mapWidth;
	    return v;
	}
	
	public TR()
		{
		keyStatus = new KeyStatus(frame);
		try{
		SwingUtilities.invokeAndWait(new Runnable()
			{
			@Override
			public void run(){
			configureMenuBar();
			//frame.setBackground(Color.black);
			frame.getContentPane().add(gpu.getComponent());
			frame.setVisible(true);
			frame.setSize(800,600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//frame.pack();
			}});
		}catch(Exception e){e.printStackTrace();}
		threadManager = new ThreadManager(this);
		gpu.takeGL();
		renderer=new Renderer(gpu);
		gpu.releaseGL();
		setResourceManager(new ResourceManager(this));
		world = new World(
				256*mapSquareSize,
				14.*mapSquareSize,
				256*mapSquareSize,
				mapSquareSize*visibilityDiameterInMapSquares/2., this);
		getRenderer().setRootGrid(world);
		}//end constructor
	
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
		JMenuItem file_exit=new JMenuItem("Exit");
		JMenuItem debugStatesMenuItem = new JMenuItem("Debug States");
		JMenuItem gpuMemDump = new JMenuItem("Dump GPU Memory");
		//Menu item behaviors
		file_exit.addActionListener(new ActionListener(){
		    @Override public void actionPerformed(ActionEvent arg0){System.exit(1);}});
		debugStatesMenuItem.addActionListener(new ActionListener(){
		    @Override public void actionPerformed(ActionEvent ev){reporter.setVisible(true);};});
		gpuMemDump.addActionListener(new ActionListener(){
		    @Override public void actionPerformed(ActionEvent ev){new GPUMemDump(TR.this);};});
		final String showDebugStatesOnStartup = System.getProperty("org.jtrfp.trcl.showDebugStates");
		if(showDebugStatesOnStartup!=null){if(showDebugStatesOnStartup.toUpperCase().contains("TRUE")){reporter.setVisible(true);}}
		file.add(file_exit);
		file.add(gpuMemDump);
		window.add(debugStatesMenuItem);
		frame.getJMenuBar().add(file);
		frame.getJMenuBar().add(window);
		}
	
	public static int bidiMod(int v, int mod)
		{
		while(v<0)v+=mod;
		v%=mod;
		return v;
		}

	/**
	 * @return the trConfig
	 */
	public TRConfiguration getTrConfig(){
	    	if(trConfig==null)trConfig=new TRConfiguration();
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
	public KeyStatus getKeyStatus(){
		return keyStatus;}
	public Color [] getGlobalPalette(){
	    if(globalPalette==null)globalPalette=Util.DEFAULT_PALETTE;
	    return globalPalette;}
	public Color [] getDarkIsClearPalette(){
	    if(darkIsClearPalette==null){
		darkIsClearPalette = new Color[256];
		    for(int i=0; i<256; i++){
			float newAlpha=(float)Math.pow(((globalPalette[i].getRed()+globalPalette[i].getGreen()+globalPalette[i].getBlue())/(3f*255f)),.5);
			darkIsClearPalette[i]=new Color(globalPalette[i].getRed()/255f,globalPalette[i].getGreen()/255f,globalPalette[i].getBlue()/255f,newAlpha);
		    }//end for(colors)
	    }//end if(null)
	    return darkIsClearPalette;
	}//end getDarkIsClearPalette
	
	public GPU getGPU(){return gpu;}

	/**
	 * @return the renderer
	 */
	public Renderer getRenderer()
		{
		return renderer;
		}

	/**
	 * @return the world
	 */
	public World getWorld()
		{
		return world;
		}

	public ThreadManager getThreadManager()
		{return threadManager;}

	public CollisionManager getCollisionManager()
		{return collisionManager;}

	public void setPlayer(Player player)
		{this.player=player;}
	
	public Player getPlayer(){return player;}

	/**
	 * @return the reporter
	 */
	public Reporter getReporter() {
	    return reporter;
	}

	public void setOverworldSystem(OverworldSystem overworldSystem) {
	    this.overworldSystem=overworldSystem;
	}

	/**
	 * @return the overworldSystem
	 */
	public OverworldSystem getOverworldSystem() {
	    return overworldSystem;
	}

	/**
	 * @return the altitudeMap
	 */
	public InterpolatingAltitudeMap getAltitudeMap() {
	    return altitudeMap;
	}

	/**
	 * @param altitudeMap the altitudeMap to set
	 */
	public void setAltitudeMap(InterpolatingAltitudeMap altitudeMap) {
	    this.altitudeMap = altitudeMap;
	}

	/**
	 * @return the backdropSystem
	 */
	public BackdropSystem getBackdropSystem() {
	    return backdropSystem;
	}

	/**
	 * @param backdropSystem the backdropSystem to set
	 */
	public void setBackdropSystem(BackdropSystem backdropSystem) {
	    this.backdropSystem = backdropSystem;
	}

	public Game getGame() {
	    return game;
	}

	/**
	 * @return the navSystem
	 */
	public NAVSystem getNavSystem() {
	    if(navSystem==null){
		navSystem=new NAVSystem(getWorld(),this);
	    }
	    return navSystem;
	}

	/**
	 * @param navSystem the navSystem to set
	 */
	public void setNavSystem(NAVSystem navSystem) {
	    this.navSystem = navSystem;
	}

	/**
	 * @return the hudSystem
	 */
	public HUDSystem getHudSystem() {
	    if(hudSystem==null)hudSystem = new HUDSystem(getWorld());
	    hudSystem.activate();
	    return hudSystem;
	}

	/**
	 * @param hudSystem the hudSystem to set
	 */
	public void setHudSystem(HUDSystem hudSystem) {
	    this.hudSystem = hudSystem;
	}

	public static double legacy2MapSquare(double z) {
	    return ((z/crossPlatformScalar)/mapWidth)*255.;
	}
	
	public void gatherSysInfo(){
    	    final GPU gpu = getGPU();
    	    final Reporter r = getReporter();
    	    GL3 gl = gpu.takeGL();
    	    r.report("org.jtrfp.trcl.flow.RunMe.glVendor", gpu.glGetString(GL3.GL_VENDOR));
    	    r.report("org.jtrfp.trcl.flow.RunMe.glRenderer", gpu.glGetString(GL3.GL_RENDERER));
    	    r.report("org.jtrfp.trcl.flow.RunMe.glVersion", gpu.glGetString(GL3.GL_VERSION));
    	    getGPU().releaseGL();
    	    r.report("org.jtrfp.trcl.flow.RunMe.availableProcs", Runtime.getRuntime().availableProcessors());
    	
    	    for(Entry<Object,Object> prop:System.getProperties().entrySet())
    		{r.report((String)prop.getKey(),prop.getValue());}
    	    }//end gatherSysInfo()

	public void startMissionSequence(String lvlFileName) {
	    recursiveMissionSequence(lvlFileName);
	}
	
	private void recursiveMissionSequence(String lvlFileName){
	    try{
	    currentMission = new Mission(this, getResourceManager().getLVL(lvlFileName));
	    Mission.Result result = currentMission.go();
	    final String nextLVL=result.getNextLVL();
	    if(nextLVL!=null)recursiveMissionSequence(nextLVL);
	    }catch(IllegalAccessException e){e.printStackTrace();}
	    catch(FileLoadException e){e.printStackTrace();}
	    catch(IOException e){e.printStackTrace();}
	}

	/**
	 * @return the currentMission
	 */
	public Mission getCurrentMission() {
	    return currentMission;
	}

	public void setGlobalPalette(Color[] palette) {
	    globalPalette=palette;
	}

	public static double twosComplimentDistance(double[] l,
		double[] r) {
	    final double dx=deltaRollover(l[0]-r[0]);
	    final double dy=deltaRollover(l[1]-r[1]);
	    final double dz=deltaRollover(l[2]-r[2]);
	    return Math.sqrt( (dx)*(dx) + (dy)*(dy) + (dz)*(dz) );
	}

	public static double[] twosComplimentSubtract(double[] l,
		double[] r, double [] dest) {
	    dest[0]=deltaRollover(l[0]-r[0]);
	    dest[1]=deltaRollover(l[1]-r[1]);
	    dest[2]=deltaRollover(l[2]-r[2]);
	    return dest;
	}

	public MatrixWindow getMatrixWindow() {
	    return matrixWindow;
	}

	/**
	 * @return the tv2fdWindow
	 */
	public TriangleVertex2FlatDoubleWindow getTv2fdWindow() {
	    return tv2fdWindow;
	}

	/**
	 * @return the triangleVertexWindow
	 */
	public TriangleVertexWindow getTriangleVertexWindow() {
	    return triangleVertexWindow;
	}

	/**
	 * @return the objectListWindow
	 */
	public ObjectListWindow getObjectListWindow() {
	    return objectListWindow;
	}
}//end TR
