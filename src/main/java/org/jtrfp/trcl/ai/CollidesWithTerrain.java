package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.TerrainChunk;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.objects.Velocible;
import org.jtrfp.trcl.objects.WorldObject;

public class CollidesWithTerrain extends Behavior {
    private static final double nudge=1000;
    private boolean bounce=false;
    private double pad=0;
    private boolean groundLock=false;
    private InterpolatingAltitudeMap map;
    public CollidesWithTerrain(boolean bounce, double pad){
	this.bounce=bounce;
	this.pad=pad;
    }
    @Override
    public void _tick(long tickTimeMillis){
	if(map==null)return;//null map means no terrain present, which means no need to check
	final WorldObject p = getParent();
	final Vector3D thisPos=p.getPosition();
	final double height = map.heightAt((thisPos.getX()/TR.mapSquareSize), 
	    (thisPos.getZ()/TR.mapSquareSize))*(p.getTr().getWorld().sizeY/2);
	final Vector3D groundNormal = (map.normalAt((thisPos.getX()/TR.mapSquareSize), 
	    (thisPos.getZ()/TR.mapSquareSize)));

    	if(groundLock){p.setPosition(new Vector3D(thisPos.getX(),height+pad,thisPos.getZ()));return;}
    	
	if(thisPos.getY()<height+pad)
	    {p.setPosition(new Vector3D(thisPos.getX(),height+pad+nudge,thisPos.getZ()));
	    //Reflect heading,top
	    if(bounce){
	    	final Vector3D oldHeading = p.getHeading();
	    	final Vector3D oldTop = p.getTop();
	    	final Vector3D newHeading = (groundNormal.scalarMultiply(groundNormal.dotProduct(oldHeading)*-2).add(oldHeading));
	    	final Velocible v=p.getBehavior().probeForBehavior(Velocible.class);
	    	final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	    	if(rmb!=null){//If this is a spinning object, reverse its spin momentum
	    	    rmb.setLateralMomentum(rmb.getLateralMomentum()*-1);
	    	    rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*-1);
	    	    rmb.setPolarMomentum(rmb.getPolarMomentum()*-1);
	    	    }
	    	final Vector3D oldVelocity = v.getVelocity();
	    	v.setVelocity(groundNormal.scalarMultiply(groundNormal.dotProduct(oldVelocity)*-2).add(oldVelocity));
	    	p.setHeading(newHeading);
	    	final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
	    	Vector3D newTop = resultingRotation.applyTo(oldTop);
		p.setTop(newTop);
		//System.out.println("BOUNCE. GroundNorm="+groundNormal+" Old heading="+oldHeading+" New heading="+newHeading);
		//System.out.println("OldTop="+oldTop+" NewTop="+newTop);
	    	}//end if(bounce)
	    }//end if()
    map=null;
    }//end _tick
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof TerrainChunk){
	    if(map==null)map = (InterpolatingAltitudeMap)((TerrainChunk)other).getAltitudeMap();}
    }//end _tick
    /**
     * @return the groundLock
     */
    public boolean isGroundLock() {
        return groundLock;
    }
    /**
     * @param groundLock the groundLock to set
     */
    public CollidesWithTerrain setGroundLock(boolean groundLock) {
        this.groundLock = groundLock;
        return this;
    }
}//end BouncesOffTerrain
