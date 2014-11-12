/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.core;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.KeyStatus;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.OutputDump;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.dbg.Reporter;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.GameShell;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.tools.Util;

public final class TR{
	public static final double 	unitCircle			=65535;
	public static final double 	crossPlatformScalar		=16;//Shrinks everything so that we can use floats instead of ints
	public static final double 	mapSquareSize			=Math.pow(2, 20)/crossPlatformScalar;
	public static final double 	mapWidth			=mapSquareSize*256;
	public static final double 	mapCartOffset			=mapWidth/2.;//This is the scaled-down version, not the full version
	public static final double 	visibilityDiameterInMapSquares	=35;
	public static final int 	terrainChunkSideLengthInSquares	=4;//Keep at power of two for now. 4x4 = 16. 16x6 = 96. 96 vertices per GLSL block means 1 chunk per block.
	public static final double 	antiGamma			=1.6;
	public static final boolean 	ANIMATED_TERRAIN		=false;
	
	public final TRFutureTask<GPU> 		gpu;
	public final TRFutureTask<SoundSystem>	soundSystem;
	private Player 				player;
	public final RootWindow 		rootWindow;
	private Color [] 			globalPalette, 
						darkIsClearPalette;
	private ColorPaletteVectorList		globalPaletteVL,
						darkIsClearPaletteVL;
	private final KeyStatus 		keyStatus;
	private ResourceManager 		resourceManager;
	public final ThreadManager 		threadManager;
	public final TRFutureTask<Renderer> 	renderer;
	private final CollisionManager 		collisionManager	= new CollisionManager(this);
	private final Reporter 			reporter		= new Reporter();
	private Game 				game;
	
