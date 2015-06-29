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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.beh.FacingObject;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.RotateAroundObject;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.core.LazyTRFuture;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TXTMissionBriefFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission.Result;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.EnemyIntro;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;

public class BriefingScreen extends RenderableSpacePartitioningGrid {
    private static final double Z_INCREMENT = .00001;
    private static final double Z_START = -.99999;
    private static final double BRIEFING_SPRITE_Z = Z_START;
    private static final double TEXT_Z = BRIEFING_SPRITE_Z + Z_INCREMENT;
    private static final double TEXT_BG_Z = TEXT_Z + Z_INCREMENT;
    
    public static final double MAX_Z_DEPTH = TEXT_BG_Z + Z_INCREMENT;
    //private static final double Z = .000000001;
    private final TR 		  tr;
    private final Sprite2D	  briefingScreen;
    private final CharAreaDisplay briefingChars;
    private final Sprite2D	  blackRectangle;
    private volatile double  	  scrollPos = 0;
    private double		  scrollIncrement=.01;
    private final int		  NUM_LINES=10;
    private final int		  WIDTH_CHARS=36;
    private ArrayList<Runnable>	  scrollFinishCallbacks = new ArrayList<Runnable>();
    //private TXTMissionBriefFile missionTXT;
    private ColorPaletteVectorList palette;
    private LVLFile		lvl;
    private TimerTask	  scrollTimer;
    private WorldObject	  planetObject;
    
    private final LazyTRFuture<TXTMissionBriefFile> missionTXT;

