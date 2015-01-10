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
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.ThreadManager;
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
	    final TR tr=getTr();
	    final Mission mission = tr.getGame().getCurrentMission();
	    final WorldObject player = tr.getGame().getPlayer();
	    final double [] playerPos = player.getPosition();
	    final Vector3D playerPosXY = new Vector3D(playerPos[0],playerPos[2],0);
	    final Vector3D playerHeading = player.getHeading();
	    final Vector3D playerHeadingXY = new Vector3D(playerHeading.getX(),playerHeading.getZ(),0);
	    if(mission.currentNAVObjective()==null){setVisible(false);return;}
	    if(mission.currentNAVObjective().getTarget()==null){setVisible(false);return;}
	    	else setVisible(true);
	    final double [] loc =mission.currentNAVObjective().getTarget().getPosition();
	    final Vector3D navLocXY = new Vector3D(loc[0],loc[2],0);
	    final Vector3D player2NavVectorXY = TR.twosComplimentSubtract(navLocXY, playerPosXY);
	    final double modernDistance = player2NavVectorXY.getNorm();
	    counter++;counter%=Math.ceil(TEXT_UPDATE_INTERVAL_MS/(1000/ThreadManager.GAMEPLAY_FPS));
	    //This need only be done occasionally
	    if(counter==0){
		getTr().getGame().getHUDSystem().getDistance().setContent(""+(int)((modernDistance*16)/TR.mapSquareSize));
		getTr().getGame().getHUDSystem().getSector().setContent(((byte)((playerPos[2])/TR.mapSquareSize)&0xFF)+"."+
		    ((int)((playerPos[0])/TR.mapSquareSize)&0xFF));
	    }
	    final Vector3D normPlayer2NavVector = player2NavVectorXY.normalize();
	    //Kludge to correct negative X bug in engine. (mirrored world)
	    final Vector3D correctedNormPlayer2NavVector = new Vector3D(-normPlayer2NavVector.getX(),normPlayer2NavVector.getY(),0);
	    final Rotation rot = new Rotation(Vector3D.PLUS_J,playerHeadingXY.getNorm()!=0?playerHeadingXY:Vector3D.PLUS_I);
	    setTop(rot.applyTo(correctedNormPlayer2NavVector).normalize());
	}//_ticks(...)
    }//end NavArrowBehavior
}//end NavArrow
