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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.beh.FacingObject;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.RotateAroundObject;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TXTMissionBriefFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission.Result;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.EnemyIntro;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;

public class BriefingScreen extends RenderableSpacePartitioningGrid {
    private static final double Z = .000000001;
    private final TR 		  tr;
    private final Sprite2D	  briefingScreen;
    private final CharAreaDisplay briefingChars;
    private final Sprite2D	  blackRectangle;
    private volatile double  	  scrollPos = 0;
    private double		  scrollIncrement=.01;
    private final int		  NUM_LINES=10;
    private final int		  WIDTH_CHARS=36;
    private ArrayList<Runnable>	  scrollFinishCallbacks = new ArrayList<Runnable>();
    private TXTMissionBriefFile missionTXT;
    private ColorPaletteVectorList palette;
    private LVLFile		lvl;
    private TimerTask	  scrollTimer;

    public BriefingScreen(SpacePartitioningGrid<PositionedRenderable> parent, final TR tr, GLFont font) {
	super(parent);
	briefingScreen = new Sprite2D(tr,0, 2, 2,
		tr.getResourceManager().getSpecialRAWAsTextures("BRIEF.RAW", tr.getGlobalPalette(),
		tr.gpu.get().getGl(), 0,false),true);
	add(briefingScreen);
	this.tr	      = tr;
	briefingChars = new CharAreaDisplay(this,.047,WIDTH_CHARS,NUM_LINES,tr,font);
	//briefingChars.setFontSize(.035);//TODO: Implement
	briefingChars.activate();
	briefingChars.setPosition(-.7, -.45, Z*200);
	briefingScreen.setPosition(0,0,Z);
	briefingScreen.notifyPositionChange();
	briefingScreen.setActive(true);
	briefingScreen.setVisible(true);
	
	blackRectangle = new Sprite2D(tr,0, 2, .6, tr.gpu.get().textureManager.get().solidColor(Color.BLACK), true);
	add(blackRectangle);
	blackRectangle.setPosition(0, -.7, Z*300);
	blackRectangle.setVisible(true);
	blackRectangle.setActive(true);
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
	final Camera camera 	 = tr.renderer.get().getCamera();
	final GL3	gl	 = tr.gpu.get().getGl();
	this.lvl		 = lvl;
	camera.probeForBehavior(MatchPosition.class) 	 .setEnable(false);
	camera.probeForBehavior(MatchDirection.class)	 .setEnable(false);
	camera.probeForBehavior(FacingObject.class)  	 .setEnable(true);
	camera.probeForBehavior(RotateAroundObject.class).setEnable(true);
	//Planet introduction
	game.setDisplayMode(game.briefingMode);
	WorldObject planetObject;
	try{
	 final Model planetModel = rm.getBINModel(
		 missionTXT.getPlanetModelFile(),
		 rm.getRAWAsTexture(missionTXT.getPlanetTextureFile(), 
			 getPalette(), gl, true),
		 8,true,getPalette(),gl);
	 	     planetObject = new WorldObject(tr,planetModel);
	 planetObject.setPosition(0, TR.mapSquareSize*20, 0);
	 tr.getWorld().add(planetObject);
	 planetObject.setVisible(true);
	 camera.probeForBehavior(FacingObject.class)	  .setTarget(planetObject);
	 camera.probeForBehavior(RotateAroundObject.class).setTarget(planetObject);
	 camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.05);
	 camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{0,-planetModel.getTriangleList().getMaximumVertexDims().getY(),0});
	    camera.probeForBehavior(RotateAroundObject.class).setDistance(
		    planetModel.getTriangleList().getMaximumVertexDims().getX()*4);
	 }catch(Exception e){tr.showStopper(e);}
    }//end planetDisplayMode()
    
    public void missionCompleteSummary(LVLFile lvl, Result r){
	planetDisplayMode(lvl);
	final Game   game 	 = tr.getGame();
	setContent("Air targets destroyed: "+r.getAirTargetsDestroyed()+
		"\nGround targets destroyed: "+r.getGroundTargetsDestroyed()+
		"\nVegetation destroyed: "+r.getFoliageDestroyed()+
		"\nTunnels found: "+(int)(r.getTunnelsFoundPctNorm()*100.)+"%");
	game.getCurrentMission().getOverworldSystem().activate();
	tr.getWorld().setFogColor(Color.black);
	tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
    }//end missionCompleteSummary()

    public void briefingSequence(LVLFile lvl) {
	final Game   game 	 = tr.getGame();
	final ResourceManager rm = tr.getResourceManager();
	final Camera camera 	 = tr.renderer.get().getCamera();
	missionTXT
				 = rm.getMissionText(lvl.getBriefingTextFile());
	planetDisplayMode(lvl);
	setContent(
		missionTXT.getMissionText().replace("\r",""));
	game.getCurrentMission().getOverworldSystem().activate();
	tr.getWorld().setFogColor(Color.black);
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
	tr.getWorld().setFogColor(game.getCurrentMission().getOverworldSystem().getFogColor());
	for(EnemyIntro intro:game.getCurrentMission().getOverworldSystem().getObjectSystem().getDefPlacer().getEnemyIntros()){
	    final WorldObject wo = intro.getWorldObject();
	    camera.probeForBehavior(FacingObject.class).setTarget(wo);
	    camera.probeForBehavior(RotateAroundObject.class).setTarget(wo);
	    camera.probeForBehavior(RotateAroundObject.class).setAngularVelocityRPS(.3);
	    //Roughly center the object (ground objects have their bottom at Y=0)
	    camera.probeForBehavior(RotateAroundObject.class).setOffset(
		    new double []{0,wo.getModel().getTriangleList().getMaximumVertexDims().getY()/2,0});
	    camera.probeForBehavior(RotateAroundObject.class).setDistance(
		    wo.getModel().getTriangleList().getMaximumVertexDims().getX()*6);
	    wo.setRespondToTick(false);//freeze
	    briefingChars.setScrollPosition(NUM_LINES-2);
	    setContent(intro.getDescriptionString());
	    tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
	    wo.setRespondToTick(true);//unfreeze
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
