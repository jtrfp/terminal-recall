/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.OutputDump;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.TRConfigRootFactory.TRConfigRoot;
import org.jtrfp.trcl.ctl.ControllerInputsFactory.ControllerInputs;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.ConfigWindowFactory.ConfigWindow;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.CollisionManager;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.tools.Util;
import org.springframework.stereotype.Component;

@Component
public final class TRFactory implements FeatureFactory<Features>{
    //// BEAN PROPERTIES
    public static final String      RUN_STATE                       ="runState";

    public static final String []   EXIT_MENU_PATH = new String[]  {"File","Exit"};
    public static final String []   CONFIG_MENU_PATH = new String[]{"File","Configure..."};
    public static final double 	unitCircle			=65535;
    public static final double 	crossPlatformScalar		=16;//Shrinks everything so that we can use floats instead of ints
    public static final double 	mapSquareSize			=Math.pow(2, 20)/crossPlatformScalar;
    public static final double 	mapWidth			=mapSquareSize*256;
    public static final double 	mapCartOffset			=mapWidth/2.;//This is the scaled-down version, not the full version
    public static final double 	visibilityDiameterInMapSquares	=27;
    public static final int 	terrainChunkSideLengthInSquares	=16;//Keep at multiple of 4 which divides into 256 for now. 4x4 = 16. 16x6 = 96. 96 vertices per GLSL block means 1 chunk per block.
    public static final double 	antiGamma			=1.6;
    public static final boolean 	ANIMATED_TERRAIN		=false;

    public interface TRRunState{}
    public interface TRConstructing extends TRRunState{}
    public interface TRConstructed  extends TRRunState{}
    public interface TRDestructing  extends TRRunState{}
    public interface TRDestructed   extends TRRunState{}

    public static double legacy2MapSquare(double z) {
	return ((legacy2Modern(z)/TRFactory.mapSquareSize)+256)%256;
    }

