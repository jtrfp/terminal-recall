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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.obj.EnemyIntro;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.Sprite2D;

public class BriefingScreen extends RenderableSpacePartitioningGrid {
    private static final double Z = .000000001;
    private final TR 		  tr;
    private final Sprite2D	  briefingScreen;
    private final CharAreaDisplay briefingChars;
    private final Sprite2D	  blackRectangle;
    private volatile double  	  scrollPos = 0;
    private double		  scrollIncrement=.02;
    private final int		  NUM_LINES=10;
    private final int		  WIDTH_CHARS=36;
    private ArrayList<Runnable>	  scrollFinishCallbacks = new ArrayList<Runnable>();
    private final TimerTask	  scrollTimer=
	    new TimerTask(){
	    @Override
	    public void run() {
		scrollPos+=scrollIncrement;
		briefingChars.setScollPosition(scrollPos);
		if(scrollPos>briefingChars.getNumActiveLines()+NUM_LINES){
		    BriefingScreen.this.stopScroll();
		    notifyScrollFinishCallbacks();
		    scrollFinishCallbacks.clear();
		}
	    }};

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
	tr.getThreadManager().getLightweightTimer().scheduleAtFixedRate(scrollTimer, 0, 20);
    }//end startScroll()

    public void stopScroll() {
	scrollTimer.cancel();
    }

    public void briefingSequence(LVLFile lvl) {
	final Game game = tr.getGame();
	//Planet introduction
	game.setDisplayMode(game.briefingMode);
	setContent(
		tr.getResourceManager().getMissionText(lvl.getBriefingTextFile()).getMissionText().replace('\r','\n'));
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
	game.getCurrentMission().getOverworldSystem().activate();
	//Enemy introduction
	for(EnemyIntro intro:game.getCurrentMission().getOverworldSystem().getObjectSystem().getDefPlacer().getEnemyIntros()){
	    //TODO: Give camera circleObject behavior
	    //TODO: Give camera faceObject behavior
	    System.out.println(intro.getDescriptionString());
	    briefingChars.setScollPosition(NUM_LINES-2);
	    setContent(intro.getDescriptionString());
	    tr.getKeyStatus().waitForSequenceTyped(KeyEvent.VK_SPACE);
	}//end for(enemyIntros)
    }//end briefingSequence

}//end BriefingScreen
