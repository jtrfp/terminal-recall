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

package org.jtrfp.trcl;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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
import org.jtrfp.trcl.ctl.ControllerSink;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TXTMissionBriefFile;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.BriefingLayout;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.miss.Mission;
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
    private final ControllerBarrier fireBarrier;

    public BriefingScreen(final TR tr, GLFont font, BriefingLayout layout, String debugName) {
	super();
	this.layout=layout;
	final ControllerSinks controllerInputs = tr.getControllerInputs();
	fireBarrier = new ControllerBarrier(
		controllerInputs.getSink(UserInputWeaponSelectionBehavior.FIRE),
		controllerInputs.getSink(NEXT_SCREEN_CTL));
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
	game.getPlayer().setActive(false);
	briefingChars.setScrollPosition(layout.getNumLines()-2);
	setContent("Air targets destroyed: "+summary.getAirTargetsDestroyed()+
		"\nGround targets destroyed: "+summary.getGroundTargetsDestroyed()+
		"\nVegetation destroyed: "+summary.getFoliageDestroyed()+
		"\nTunnels found: "+(int)((1.-(double)ts.getTunnelsRemaining().size()/(double)ts.getTotalNumTunnels())*100.)+"%");
	final TXTMissionBriefFile txtMBF = tr.getResourceManager().getMissionText(lvl.getBriefingTextFile());
	
	planetDisplayMode(txtMBF.getPlanetModelFile(),txtMBF.getPlanetTextureFile(),lvl);
	fireBarrier.waitForEvent();
	final Camera camera 	 = tr.mainRenderer.getCamera();
	camera.probeForBehavior(MatchPosition.class) 	 .setEnable(true);
	camera.probeForBehavior(MatchDirection.class)	 .setEnable(true);
	camera.probeForBehavior(FacingObject.class)  	 .setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
    }//end missionCompleteSummary()

    public void briefingSequence(LVLFile lvl) {
	final Game   game 	 = Features.get(tr,GameShell.class).getGame();
	final Renderer renderer  = tr.mainRenderer;
	final Camera camera 	 = renderer.getCamera();
	game.getPlayer().setActive(false);
	final TXTMissionBriefFile txtMBF = tr.getResourceManager().getMissionText(lvl.getBriefingTextFile());
	String  content = txtMBF.getMissionText().replace("\r","");
	tr.setRunState(new Mission.PlanetBrief(){});
	planetDisplayMode(txtMBF.getPlanetModelFile(),txtMBF.getPlanetTextureFile(), lvl);
	final String playerName = game.getPlayerName();
	for(String token:layout.getNameTokens())
	    content=content.replace(token, playerName);
	setContent(content);
	startScroll();
	final boolean [] mWait = new boolean[]{false};
	addScrollFinishCallback(new Runnable(){
	    @Override
	    public void run() {
		synchronized(mWait){mWait[0] = true; mWait.notifyAll();}
	    }});
	fireBarrier.waitForEvent();
	stopScroll();
	//Enemy introduction
	tr.setRunState(new Mission.EnemyBrief() {});
	final SkySystem skySystem = game.getCurrentMission().getOverworldSystem().getSkySystem();
	renderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	renderer.getSkyCube().setSkyCubeGen(skySystem.getBelowCloudsSkyCubeGen());
	renderer.setAmbientLight(skySystem.getSuggestedAmbientLight());
	renderer.setSunColor(skySystem.getSuggestedSunColor());
	final OverworldSystem overworldSystem = game.getCurrentMission().getOverworldSystem();
	final List<DEFObject> defObjects = overworldSystem.getDefList();
	for(DEFObject def:defObjects){
	    if(def.hasBehavior(RequestsMentionOnBriefing.class)){
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
		boolean chamberMode = false;
		chamberMode = def.isShieldGen() || def.isBoss();
		if(chamberMode)
		    overworldSystem.setChamberMode(true);
		def.tick(System.currentTimeMillis());//Make sure its position and state is sane.
		camera.tick(System.currentTimeMillis());//Make sure the camera knows what is going on.
		def.setRespondToTick(false);//freeze
		briefingChars.setScrollPosition(layout.getNumLines()-2);
		setContent(descriptionString);
		fireBarrier.waitForEvent();
		//Restore previous state.
		def.setVisible(vis);
		def.setActive(act);
		def.setRespondToTick(true);//unfreeze
		if(chamberMode)
		    overworldSystem.setChamberMode(false);
	    }//end if(requestsBehavior)
	}//end for(enemyIntros)
	camera.probeForBehavior(FacingObject.class).setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
	camera.probeForBehavior(MatchPosition.class).setEnable(true);
	camera.probeForBehavior(MatchDirection.class).setEnable(true);
    }//end briefingSequence
    
    private ColorPaletteVectorList getPalette(LVLFile lvl){
	if(palette==null){
	    try{palette = new ColorPaletteVectorList(tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile()));}
	    catch(Exception e){tr.showStopper(e);}
	}//end if(null)
	return palette;
    }//end ColorPaletteVectorList
    
    private class ControllerBarrier implements PropertyChangeListener{
	private CountDownLatch        latch;
	private final Collection<ControllerSink>        inputs = new ArrayList<ControllerSink>();
	private final Collection<PropertyChangeListener> weakPLs= new ArrayList<PropertyChangeListener>();//Hard reference; do not remove!
	
	public ControllerBarrier(ControllerSink ... inputs){
	    for(ControllerSink in : inputs){
		this.inputs.add(in);
		final WeakPropertyChangeListener weakPL;
		weakPLs.add(weakPL = new WeakPropertyChangeListener(this,in));
		in.addPropertyChangeListener(weakPL);
		}
	}
	
	public void release(){
	    for(ControllerSink in : inputs)
	     in.removePropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
	    if(latch == null)
		return;
	    Object nV = arg0.getNewValue();
	    if(nV instanceof Double){
		final double newValue = (Double)nV;
		if(newValue > .75){
		    latch.countDown();
		}//end if(>.75)
	    }//end if(instanceof Double)
	}//end propertyChange(...)
	
	public void waitForEvent(){
	    latch = new CountDownLatch(1);
	    try{latch.await();}
	    catch(Exception e){e.printStackTrace();}
	    latch = null;
	}//end waitForFire()
    }//end WaitForFireButton
}//end BriefingScreen
