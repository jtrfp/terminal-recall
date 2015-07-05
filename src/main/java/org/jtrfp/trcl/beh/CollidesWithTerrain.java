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
package org.jtrfp.trcl.beh;

import java.awt.Point;
import java.util.Collection;

import org.jtrfp.trcl.NormalMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.TerrainChunk;
import org.jtrfp.trcl.obj.TunnelEntranceObject;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTerrain extends Behavior {
    private double[] 		surfaceNormalVar;
    public static final double 	CEILING_Y_NUDGE 	= -5000;
    private int 		tickCounter 		= 0;
    private boolean 		autoNudge 		= false;
    private double 		nudgePadding 		= 5000;
    private boolean		recentlyCollided	= false;
    private boolean		tunnelEntryCapable	= false;
    private boolean		ignoreHeadingForImpact	= true;
    private boolean		ignoreCeiling           = false;
    private static		TerrainChunk		dummyTerrainChunk;
    // WORK VARS
    private final double[]      groundNormal            = new double[3];
    private final double[]      downhillDirectionXZ     = new double[3];
    private final double[]      ceilingNormal           = new double[3];
    private OverworldSystem     lastOWS;
    private NormalMap           normalMap;
    @Override
    public void _tick(long tickTimeMillis) {
	if (tickCounter++ % 2 == 0 && !recentlyCollided)
	    return;
	recentlyCollided=false;
	final WorldObject p = getParent();
	final TR tr = p.getTr();
	final World world = tr.getWorld();
	final Mission mission = tr.getGame().getCurrentMission();
	if(mission.getOverworldSystem()!=lastOWS){
	    normalMap=null;
	    lastOWS=mission.getOverworldSystem();
	}
	try{if(normalMap==null)
		normalMap=new NormalMap(mission.getOverworldSystem().getAltitudeMap());
	}catch(NullPointerException e){return;}
	final OverworldSystem ows = mission.getOverworldSystem();
	if(ows==null)
	    return;
	if(ows.isTunnelMode())
	    return;//No terrain to collide with while in tunnel mode.
	if(normalMap==null)return;
	final double[] thisPos = p.getPosition();
	final double groundHeight= normalMap.heightAt(thisPos[0],thisPos[2]);
	final double ceilingHeight = (tr.getWorld().sizeY - groundHeight)
		+ CEILING_Y_NUDGE;
	
	normalMap.normalAt(thisPos[0], thisPos[1], groundNormal);
	downhillDirectionXZ[0] = groundNormal[0];
	downhillDirectionXZ[1] = 0;
	downhillDirectionXZ[2] = groundNormal[2];
	
	if (Vect3D.norm(downhillDirectionXZ) != 0)
	    Vect3D.normalize(downhillDirectionXZ, downhillDirectionXZ);
	else
	    {downhillDirectionXZ[0]=0;downhillDirectionXZ[1]=1;downhillDirectionXZ[2]=0;}
	
	final OverworldSystem overworldSystem = tr.getGame().getCurrentMission().getOverworldSystem();
	if(overworldSystem==null)return;
	final boolean terrainMirror = overworldSystem.isChamberMode();
	final double thisY = thisPos[1];
	boolean groundImpact = thisY < (groundHeight + (autoNudge ? nudgePadding
		: 0));
	final boolean ceilingImpact = (thisY > ceilingHeight && terrainMirror && !ignoreCeiling);
	
	ceilingNormal[0] = groundNormal[0];
	ceilingNormal[1] = -groundNormal[1];
	ceilingNormal[2] = groundNormal[2];
	
	double [] surfaceNormal = groundImpact ? groundNormal : ceilingNormal;
	final double dot = Vect3D.dot3(surfaceNormal,getParent().getHeading().toArray());
	//final double dot = surfaceNormal.dotProduct(getParent().getHeading());
	if (terrainMirror && groundHeight/(tr.getWorld().sizeY/2) > .97) {
	    groundImpact = true;
	    surfaceNormal = downhillDirectionXZ;
	}//end if(smushed between floor and ceiling)
	
	if (tunnelEntryCapable && groundImpact && dot < 0){
		final OverworldSystem os = mission.getOverworldSystem();
		if(!os.isTunnelMode() ){
		    TunnelEntranceObject teo = mission.getTunnelEntranceObject(new Point(
				(int)(thisPos[0] / TR.mapSquareSize),
				(int)(thisPos[2] / TR.mapSquareSize)));
		    if(teo!=null && !mission.isBossFight())
			{mission.enterTunnel(teo.getSourceTunnel());return;}
		}//end if(above ground)
	}//end if(tunnelEntryCapable())

	if (groundImpact || ceilingImpact) {// detect collision
	    recentlyCollided=true;
	    double padding = autoNudge ? nudgePadding : 0;
	    padding *= groundImpact ? 1 : -1;
	    thisPos[1] = (groundImpact ? groundHeight : ceilingHeight)
		    + padding;
	    p.notifyPositionChange();
	    if(dot < 0 || ignoreHeadingForImpact){//If toward ground, call impact listeners.
		surfaceNormalVar = surfaceNormal;
		final Behavior behavior = p.getBehavior();
		behavior.probeForBehaviors(sub, SurfaceImpactListener.class);
	    }//end if(pointedTowardGround)
	}// end if(collision)
    }// end _tick
    
    private final Submitter<SurfaceImpactListener> sub = new Submitter<SurfaceImpactListener>() {
	@Override
	public void submit(SurfaceImpactListener item) {
	    item.collidedWithSurface(getDummyTerrainChunk(getParent().getTr()), surfaceNormalVar);
	}

	@Override
	public void submit(Collection<SurfaceImpactListener> items) {
	    for (SurfaceImpactListener l : items) {
		submit(l);
	    }//end for(items)
	}//end submit(...)
    };

    /**
     * @return the autoNudge
     */
    public boolean isAutoNudge() {
	return autoNudge;
    }

    /**
     * @param autoNudge
     *            the autoNudge to set
     */
    public CollidesWithTerrain setAutoNudge(boolean autoNudge) {
	this.autoNudge = autoNudge;
	return this;
    }

    /**
     * @return the nudgePadding
     */
    public double getNudgePadding() {
	return nudgePadding;
    }

    /**
     * @param nudgePadding
     *            the nudgePadding to set
     */
    public CollidesWithTerrain setNudgePadding(double nudgePadding) {
	this.nudgePadding = nudgePadding;
	return this;
    }

    /**
     * @return the tunnelEntryCapable
     */
    public boolean isTunnelEntryCapable() {
        return tunnelEntryCapable;
    }

    /**
     * @param tunnelEntryCapable the tunnelEntryCapable to set
     */
    public CollidesWithTerrain setTunnelEntryCapable(boolean tunnelEntryCapable) {
        this.tunnelEntryCapable = tunnelEntryCapable;
        return this;
    }

    /**
     * @param ignoreHeadingForImpact the ignoreHeadingForImpact to set
     */
    public CollidesWithTerrain setIgnoreHeadingForImpact(boolean ignoreHeadingForImpact) {
        this.ignoreHeadingForImpact = ignoreHeadingForImpact;
        return this;
    }

    public CollidesWithTerrain setIgnoreCeiling(boolean ignoreCeiling) {
	this.ignoreCeiling=ignoreCeiling;
	return this;
    }

    /**
     * @return the ignoreCeiling
     */
    public boolean isIgnoreCeiling() {
        return ignoreCeiling;
    }
    
    private static TerrainChunk getDummyTerrainChunk(TR tr){
	if (dummyTerrainChunk==null)
	    dummyTerrainChunk = new TerrainChunk(tr);
	return dummyTerrainChunk;
    }
}// end BouncesOffTerrain
