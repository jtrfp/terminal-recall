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

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.NAVObjective;
import org.jtrfp.trcl.obj.MiniMap;
import org.jtrfp.trcl.obj.NAVRadarBlipFactory;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.WorldObject.RenderFlags;

public class NAVSystem extends RenderableSpacePartitioningGrid {
private final NavArrow arrow;
private final MiniMap miniMap;
private final TR tr;
private NAVRadarBlipFactory blips;
private final DashboardLayout layout;

    public NAVSystem(TR tr, DashboardLayout layout) {
	super();
	this.tr=tr;
	this.layout=layout;
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr,layout, new Point2D.Double(.16,.16), "NAVSystem.arrow");
	miniMap = new MiniMap(tr);
	miniMap.setPosition(0,0,.00001);
	miniMap.setImmuneToOpaqueDepthTest(true);
	miniMap.setRenderFlag(RenderFlags.IgnoreCamera);
	miniMap.setActive(true);
	miniMap.setVisible(true);
	tr.addPropertyChangeListener(TR.RUN_STATE, new RunStateListener());
	final double mmRadius = layout.getMiniMapRadius();
	miniMap.setModelSize(new double[]{mmRadius,mmRadius});
	final Point2D.Double pos = layout.getMiniMapPosition();
	final double [] aPos = arrow.getPosition();
	aPos[0]=pos.getX();
	aPos[1]=pos.getY();
	aPos[2]=.00001;
	System.arraycopy(aPos, 0, miniMap.getPosition(), 0, 3);
	miniMap.getPosition()[2]=.001;
	miniMap.notifyPositionChange();
	miniMap.setMapPositionFromTile(0, 0);
	arrow.notifyPositionChange();
	arrow.setRenderFlag(RenderFlags.IgnoreCamera);
	add(arrow);
	add(miniMap);
	final Point2D.Double mmPos = layout.getMiniMapPosition();
	getBlips().setPositionOrigin(new Vector3D(mmPos.getX(), mmPos.getY(), 0.));
	System.out.println("...Done.");
    }//end constructor
    
    private class RunStateListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object runState = evt.getNewValue();
	    final MiniMap miniMap = getMiniMap();
	    if(runState instanceof Mission.OverworldState){
		  miniMap.setTextureMesh(tr.getGameShell().getGame().getCurrentMission().getOverworldSystem().getTextureMesh());
		  miniMap.setVisible(true);
	    }else {
		miniMap.setVisible(false);
		miniMap.setTextureMesh(null);
		}
	}//end propertyChange(...)
    }//end RunStateListener
    
    public void updateNAVState(){
	final Game game = tr.getGameShell().getGame();
	if(game==null)return;
	final Mission mission = game.getCurrentMission();
	if(mission==null)return;
	final NAVObjective obj = mission.currentNAVObjective();
	if(obj==null)return;
	((TVF3Game)tr.getGameShell().getGame()).getHUDSystem().
		getObjective().
		setContent(layout.getHumanReadableObjective(obj));
	final WorldObject target = obj.getTarget();
	if(target!=null)
	    target.probeForBehaviors(ntbSubmitter, NAVTargetableBehavior.class);
    }//end updateNAVState()
    
    private final Submitter<NAVTargetableBehavior> ntbSubmitter = new Submitter<NAVTargetableBehavior>(){

	@Override
	public void submit(NAVTargetableBehavior item) {
	    item.notifyBecomingCurrentTarget();
	}

	@Override
	public void submit(Collection<NAVTargetableBehavior> items) {
	    for(NAVTargetableBehavior i:items){this.submit(i);}
	}
	
    };//end ntbSubmitter

    /**
     * @return the blips
     */
    public NAVRadarBlipFactory getBlips() {
	if(blips == null)
	    blips = generateBlips();
        return blips;
    }//end getBlips()
    
    protected NAVRadarBlipFactory generateBlips(){
	return new NAVRadarBlipFactory(tr,this,"NAVSystem.blips", true);
    }//end generateBlips()

    public double[] getHeadingXY() {
	return arrow.getTopArray();
    }
    
    public void activate(){
	//tr.getDefaultGrid().addBranch(this);
	updateNAVState();
    }

    public MiniMap getMiniMap() {
        return miniMap;
    }
}//end NAVSystem
