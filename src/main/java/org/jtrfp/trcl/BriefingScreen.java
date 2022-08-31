/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2018 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.FacingObject;
import org.jtrfp.trcl.beh.HasDescription;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.RequestsMentionOnBriefing;
import org.jtrfp.trcl.beh.RotateAroundObject;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerSink;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TXTMissionBriefFile;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.Game.GameRunMode;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.BriefingLayout;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.Mission.Briefing;
import org.jtrfp.trcl.miss.Mission.MissionSummary;
import org.jtrfp.trcl.miss.TunnelSystemFactory.TunnelSystem;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class BriefingScreen extends RenderableSpacePartitioningGrid {
    public static final String  NEXT_SCREEN_CTL = "Next Screen";
    private static final double Z_INCREMENT       = .00001;
    private static final double Z_START           = -.99999;
    private static final double BRIEFING_SPRITE_Z = Z_START;
    private static final double TEXT_Z = BRIEFING_SPRITE_Z + Z_INCREMENT;
    private static final double TEXT_BG_Z         = TEXT_Z + Z_INCREMENT;
    
    public static final double MAX_Z_DEPTH = TEXT_BG_Z + Z_INCREMENT;
    private final TR 		  tr;
    private final Sprite2D	  briefingScreen;
    private final CharAreaDisplay briefingChars;
    private final Sprite2D	  blackRectangle;
    private volatile double  	  scrollPos = 0;
    private final double	  scrollIncrement;
    private ArrayList<Runnable>	  scrollFinishCallbacks = new ArrayList<Runnable>();
    private ColorPaletteVectorList  palette;
    private TimerTask	            scrollTimer;
    private WorldObject	            planetObject;
    private final BriefingLayout    layout;
    //private final ControllerBarrier fireBarrier;
    //private final RunStateListener  runStateListener = new RunStateListener();
    private TypeRunStateHandler     runStateListener;
    private Runnable                scrollFinishCallback;
    private final Collection<Runnable>nextScreenRunnable = new ArrayList<>(2);
    private PropertyChangeListener  nextScreenPCL;
    
    public abstract class BriefingStage implements Briefing {
	private final LVLFile lvl;
	
	public BriefingStage(LVLFile lvl){
	    this.lvl = lvl;
	}

	public LVLFile getLvl() {
	    return lvl;
	}
    }//end BriefingState
    public class PlanetDisplay extends BriefingStage {
	public PlanetDisplay(LVLFile lvl) {
	    super(lvl);
	}
    }//end PlanetDisplay
    
    public class EnemyBriefings extends BriefingStage {

	public EnemyBriefings(LVLFile lvl) {
	    super(lvl);
	}
	
    }//end EnemyBriefings
    
    public class SpecificEnemyBriefing extends EnemyBriefings {
	private final Queue<DEFObject> enemyDEFs;
	
	public SpecificEnemyBriefing(LVLFile lvl, Queue<DEFObject> enemyDEFs) {
	    super(lvl);
	    this.enemyDEFs = enemyDEFs;
	}//end constructor

	public Queue<DEFObject> getEnemyDEFs() {
	    return enemyDEFs;
	}
    }//end EnemyBriefing
    
    public class MissionSummaryState implements Briefing {
	private final LVLFile lvl;
	private final MissionSummary summary;
	public MissionSummaryState(MissionSummary summary, LVLFile lvl) {
	    this.lvl = lvl;
	    this.summary = summary;
	}
	public LVLFile getLvl() {
	    return lvl;
	}
	public MissionSummary getSummary() {
	    return summary;
	}
    }//end MissionSummary

    public BriefingScreen(final TR tr, GLFont font, final BriefingLayout layout, String debugName) {
	super();
	this.layout=layout;
	final ControllerMapper cm = Features.get(Features.getSingleton(), ControllerMapper.class);
	final ControllerSinks controllerInputs = Features.get(cm, ControllerSinks.class);
	final ControllerSink nextScreenSink = controllerInputs.getSink(NEXT_SCREEN_CTL);
	final ControllerSink fireButtonSink = controllerInputs.getSink(UserInputWeaponSelectionBehavior.FIRE);
	//fireBarrier = new ControllerBarrier(
	//	controllerInputs.getSink(UserInputWeaponSelectionBehavior.FIRE),
	//	controllerInputs.getSink(NEXT_SCREEN_CTL));
	briefingScreen = new Sprite2D(tr,0, 2, 2,
		tr.getResourceManager().getSpecialRAWAsTextures("BRIEF.RAW", tr.getGlobalPalette(),
		Features.get(tr, GPUFeature.class).getGl(), 0,false, true),true,"BriefingScreen."+debugName);
	add(briefingScreen);
	this.tr	      = tr;
	briefingChars = new CharAreaDisplay(layout.getFontSizeGL(),layout.getNumCharsPerLine(),layout.getNumLines(),tr,font);
	blockingAddBranch(briefingChars);
	final Point2D.Double textPos = layout.getTextPosition();
	briefingChars.setPosition(textPos.getX(), textPos.getY(), TEXT_Z);
	briefingScreen.setPosition(0,0,BRIEFING_SPRITE_Z);
	briefingScreen.notifyPositionChange();
	briefingScreen.setImmuneToOpaqueDepthTest(true);
	briefingScreen.setActive(true);
	briefingScreen.setVisible(true);
	
	blackRectangle = new Sprite2D(0, 2, .6, Features.get(tr,GPUFeature.class).textureManager.get().solidColor(Color.BLACK), false,"BriefingScreen.blackRectangle."+debugName);
	add(blackRectangle);
	blackRectangle.setImmuneToOpaqueDepthTest(true);
	blackRectangle.setPosition(0, -.7, TEXT_BG_Z);
	blackRectangle.setVisible(true);
	blackRectangle.setActive(true);
	
	scrollIncrement = layout.getScrollIncrement();
	
	tr.addPropertyChangeListener(TRFactory.RUN_STATE, runStateListener = new TypeRunStateHandler(Briefing.class){
	    TypeRunStateHandler 
	    
	    startPlanetBriefingHandler = new TypeRunStateHandler(PlanetDisplay.class){
		private Runnable briefingExitRunnable;

		@Override
		public void enteredRunState(Object oldState, Object newState) {
		    System.out.println("START PLANET BRIEFING");
		    final PlanetDisplay pd = (PlanetDisplay)newState;
		    final Game   game 	 = Features.get(tr,GameShell.class).getGame();
		    //Set up planet brief
		    game.getPlayer().setActive(false);
		    final TXTMissionBriefFile txtMBF = tr.getResourceManager().getMissionText(pd.getLvl().getBriefingTextFile());
		    String  content = txtMBF.getMissionText().replace("\r","");

		    planetDisplayMode(txtMBF.getPlanetModelFile(),txtMBF.getPlanetTextureFile(), pd.getLvl());
		    final String playerName = game.getPlayerName();
		    for(String token:layout.getNameTokens())
			content=content.replace(token, playerName);
		    setContent(content);
		    startScroll();
		    nextScreenRunnable.add(briefingExitRunnable = new Runnable(){
			@Override
			public void run() {
			    tr.setRunState(new EnemyBriefings(pd.getLvl()));
			}});
		    //final boolean [] mWait = new boolean[]{false};
		    addScrollFinishCallback(scrollFinishCallback = new Runnable(){
			@Override
			public void run() {
			    //synchronized(mWait){mWait[0] = true; mWait.notifyAll();}
			    //Need to run in transient thread to change run state
			    final Executor executor = TransientExecutor.getSingleton();
			    synchronized(executor){
				executor.execute(new Runnable(){
				    @Override
				    public void run() {
					tr.setRunState(new EnemyBriefings(pd.getLvl()));
				    }});
			    }//end sync(exeuctor)
			}});
		}//end enteredRunState

		@Override
		public void exitedRunState(Object oldState, Object newState) {
		    removeScrollFinishCallback(scrollFinishCallback);
		    stopScroll();
		    nextScreenRunnable.remove(briefingExitRunnable);
		}},
		enemyIntroHandler = new TypeRunStateHandler(EnemyBriefings.class){

		    @Override
		    public void enteredRunState(Object oldState,
			    Object newState) {
			final Game   game 	 = Features.get(tr,GameShell.class).getGame();
			final Renderer renderer  = tr.mainRenderer;
			final BriefingStage newStage = (BriefingStage)newState;
			//final SkySystem skySystem = game.getCurrentMission().getOverworldSystem().getSkySystem();
			renderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
			/*
			renderer.getSkyCube().setSkyCubeGen(skySystem.getBelowCloudsSkyCubeGen());
			renderer.setAmbientLight(skySystem.getSuggestedAmbientLight());
			renderer.setSunColor(skySystem.getSuggestedSunColor());
			*/
			final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
			overworldSystem.applyToRenderer(renderer);
			final List<DEFObject> defObjects = overworldSystem.getDefList();
			final ArrayDeque<DEFObject> defs = new ArrayDeque<>();
			for(DEFObject obj:defObjects)
			    if(obj.hasBehavior(RequestsMentionOnBriefing.class))
				defs.add(obj);
			final Executor executor = TransientExecutor.getSingleton();
			synchronized(executor){
			    executor.execute(new Runnable(){
				@Override
				public void run() {
				    tr.setRunState(new SpecificEnemyBriefing(newStage.getLvl(),defs));
				}});
			    }//end sync(executor)
		    }//end enteredRunState(...)

		    @Override
		    public void exitedRunState(Object oldState,
			    Object newState) {
			// TODO Auto-generated method stub

		    }};
		 final PropertyChangeListener specificEnemyIntroHandler = new PropertyChangeListener(){
		     Runnable nextScreen;

		    @Override
		    public void propertyChange(PropertyChangeEvent evt) {
			final Object newValue = evt.getNewValue(), oldValue = evt.getOldValue();
			final Game   game 	 = Features.get(tr,GameShell.class).getGame();
			final Renderer renderer  = tr.mainRenderer;
			final Camera camera 	 = renderer.getCamera();
			if( !(newValue instanceof SpecificEnemyBriefing) && oldValue instanceof SpecificEnemyBriefing ) {
			    camera.probeForBehavior(FacingObject.class).setEnable(false);
			    camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
			    camera.probeForBehavior(MatchPosition.class).setEnable(true);
			    camera.probeForBehavior(MatchDirection.class).setEnable(true);
			    nextScreenRunnable.remove(nextScreen);
			}//end if(leaving enemy briefing)
			else if( newValue instanceof SpecificEnemyBriefing && newValue != oldValue ) {
			    final SpecificEnemyBriefing briefing = (SpecificEnemyBriefing)newValue;
			    final DEFObject def = briefing.getEnemyDEFs().poll();
			    if( def == null){
				final Executor executor = TransientExecutor.getSingleton();
				synchronized(executor){
				    executor.execute(new Runnable(){
					@Override
					public void run() {
					    game.getCurrentMission().switchToOverworldState();
					}});
				}//end sync(executor)
			    }//end if(null)
			    else { //Requests mention on briefing
				String descriptionString;
				try{descriptionString = def.probeForBehavior(HasDescription.class).getHumanReadableDescription();
				}
				catch(BehaviorNotFoundException e){
				    descriptionString = null;
				}
				if(descriptionString == null)
				    descriptionString = "[no description]";
				final boolean vis = def.isVisible();
				final boolean act = def.isActive();
				def.setActive(true);
				def.setVisible(true);
				camera.probeForBehavior(FacingObject.class).setTarget(def);
				camera.probeForBehavior(FacingObject.class).setHeadingOffset(layout.cameraHeadingAdjust());
				camera.probeForBehavior(RotateAroundObject.class).setTarget(def);
				camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.3);
				//Roughly center the object (ground objects have their bottom at Y=0)
				if(def.getModel().getTriangleList()!=null){
				    camera.probeForBehavior(RotateAroundObject.class).setOffset(
					    new double []{
						    0,
						    def.getModel().
						    getTriangleList().
						    getMaximumVertexDims().
						    getY(),
						    0});
				    camera.probeForBehavior(RotateAroundObject.class).setDistance(
					    def.getModel().getTriangleList().getMaximumVertexDims().getX()*3);}
				else if(def.getModel().getTransparentTriangleList()!=null){
				    camera.probeForBehavior(RotateAroundObject.class).setOffset(
					    new double []{
						    0,
						    def.getModel().
						    getTransparentTriangleList().
						    getMaximumVertexDims().
						    getY(),
						    0});
				    camera.probeForBehavior(RotateAroundObject.class).setDistance(
					    def.getModel().getTransparentTriangleList().getMaximumVertexDims().getX()*6);}
				//If this intro takes place in the chamber, enter chamber mode.
				//boolean chamberMode = false;
				final boolean chamberMode = def.isShieldGen() || def.isBoss();
				if(chamberMode){
				    final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
				    overworldSystem.setChamberMode(true);
				    }
				def.tick(System.currentTimeMillis());//Make sure its position and state is sane.
				camera.tick(System.currentTimeMillis());//Make sure the camera knows what is going on.
				def.setRespondToTick(false);//freeze
				stopScroll();
				briefingChars.setScrollPosition(layout.getNumLines()-2);
				setContent(descriptionString);
				//fireBarrier.waitForEvent();
				nextScreenRunnable.add(nextScreen = new Runnable(){
				    @Override
				    public void run() {
					//Restore previous state.
					def.setVisible(vis);
					def.setActive(act);
					def.setRespondToTick(true);//unfreeze
					if(chamberMode){
					    final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
					    overworldSystem.setChamberMode(false);}
					tr.setRunState(new SpecificEnemyBriefing(briefing.getLvl(),briefing.getEnemyDEFs()));
				    }});
			    }//end if(requestsMention)
			}//end if(SpecificEnemyBriefing)
		    }//end propertyChange(...)
		 },
			 missionSummaryHandler = new TypeRunStateHandler(MissionSummaryState.class) {
		     Runnable nextScreen;

			    @Override
			    public void enteredRunState(Object oldState,
				    Object newState) {
				final MissionSummaryState ms = (MissionSummaryState)newState;
				final LVLFile lvl            = ms.getLvl();
				final MissionSummary summary = ms.getSummary();
				final Game   game 	     = Features.get(tr,GameShell.class).getGame();
				final TunnelSystem ts        = Features.get(game.getCurrentMission(), TunnelSystem.class);
				
				game.getPlayer().setActive(false);
				briefingChars.setScrollPosition(layout.getNumLines()-2);
				setContent("Air targets destroyed: "+summary.getAirTargetsDestroyed()+
					"\nGround targets destroyed: "+summary.getGroundTargetsDestroyed()+
					"\nVegetation destroyed: "+summary.getFoliageDestroyed()+
					"\nTunnels found: "+(int)((1.-(double)ts.getTunnelsRemaining().size()/(double)ts.getTotalNumTunnels())*100.)+"%");
				final TXTMissionBriefFile txtMBF = tr.getResourceManager().getMissionText(lvl.getBriefingTextFile());
				
				planetDisplayMode(txtMBF.getPlanetModelFile(),txtMBF.getPlanetTextureFile(),lvl);
				//fireBarrier.waitForEvent();
				nextScreenRunnable.add(nextScreen = new Runnable(){
				    @Override
				    public void run() {
					final Mission mission = game.getCurrentMission();
					mission.missionComplete();
				    }});
			    }//end enteredRunState()

			    @Override
			    public void exitedRunState(Object oldState,
				    Object newState) {
				final Camera camera 	 = tr.mainRenderer.getCamera();
				camera.probeForBehavior(MatchPosition.class) 	 .setEnable(true);
				camera.probeForBehavior(MatchDirection.class)	 .setEnable(true);
				camera.probeForBehavior(FacingObject.class)  	 .setEnable(false);
				camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
				nextScreenRunnable.remove(nextScreen);
			    }//end exitedRunState()
		 };

	    @Override
	    public void enteredRunState(Object oldState, Object newState) {
		tr.addPropertyChangeListener(TRFactory.RUN_STATE, startPlanetBriefingHandler);
		tr.addPropertyChangeListener(TRFactory.RUN_STATE, enemyIntroHandler);
		tr.addPropertyChangeListener(TRFactory.RUN_STATE, specificEnemyIntroHandler);
		tr.addPropertyChangeListener(TRFactory.RUN_STATE, missionSummaryHandler);
		
		fireButtonSink.addPropertyChangeListener(nextScreenPCL = new PropertyChangeListener(){
		    @Override
		    public void propertyChange(PropertyChangeEvent evt) {
			if((Double)evt.getNewValue() > .5 && (Double)evt.getOldValue() < .5) {
			    final Collection<Runnable> nextScreenRunnables  = BriefingScreen.this.nextScreenRunnable;
			    final Executor executor = TransientExecutor.getSingleton();
			    synchronized(executor){
				for(Runnable r:nextScreenRunnables)
				    executor.execute(r);
				nextScreenRunnables.clear();
			    }//end sync(executor)
			}
		    }});
		nextScreenSink.addPropertyChangeListener(nextScreenPCL);
		
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, TRFactory.RUN_STATE, oldState, newState);
		startPlanetBriefingHandler.propertyChange(evt);
		enemyIntroHandler         .propertyChange(evt);
		specificEnemyIntroHandler .propertyChange(evt);
		missionSummaryHandler     .propertyChange(evt);
	    }//end enteredRunState(...)

	    @Override
	    public void exitedRunState(Object oldState, Object newState) {
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, startPlanetBriefingHandler);
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, enemyIntroHandler);
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, specificEnemyIntroHandler);
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, missionSummaryHandler);
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, TRFactory.RUN_STATE, oldState, newState);
		startPlanetBriefingHandler.propertyChange(evt);
		enemyIntroHandler         .propertyChange(evt);
		specificEnemyIntroHandler .propertyChange(evt);
		missionSummaryHandler     .propertyChange(evt);
		//tr.removePropertyChangeListener(TRFactory.RUN_STATE, this);
		fireButtonSink.removePropertyChangeListener(nextScreenPCL);
		nextScreenSink.removePropertyChangeListener(nextScreenPCL);
		nextScreenRunnable.clear();
	    }});
	
	//XXX Kludge to clean up the run state listeners if the game is aborted, else they will reference leak and keep reacting even when new BriefingScreens are created.
	tr.addPropertyChangeListener(TRFactory.RUN_STATE, new TypeRunStateHandler (GameRunMode.class){
	    @Override
	    public void enteredRunState(Object oldState, Object newState) {}
	    @Override
	    public void exitedRunState(Object oldState, Object newState) {
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, this);
		tr.removePropertyChangeListener(TRFactory.RUN_STATE, runStateListener); 
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, TRFactory.RUN_STATE, oldState, newState);
		runStateListener.propertyChange(evt);
	    }});
    }//end constructor

    protected void notifyScrollFinishCallbacks() {
	for(Runnable r:scrollFinishCallbacks){
	    r.run();
	}
    }//end notifyScrollFinishCallbacks()
    
    public void addScrollFinishCallback(Runnable r){
	scrollFinishCallbacks.add(r);
    }
    
    public void removeScrollFinishCallback(Runnable r){
	scrollFinishCallbacks.remove(r);
    }

    public void setContent(String content) {
	briefingChars.setContent(content);
    }

    public void startScroll() {
	scrollPos=0;
	tr.getThreadManager().getLightweightTimer().scheduleAtFixedRate(scrollTimer = new TimerTask(){
	    @Override
	    public void run() {
		scrollPos+=scrollIncrement;
		briefingChars.setScrollPosition(scrollPos);
		if(scrollPos>briefingChars.getNumActiveLines()+layout.getNumLines()){
		    BriefingScreen.this.stopScroll();
		    notifyScrollFinishCallbacks();
		    scrollFinishCallbacks.clear();
		}
	    }}, 0, 20);
    }//end startScroll()

    public void stopScroll() {
	scrollTimer.cancel();
    }
    
    private void planetDisplayMode(String planetModelFile, String planetTextureFile, LVLFile lvl){
	final ResourceManager rm = tr.getResourceManager();
	final Camera camera 	 = tr.mainRenderer.getCamera();
	
	//TODO: Depth range
	
	//Planet introduction
	if(planetObject!=null){
	    remove(planetObject);
	    planetObject=null;
	}
	try{
	 final boolean isPlanetTextureNull = planetTextureFile.toLowerCase().contentEquals("null.raw");
	 final GL33Model planetModel = rm.getBINModel(planetModelFile,
		 isPlanetTextureNull? null : rm.getRAWAsTexture(planetTextureFile, 
			 getPalette(lvl), null, false, true),
		 8,false,getPalette(lvl),null);
	 planetObject = new WorldObject(planetModel);
	 planetObject.setPosition(0, TRFactory.mapSquareSize*20, 0);
	 add(planetObject);
	 planetObject.setVisible(true);
	 camera.probeForBehavior(FacingObject.class)	  .setTarget(planetObject);
	 camera.probeForBehavior(FacingObject.class)      .setHeadingOffset(layout.cameraHeadingAdjust());
	 camera.probeForBehavior(RotateAroundObject.class).setTarget(planetObject);
	 camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.05);
	 camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{0,-planetModel.getTriangleList().getMaximumVertexDims().getY(),0});
	    camera.probeForBehavior(RotateAroundObject.class).setDistance(
		    planetModel.getTriangleList().getMaximumVertexDims().getX()*2);
	 }catch(Exception e){tr.showStopper(e);}
	// Turn the camera to the planet
	camera.probeForBehavior(MatchPosition.class).setEnable(false);
	camera.probeForBehavior(MatchDirection.class).setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(true);
	camera.probeForBehavior(FacingObject.class).setEnable(true);
	final Renderer renderer = tr.mainRenderer;
	renderer.getCamera()
		.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class)
		.setEnable(false);
	renderer.getSkyCube().setSkyCubeGen(SkySystem.SPACE_STARS);
	renderer.setAmbientLight(SkySystem.SPACE_AMBIENT_LIGHT);
	renderer.setSunColor(SkySystem.SPACE_SUN_COLOR);
    }//end planetDisplayMode()
    
    public void missionCompleteSummary(LVLFile lvl, MissionSummary summary){
	final Game   game 	 = Features.get(tr,GameShell.class).getGame();
	final Mission mission    = game.getCurrentMission();
	final TunnelSystem ts    = Features.get(mission, TunnelSystem.class);
	
	tr.setRunState(new MissionSummaryState(summary,lvl));
    }//end missionCompleteSummary()

    public void briefingSequence(LVLFile lvl) {
	System.out.println("BriefingScreen.briefingSequence()");
	//XXX This is already added in the constructor
	//tr.addPropertyChangeListener(TRFactory.RUN_STATE, runStateListener);//Removes itself
	tr.setRunState(new PlanetDisplay(lvl));
    }//end briefingSequence
    
    private ColorPaletteVectorList getPalette(LVLFile lvl){
	if(palette==null){
	    try{palette = new ColorPaletteVectorList(tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile()));}
	    catch(Exception e){tr.showStopper(e);}
	}//end if(null)
	return palette;
    }//end ColorPaletteVectorList
    
}//end BriefingScreen
