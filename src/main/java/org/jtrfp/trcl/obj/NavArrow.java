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

import java.awt.Color;
import java.awt.geom.Point2D;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.HUDSystem;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.NAVObjective;
import org.jtrfp.trcl.miss.TunnelSystemFactory.TunnelSystem;

public class NavArrow extends WorldObject implements RelevantEverywhere {
private static final double WIDTH=.16;
private static final double HEIGHT=.16;
private static final double Z=.0001;
private static final int TEXT_UPDATE_INTERVAL_MS=150;
private final DashboardLayout layout;
private Color backgroundColor;
private static final int BACKGROUND_INDEX = 10;
private Vector3D topOrigin = Vector3D.PLUS_J;
private Rotation vectorHack = Rotation.IDENTITY;

    public NavArrow(TR tr, DashboardLayout layout, Point2D.Double size, String debugName) {//TODO: Accept outside width/height parms
	super(tr);
	final Model m = new Model(false, tr, debugName);
	m.addTriangles(Triangle.quad2Triangles(size.getX(),size.getY(),0,0,Z, true, getTexture(tr)));
	//this.setRenderFlag(RenderFlags.IgnoreCamera);
	setImmuneToOpaqueDepthTest(true);
	setModel(m);
	/*
	super(tr, Z, 
		WIDTH, 
		HEIGHT, 
		getTexture(tr), 
		true,
		debugName);
	*/
	this.layout=layout;
	try{
	addBehavior(new NavArrowBehavior());
	}//end try{}
	catch(Exception e){e.printStackTrace();}
    }//end constructor
    
    @Override
    public boolean supportsLoop(){
	return false;
    }
    
    public Color getBackgroundColor(){
	if(backgroundColor == null)
	    backgroundColor = getTr().getGlobalPalette()[BACKGROUND_INDEX];
	return backgroundColor;
    }//end getBackgroundColor()
    
    private static Texture getTexture(TR tr){
	try{
	    return tr.getResourceManager().getRAWAsTexture("NAVTAR01.RAW", 
		tr.getGlobalPaletteVL(),null,false);}
	catch(Exception e){e.printStackTrace();}
	return null;
    }
    
    private class NavArrowBehavior extends Behavior{
	private int counter=0;
	@Override
	public void tick(long time){
	    final TR tr              = getTr();
	    final Game game          = tr.getGame();
	    final Mission mission    = game.getCurrentMission();
	    final WorldObject player = game.getPlayer();
	    final HUDSystem hudSystem= ((TVF3Game)game).getHUDSystem();
	    if(mission==null || player == null || hudSystem == null)
		return;
	    final NAVObjective navObjective = mission.currentNAVObjective();
	    if(navObjective==null)            {setVisible(false);return;}
	    if(navObjective.getTarget()==null){setVisible(false);return;}
	    counter++;counter%=Math.ceil(TEXT_UPDATE_INTERVAL_MS/(1000/ThreadManager.GAMEPLAY_FPS));

	    final double [] playerPos = player.getPosition();
	    Vector3D navLocXY=Vector3D.ZERO;
	    String sectorMsg="";
	    //Tunnel
	    if(tr.getRunState() instanceof Mission.TunnelState){
		if(counter==0){
		    setVisible(false);
		    final TunnelSystem ts = Features.get(mission, TunnelSystem.class);
		    final TunnelExitObject eo = ts.getCurrentTunnel().getExitObject();
		    final double [] eoPos = eo.getPosition();
		    navLocXY = new Vector3D(eoPos[0],eoPos[2],0);
		    if(layout != null)
		     hudSystem.getObjective().setContent(layout.getHumanReadableObjective(new NAVObjective(null){

			@Override
			public String getDescription() {
			    return "Exit Tunnel";
			}

			@Override
			public WorldObject getTarget() {
			    return null;
			}}));
		    sectorMsg = "???.???";
		}
	    }else{//No Tunnel
		setVisible(true);
		final double [] loc =mission.currentNAVObjective().getTarget().getPosition();
		navLocXY = new Vector3D(loc[0],loc[2],0);
		sectorMsg = TR.modernToMapSquare(playerPos[2])+"."+
			TR.modernToMapSquare(playerPos[0]);
	    }//end no tunnel

	    final Vector3D playerPosXY = new Vector3D(playerPos[0],playerPos[2],0);
	    Vector3D player2NavVectorXY = TR.twosComplimentSubtract(navLocXY, playerPosXY);
	    if(player2NavVectorXY.getNorm()==0)
		player2NavVectorXY=Vector3D.PLUS_I;
	    final double modernDistance = player2NavVectorXY.getNorm();

	    if(counter==0){
		hudSystem.getDistance().setContent(""+(int)((modernDistance*16)/TR.mapSquareSize));
		hudSystem.getSector().setContent(sectorMsg);
	    }

	    final Vector3D playerHeading = player.getHeading();
	    final Vector3D playerHeadingXY = new Vector3D(playerHeading.getX(),playerHeading.getZ(),0);
	    if(playerHeadingXY.getNorm() == 0)
		return;
	    final Vector3D normPlayer2NavVector = player2NavVectorXY.normalize();
	    
	    //Kludge to correct negative X bug in engine. (mirrored world)
	    final Vector3D correctedNormPlayer2NavVector = new Vector3D(-normPlayer2NavVector.getX(),normPlayer2NavVector.getY(),0);
	    final Rotation camRot    = new Rotation(Vector3D.PLUS_J,playerHeadingXY);
	    final Rotation renderRot = new Rotation(Vector3D.PLUS_K, Vector3D.PLUS_J,getHeading(),getTopOrigin());
	    setTop(renderRot.applyTo(vectorHack.applyTo(camRot.applyTo(correctedNormPlayer2NavVector))).normalize());
	}//_ticks(...)
    }//end NavArrowBehavior

    public Vector3D getTopOrigin() {
        return topOrigin;
    }

    public void setTopOrigin(Vector3D topOrigin) {
        this.topOrigin = topOrigin;
    }

    public Rotation getVectorHack() {
        return vectorHack;
    }

    public void setVectorHack(Rotation vectorHack) {
        this.vectorHack = vectorHack;
    }
}//end NavArrow
