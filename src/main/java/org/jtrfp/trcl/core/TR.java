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
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.BackdropSystem;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.dbg.Reporter;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.tools.Util;

public final class TR{
	public static final double unitCircle=65535;
	public static final double crossPlatformScalar=16;//Shrinks everything so that we can use floats instead of ints
	public static final double mapSquareSize=Math.pow(2, 20)/crossPlatformScalar;
	public static final double mapWidth=mapSquareSize*256;
	public static final double mapCartOffset=mapWidth/2.;//This is the scaled-down version, not the full version
	public static final double visibilityDiameterInMapSquares=35;
	public static final int terrainChunkSideLengthInSquares=4;//Keep at power of two for now. 4x4 = 16. 16x6 = 96. 96 vertices per GLSL block means 1 chunk per block.
	public static final double antiGamma=1.6;
	public static final boolean ANIMATED_TERRAIN=false;
	
	public final TRFutureTask<GPU> gpu;
	private Player player;
	public final RootWindow rootWindow;
	private Color [] globalPalette, darkIsClearPalette;
	private final KeyStatus keyStatus;
	private ResourceManager resourceManager;
	public final ThreadManager threadManager;
	public final TRFutureTask<Renderer> renderer;
	private final CollisionManager collisionManager = new CollisionManager(this);
	private final Reporter reporter = new Reporter();
	private OverworldSystem overworldSystem;
	private InterpolatingAltitudeMap altitudeMap;
	private BackdropSystem backdropSystem;
	private Game game = new Game();
	private NAVSystem navSystem;
	private HUDSystem hudSystem;
	private Mission currentMission;
	
	public final TRFutureTask<MatrixWindow> matrixWindow ;
	public final TRFutureTask<ObjectListWindow> objectListWindow;
	public final TRFutureTask<ObjectDefinitionWindow> objectDefinitionWindow;
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
    public static double legacy2Modern(double v) {
	return v < 0 ? (v + 268435456) / crossPlatformScalar : v
		/ crossPlatformScalar;
    }

    public static double modern2Legacy(double v) {
	v *= crossPlatformScalar;
	return v > 134217727 ? v - 268435456 : v;
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
	
	public TR(){
	    	AutoInitializable.Initializer.initialize(this);
	    	rootWindow = new RootWindow(this);
		keyStatus = new KeyStatus(rootWindow);
		threadManager = new ThreadManager(this);
		gpu = new TRFutureTask<GPU>(this,new Callable<GPU>(){
		    @Override
		    public GPU call() throws Exception {
			Thread.currentThread().setName("GPU constructor");
			return new GPU(TR.this);
		    }});
		threadManager.threadPool.submit(gpu);
		System.out.println("Initializing graphics engine...");
		    renderer=new TRFutureTask<Renderer>(this,new Callable<Renderer>(){
			@Override
			public Renderer call() throws Exception {
			    Thread.currentThread().setName("Renderer constructor.");
			    return new Renderer(TR.this.gpu.get());
			}//end call()
		    });threadManager.threadPool.submit(renderer);
		    matrixWindow=new TRFutureTask<MatrixWindow>(this,new Callable<MatrixWindow>(){
			@Override
			public MatrixWindow call() throws Exception {
			    Thread.currentThread().setName("MatrixWindow constructor.");
			    return new MatrixWindow(TR.this);
			}//end call()
		    });threadManager.threadPool.submit(matrixWindow);
		    objectListWindow=new TRFutureTask<ObjectListWindow>(this,new Callable<ObjectListWindow>(){
			@Override
			public ObjectListWindow call() throws Exception {
			    Thread.currentThread().setName("ObjectListWindow constructor.");
			    return new ObjectListWindow(TR.this);
			}//end call()
		    });threadManager.threadPool.submit(objectListWindow);
		    objectDefinitionWindow=new TRFutureTask<ObjectDefinitionWindow>(this,new Callable<ObjectDefinitionWindow>(){
			@Override
			public ObjectDefinitionWindow call() throws Exception {
			    Thread.currentThread().setName("ObjectDefinitionWindow constructor.");
			    return new ObjectDefinitionWindow(TR.this);
			}//end call()
		    });threadManager.threadPool.submit(objectDefinitionWindow);
		    System.out.println("...Done");
		setResourceManager(new ResourceManager(this));
		world = new World(
				256*mapSquareSize,
				14.*mapSquareSize,
				256*mapSquareSize,
				mapSquareSize*visibilityDiameterInMapSquares/2., this);
		renderer.get().setRootGrid(world);
		}//end constructor
	
    public void showStopper(final Exception e) {
	System.err.println("==== SHOWSTOPPER ====");
			    e.printStackTrace();
	System.err.println("======================");
	System.err.println("\nToo full of fail to continue. Exiting...\n\n");
	System.exit(-1);
	/*
	try {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    int c = JOptionPane.showConfirmDialog(rootWindow,
			    "A component of Terminal Recall triggered a showstopper error:\n"
				    + e.getLocalizedMessage()
				    + "\n\nContinue anyway?", "Uh Oh...",
			    JOptionPane.ERROR_MESSAGE);
		    if (c == 1)
			System.exit(1);
		}
	    });
	} catch (Exception ite) {
	    showStopper(ite);
	}
	showStopper(e);
	*/
    }

    public static int bidiMod(int v, int mod) {
	while (v < 0)
	    v += mod;
	v %= mod;
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
    	    final GPU _gpu = gpu.get();
    	    final Reporter r = getReporter();
    	    _gpu.getGl().getContext().makeCurrent();
    	    r.report("org.jtrfp.trcl.flow.RunMe.glVendor", _gpu.glGetString(GL3.GL_VENDOR));
    	    r.report("org.jtrfp.trcl.flow.RunMe.glRenderer", _gpu.glGetString(GL3.GL_RENDERER));
    	    r.report("org.jtrfp.trcl.flow.RunMe.glVersion", _gpu.glGetString(GL3.GL_VERSION));
    	    r.report("org.jtrfp.trcl.flow.RunMe.availableProcs", Runtime.getRuntime().availableProcessors());
    	    _gpu.getGl().getContext().release();
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
	    }catch(IllegalAccessException e){showStopper(e);}
	    catch(FileLoadException e){showStopper(e);}
	    catch(IOException e){showStopper(e);}
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
	
	public static double sloppyTwosComplimentTaxicabDistanceXZ(double []l, double []r){
	    return deltaRollover(Math.abs(l[0]-r[0])+Math.abs(l[2]-r[2]));
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
	public RootWindow getRootWindow() {
	    return rootWindow;
	}
}//end TR
