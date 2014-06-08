package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.OverworldSystem;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTerrain extends Behavior {
    private boolean 		groundLock 		= false;
    private Vector3D 		surfaceNormalVar;
    public static final double 	CEILING_Y_NUDGE 	= -5000;
    private int 		tickCounter 		= 0;
    private boolean 		autoNudge 		= false;
    private double 		nudgePadding 		= 5000;
    private boolean		recentlyCollided	= false;

    @Override
    public void _tick(long tickTimeMillis) {
	if (tickCounter++ % 2 == 0 && !recentlyCollided)
	    return;
	recentlyCollided=false;
	final WorldObject p = getParent();
	final TR tr = p.getTr();
	final World world = tr.getWorld();
	final InterpolatingAltitudeMap aMap = tr.getAltitudeMap();
	if(aMap==null)return;
	final double[] thisPos = p.getPosition();
	final double groundHeightNorm = aMap.heightAt(
		(thisPos[0] / TR.mapSquareSize),
		(thisPos[2] / TR.mapSquareSize));
	final double groundHeight = groundHeightNorm * (world.sizeY / 2);
	final double ceilingHeight = (1.99 - aMap.heightAt(
		(thisPos[0] / TR.mapSquareSize),
		(thisPos[2] / TR.mapSquareSize)))
		* (world.sizeY / 2) + CEILING_Y_NUDGE;
	final Vector3D groundNormal = (aMap.normalAt(
		(thisPos[0] / TR.mapSquareSize),
		(thisPos[2] / TR.mapSquareSize)));
	Vector3D downhillDirectionXZ = new Vector3D(groundNormal.getX(), 0,
		groundNormal.getZ());
	if (downhillDirectionXZ.getNorm() != 0)
	    downhillDirectionXZ = downhillDirectionXZ.normalize();
	else
	    downhillDirectionXZ = Vector3D.PLUS_J;
	final OverworldSystem overworldSystem = tr.getOverworldSystem();
	final boolean terrainMirror = overworldSystem.isChamberMode();
	final double thisY = thisPos[1];
	boolean groundImpact = thisY < (groundHeight + (autoNudge ? nudgePadding
		: 0));
	final boolean ceilingImpact = (thisY > ceilingHeight && terrainMirror);
	final Vector3D ceilingNormal = new Vector3D(groundNormal.getX(),
		-groundNormal.getY(), groundNormal.getZ());
	Vector3D surfaceNormal = groundImpact ? groundNormal : ceilingNormal;
	if (terrainMirror && groundHeightNorm > .97) {
	    groundImpact = true;
	    surfaceNormal = downhillDirectionXZ;
	}//end if(smushed between floor and ceiling)

	if (groundLock) {
	    recentlyCollided=true;
	    thisPos[1] = groundHeight;
	    p.notifyPositionChange();
	    return;
	}//end if(groundLock)

	if (groundImpact || ceilingImpact) {// detect collision
	    recentlyCollided=true;
	    double padding = autoNudge ? nudgePadding : 0;
	    padding *= groundImpact ? 1 : -1;
	    thisPos[1] = (groundImpact ? groundHeight : ceilingHeight)
		    + padding;
	    p.notifyPositionChange();
	    // Call impact listeners
	    surfaceNormalVar = surfaceNormal;
	    final Behavior behavior = p.getBehavior();
	    behavior.probeForBehaviors(sub, SurfaceImpactListener.class);
	}// end if(collision)
    }// end _tick

    private final Submitter<SurfaceImpactListener> sub = new Submitter<SurfaceImpactListener>() {
	@Override
	public void submit(SurfaceImpactListener item) {
	    item.collidedWithSurface(null, surfaceNormalVar.toArray());
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
}// end BouncesOffTerrain
