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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.conf.TRConfiguration;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.TextureDescription;
import org.jtrfp.trcl.gpu.TextureManager;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

public class Crosshairs extends WorldObject implements RelevantEverywhere {

    public Crosshairs(TR tr) {
	super(tr);
	setImmuneToOpaqueDepthTest(true);
	// Crosshairs
	Model crossModel = null;
	final TextureManager tm = tr.gpu.get().textureManager.get();
	// Fallback
	final int NUM_FRAMES = 16;
	final double LUM_STEP = 200./NUM_FRAMES;
	Texture[] greenThrobFrames = new Texture[NUM_FRAMES];
	for (int f = 0; f < NUM_FRAMES; f++) {
	    greenThrobFrames[f] = greenThrobFrames[(NUM_FRAMES-1) - f] = (Texture) tm
		    .solidColor(new Color(
			    (int)(f * LUM_STEP * .8), 
			    (int)(f * LUM_STEP), 
			    (int)(f * LUM_STEP*.8), 170));
	}//end for(NUM_FRAMES)
	TextureDescription greenThrob = new AnimatedTexture(new Sequencer(80,
		greenThrobFrames.length, false), greenThrobFrames);/*tr.gpu.get().textureManager.get().getFallbackTexture();*/
	// TODO: Set crosshairs as a player-tracking object
	/*
	 * The official crosshairs. We supply the 'green throb' TARGET.BIN has a
	 * size range of [-8192,8192], a far cry from OpenGL's [-1,1] range.
	 * Also has a Z offset of +204,800. Scaling down by 204800 and
	 * subtracting 1 for a Z of zero we get correct size. In the real game
	 * TARGET.BIN is apparently appended to the player ship model itself
	 * such that the Z protrusion is real. Furthermore, enemies try to
	 * attack the crosshairs instead of the plane, perhaps as a kludge for
	 * motion-compensated aiming.
	 */
	try {
	    crossModel = tr.getResourceManager().getBINModel("TARGET.BIN",
		    greenThrob, 1./TR.crossPlatformScalar, false, tr.getGlobalPaletteVL(),null);
	    //crossModel = tr.getResourceManager().getBINModel("TARGET.BIN", tr.getGlobalPaletteVL(), null,tr.gpu.get().getGl());
	} catch (Exception e) {
	    tr.showStopper(e);
	}
	final List<Triangle> tl = crossModel.getRawTriangleLists().get(0);
	for (Triangle t : tl)
	    t.setCentroidNormal(Vector3D.ZERO);
	setModel(crossModel);
	installReactiveListeners(tr);
	//Install to Player
	addBehavior(new MatchPosition());
	addBehavior(new MatchDirection());
	final IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
	tr.addPropertyChangeListener(TR.GAME, gameIP);
	gameIP.addTargetPropertyChangeListener(Game.PLAYER, new PlayerListener());
	gameIP.setTarget(tr.getGame());
	this.setRespondToTick(true);
	setActive(true);
	setVisible(true);
    }//end constructor
    
    @Override
    public boolean supportsLoop(){return false;}
    
    private void registerPlayer(Player player){
	System.out.println("Found player: "+player);
	Crosshairs.this.probeForBehavior(MatchPosition.class) .setTarget(player);
	Crosshairs.this.probeForBehavior(MatchDirection.class).setTarget(player);
    }//end registerPlayer()
    
    private class PlayerListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Player player = (Player)evt.getNewValue();
	    if(player != null)
		registerPlayer(player);
	}//end propertyChange(...)
    }//end PlayerListener
    
    private void installReactiveListeners(final TR tr){
	tr.configManager.getConfig().addPropertyChangeListener(TRConfiguration.CROSSHAIRS_ENABLED,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		updateCrosshairsVisibilityState(
			(Boolean)evt.getNewValue(),
			tr.getGame().getCurrentMission().isSatelliteView());
	    }});
	IndirectProperty<Mission> currentMission = new IndirectProperty<Mission>();
	currentMission.addTargetPropertyChangeListener(Mission.SATELLITE_VIEW,new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		Boolean newValue = (Boolean)evt.getNewValue();
		if(newValue==null) newValue=false;
		updateCrosshairsVisibilityState(
			tr.configManager.getConfig().isCrosshairsEnabled(),
			newValue);
	    }});
	((TVF3Game)tr.getGame()).addPropertyChangeListener(Game.CURRENT_MISSION, currentMission);
    }//end installReactiveListeners
    
    private void updateCrosshairsVisibilityState(
	    boolean isUserSetActiveCrosshairs,
	    boolean isSatelliteViewMode ){
	final boolean visible = 
		isUserSetActiveCrosshairs &&
		!isSatelliteViewMode;
	setVisible(visible);
    }//end updateCrosshairsVisibilityState()
}// end Crosshairs