    public BriefingScreen(SpacePartitioningGrid<PositionedRenderable> parent, final TR tr, GLFont font) {
	super(parent);
	briefingScreen = new Sprite2D(tr,0, 2, 2,
		tr.getResourceManager().getSpecialRAWAsTextures("BRIEF.RAW", tr.getGlobalPalette(),
		tr.gpu.get().getGl(), 0,false),true);
	add(briefingScreen);
	this.tr	      = tr;
	briefingChars = new CharAreaDisplay(this,.047,WIDTH_CHARS,NUM_LINES,tr,font);
	blockingAddBranch(briefingChars);
	briefingChars.setPosition(-.7, -.45, TEXT_Z);
	briefingScreen.setPosition(0,0,BRIEFING_SPRITE_Z);
	briefingScreen.notifyPositionChange();
	briefingScreen.setImmuneToOpaqueDepthTest(true);
	briefingScreen.setActive(true);
	briefingScreen.setVisible(true);
	
	blackRectangle = new Sprite2D(tr,0, 2, .6, tr.gpu.get().textureManager.get().solidColor(Color.BLACK), false);
	add(blackRectangle);
	blackRectangle.setImmuneToOpaqueDepthTest(true);
	blackRectangle.setPosition(0, -.7, TEXT_BG_Z);
	blackRectangle.setVisible(true);
	blackRectangle.setActive(true);
	
	missionTXT = new LazyTRFuture<TXTMissionBriefFile>(tr,new Callable<TXTMissionBriefFile>(){
	    @Override
	    public TXTMissionBriefFile call(){
		return tr.getResourceManager().getMissionText(lvl.getBriefingTextFile());
	    }//end call()
	});
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
		if(scrollPos>briefingChars.getNumActiveLines()+NUM_LINES){
		    BriefingScreen.this.stopScroll();
		    notifyScrollFinishCallbacks();
		    scrollFinishCallbacks.clear();
		}
	    }}, 0, 20);
    }//end startScroll()

    public void stopScroll() {
	scrollTimer.cancel();
    }
    
    private void planetDisplayMode(LVLFile lvl){
	final Game   game 	 = tr.getGame();
	final ResourceManager rm = tr.getResourceManager();
	final Camera camera 	 = tr.mainRenderer.get().getCamera();
	final GPU 	gpu 	 = tr.gpu.get();
	final GL3	gl	 = gpu.getGl();
	this.lvl		 = lvl;
	
	//TODO: Depth range
	
	//Planet introduction
	if(planetObject!=null){
	    remove(planetObject);
	    planetObject=null;
	}
	try{
	 final Model planetModel = rm.getBINModel(
		 missionTXT.get().getPlanetModelFile(),
		 rm.getRAWAsTexture(missionTXT.get().getPlanetTextureFile(), 
			 getPalette(), null, false, true),
		 8,false,getPalette(),null);
	 	     planetObject = new WorldObject(tr,planetModel);
	 planetObject.setPosition(0, TR.mapSquareSize*20, 0);
	 add(planetObject);
	 planetObject.setVisible(true);
	 camera.probeForBehavior(FacingObject.class)	  .setTarget(planetObject);
	 camera.probeForBehavior(RotateAroundObject.class).setTarget(planetObject);
	 camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.05);
	 camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{0,-planetModel.getTriangleList().getMaximumVertexDims().getY(),0});
	    camera.probeForBehavior(RotateAroundObject.class).setDistance(
		    planetModel.getTriangleList().getMaximumVertexDims().getX()*4);
	 }catch(Exception e){tr.showStopper(e);}
	// Turn the camera to the planet
	camera.probeForBehavior(MatchPosition.class).setEnable(false);
	camera.probeForBehavior(MatchDirection.class).setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(true);
	camera.probeForBehavior(FacingObject.class).setEnable(true);
	final Renderer renderer = tr.mainRenderer.get();
	renderer.getCamera()
		.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class)
		.setEnable(false);
	renderer.getSkyCube().setSkyCubeGen(SkySystem.SPACE_STARS);
	renderer.setAmbientLight(SkySystem.SPACE_AMBIENT_LIGHT);
	renderer.setSunColor(SkySystem.SPACE_SUN_COLOR);
	//game.setDisplayMode(game.briefingMode);
    }//end planetDisplayMode()
    
    public void missionCompleteSummary(LVLFile lvl, Result r){
	final Game   game 	 = tr.getGame();
	game.getPlayer().setActive(false);
	briefingChars.setScrollPosition(NUM_LINES-2);
	setContent("Air targets destroyed: "+r.getAirTargetsDestroyed()+
		"\nGround targets destroyed: "+r.getGroundTargetsDestroyed()+
		"\nVegetation destroyed: "+r.getFoliageDestroyed()+
		"\nTunnels found: "+(int)(r.getTunnelsFoundPctNorm()*100.)+"%");
	//tr.getDefaultGrid().nonBlockingAddBranch(game.getCurrentMission().getOverworldSystem());
	planetDisplayMode(lvl);
	tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
	final Camera camera 	 = tr.mainRenderer.get().getCamera();
	camera.probeForBehavior(MatchPosition.class) 	 .setEnable(true);
	camera.probeForBehavior(MatchDirection.class)	 .setEnable(true);
	camera.probeForBehavior(FacingObject.class)  	 .setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
	/*World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		//BriefingScreen.this.addBranch(briefingChars);
		tr.getDefaultGrid().removeBranch(game.getCurrentMission().getOverworldSystem());
	    }});*/
    }//end missionCompleteSummary()

    public void briefingSequence(LVLFile lvl) {
	final Game   game 	 = tr.getGame();
	final ResourceManager rm = tr.getResourceManager();
	final Renderer renderer  = tr.mainRenderer.get();
	final Camera camera 	 = renderer.getCamera();
	final OverworldSystem overworld
				= game.getCurrentMission().getOverworldSystem();
	//missionTXT		 = rm.getMissionText(lvl.getBriefingTextFile());
	missionTXT.reset();
	game.getPlayer().setActive(false);
	planetDisplayMode(lvl);
	setContent(
		missionTXT.get().getMissionText().replace("\r","").replace("$C", ""+game.getPlayerName()));
	//tr.getDefaultGrid().nonBlockingAddBranch(overworld);
	startScroll();
	final boolean [] mWait = new boolean[]{false};
	addScrollFinishCallback(new Runnable(){
	    @Override
	    public void run() {
		synchronized(mWait){mWait[0] = true; mWait.notifyAll();}
	    }});
	final Thread spacebarWaitThread;
	(spacebarWaitThread = new Thread(){
	    @Override
	    public void run(){
		tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
		synchronized(mWait){mWait[0] = true; mWait.notifyAll();}
	    }//end run()
	}).start();
	try{synchronized(mWait){while(!mWait[0])mWait.wait();}}
	catch(InterruptedException e){}
	stopScroll();
	spacebarWaitThread.interrupt();
	
	//Enemy introduction
	final SkySystem skySystem = game.getCurrentMission().getOverworldSystem().getSkySystem();
	renderer.getCamera().probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(true);
	renderer.getSkyCube().setSkyCubeGen(skySystem.getBelowCloudsSkyCubeGen());
	renderer.setAmbientLight(skySystem.getSuggestedAmbientLight());
	renderer.setSunColor(skySystem.getSuggestedSunColor());
	for(EnemyIntro intro:game.getCurrentMission().getOverworldSystem().getObjectSystem().getDefPlacer().getEnemyIntros()){
	    final WorldObject wo = intro.getWorldObject();
	    final boolean vis = wo.isVisible();
	    final boolean act = wo.isActive();
	    wo.setActive(true);
	    wo.setVisible(true);
	    camera.probeForBehavior(FacingObject.class).setTarget(wo);
	    camera.probeForBehavior(RotateAroundObject.class).setTarget(wo);
	    camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.3);
	    //Roughly center the object (ground objects have their bottom at Y=0)
	    if(wo.getModel().getTriangleList()!=null){
	     camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{
			    0,
			    wo.getModel().
			     getTriangleList().
			     getMaximumVertexDims().
			     getY()/2,
			    0});
	     camera.probeForBehavior(RotateAroundObject.class).setDistance(
			    wo.getModel().getTriangleList().getMaximumVertexDims().getX()*6);}
	    else if(wo.getModel().getTransparentTriangleList()!=null){
	     camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{
			 0,
			 wo.getModel().
			 getTransparentTriangleList().
			 getMaximumVertexDims().
			 getY()/2,
			 0});
	     camera.probeForBehavior(RotateAroundObject.class).setDistance(
			    wo.getModel().getTransparentTriangleList().getMaximumVertexDims().getX()*6);}
	    //If this intro takes place in the chamber, enter chamber mode.
	    boolean chamberMode = false;
	    if(wo instanceof DEFObject){
		final DEFObject def = (DEFObject)wo;
		chamberMode = def.isShieldGen() || def.isBoss();
	    }
	    if(chamberMode)
		tr.getGame().getCurrentMission().getOverworldSystem().setChamberMode(true);
	    wo.tick(System.currentTimeMillis());//Make sure its position and state is sane.
	    camera.tick(System.currentTimeMillis());//Make sure the camera knows what is going on.
	    wo.setRespondToTick(false);//freeze
	    briefingChars.setScrollPosition(NUM_LINES-2);
	    setContent(intro.getDescriptionString());
	    tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
	    //Restore previous state.
	    wo.setVisible(vis);
	    wo.setActive(act);
	    wo.setRespondToTick(true);//unfreeze
	    if(chamberMode)
		tr.getGame().getCurrentMission().getOverworldSystem().setChamberMode(false);
	}//end for(enemyIntros)
	camera.probeForBehavior(FacingObject.class).setEnable(false);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(false);
	camera.probeForBehavior(MatchPosition.class).setEnable(true);
	camera.probeForBehavior(MatchDirection.class).setEnable(true);
    }//end briefingSequence
    
    private ColorPaletteVectorList getPalette(){
	if(palette==null){
	    try{palette = new ColorPaletteVectorList(tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile()));}
	    catch(Exception e){tr.showStopper(e);}
	}//end if(null)
	return palette;
    }//end ColorPaletteVectorList
}//end BriefingScreen
