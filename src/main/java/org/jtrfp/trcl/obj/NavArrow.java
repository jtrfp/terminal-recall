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
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;

public class NavArrow extends Sprite2D {
private static final double WIDTH=.16;
private static final double HEIGHT=.16;
private static final double Z=.0001;
private static final int TEXT_UPDATE_INTERVAL_MS=150;
private final NAVSystem nav;
    public NavArrow(TR tr, NAVSystem navSystem) {
	super(tr, Z, 
		WIDTH, 
		HEIGHT, 
		getTexture(tr), true);
	this.nav=navSystem;
	setImmuneToOpaqueDepthTest(true);
	try{
	addBehavior(new NavArrowBehavior());
	}//end try{}
	catch(Exception e){e.printStackTrace();}
    }//end constructor
    
    private static TextureDescription getTexture(TR tr){
	try{
	    return tr.getResourceManager().getRAWAsTexture("NAVTAR01.RAW", 
		tr.getGlobalPaletteVL(),false);}
	catch(Exception e){e.printStackTrace();}
	return null;
    }
    
    private class NavArrowBehavior extends Behavior{
	private int counter=0;
	@Override
	public void _tick(long time){
	    final TR tr              = getTr();
	    final Game game          = tr.getGame();
	    final Mission mission    = game.getCurrentMission();
	    final WorldObject player = game.getPlayer();
	    final HUDSystem hudSystem= game.getHUDSystem();
	    if(mission.currentNAVObjective()==null){setVisible(false);return;}
	    if(mission.currentNAVObjective().getTarget()==null){setVisible(false);return;}
	    counter++;counter%=Math.ceil(TEXT_UPDATE_INTERVAL_MS/(1000/ThreadManager.GAMEPLAY_FPS));

	    final double [] playerPos = player.getPosition();
	    Vector3D navLocXY=Vector3D.ZERO;
	    String sectorMsg="";
	    //Tunnel
	    if(mission.getMissionMode() instanceof Mission.TunnelMode){
		if(counter==0){
		    setVisible(false);
		    final TunnelExitObject eo = mission.getCurrentTunnel().getExitObject();
		    final double [] eoPos = eo.getPosition();
		    navLocXY = new Vector3D(eoPos[0],eoPos[2],0);
		    hudSystem.getObjective().setContent("Exit Tunnel");
		    sectorMsg = "???.???";
		}
	    }else{//No Tunnel
		setVisible(true);
		final double [] loc =mission.currentNAVObjective().getTarget().getPosition();
		navLocXY = new Vector3D(loc[0],loc[2],0);
		sectorMsg = ((byte)((playerPos[2])/TR.mapSquareSize)&0xFF)+"."+
			((int)((playerPos[0])/TR.mapSquareSize)&0xFF);
	    }//end no tunnel

	    final Vector3D playerPosXY = new Vector3D(playerPos[0],playerPos[2],0);
	    final Vector3D player2NavVectorXY = TR.twosComplimentSubtract(navLocXY, playerPosXY);
	    final double modernDistance = player2NavVectorXY.getNorm();

	    if(counter==0){
		hudSystem.getDistance().setContent(""+(int)((modernDistance*16)/TR.mapSquareSize));
		hudSystem.getSector().setContent(sectorMsg);
	    }

	    final Vector3D playerHeading = player.getHeading();
	    final Vector3D playerHeadingXY = new Vector3D(playerHeading.getX(),playerHeading.getZ(),0);
	    final Vector3D normPlayer2NavVector = player2NavVectorXY.normalize();
	    //Kludge to correct negative X bug in engine. (mirrored world)
	    final Vector3D correctedNormPlayer2NavVector = new Vector3D(-normPlayer2NavVector.getX(),normPlayer2NavVector.getY(),0);
	    final Rotation rot = new Rotation(Vector3D.PLUS_J,playerHeadingXY.getNorm()!=0?playerHeadingXY:Vector3D.PLUS_I);
	    setTop(rot.applyTo(correctedNormPlayer2NavVector).normalize());
	}//_ticks(...)
    }//end NavArrowBehavior
}//end NavArrow
