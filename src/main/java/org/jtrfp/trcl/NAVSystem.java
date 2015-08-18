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
import java.util.Collection;

import org.jtrfp.trcl.beh.NAVTargetableBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.obj.NAVRadarBlipFactory;
import org.jtrfp.trcl.obj.NavArrow;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

public class NAVSystem extends RenderableSpacePartitioningGrid {
private final NavArrow arrow;
private final TR tr;
private final NAVRadarBlipFactory blips;
private final DashboardLayout layout;

    public NAVSystem(SpacePartitioningGrid<PositionedRenderable> parent, 
	    TR tr, DashboardLayout layout) {
	super();
	this.tr=tr;
	this.layout=layout;
	System.out.println("Setting up NAV system...");
	arrow = new NavArrow(tr,this,layout);
	final Point2D.Double pos = layout.getMiniMapPosition();
	final double [] aPos = arrow.getPosition();
	aPos[0]=pos.getX();
	aPos[1]=pos.getY();
	aPos[2]=.00001;
	arrow.notifyPositionChange();
	//arrow.setPosition(new Vector3D(.825,.8,0));
	add(arrow);
	blips=new NAVRadarBlipFactory(tr,this,layout);
	System.out.println("...Done.");
    }//end constructor
    
    public void updateNAVState(){
	final Game game = tr.getGame();
	if(game==null)return;
	final Mission mission = game.getCurrentMission();
	if(mission==null)return;
	final NAVObjective obj = mission.currentNAVObjective();
	if(obj==null)return;
	tr.getGame().getHUDSystem().
		getObjective().
		setContent(layout.getHumanReadableObjective(obj));
	final WorldObject target = obj.getTarget();
	if(target!=null)
	    target.getBehavior().probeForBehaviors(ntbSubmitter, NAVTargetableBehavior.class);
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
        return blips;
    }

    public double[] getHeadingXY() {
	return arrow.getTopArray();
    }
    
    public void activate(){
	//tr.getDefaultGrid().addBranch(this);
	updateNAVState();
    }
}//end NAVSystem