	public final TRFutureTask<MatrixWindow> 		matrixWindow ;
	public final TRFutureTask<ObjectListWindow> 		objectListWindow;
	public final TRFutureTask<ObjectDefinitionWindow> 	objectDefinitionWindow;
	private TRConfiguration []				trConfig;
	private final World 					world;
	private final GameShell					gameShell;
	
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
	    	try{new OutputDump();}
	    	catch(Exception e){e.printStackTrace();}
	    	AutoInitializable.Initializer.initialize(this);
	    	rootWindow = new RootWindow(this);
	    	if(getTrConfig()[0].isWaitForProfiler()){
	    	    waitForProfiler();
	    	}//end if(waitForProfiler)
		keyStatus = new KeyStatus(rootWindow);
		gpu = new TRFutureTask<GPU>(this,new Callable<GPU>(){
		    @Override
		    public GPU call() throws Exception {
			return new GPU(TR.this);
		    }});
		soundSystem = new TRFutureTask<SoundSystem>(this, new Callable<SoundSystem>(){
		    @Override
		    public SoundSystem call() throws Exception {
			return new SoundSystem(TR.this);
		    }});
		threadManager = new ThreadManager(this);
		threadManager.threadPool.submit(gpu);
		threadManager.threadPool.submit(soundSystem);//TODO: Use new methods
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
			    return new MatrixWindow(TR.this);
			}//end call()
		    });threadManager.threadPool.submit(matrixWindow);
		    objectListWindow=new TRFutureTask<ObjectListWindow>(this,new Callable<ObjectListWindow>(){
			@Override
			public ObjectListWindow call() throws Exception {
			    return new ObjectListWindow(TR.this);
			}//end call()
		    });threadManager.threadPool.submit(objectListWindow);
		    objectDefinitionWindow=new TRFutureTask<ObjectDefinitionWindow>(this,new Callable<ObjectDefinitionWindow>(){
			@Override
			public ObjectDefinitionWindow call() throws Exception {
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
		gameShell = new GameShell(this);
		gameShell.startShell();
		}//end constructor
	
    private void waitForProfiler() {
	    JOptionPane.showMessageDialog(rootWindow, "Connect profiler and click OK to continue.","Connect profiler",JOptionPane.OK_OPTION);
	}

    public void showStopper(final Exception e) {
	System.err.println("==== SHOWSTOPPER ====");
			    e.printStackTrace();
	System.err.println("======================");
	System.err.println("\nIrrecoverable. Exiting...\n\n");
	System.exit(-1);
    }

    public static int bidiMod(int v, int mod) {
	while (v < 0)
	    v += mod;
	v %= mod;
	return v;
    }
    
    /**
     * A non-diplomatic way of requesting a GC event.
     * In theory, this call will force (probability=100%) a full sweep including Weak and Soft references.
     * Unlike System.gc() which serves as a suggestion to execute a GC sweep, nuclearGC creates an
     * OutOfMemoryError scenario, forcing a sweep in order for the JVM to keep functioning.
     * <br><br>
     * This was created mainly in response to GPU resources depending on being freed by finalization but the 
     * GC ignoring this need as it focuses on CPU resources only. By ensuring the clearance of unused cache 
     * resources after a level-loading, it can be assured to some degree that unneeded GPU resources will also be
     * released.
     * <br><br>
     * WARNING: This is not a substitute for good programming. If live references are left hanging they will
     * not be freed by the GC, ever. Also note that this will clear any well-designed caching system so it is
     * advised to defer calling this method until the program is at its fullest (post-loading) state to avoid 
     * unnecessary cache misses.
     * 
     * Idea adapted from:
     * http://stackoverflow.com/questions/3785713/how-to-make-the-java-system-release-soft-references>
     * 
     * @since Sep 15, 2014
     */
    public static void nuclearGC(){
	try{
	    final ArrayList<byte[]> spaceHog = new ArrayList<byte[]>();
	    while(true){
		spaceHog.add(new byte[1024*1024*16]);//16MB
	    }
	}catch(OutOfMemoryError e){}
	//Still alive? Great!
	System.gc();
    }//end nuclearGC()

	/**
	 * @return the trConfig
	 */
	public TRConfiguration[] getTrConfig(){
	    	if(trConfig==null){
	    	    trConfig=new TRConfiguration[1]; 
	    	    trConfig[0]=TRConfiguration.getConfig();}
		return trConfig;
		}

    /**
     * @return the resourceManager
     */
    public ResourceManager getResourceManager() {
	return resourceManager;
    }

    /**
     * @param resourceManager
     *            the resourceManager to set
     */
    public void setResourceManager(ResourceManager resourceManager) {
	this.resourceManager = resourceManager;
    }

    public static double[] floats2Doubles(float[] toConvert) {
	double[] result = new double[toConvert.length];
	for (int i = 0; i < toConvert.length; i++) {
	    result[i] = toConvert[i];
	}
	return result;
    }// end floats2Doubles

    public static float[] doubles2Floats(double[] toConvert) {
	float[] result = new float[toConvert.length];
	for (int i = 0; i < toConvert.length; i++) {
	    result[i] = (float) toConvert[i];
	}
	return result;
    }// end floats2Floats

    public boolean isStampingTextures() {
	return false;
    }

    public Game newGame(VOXFile mission) {
	return game = new Game(this, mission);
    }// end newGame(...)

    /**
     * @return the keyStatus
     */
    public KeyStatus getKeyStatus() {
	return keyStatus;
    }

    public Color[] getGlobalPalette() {
	if (globalPalette == null)
	    globalPalette = Util.DEFAULT_PALETTE;
	return globalPalette;
    }

    public Color[] getDarkIsClearPalette() {
	if (darkIsClearPalette == null) {
	    darkIsClearPalette = new Color[256];
	    for (int i = 0; i < 256; i++) {
		float newAlpha = (float) Math
			.pow(((globalPalette[i].getRed()
				+ globalPalette[i].getGreen() + globalPalette[i]
				.getBlue()) / (3f * 255f)), .5);
		darkIsClearPalette[i] = new Color(
			globalPalette[i].getRed() / 255f,
			globalPalette[i].getGreen() / 255f,
			globalPalette[i].getBlue() / 255f, newAlpha);
	    }// end for(colors)
	}// end if(null)
	return darkIsClearPalette;
    }// end getDarkIsClearPalette

    /**
     * @return the world
     */
    public World getWorld() {
	return world;
    }

    public ThreadManager getThreadManager() {
	return threadManager;
    }

    public CollisionManager getCollisionManager() {
	return collisionManager;
    }

    public void setPlayer(Player player) {
	this.player = player;
    }

    public Player getPlayer() {
	return player;
    }

    /**
     * @return the reporter
     */
    public Reporter getReporter() {
	return reporter;
    }

    public Game getGame() {
	return game;
    }

    public static double legacy2MapSquare(double z) {
	return ((z / crossPlatformScalar) / mapWidth) * 255.;
    }

    public void gatherSysInfo() {
	final GPU _gpu = gpu.get();
	final Reporter r = getReporter();
	getThreadManager().submitToGL(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		r.report("org.jtrfp.trcl.flow.RunMe.glVendor",
			_gpu.glGetString(GL3.GL_VENDOR));
		r.report("org.jtrfp.trcl.flow.RunMe.glRenderer",
			_gpu.glGetString(GL3.GL_RENDERER));
		r.report("org.jtrfp.trcl.flow.RunMe.glVersion",
			_gpu.glGetString(GL3.GL_VERSION));
		r.report("org.jtrfp.trcl.flow.RunMe.availableProcs", Runtime
			.getRuntime().availableProcessors());
		for (Entry<Object, Object> prop : System.getProperties()
			.entrySet()) {
		    r.report((String) prop.getKey(), prop.getValue());
		}
		return null;
	    }// end call()
	}).get();
    }// end gatherSysInfo()

    public void setGlobalPalette(Color[] palette) {
	globalPalette = palette;
    }

    public static double sloppyTwosComplimentTaxicabDistanceXZ(double[] l,
	    double[] r) {
	return deltaRollover(Math.abs(l[0] - r[0]) + Math.abs(l[2] - r[2]));
    }

    public static double twosComplimentDistance(double[] l, double[] r) {
	final double dx = deltaRollover(l[0] - r[0]);
	final double dy = deltaRollover(l[1] - r[1]);
	final double dz = deltaRollover(l[2] - r[2]);
	return Math.sqrt((dx) * (dx) + (dy) * (dy) + (dz) * (dz));
    }

    public static double[] twosComplimentSubtract(double[] l, double[] r,
	    double[] dest) {
	dest[0] = deltaRollover(l[0] - r[0]);
	dest[1] = deltaRollover(l[1] - r[1]);
	dest[2] = deltaRollover(l[2] - r[2]);
	return dest;
    }

    public RootWindow getRootWindow() {
	return rootWindow;
    }

    public ColorPaletteVectorList getGlobalPaletteVL() {
	if(globalPaletteVL==null)
	    globalPaletteVL=new ColorPaletteVectorList(getGlobalPalette());
	return globalPaletteVL;
    }

    public ColorPaletteVectorList getDarkIsClearPaletteVL() {
	if(darkIsClearPaletteVL==null)
	    darkIsClearPaletteVL=new ColorPaletteVectorList(getDarkIsClearPalette());
	return darkIsClearPaletteVL;
    }

    public GameShell getGameShell() {
	return gameShell;
    }
}//end TR
