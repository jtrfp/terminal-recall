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
import java.lang.ref.WeakReference;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.NormalMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.miss.TunnelSystemFactory.TunnelSystem;
import org.jtrfp.trcl.obj.Player;
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
    private final double[]      ceilingNormal           = new double[3];
    private WeakReference<OverworldSystem>lastOWS;
    private NormalMap           normalMap;
    @Override
    public void tick(long tickTimeMillis) {
	if (tickCounter++ % 2 == 0 && !recentlyCollided)
	    return;
	recentlyCollided=false;
	final WorldObject p = getParent();
	final TR tr = p.getTr();
	final World world = tr.getWorld();
	final Mission mission = tr.getGame().getCurrentMission();
	OverworldSystem ows = lastOWS!=null?lastOWS.get():null;
	if(mission.getOverworldSystem()!=ows){
	    normalMap=null;
	    lastOWS=new WeakReference<OverworldSystem>(mission.getOverworldSystem());
	}
	try{if(normalMap==null)
		//normalMap=new NormalMap(new WallOffAltitudeMap(mission.getOverworldSystem().getAltitudeMap(),tr.getWorld().sizeY/2.1,tr.getWorld().sizeY*2));
	    normalMap=new NormalMap(mission.getOverworldSystem().getAltitudeMap());
	}catch(NullPointerException e){return;}
	ows = mission.getOverworldSystem();
	if(ows==null)
	    return;
	if(ows.isTunnelMode())
	    return;//No terrain to collide with while in tunnel mode.
	if(normalMap==null)
	    return;
	final OverworldSystem overworldSystem = tr.getGame().getCurrentMission().getOverworldSystem();
	if(overworldSystem==null)return;
	final boolean terrainMirror = overworldSystem.isChamberMode();
	final double[] thisPos = p.getPosition();
	if(ows.getNormalizedAltitudeMap().heightAt(thisPos[0],thisPos[2])>.9 && terrainMirror)
	    return;//Stuck in wall. Don't bother.
	final double groundHeight= normalMap.heightAt(thisPos[0],thisPos[2]);
	final double ceilingHeight = (tr.getWorld().sizeY - groundHeight)
		+ CEILING_Y_NUDGE;
	
	normalMap.normalAt(thisPos[0], thisPos[2], groundNormal);
	final double thisY = thisPos[1];
	boolean groundImpact = thisY < (groundHeight + (autoNudge ? nudgePadding
		: 0));
	final boolean ceilingImpact = (thisY > ceilingHeight && terrainMirror && !ignoreCeiling);
	
	ceilingNormal[0] = groundNormal[0];
	ceilingNormal[1] = -groundNormal[1];
	ceilingNormal[2] = groundNormal[2];
	
	double [] surfaceNormal = groundImpact ? groundNormal : ceilingNormal;
	final double dot = Vect3D.dot3(surfaceNormal,getParent().getHeading().toArray());
	if (tunnelEntryCapable && groundImpact && dot < 0){
		final OverworldSystem os = mission.getOverworldSystem();
		if(!os.isTunnelMode() ){
		    final TunnelSystem ts = Features.get(mission,TunnelSystem.class);
		    TunnelEntranceObject teo = ts.getTunnelEntranceObject(new Point(
				TR.modernToMapSquare((thisPos[0])),
				TR.modernToMapSquare( thisPos[2])));
		    if(teo==null)
			new Exception("TEO is null!").printStackTrace();
		    if(teo!=null && !mission.isBossFight())
			{ts.enterTunnel(teo);return;}
		}//end if(above ground)
	}//end if(tunnelEntryCapable())

	if (groundImpact || ceilingImpact && dot < 0) {// detect collision
	    if(getParent() instanceof Player)
		System.err.println(new Vector3D(surfaceNormal));
	    recentlyCollided=true;
	    double padding = autoNudge ? nudgePadding : 0;
	    padding *= groundImpact ? 1 : -1;
	    thisPos[1] = (groundImpact ? groundHeight + padding: ceilingHeight - padding);
	    p.notifyPositionChange();
	   /* if(dot < 0 || ignoreHeadingForImpact)*/{//If toward ground, call impact listeners.
		surfaceNormalVar = surfaceNormal;
		p.probeForBehaviors(sub, SurfaceImpactListener.class);
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