    public static int modernToMapSquare(double z){
	return (int)(((z/TRFactory.mapSquareSize)+256)%256);
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
	    synchronized(isInNuclearGC){
		if(isInNuclearGC.get())
		    try{isInNuclearGC.wait();return;}catch(Exception e){e.printStackTrace();}
		isInNuclearGC.set(true);
	    }
	    final ArrayList<byte[]> spaceHog = new ArrayList<byte[]>();
	    while(true){
		spaceHog.add(new byte[1024*1024*16]);//16MB
	    }
	}catch(OutOfMemoryError e){
	    System.runFinalization();
	}
	//Still alive? Great!
	synchronized(isInNuclearGC){
	    isInNuclearGC.set(false);
	    isInNuclearGC.notifyAll();
	}
    }//end nuclearGC()

    private static AtomicBoolean isInNuclearGC = new AtomicBoolean(false);

    public static double rolloverDistance(double distance) {
	if(distance > TRFactory.mapWidth/2)
	    distance = TRFactory.mapWidth-distance;
	return distance;
    }

    public static int bidiMod(int v, int mod) {
	while (v < 0)
	    v += mod;
	v %= mod;
	return v;
    }

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

    public static double sloppyTwosComplimentTaxicabDistanceXZ(double[] l,
	    double[] r) {
	return deltaRollover(Math.abs(l[0] - r[0]) + Math.abs(l[2] - r[2]));
    }

    public static double twosComplimentDistance(double[] l, double[] r) {
	final double dx = deltaRollover(l[0] - r[0]);
	final double dy = deltaRollover(l[1] - r[1]);
	final double dz = deltaRollover(l[2] - r[2]);
	assert !Vect3D.isAnyNaN(l);
	assert !Vect3D.isAnyNaN(r);
	return Math.sqrt((dx) * (dx) + (dy) * (dy) + (dz) * (dz));
    }

    public static double[] twosComplimentSubtract(double[] l, double[] r,
	    double[] dest) {
	assert !Vect3D.isAnyNaN(l);
	assert !Vect3D.isAnyNaN(r);
	dest[0] = deltaRollover(l[0] - r[0]);
	dest[1] = deltaRollover(l[1] - r[1]);
	dest[2] = deltaRollover(l[2] - r[2]);
	assert !Vect3D.isAnyNaN(dest):"l="+l[0]+" "+l[1]+" "+l[2]+" r="+r[0]+" "+r[1]+" "+r[2];
	return dest;
    }

    public static final class TR implements UncaughtExceptionHandler, Feature<Features>{
	public final TRFutureTask<GPU> 		gpu;
	public final TRFutureTask<SoundSystem>	soundSystem;
	private Player 				player;
	public  RootWindow 		        rootWindow;
	private MenuSystem       		menuSystem;
	private Color [] 			globalPalette, 
	darkIsClearPalette;
	private ColorPaletteVectorList		globalPaletteVL,
	darkIsClearPaletteVL;
	//private final KeyStatus 		keyStatus;
	private ResourceManager 		resourceManager;
	public final ThreadManager 		threadManager;
	public final Renderer               	mainRenderer/*, secondaryRenderer*/;
	private final CollisionManager 		collisionManager	= new CollisionManager(this);
	//private Reporter 			reporter;
	//private Game 				game;
	private final PropertyChangeSupport	pcSupport;

	private World 				world;
	private GameShell			gameShell;
	private RenderableSpacePartitioningGrid	defaultGrid;

	private ConfigWindow                    configWindow;
	private TRRunState                      runState;

	//private final ConfigMenuItemListener	configMenuItemListener = new ConfigMenuItemListener();
	private TRConfigRoot configManager;

	private ControllerInputs controllerInputs;
	
	public TR(){
	    threadManager = null;
	    soundSystem   = null;
	    pcSupport     = null;
	    mainRenderer  = null;
	    gpu           = null;
	}//end TR()

	public TR(boolean filler){
	    //this.config       = configManager;
	    //this.configWindow = configWindow;
	    //this.rootWindow   = rootWindow;
	    //this.configManager= configManager;

	    //rootWindow.initialize();
	    /*
	    menuSystem = Features.get(rootWindow, MenuSystem.class);

	    menuSystem.addMenuItem(CONFIG_MENU_PATH);
	    menuSystem.setMenuItemEnabled(true, CONFIG_MENU_PATH);
	    menuSystem.addMenuItemListener(configMenuItemListener, CONFIG_MENU_PATH);

	    menuSystem.addMenuItem(EXIT_MENU_PATH);
	    menuSystem.setMenuItemEnabled(true, EXIT_MENU_PATH);
	    menuSystem.addMenuItemListener(exitMenuItemListener, EXIT_MENU_PATH);
	     */
	    try{new OutputDump();}
	    catch(Exception e){e.printStackTrace();}
	    //AutoInitializable.Initializer.initialize(this);
	    pcSupport = new PropertyChangeSupport(this);
	    //keyStatus = new KeyStatus(rootWindow);
	    threadManager = new ThreadManager(this);
	    gpu = new TRFutureTask<GPU>(new Callable<GPU>(){
		@Override
		public GPU call() throws Exception {
		    return new GPU(threadManager.threadPool, threadManager, threadManager, TR.this, getRootWindow().getCanvas(),getWorld());
		}},TR.this);
	    soundSystem = new TRFutureTask<SoundSystem>(new Callable<SoundSystem>(){
		@Override
		public SoundSystem call() throws Exception {
		    return new SoundSystem(TR.this);
		}},TR.this);
	    threadManager.threadPool.submit(gpu);
	    threadManager.threadPool.submit(soundSystem);//TODO: Use new methods
	    System.out.println("Initializing graphics engine...");
	    /*secondaryRenderer=new TRFutureTask<Renderer>(new Callable<Renderer>(){
			@Override
			public Renderer call() throws Exception {
			    Thread.currentThread().setName("Renderer constructor.");
			    Renderer renderer = gpu.
				    get().
				    rendererFactory.
				    get().
				    newRenderer("secondaryRenderer");
			    renderer.setOneShotBehavior(true);
			    return renderer;
			}//end call()
		    },TRFactory.this);threadManager.threadPool.submit(secondaryRenderer);
	     */
	    mainRenderer  = gpu.
			    get().
			    rendererFactory.
			    get().
			    newRenderer("mainRenderer");
	    mainRenderer.setEnabled(true);
		//}//end call()
	    //},TR.this);
	    //threadManager.threadPool.submit(mainRenderer);
	    System.out.println("...Done");
	    setResourceManager(new ResourceManager(this));

	    final Renderer renderer = mainRenderer;
	    renderer.getCamera().setRootGrid(getDefaultGrid());
	    getThreadManager().addRepeatingGLTask(renderer.render);

	    /////// SECONDARY RENDERER //////////////
	    //secondaryRenderer.get().getCamera().setRootGrid(getDefaultGrid());//TODO: Stub
	    //secondaryRenderer.get().getCamera().setPosition(0, TRFactory.mapSquareSize*5, 0);//TODO: Stub
	    //secondaryRenderer.get().setRenderingTarget(gpu.get().rendererFactory.get().getPortalFrameBuffers()[0]);

	    Runtime.getRuntime().addShutdownHook(new Thread(){
		public void run(){
		    soundSystem.get().setPaused(true);
		    try{getConfigManager().saveConfigurations();
		    }catch(Exception e){System.err.println(
			    "Failed to write the config file.\n"
			            + e.getClass().getName()+"\n"
				    + e.getMessage()+"\n");
		    e.printStackTrace();
		    }//end catch(Exception)
		    System.err.println("Great work, Guys!");
		}//end run()
	    });
	    //renderer.getCamera().getFlatRelevanceCollection().addTarget(collisionManager.getInputRelevanceCollection(), true);
	    renderer.getCamera().getRelevancePairs().addTarget(collisionManager.getInputRelevancePairCollection(), true);
	}//end constructor

	private class ConfigMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		getConfigWindow().setVisible(true);
	    }
	}//end ConfigMenuItemListener

	private void waitForProfiler() {
	    JOptionPane.showMessageDialog(rootWindow, "Connect profiler and click OK to continue.","Connect profiler",JOptionPane.OK_OPTION);
	}

	public void showStopper(final Throwable throwable) {
	    System.err.println("==== SHOWSTOPPER ====");
	    throwable.printStackTrace();
	    System.err.println("======================");
	    System.err.println("\nIrrecoverable. Exiting...\n\n");
	    System.exit(-1);
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

	public boolean isStampingTextures() {
	    return false;
	}
	/*
    public Game newGame(VOXFile mission, GameVersion gameVersion) {//TODO: Refactor this out
	final TVF3Game newGame = new TVF3Game(this, gameVersion);
	newGame.setVox(mission);
	setGame(newGame);
	return newGame;
    }// end newGame(...)

    private Future<Game> setGame(final Game newGame) {
	if(newGame==game)
	    return new DummyFuture<Game>(game);
	final Game oldGame=game;
	game=newGame;
	Future<Game> result = null;
	final RenderableSpacePartitioningGrid defaultGrid = getDefaultGrid();
	if(oldGame instanceof TVF3Game || newGame instanceof TVF3Game){
	    result = World.relevanceExecutor.submit(new Callable<Game>(){
		@Override
		public Game call() throws Exception {
		    if(oldGame instanceof TVF3Game)
			defaultGrid.removeBranch(((TVF3Game)oldGame).getPartitioningGrid());
		    if(newGame instanceof TVF3Game)
			defaultGrid.addBranch(((TVF3Game)newGame).getPartitioningGrid());
		    return oldGame;
		}});
	}//end if(TVF3Game)
	if(newGame==null)
	 getThreadManager().setPaused(true);
	pcSupport.firePropertyChange(GAME, oldGame, newGame);
	if(result == null)
	    result = new DummyFuture<Game>(oldGame);
	return result;
    }//end setGame()
	 */
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
	    if(world==null)
		world = new World(
			256*mapSquareSize,
			16.*mapSquareSize,
			256*mapSquareSize,
			mapSquareSize*visibilityDiameterInMapSquares/2., this);
	    return world;
	}

	public ThreadManager getThreadManager() {
	    return threadManager;
	}

	public CollisionManager getCollisionManager() {
	    return collisionManager;
	}

	public void gatherSysInfo() {
	    final GPU _gpu = gpu.get();
	    /*
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
			    .getRuntime().availableProcessors()+"");
		    for (Entry<Object, Object> prop : System.getProperties()
			    .entrySet()) {
			r.report((String) prop.getKey(), prop.getValue().toString());
		    }
		    return null;
		}// end call()
	    }).get();
	    */
	}// end gatherSysInfo()

	public void setGlobalPalette(Color[] palette) {
	    globalPalette = palette;
	}

	public RootWindow getRootWindow() {
	    if(rootWindow == null)
		rootWindow = Features.get(this, RootWindow.class);
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

	public TR addPropertyChangeListener(String propertyName, PropertyChangeListener l){
	    pcSupport.addPropertyChangeListener(propertyName, l);
	    return this;
	}

	public TR removePropertyChangeListener(PropertyChangeListener l){
	    pcSupport.removePropertyChangeListener(l);
	    return this;
	}



	/**
	 * @return the menuSystem
	 */
	/*public MenuSystem getMenuSystem() {//TODO: Remove this. TR is not supposed to be aware of MenuSystem so there's no point.
	    final Frame frame = getRootWindow();
	    return Features.get(frame, MenuSystem.class);
	}*/

	/**
	 * @return the defaultGrid
	 */
	public synchronized RenderableSpacePartitioningGrid getDefaultGrid() {
	    if(defaultGrid==null){
		try{World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			defaultGrid = world.newRootGrid();
		    }}).get();}catch(Exception e){throw new RuntimeException(e);}
	    }//end if(null)
	    return defaultGrid;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
	    showStopper(e);
	}

	//TODO: Refactor out - TR should not be aware
	public ControllerInputs getControllerInputs() {
	    if(controllerInputs == null){
		final ControllerMapper cm = Features.get(Features.getSingleton(), ControllerMapper.class);
		controllerInputs = Features.get(cm, ControllerInputs.class);
		}
	    return controllerInputs;
	}

	public ConfigWindow getConfigWindow() {
	    if(configWindow == null)
		configWindow = Features.get(this, ConfigWindow.class);
	    return configWindow;
	}

	public TRRunState getRunState() {
	    return runState;
	}

	public void setRunState(TRRunState runState) {
	    final TRRunState oldRunState = this.runState;
	    this.runState                = runState;
	    System.out.println("setRunState "+runState+" old="+oldRunState);
	    pcSupport.firePropertyChange(RUN_STATE, oldRunState, runState);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcSupport.getPropertyChangeListeners();
	}

	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcSupport.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcSupport.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
	    return pcSupport.hasListeners(propertyName);
	}

	@Override
	public void apply(Features target) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void destruct(Features target) {
	    // TODO Auto-generated method stub

	}

	public TRConfigRoot getConfigManager() {
	    if(configManager == null)
		configManager = Features.get(this,TRConfigRoot.class);
	    return configManager;
	}

	public void setConfigManager(TRConfigRoot configManager) {
	    this.configManager = configManager;
	}
    }//end TR

    @Override
    public Feature<Features> newInstance(Features target) {
	return new TR(true);
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TR.class;
    }
}//end TRFactory